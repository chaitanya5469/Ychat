package chaitu.android.ychat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import chaitu.android.ychat.R;
import chaitu.android.ychat.listeners.UserListener;
import chaitu.android.ychat.model.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder>{
    Context context;
    UserListener userListener;
    List<User>userList;

    public UserAdapter(Context context, List<User> userList,UserListener userListener) {
        this.context = context;
        this.userList = userList;
        this.userListener=userListener;
    }



    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(LayoutInflater.from(context).inflate(R.layout.item_user,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user=userList.get(position);
        holder.name.setText(user.name);
        holder.phone.setText(user.mobile);
        if(!user.profilePic.isEmpty())
            try {
            Glide.with(context).load(user.profilePic).into(holder.dp);
        } catch (Exception e) {
            Glide.with(context).load(R.drawable.baseline_person_24).into(holder.dp);
        }
        holder.itemView.setOnClickListener(view -> {
            userListener.onUserClicked(view,user,position);
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

   public static class UserViewHolder extends RecyclerView.ViewHolder{
        TextView name,phone;
        CircleImageView dp;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.name);
            phone=itemView.findViewById(R.id.mobile);
            dp=itemView.findViewById(R.id.dp);
        }
    }

}
