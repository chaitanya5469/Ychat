package chaitu.android.ychat.listeners;

import android.view.View;

import chaitu.android.ychat.model.User;

public interface UserListener {
    void onUserClicked(View view, User user, int position);
    void onUserLongClicked(View view,User user,int position);
}
