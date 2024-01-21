package chaitu.android.ychat.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import chaitu.android.ychat.model.Conversation;
import chaitu.android.ychat.R;
import chaitu.android.ychat.activities.ChatActivity;
import chaitu.android.ychat.listeners.ConversationListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>{
    Context context;
    List<Conversation> conversationList;
    ConversationListener listener;

    public ConversationAdapter(Context context, List<Conversation> conversationList, ConversationListener listener) {
        this.context = context;
        this.conversationList = conversationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversationViewHolder(LayoutInflater.from(context).inflate(R.layout.item_conversation,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation=conversationList.get(position);
        holder.itemView.setOnClickListener(view -> listener.onConversationClicked(view,conversation,position));
        holder.message.setText(conversation.getMessage());
        holder.time.setText(ChatActivity.getFormattedTime(conversation.getTime()));
        String senderMobile=conversation.getSender().equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())?conversation.receiver:conversation.sender;
        FirebaseDatabase.getInstance().getReference("Users").child(senderMobile).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.name.setText(snapshot.child("name").getValue(String.class));
               String dp= snapshot.child("profilePic").getValue(String.class);
               String token=snapshot.child("token").getValue(String.class);
               conversation.setDp(dp);
               conversation.setUserToken(token);
               conversation.setSender(senderMobile);
               conversation.setName(holder.name.getText().toString());

                if (!dp.isEmpty()) {
                    try {
                        Glide.with(context).load(dp).into(holder.dp);
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
        holder.itemView.setOnClickListener(view -> listener.onConversationClicked(view,conversation,position));
        holder.itemView.setOnLongClickListener(view -> listener.onConversationLongClicked(view,conversation,position));

    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

  public static class ConversationViewHolder extends RecyclerView.ViewHolder{
      TextView name,message,time;
      CircleImageView dp;
        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.name);
            message=itemView.findViewById(R.id.message);
            time=itemView.findViewById(R.id.time);
            dp=itemView.findViewById(R.id.dp);
        }
    }
}
