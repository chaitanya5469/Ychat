package chaitu.android.ychat.activities;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.UriUtils;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import chaitu.android.ychat.adapters.ChatAdapter;
import chaitu.android.ychat.model.Conversation;
import chaitu.android.ychat.ImageCompressorKt;
import chaitu.android.ychat.model.Message;
import chaitu.android.ychat.R;
import chaitu.android.ychat.model.User;
import chaitu.android.ychat.listeners.MsgListener;
import chaitu.android.ychat.notificationUtil.FCMNotificationUtil;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends BaseActivity implements MsgListener {
    AlertDialog.Builder builder;
    AlertDialog progressDialog;
    User user;
    TextView nameTv, phoneTv,onlineTv;
    EditText messageEt;
    ImageView sendBtn,imgBtn,backBtn,deleteBtn,cancelBtn;
    List<Message>messageList,selectedMessages;
    RecyclerView messageRecycler;
    ChatAdapter chatAdapter;
    ProgressBar pb;
    String myDp;
    String conversationId,myName;
    String mymobile = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
    CircleImageView dp;
    boolean isMultiselect=false;
    ActivityResultLauncher<PickVisualMediaRequest> launcher=registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), o -> {
        if(o==null){
            Toast.makeText(ChatActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }else{
            //TODO

            File f = UriUtils.uri2File(o);
            try {
                ImageCompressorKt.compressBitmap(ChatActivity.this, f, file1 ->
                        {
                            uploadImage(file1);
                            return null;
                        }
                        );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    });



    private void uploadImage(File file) {
        final ProgressBar progressBar = new ProgressBar(this);
        progressDialog=  getDialogProgressBar().setView(progressBar).create();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        progressBar.setLayoutParams(lp);
        progressDialog.show();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/"+file.getName());
        storageReference.putFile(Uri.fromFile(file))
                .addOnProgressListener(snapshot -> {
            int progress= (int) (snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
            getDialogProgressBar().setTitle("Loading.."+progress+"%");
            progressBar.setProgress(progress);
        }).addOnCompleteListener(task -> {
            if(!task.isSuccessful()){
                progressDialog.dismiss();
                Toast.makeText(this, task.getException().toString(), Toast.LENGTH_SHORT).show();
            }
            storageReference.getDownloadUrl().addOnCompleteListener(task1 -> {
                progressDialog.dismiss();
                if (task1.isSuccessful()) {
                   String downloadUri = task1.getResult().toString();
                   sendMessage(user,"Photo",downloadUri);
                } else {
                    Toast.makeText(this, task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
    public AlertDialog.Builder getDialogProgressBar() {

        if (builder == null) {
            builder = new AlertDialog.Builder(this);
        }
        return builder;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            user=getIntent().getSerializableExtra("user",User.class);
        }else  user= (User) getIntent().getSerializableExtra("user");
        selectedMessages=new ArrayList<>();
        backBtn=findViewById(R.id.back);
        deleteBtn=findViewById(R.id.delete);
        cancelBtn=findViewById(R.id.cancel);
        backBtn.setOnClickListener(view ->{
            if (isMultiselect)cancelBtn.callOnClick();
            else super.onBackPressed();
        });
        nameTv =findViewById(R.id.name);
        phoneTv =findViewById(R.id.mobile);
        onlineTv=findViewById(R.id.textUserAvailability);
        dp=findViewById(R.id.dp);
        messageRecycler=findViewById(R.id.chatRecycler);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this,RecyclerView.VERTICAL,false);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        messageRecycler.setLayoutManager(linearLayoutManager);
        messageEt=findViewById(R.id.message_edit_text);
        sendBtn=findViewById(R.id.send_btn);
        imgBtn=findViewById(R.id.img_btn);
        pb=findViewById(R.id.pb);
        messageEt=findViewById(R.id.message_edit_text);
        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            DatabaseReference reference=  FirebaseDatabase.getInstance().getReference("Users").child(mymobile).child("typing");
                if (charSequence.toString().isEmpty()) {
                    reference.removeValue();
                }else reference.setValue(user.mobile);
                checkOnline();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        messageList=new ArrayList<>();
        imgBtn.setOnClickListener(view -> launcher.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));
        sendBtn.setOnClickListener(view -> {
            String message=messageEt.getText().toString().trim();
            if(message.isEmpty()){
                messageEt.setError("Enter a valid message");
            }else sendMessage(user,message,"");
        });

        nameTv.setText(user.name);
        phoneTv.setText(user.mobile);
        if(!user.profilePic.isEmpty())
            try {
            Glide.with(this).load(user.profilePic).into(dp);
        } catch (Exception e) {
            Log.d("tag","No dP");
            Glide.with(this).load(R.drawable.baseline_person_24).into(dp);
        }
        cancelBtn.setOnClickListener(view -> {
            
            for (int i=0;i<messageList.size();i++){
                Message message=messageList.get(i);
                if (selectedMessages.contains(message)) {
                    this.onCMessageClicked(messageRecycler.getChildAt(i),message,i);
                    selectedMessages.remove(message);
                }
                if (i == messageList.size() - 1) {
                    isMultiselect=false;
                }


            }
            setLayout();
        });
        deleteBtn.setOnClickListener(view -> deleteMessages());
        checkConversation();
        checkOnline();
        loadMessages();
        FirebaseDatabase.getInstance().getReference("Users").child(mymobile).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myName=snapshot.child("name").getValue(String.class);
                myDp=snapshot.child("profilePic").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkOnline();
    }

    private void checkOnline() {
        FirebaseDatabase.getInstance().getReference("Users").child(user.mobile).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("user_availability").exists()) {
                    Integer availability = snapshot.child("user_availability").getValue(Integer.class);
                    String typing=snapshot.child("typing").getValue(String.class);
                    if(typing!=null&&typing.equals(mymobile)){
                        onlineTv.setText("Typing");
                    }
                    if (availability != null) {
                        if (availability==1){
                            onlineTv.setVisibility(View.VISIBLE);
                        }else {
                            onlineTv.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showSnackBar(View v,String text) {
        Snackbar snackbar = Snackbar.make(
                v, // Pass the view reference
                text, // The message you want to display
                Snackbar.LENGTH_LONG // The duration of the Snackbar (LENGTH_LONG or LENGTH_SHORT)
        );
        View snackbarView = snackbar.getView(); // Get the Snackbar's view
        snackbarView.setBackgroundColor(Color.parseColor("#FF0ADD26")); // Set background color
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        Drawable icon = getResources().getDrawable(R.drawable.done_24);
        icon.setTint(getResources().getColor(R.color.colorIcons));
        textView.setCompoundDrawablesWithIntrinsicBounds(icon,null,null,null);// Set text color
        snackbar.setActionTextColor(Color.YELLOW); // Set action text color
        snackbar.show(); // Display the Snackbar

    }
    private void deleteMessages() {


        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Chats");
        for(Message message:selectedMessages){
            reference.child(message.getTime()).removeValue();
        }
        isMultiselect=false;

        setLayout();
        showSnackBar(messageRecycler,"Messages Deleted");
    }

    private void loadMessages() {
        messageList=new ArrayList<>();
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){

                    if((ds.child("sender").getValue().equals(user.mobile)&&ds.child("receiver").getValue().equals(mymobile))||(ds.child("receiver").getValue().equals(user.mobile)&&ds.child("sender").getValue().equals(mymobile))){
                      Message message=new Message();
                      message.setMessage(ds.child("message").getValue(String.class));
                        if (ds.child("image").exists()) {
                            message.setImage(ds.child("image").getValue(String.class));
                        }else{
                            message.setImage("");
                        }
                      message.setSender(ds.child("sender").getValue(String.class));
                      message.setReceiver(ds.child("receiver").getValue(String.class));
                      message.setTime(ds.child("time").getValue(String.class));
                      message.setSeen(ds.child("seen").getValue(String.class));
                        if (message.getReceiver().equals(mymobile)) {
                            HashMap<String,Object>map=new HashMap<>();
                            map.put("seen","Seen");
                            ds.getRef().updateChildren(map);
                        }
                        messageList.add(message);
                    }
                }
                pb.setVisibility(View.GONE);
                if(!messageList.isEmpty()){
                    chatAdapter=new ChatAdapter(ChatActivity.this,messageList,ChatActivity.this);
                    messageRecycler.setAdapter(chatAdapter);
                    messageRecycler.smoothScrollToPosition(messageList.size()-1);
                    chatAdapter.notifyItemRangeChanged(messageList.size(),messageList.size());
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(User user, String message,String image) {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Chats");
        HashMap<String,String>map=new HashMap<>();
        map.put("receiver",user.mobile);
        map.put("sender", mymobile);
        map.put("image",image);
        map.put("message",message);
        map.put("seen","sent");
        String time = String.valueOf(Calendar.getInstance().getTimeInMillis());
        map.put("time",time);
        reference.child(time).setValue(map).addOnSuccessListener(unused -> {
            messageEt.setText("");
            chatAdapter.notifyDataSetChanged();
            if(conversationId==null)
                addConversation(message,time);
            else updateConversation(message,time);

            Log.d("msg","sent");
            if (onlineTv.getVisibility() != View.VISIBLE) {
                sendNotification(message);
            }
        }).addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());

    }



    private void addConversation(String message, String time) {
        HashMap<String,Object>map=new HashMap<>();
        map.put("receiver",user.mobile);
        map.put("sender", mymobile);
        map.put("message",message);
        map.put("time",time);
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Conversations").push();
        reference.setValue(map);
    }
    private void updateConversation(String message, String time) {
        HashMap<String,Object>map=new HashMap<>();

        map.put("message",message);
        map.put("time",time);
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Conversations").child(conversationId);
        reference.updateChildren(map);
    }
    private void checkConversation(){
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Conversations");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){
                    Conversation conversation=ds.getValue(Conversation.class);
                    if((conversation.getSender().equals(mymobile)&&conversation.getReceiver().equals(user.mobile))||(conversation.getSender().equals(user.mobile)&&conversation.getReceiver().equals(mymobile))){
                        conversationId=ds.getRef().getKey();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    public static String getFormattedTime(String time){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM h:mm a");
        return formatter.format(new Date(Long.parseLong(time)));

    }


    @Override
    public void onCMessageClicked(View view, Message message, int position) {
        if (isMultiselect){
            if (!selectedMessages.contains(message)) {
                if (message.getSender().equals(mymobile)) {
                    view.setBackgroundColor(Color.parseColor("#c3e6fc"));
                    selectedMessages.add(message);
                }
            }else{
                selectedMessages.remove(message);
                view.setBackgroundColor(Color.parseColor("#00000000"));
            }
            if (selectedMessages.isEmpty()) {
                isMultiselect=false;
            }
            setLayout();
        }

    }

    private void setLayout() {
        if(isMultiselect){
            dp.setVisibility(View.GONE);
            phoneTv.setVisibility(View.GONE);
            deleteBtn.setVisibility(View.VISIBLE);
            cancelBtn.setVisibility(View.VISIBLE);

        }
        else{
            cancelBtn.setVisibility(View.GONE);
            deleteBtn.setVisibility(View.GONE);
            dp.setVisibility(View.VISIBLE);
            phoneTv.setVisibility(View.VISIBLE);
        }
        if (selectedMessages.isEmpty()) {
            isMultiselect=false;
            dp.setVisibility(View.VISIBLE);
            phoneTv.setVisibility(View.VISIBLE);
            nameTv.setText(user.name);
        }else{
            nameTv.setText(selectedMessages.size()+" Selected");
        }

    }

    @Override
    public boolean onCMessageLongClicked(View view, Message message, int position) {
        if(!isMultiselect)isMultiselect=true;
        if (!selectedMessages.contains(message)) {
            if (message.getSender().equals(mymobile)) {
                view.setBackgroundColor(Color.parseColor("#c3e6fc"));
                selectedMessages.add(message);
            }
        }else{
            selectedMessages.remove(message);
            view.setBackgroundColor(Color.parseColor("#00000000"));
        }
        if (selectedMessages.isEmpty()) {
            isMultiselect=false;
        }
        setLayout();
        return true;
    }
    private void sendNotification(String message){
        JSONArray tokens=new JSONArray();
        tokens.put(user.getToken());

        JSONObject data=new JSONObject();


        try {
            data.put("mobile",mymobile);
            data.put("name",myName);
            data.put("dp",myDp);
            data.put("token",user.getToken());
            data.put("message",message);
            JSONObject body=new JSONObject();
            body.put("data",data);
            body.put("registration_ids",tokens);
            FCMNotificationUtil.sendNotification(getApplicationContext(),body.toString());
        } catch (JSONException e) {
           e.printStackTrace();
        }


    }
}