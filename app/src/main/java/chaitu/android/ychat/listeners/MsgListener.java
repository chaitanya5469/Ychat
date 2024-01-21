package chaitu.android.ychat.listeners;

import android.view.View;

import chaitu.android.ychat.model.Message;

public interface MsgListener {
    void onCMessageClicked(View view, Message message, int position);
    boolean onCMessageLongClicked(View view, Message message, int position);


}
