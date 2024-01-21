package chaitu.android.ychat.listeners;

import android.view.View;

import chaitu.android.ychat.model.Conversation;

public interface ConversationListener {
    void onConversationClicked(View view, Conversation conversation, int position);
   boolean onConversationLongClicked(View view,Conversation conversation,int position);

}
