package chaitu.android.ychat.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import chaitu.android.ychat.model.Message;
import chaitu.android.ychat.R;
import chaitu.android.ychat.activities.ChatActivity;
import chaitu.android.ychat.listeners.MsgListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
     Context context;
    List<Message> messageList;
    MsgListener listener;
   private static final int SENT  = 1;
    private static final int RECEIVED = 2;

    public ChatAdapter(Context context, List<Message> messageList,MsgListener listener) {
        this.context = context;
        this.messageList = messageList;
        this.listener=listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == RECEIVED) {
            return new RecievedMsgHolder(LayoutInflater.from(context).inflate(R.layout.recieved_msg,parent,false));
        }else{
            return new SentMsgHolder(LayoutInflater.from(context).inflate(R.layout.sent_msg,parent,false));
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == SENT) {
            ((SentMsgHolder)holder).setData(messageList.get(position));
        }else{
            ((RecievedMsgHolder)holder).setData(messageList.get(position));
        }
        holder.itemView.setOnClickListener(view -> listener.onCMessageClicked(view,messageList.get(position),position));
        holder.itemView.setOnLongClickListener(view -> listener.onCMessageLongClicked(view,messageList.get(position),position));

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        String myMobile= FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        if (!messageList.get(position).getReceiver().equals(myMobile)) {
            return SENT;
        }else
            return RECEIVED;
    }

     class SentMsgHolder extends RecyclerView.ViewHolder{
        TextView messageTv,time,seenTv;
        ImageView imageView;

        public SentMsgHolder(@NonNull View itemView) {
            super(itemView);
            messageTv=itemView.findViewById(R.id.msg);
            time=itemView.findViewById(R.id.time);
            imageView=itemView.findViewById(R.id.image);
            seenTv=itemView.findViewById(R.id.seenTv);
        }
        public void setData(Message message){
            messageTv.setText(message.getMessage());
            time.setText(ChatActivity.getFormattedTime(message.getTime()));
            if (message.getSeen() != null) {
                seenTv.setVisibility(View.VISIBLE);
                seenTv.setText(message.getSeen());
            }else seenTv.setVisibility(View.GONE);
            if(message.getImage().isEmpty()){
                messageTv.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
            }else{
                messageTv.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                try {
                    Glide.with(context).load(message.getImage()).fitCenter().centerCrop().into(imageView);
                } catch (Exception e) {
                    Log.d("tag",e.getMessage());
                }
            }
        }
    }
     class RecievedMsgHolder extends RecyclerView.ViewHolder{
        TextView messageTv, timeTv;
        CircleImageView profilePic;
        ImageView imageView;
        public RecievedMsgHolder(@NonNull View itemView) {
            super(itemView);
            messageTv=itemView.findViewById(R.id.msg);
            timeTv =itemView.findViewById(R.id.time);
            profilePic =itemView.findViewById(R.id.dp);
            imageView=itemView.findViewById(R.id.image);

        }
        public void setData(Message message){
            if(message.getImage().isEmpty()){
                messageTv.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);

            }else{
                messageTv.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                try {
                    Glide.with(context).load(message.getImage()).fitCenter().centerCrop().into(imageView);
                } catch (Exception e) {
                    Log.d("tag",e.getMessage());
                }
            }
            messageTv.setText(message.getMessage());
            timeTv.setText(ChatActivity.getFormattedTime(message.getTime()));
            FirebaseDatabase.getInstance().getReference("Users").child(message.getSender()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String dp= snapshot.child("profilePic").getValue(String.class);
                    if (!dp.isEmpty()) {
                        try {
                            Glide.with(context).load(dp).into(profilePic);
                        } catch (Exception e) {
                            Log.d("tag",e.getMessage());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


        }
    }
}
