package com.youdo.karma.db;

import android.content.Context;
import android.text.TextUtils;

import com.youdo.karma.CSApplication;
import com.youdo.karma.R;
import com.youdo.karma.db.base.DBManager;
import com.youdo.karma.entity.ClientUser;
import com.youdo.karma.entity.Conversation;
import com.youdo.karma.greendao.ConversationDao;
import com.youdo.karma.listener.MessageChangedListener;
import com.youdo.karma.manager.AppManager;
import com.youdo.karma.net.request.DownloadFileRequest;
import com.youdo.karma.utils.FileAccessorUtils;
import com.youdo.karma.utils.Md5Util;
import com.yuntongxun.ecsdk.ECMessage;
import com.yuntongxun.ecsdk.im.ECTextMessageBody;

import org.greenrobot.greendao.query.QueryBuilder;

import java.io.File;
import java.util.List;


/**
 * 
 * @ClassName:ConversationSqlManager
 * @Description:会话数据库管理
 * @author wangyb
 * @Date:2015年5月24日下午8:03:20
 *
 */
public class ConversationSqlManager extends DBManager {

	private static ConversationSqlManager mInstance;
	private ConversationDao conversationDao;
	private Context mContext;

	private ConversationSqlManager(Context context) {
		super(context);
		mContext = context;
		conversationDao = getDaoSession().getConversationDao();
	}

	public static ConversationSqlManager getInstance(Context context) {
		if (mInstance == null) {
			synchronized (ConversationSqlManager.class) {
				if (mInstance == null) {
					mInstance = new ConversationSqlManager(context);
				}
			}
		}
		return mInstance;
	}


	/**
	 * 获取所有会话统计未读消息数
	 * @return
	 */
	public int getAnalyticsUnReadConversation() {
		List<Conversation> conversations = conversationDao.loadAll();
		if (conversations == null || conversations.size() == 0) {
			return 0 ;
		}
		int total = 0;
		for (int i = 0; i < conversations.size(); i++) {
			Conversation c = conversations.get(i);
			total += c.unreadCount;
		}
		return total;
	}

	/**
	 * 获取未读会话个数
	 * @return
	 */
	public int getConversationUnReadNum() {
		List<Conversation> conversations = conversationDao.loadAll();
		if (conversations == null || conversations.size() == 0) {
			return 0 ;
		}
		int total = 0;
		for (int i = 0; i < conversations.size(); i++) {
			if (conversations.get(i).unreadCount > 0) {
				total++;
			}
		}
		return total;
	}

	/**
	 * 根据id查询会话
	 * @return
	 */
	public Conversation queryConversationForById(String conversationId) {
		return null;
	}

	/**
	 * 根据聊天对象的userid来查询会话
	 * @return
	 */
	public Conversation queryConversationForByTalkerId(String talkerId) {
		QueryBuilder<Conversation> qb = conversationDao.queryBuilder();
		qb.where(ConversationDao.Properties.Talker.eq(talkerId));
		return qb.unique();
	}

	/**
	 * 查询所有的会话
	 * @return
     */
	public List<Conversation> queryConversations() {
		return conversationDao.loadAll();
	}

	/**
	 * 修改会话
	 * @return
	 */
	public void updateConversation(Conversation conversation) {
		if (conversation == null
				|| TextUtils.isEmpty(String.valueOf(conversation.id))) {
			return;
		}
		conversationDao.update(conversation);
	}

	/**
	 * 删除所有会话
	 * 
	 * @return
	 */
	public void deleteAllConversation() {
		conversationDao.deleteAll();
	}

	public void deleteConversationById(Conversation conversation) {
		conversationDao.delete(conversation);
	}


	/**
	 * 官方消息发送时插入的会话
	 * @param conversation
	 * @return
	 */
	public long inserConversation(Conversation conversation) {
		return conversationDao.insertOrReplace(conversation);
//		return conversationDao.insert(conversation);
	}
	/**
	 * 插入一个新会话记录
	 * @param ecMessage
	 * @return
	 */
	public long insertConversation(ClientUser clientUser, ECMessage ecMessage) {
		String talker = "";
		String sender = "";
		String talkerName = "";
		String portraitUrl = "";
		boolean isSend = false;
		if (ecMessage.getDirection() == ECMessage.Direction.SEND) {
			isSend = true;
		}
		if (isSend) {
			talker = ecMessage.getTo();
			sender = ecMessage.getForm();
			talkerName = clientUser.user_name;
			portraitUrl = clientUser.face_url;
		} else {
			talker = ecMessage.getForm();
			sender = ecMessage.getTo();
			String[] userInfo = ecMessage.getUserData().split(";");
			if (userInfo.length > 1) {
				portraitUrl = userInfo[1];
				talkerName = userInfo[0];
			}
		}
		//根据talker去查询有没有对应的会话
		Conversation conversation = queryConversationForByTalkerId(talker);
		if (null == conversation) {//没有会话
			conversation = new Conversation();
		}
		conversation.talker = talker;
		conversation.talkerName = talkerName;
		conversation.createTime = ecMessage.getMsgTime();
		if (!isSend && !"com.youdo.karma.activity.ChatActivity".equals(AppManager.getTopActivity(mContext))) {
			conversation.unreadCount++;
		}
		if (ecMessage.getType() == ECMessage.Type.TXT) {
			ECTextMessageBody body = (ECTextMessageBody)ecMessage.getBody();
			if (body != null) {
				conversation.content = body.getMessage();
			}
			conversation.type = ECMessage.Type.TXT.ordinal();
		} else if (ecMessage.getType() == ECMessage.Type.IMAGE) {
			conversation.content = CSApplication.getInstance().getResources().getString(R.string.image_symbol);
			conversation.type = ECMessage.Type.IMAGE.ordinal();
		} else if (ecMessage.getType() == ECMessage.Type.LOCATION) {
			conversation.content = CSApplication.getInstance().getResources().getString(R.string.location_symbol);
			conversation.type = ECMessage.Type.LOCATION.ordinal();
		} else if (ecMessage.getType() == ECMessage.Type.FILE) {

		} else if (ecMessage.getType() == ECMessage.Type.RICH_TEXT) {
			conversation.content = CSApplication.getInstance().getResources().getString(R.string.rpt_symbol);
			conversation.type = ECMessage.Type.RICH_TEXT.ordinal();
		}
		long id = conversationDao.insertOrReplace(conversation);
		conversation.id = id;
		//插入会话之后就下载头像 String faceUrl = ecMessage.getUserData(); 并更新会话
		if (TextUtils.isEmpty(conversation.localPortrait) ||
				!new File(conversation.localPortrait).exists()) {
			new DownloadPortraitTask(conversation).request(
					portraitUrl, FileAccessorUtils.FACE_IMAGE, Md5Util.md5(portraitUrl) + ".jpg");
		}
		/**
		 * 通知会话内容的改变
		 */
		MessageChangedListener.getInstance().notifyMessageChanged(String.valueOf(id));
		return id;
	}

	/**
	 * 下载头像
	 */
	class DownloadPortraitTask extends DownloadFileRequest {
		private Conversation mConversation;

		public DownloadPortraitTask(Conversation conversation) {
			mConversation = conversation;
		}

		@Override
		public void onPostExecute(String s) {
			mConversation.localPortrait = s;
			updateConversation(mConversation);
			MessageChangedListener.getInstance().notifyMessageChanged(String.valueOf(mConversation.id));
		}

		@Override
		public void onErrorExecute(String error) {
		}
	}

	public static void reset() {
		release();
		mInstance = null;
	}
}
