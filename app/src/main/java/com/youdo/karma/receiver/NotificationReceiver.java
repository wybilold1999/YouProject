package com.youdo.karma.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.youdo.karma.CSApplication;
import com.youdo.karma.activity.ChatActivity;
import com.youdo.karma.activity.LauncherActivity;
import com.youdo.karma.activity.PersonalInfoActivity;
import com.youdo.karma.config.ValueKey;
import com.youdo.karma.db.ConversationSqlManager;
import com.youdo.karma.entity.ClientUser;
import com.youdo.karma.entity.Conversation;
import com.youdo.karma.listener.MessageChangedListener;
import com.youdo.karma.listener.MessageUnReadListener;
import com.youdo.karma.manager.AppManager;
import com.youdo.karma.manager.NotificationManagerUtils;

/**
 * 通知广播
 * Created by Administrator on 2016/3/14.
 */
public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AppManager.isAppAlive(context, AppManager.getPackageName())
                && AppManager.getClientUser() != null) {
            ClientUser clientUser = (ClientUser) intent.getSerializableExtra(ValueKey.USER);
            if (clientUser != null) {
                NotificationManagerUtils.getInstance().cancelNotification();
                Conversation conversation = ConversationSqlManager.getInstance(CSApplication.getInstance())
                        .queryConversationForByTalkerId(clientUser.userId);
                if (conversation != null) {
                    conversation.unreadCount = 0;
                    ConversationSqlManager.getInstance(context).updateConversation(conversation);
                    MessageUnReadListener.getInstance().notifyDataSetChanged(0);
                    MessageChangedListener.getInstance().notifyMessageChanged("");
                }
                if (clientUser.isLocalMsg) {
                    Intent chatIntent = new Intent(context, PersonalInfoActivity.class);
                    chatIntent.putExtra(ValueKey.USER_ID, clientUser.userId);
                    chatIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(chatIntent);
                } else {
                    Intent chatIntent = new Intent(context, ChatActivity.class);
                    chatIntent.putExtra(ValueKey.USER, clientUser);
                    chatIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(chatIntent);
                }
            }
        } else {
            Intent launcherIntent = new Intent(context, LauncherActivity.class);
            launcherIntent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            context.startActivity(launcherIntent);
        }
    }
}
