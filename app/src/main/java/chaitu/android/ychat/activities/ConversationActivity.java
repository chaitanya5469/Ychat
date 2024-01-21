package chaitu.android.ychat.activities;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import chaitu.android.ychat.model.Conversation;
import chaitu.android.ychat.adapters.ConversationAdapter;
import chaitu.android.ychat.R;
import chaitu.android.ychat.model.User;
import chaitu.android.ychat.listeners.ConversationListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationActivity extends BaseActivity implements ConversationListener {
    CircleImageView dp;
    ImageButton addBtn;
    RecyclerView recyclerView;
    List<Conversation>conversations;
    ConversationAdapter conversationAdapter;
    String mymobile;
    ProgressBar pb;
    String token;
    boolean isMultiselect=false;
    int userSelectedPosition=-1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        dp=findViewById(R.id.dp);
        recyclerView=findViewById(R.id.chatRecycler);
        pb=findViewById(R.id.pb);
        addBtn=findViewById(R.id.add);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(ConversationActivity.this,LinearLayoutManager.VERTICAL,true);
        recyclerView.setLayoutManager(linearLayoutManager);
        conversations=new ArrayList<>();
         mymobile= FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        FirebaseDatabase.getInstance().getReference("Users").child(mymobile)
               .addValueEventListener(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot snapshot) {
                       String dpUrl = snapshot.child("profilePic").getValue(String.class);
                       token=snapshot.child("token").getValue(String.class);
                       if (token == null) {
                           updateToken(snapshot);
                       }
                       if (!dpUrl.isEmpty()) {
                           try {
                               Glide.with(ConversationActivity.this).load(dpUrl).into(dp);
                           } catch (Exception e) {
                               Log.d("tag", e.getMessage());
                           }
                       }
                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError error) {

                   }
               }

               );


        loadConversations();
        dp.setOnClickListener(view -> {
            Intent intent=new Intent(getApplicationContext(), UpdateProfile.class);
            intent.putExtra("isUpdate",true);
            intent.putExtra("mobile",mymobile);
            startActivity(intent);

        });
        addBtn.setOnClickListener(view -> {
            startActivity(new Intent(this, UserActivity.class));
        });

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ConversationActivity.this,new String[]{Manifest.permission.POST_NOTIFICATIONS},100);
        }

    }

    private void updateToken(DataSnapshot snapshot) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                token=task.getResult();
                snapshot.getRef().child("token").setValue(token);
            }else Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadConversations() {
        conversations=new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("Conversations").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                conversations.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    if(ds.child("sender").getValue().equals(mymobile)||ds.child("receiver").getValue().equals(mymobile)){
                        Conversation conversation=new Conversation();
                        conversation.setMessage(ds.child("message").getValue(String.class));
                        conversation.setReceiver(ds.child("receiver").getValue(String.class));
                        conversation.setSender(ds.child("sender").getValue(String.class));
                        conversation.setTime(ds.child("time").getValue(String.class));
                        conversations.add(conversation);

                    }
                    pb.setVisibility(View.GONE);
                    if (!conversations.isEmpty()) {
                        conversations.sort(Comparator.comparingLong(conversation -> Long.parseLong(conversation.getTime())));

                        conversationAdapter=new ConversationAdapter(ConversationActivity.this,conversations,ConversationActivity.this);
                        recyclerView.setAdapter(conversationAdapter);
                        recyclerView.smoothScrollToPosition(conversations.size()-1);

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onConversationClicked(View view, Conversation conversation, int position) {
        if (!isMultiselect) {
            userSelectedPosition=position;
            User user=new User(conversation.getName(),conversation.getSender(),conversation.getDp(),conversation.getUserToken());
            Intent intent=new Intent(getApplicationContext(), ChatActivity.class);
            intent.putExtra("user",user);
            startActivity(intent);
        }

    }

    @Override
    public boolean onConversationLongClicked(View view, Conversation conversation, int position) {

        return true;
    }
}