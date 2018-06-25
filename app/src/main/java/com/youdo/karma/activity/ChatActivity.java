package com.youdo.karma.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.umeng.analytics.MobclickAgent;
import com.youdo.karma.R;
import com.youdo.karma.activity.base.BaseActivity;
import com.youdo.karma.adapter.ChatEmoticonsAdapter;
import com.youdo.karma.adapter.ChatEmoticonsAdapter.OnEmojiItemClickListener;
import com.youdo.karma.adapter.ChatMessageAdapter;
import com.youdo.karma.adapter.PagerGridAdapter;
import com.youdo.karma.config.ValueKey;
import com.youdo.karma.db.ConversationSqlManager;
import com.youdo.karma.db.IMessageDaoManager;
import com.youdo.karma.entity.ClientUser;
import com.youdo.karma.entity.Conversation;
import com.youdo.karma.entity.Emoticon;
import com.youdo.karma.entity.IMessage;
import com.youdo.karma.eventtype.SnackBarEvent;
import com.youdo.karma.helper.IMChattingHelper;
import com.youdo.karma.listener.FileProgressListener;
import com.youdo.karma.listener.FileProgressListener.OnFileProgressChangedListener;
import com.youdo.karma.listener.MessageCallbackListener;
import com.youdo.karma.listener.MessageCallbackListener.OnMessageReportCallback;
import com.youdo.karma.listener.MessageStatusReportListener;
import com.youdo.karma.listener.MessageStatusReportListener.OnMessageStatusReport;
import com.youdo.karma.manager.AppManager;
import com.youdo.karma.manager.NotificationManager;
import com.youdo.karma.ui.widget.WrapperLinearLayoutManager;
import com.youdo.karma.utils.EmoticonUtil;
import com.youdo.karma.utils.FileAccessorUtils;
import com.youdo.karma.utils.FileUtils;
import com.youdo.karma.utils.ImageUtil;
import com.youdo.karma.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Cloudsoar(wangyb)
 * @datetime 2016-01-16 19:21 GMT+8
 * @email 395044952@qq.com
 */
public class ChatActivity extends BaseActivity implements OnMessageReportCallback, OnClickListener,
		OnEmojiItemClickListener, OnFileProgressChangedListener,
        OnRefreshListener, OnMessageStatusReport {
	private RecyclerView mMessageRecyclerView;
	private ChatMessageAdapter mMessageAdapter;
	private ImageView openCamera;
	private ImageView openAlbums;
	private ImageView openEmotion;
	private ImageView openLocation;
	private ImageView redPacket;
	private ImageButton mInputVoiceAndText;
	private View mKeyboardHeightView;
	private EditText mContentInput;
	private LinearLayout mEmoticonLay;
	private LinearLayout mMorePageIndicator;
	private ViewPager mEmoticonPager;
	private LinearLayout mEmoticonPageIndicator;
	private RecyclerView mEmoticonRecyclerview;
	private SwipeRefreshLayout mSwipeRefresh;

	private String mPhotoPath;
	private File mPhotoFile;
	private Uri mPhotoOnSDCardUri;

	private ClientUser mClientUser;
	private Conversation mConversation;

	private List<IMessage> mIMessages;
	private List<GridView> mChatEmoticonsGridView;
	private LinearLayoutManager mLinearLayoutManager;

	/**
	 * 消息分页条数
	 */
	private static final int PAGE_SIZE = 20;

	/**
	 * 拍照返回
	 */
	public final static int CAMERA_RESULT = 101;
	/**
	 * 相册返回
	 */
	public final static int ALBUMS_RESULT = 102;

	/**
	 * 预览图片返回
	 */
	public final static int PREVIEW_IMAGE_RESULT = 103;
	/**
	 * 分享位置
	 */
	public final static int SHARE_LOCATION_RESULT = 106;
	/**
	 * 发红包
	 */
	public static final int  SEND_RED_PACKET = 107;

	/**
	 * 跳转设置界面
	 */
	private final int REQUEST_PERMISSION_SETTING = 10001;
	/**
	 * 读写文件夹
	 */
	private final int REQUEST_PERMISSION_WRITE = 1000;

	/**
	 * 是否拥有读写权限
	 */
	private boolean isWritePersimmion = false;

	public static Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		Toolbar mToolbar = getActionBarToolbar();
		if (mToolbar != null) {
			mToolbar.setNavigationIcon(R.mipmap.ic_up);
		}

		setupView();
		setupEvent();
		setupData();
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
			}
		};
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				hideSoftKeyboard();
			}
		}, 100);

	}

	private void setupView() {
		mMessageRecyclerView = (RecyclerView) findViewById(R.id.message_recycler_view);
		mLinearLayoutManager = new WrapperLinearLayoutManager(this);
		mLinearLayoutManager.setOrientation(LinearLayout.VERTICAL);
		mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

		mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
		mSwipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);
		openCamera = (ImageView) findViewById(R.id.openCamera);
		openAlbums = (ImageView) findViewById(R.id.openAlbums);
		openEmotion = (ImageView) findViewById(R.id.openEmotion);
		openLocation = (ImageView) findViewById(R.id.openLocation);
		redPacket = (ImageView) findViewById(R.id.red_packet);
		mInputVoiceAndText = (ImageButton) findViewById(R.id.tool_view_input_text);
		mKeyboardHeightView = findViewById(R.id.keyboard_height);
		mContentInput = (EditText) findViewById(R.id.content_input);
		mEmoticonLay = (LinearLayout) findViewById(R.id.emoticon_lay);
		mMorePageIndicator = (LinearLayout) findViewById(R.id.more_page_indicator);
		mEmoticonPager = (ViewPager) findViewById(R.id.emoticon_pager);
		mEmoticonPageIndicator = (LinearLayout) findViewById(R.id.emoticon_page_indicator);

		mEmoticonRecyclerview = (RecyclerView) findViewById(R.id.emoticon_recyclerview);
		LinearLayoutManager layoutManager = new WrapperLinearLayoutManager(this);
		layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		mEmoticonRecyclerview.setLayoutManager(layoutManager);

		if (!AppManager.getClientUser().isShowVip) {
			openCamera.setVisibility(View.GONE);
			openAlbums.setVisibility(View.GONE);
			openLocation.setVisibility(View.GONE);
			redPacket.setVisibility(View.GONE);
			openEmotion.setVisibility(View.GONE);
		} else {
			openCamera.setVisibility(View.VISIBLE);
			openAlbums.setVisibility(View.VISIBLE);
			openLocation.setVisibility(View.VISIBLE);
			redPacket.setVisibility(View.VISIBLE);
			openEmotion.setVisibility(View.VISIBLE);
		}
	}

	private void setupEvent() {
		EventBus.getDefault().register(this);
		openCamera.setOnClickListener(this);
		openAlbums.setOnClickListener(this);
		openEmotion.setOnClickListener(this);
		openLocation.setOnClickListener(this);
		redPacket.setOnClickListener(this);
		mSwipeRefresh.setOnRefreshListener(this);
		mInputVoiceAndText.setOnClickListener(this);
		MessageStatusReportListener.getInstance().setOnMessageReportCallback(this);
		MessageCallbackListener.getInstance().setOnMessageReportCallback(this);

		FileProgressListener.getInstance().addOnFileProgressChangedListener(
				this);

		/** 监听RecyclerView触碰 */
		mMessageRecyclerView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					setDefaultMode();
				}
				return false;
			}
		});

		/** 监听输入框点击 */
		mContentInput.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					setInputMode();
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							scrollToBottom();
						}
					}, 400);
				}
				return false;
			}
		});

		mContentInput.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				scrollToBottom();
			}
		});

		mContentInput.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK
						&& event.getAction() == KeyEvent.ACTION_DOWN) {
					setDefaultMode();
				}
				return false;
			}
		});
	}

	private void setupData() {
		if (null != AppManager.getClientUser() &&
				AppManager.getClientUser().isShowRpt) {
			redPacket.setVisibility(View.VISIBLE);
		} else {
			redPacket.setVisibility(View.GONE);
		}
		mClientUser = (ClientUser) getIntent().getSerializableExtra(ValueKey.USER);
		if (mClientUser != null) {
			mConversation = ConversationSqlManager.getInstance(this)
					.queryConversationForByTalkerId(mClientUser.userId);
		}

		initEmoticon();
		initEmotionUI();
		mIMessages = new ArrayList<>();
		mMessageAdapter = new ChatMessageAdapter(this, mIMessages, mConversation);
		mMessageRecyclerView.setAdapter(mMessageAdapter);
		getData();
	}

	/**
	 * 初始化表情UI
	 */
	private void initEmotionUI() {
		mChatEmoticonsGridView = new ArrayList<GridView>();
		List<Emoticon> emoticons = EmoticonUtil.getInstace().getEmoticonList();
		int pageCount = (int) Math.ceil(emoticons.size() / 20.0f);
		for (int i = 0; i < pageCount; i++) {
			List<Emoticon> em = new ArrayList<Emoticon>();
			int j = i * 20;
			int end = j + 20;
			while ((j < emoticons.size()) && (j < end)) {
				em.add(emoticons.get(j));
				j++;
			}
			GridView gv = (GridView) LayoutInflater.from(this).inflate(
					R.layout.widget_emoticons, null);
			ChatEmoticonsAdapter adapter = new ChatEmoticonsAdapter(this, em,
					gv);
			gv.setAdapter(adapter);
			adapter.setOnEmojiItemClickListener(this);
			mChatEmoticonsGridView.add(gv);
		}
		if (pageCount <= 1) {
			mMorePageIndicator.setVisibility(View.GONE);
		}
		PagerGridAdapter pagerAdapter = new PagerGridAdapter(this,
				mChatEmoticonsGridView, mEmoticonPager, mEmoticonPageIndicator);
		mEmoticonPager.setAdapter(pagerAdapter);
		pagerAdapter.notifyDataSetChanged();
	}

	/**
	 * 初始化表情
	 */
	private void initEmoticon() {
		if (EmoticonUtil.getInstace().getEmoticonList().size() == 0) {
			EmoticonUtil.getInstace().initEmoticon();
		}
	}


	@Override
	public void onFileProgressChanged(IMessage message, int progress) {
		int chatMessageIndex = -1;
		if (mIMessages != null && message != null) {
			for (int i = 0; i < mIMessages.size(); i++) {
				if (mIMessages.get(i).id == message.id) {
					message.imageProgress = progress;
					if (message.isSend == IMessage.MessageIsSend.SEND) {
						if (message.msgType == IMessage.MessageType.IMG) {
							message.imageStatus = IMessage.ImageStatus.SEND;
							if (progress >= 100) {
								message.imageStatus = IMessage.ImageStatus.SEND_SUCCESS;
							}
						}

					} else {
						if (message.msgType == IMessage.MessageType.IMG) {
							message.imageStatus = IMessage.ImageStatus.RECEIVING;
							if (progress >= 100) {
								message.imageStatus = IMessage.ImageStatus.RECEIVING_SUCCESS;
							}
						}
					}
					mIMessages.set(i, message);
					chatMessageIndex = i;
					break;
				}
			}
		}
		mMessageAdapter.notifyItemChanged(chatMessageIndex);
	}

	/**
	 * 获取数据
	 */
	private void getData() {
		getMessage(String.valueOf(getMessageAdapterLastMessageTime()));
		/**
		 * 接着设置用户的信息
		 */
		if (mClientUser != null && !TextUtils.isEmpty(mClientUser.user_name)) {
			getSupportActionBar().setTitle(mClientUser.user_name);
		} else if (mConversation != null && !TextUtils.isEmpty(mConversation.talkerName)) {
			getSupportActionBar().setTitle(mConversation.talkerName);
		}
	}

	/**
	 * 获取聊天消息
	 */
	private void getMessage(String lastTime) {
		if (mConversation != null) {
			List<IMessage> result = IMessageDaoManager.getInstance(this).queryIMessageList(
					String.valueOf(mConversation.id), PAGE_SIZE, "0".equals(lastTime) ? String.valueOf(mConversation.createTime + 1) : lastTime);
			mSwipeRefresh.setRefreshing(false);
			boolean isFirst = mIMessages.size() <= 0;
			if (result != null && result.size() != 0) {
				mIMessages.addAll(0, result);
				mMessageAdapter.notifyItemRangeInserted(0, result.size());
				if (isFirst)
					scrollToBottom();
			}
		}
	}

	/**
	 * 获取最后一条消息记录的时间
	 *
	 * @return
	 */
	public long getMessageAdapterLastMessageTime() {
		long lastTime = 0;
		if (mMessageAdapter != null && mMessageAdapter.getItemCount() > 0) {
			IMessage message = mIMessages
					.get(mMessageAdapter.getItemCount() - 1);
			if (message != null) {
				lastTime = message.create_time;
			}
		}
		return lastTime;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.openCamera:
				if (AppManager.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_PERMISSION_WRITE) &&
						AppManager.checkPermission(this, Manifest.permission.CAMERA, CAMERA_RESULT)) {
					openCamera();
					setInputMode();
				}
				break;
			case R.id.openAlbums:
				openAlbums();
				break;
			case R.id.openEmotion:
				setEmojiconMode();
				break;
			case R.id.openLocation:
				toShareLocation();
				break;
			case R.id.red_packet:
				toRedPakcet();
				break;
			case R.id.tool_view_input_text:
				if (AppManager.getClientUser().isShowVip) {
					if (!TextUtils.isEmpty(mContentInput.getText().toString())) {
						if (AppManager.getClientUser().is_vip) {
							if (null != IMChattingHelper.getInstance().getChatManager()) {
								sendTextMsg();
							}
						} else {
							showVipDialog();
						}
					}
				} else {
					if (!TextUtils.isEmpty(mContentInput.getText().toString()) &&
							null != IMChattingHelper.getInstance().getChatManager()) {
						sendTextMsg();
					}
				}
				break;
		}
	}

	private void sendTextMsg() {
		IMChattingHelper.getInstance().sendTextMsg(
				mClientUser, mContentInput.getText().toString());
		mContentInput.setText("");
	}

	private void showVipDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.un_send_msg);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent intent = new Intent(ChatActivity.this, VipCenterActivity.class);
				startActivity(intent);
			}
		});
		builder.setNegativeButton(R.string.until_single, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.show();
	}

	private void showGoldDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.no_gold_un_send_msg);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent intent = new Intent(ChatActivity.this, MyGoldActivity.class);
				startActivity(intent);
			}
		});
		builder.setNegativeButton(R.string.until_single, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.show();
	}

	/**
	 * 通知消息发送状态
	 *
	 * @param message
	 */
	public void onMessageStatusReport(IMessage message) {
		for (int i = 0; i < mIMessages.size(); i++) {
			if (message.id == mIMessages.get(i).id) {
				mIMessages.get(i).status = message.status;
				mMessageAdapter.notifyItemChanged(i);
				break;
			}
		}
		scrollToBottom();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mClientUser != null) {
			AppManager.currentChatTalker = mClientUser.userId;
		}
		NotificationManager.getInstance().cancelNotification();
		MobclickAgent.onPageStart(this.getClass().getName());
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		AppManager.currentChatTalker = null;
		MobclickAgent.onPageEnd(this.getClass().getName());
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
		AppManager.currentChatTalker = null;
		MessageStatusReportListener.getInstance().setOnMessageReportCallback(null);
		MessageCallbackListener.getInstance().setOnMessageReportCallback(null);
		FileProgressListener.getInstance().removeOnFileProgressChangedListener(this);
	}

	/**
	 * 打开相机
	 */
	private void openCamera() {
		hideSoftKeyboard();
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (intent.resolveActivity(getPackageManager())!=null){
			String mPhotoDirPath = Environment
					.getExternalStoragePublicDirectory(
							Environment.DIRECTORY_DCIM).getPath();
			File mPhotoDirFile = new File(mPhotoDirPath);
			if (!mPhotoDirFile.exists()) {
				mPhotoDirFile.mkdir();
			}
			mPhotoPath = mPhotoDirPath + File.separator + getPhotoFileName();
			mPhotoFile = new File(mPhotoPath);
			if (!mPhotoFile.exists()) {
				try {
					mPhotoFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (mPhotoFile != null) {
				//FileProvider 是一个特殊的 ContentProvider 的子类，
				//它使用 content:// Uri 代替了 file:/// Uri. ，更便利而且安全的为另一个app分享文件
				mPhotoOnSDCardUri = FileProvider.getUriForFile(this,
						"com.youdo.karma.fileProvider",
						mPhotoFile);
				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
				intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoOnSDCardUri);
				startActivityForResult(intent, CAMERA_RESULT);
			}
		}
	}

	/**
	 * 打开相册
	 */
	private void openAlbums() {
		Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
		openAlbumIntent.setType("image/*");
		startActivityForResult(openAlbumIntent, ALBUMS_RESULT);
	}

	/**
	 * 返回图片文件名
	 *
	 * @return
	 */
	private String getPhotoFileName() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"'IMG'_yyyyMMdd_HHmmss");
		return dateFormat.format(date) + ".jpg";
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == CAMERA_RESULT) {
			Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
					mPhotoOnSDCardUri);
			sendBroadcast(intent);
			if (mPhotoOnSDCardUri != null && new File(mPhotoPath).exists()) {
				//压缩图片
				String imgUrl = ImageUtil.compressImage(mPhotoPath, FileAccessorUtils.IMESSAGE_IMAGE);
				Uri uri = Uri.parse("file://" + imgUrl);
				toImagePreview(uri);
			}
		} else if (resultCode == RESULT_OK && requestCode == ALBUMS_RESULT) {
			if (AppManager.getClientUser().isShowVip) {
				if (AppManager.getClientUser().is_vip) {
					if (AppManager.getClientUser().isShowGold && AppManager.getClientUser().gold_num  < 101) {
						showGoldDialog();
					} else {
						Uri uri = data.getData();
						String url = FileUtils.getPath(this, uri);
						if (!TextUtils.isEmpty(url)) {
							String fileUrl = "";
							if (url.startsWith("/storage")) {
								fileUrl = url;
							} else {
								String extSdCardPath = FileUtils.getPath();
								fileUrl = extSdCardPath + File.separator + FileUtils.getPath(this, uri);
							}
							if (null != IMChattingHelper.getInstance().getChatManager()) {
								IMChattingHelper.getInstance().sendImgMsg(mClientUser, fileUrl);
							}
						}
					}
				} else {
					showVipDialog();
				}
			} else {
				Uri uri = data.getData();
				String url = FileUtils.getPath(this, uri);
				if (!TextUtils.isEmpty(url)) {
					String fileUrl = "";
					if (url.startsWith("/storage")) {
						fileUrl = url;
					} else {
						String extSdCardPath = FileUtils.getPath();
						fileUrl = extSdCardPath + File.separator + FileUtils.getPath(this, uri);
					}
					if (null != IMChattingHelper.getInstance().getChatManager()) {
						IMChattingHelper.getInstance().sendImgMsg(mClientUser, fileUrl);
					}
				}
			}
		} else if (resultCode == RESULT_OK
				&& requestCode == PREVIEW_IMAGE_RESULT) {
			Uri uri = data.getData();
            String imgUrl = ImageUtil.compressImage(uri.getPath(), FileAccessorUtils.IMESSAGE_IMAGE);
			IMChattingHelper.getInstance().sendImgMsg(mClientUser, imgUrl);
		} else if (resultCode == RESULT_OK
				&& requestCode == SHARE_LOCATION_RESULT) {
			double latitude = data.getDoubleExtra(ValueKey.LATITUDE, 0);
			double longitude = data.getDoubleExtra(ValueKey.LONGITUDE, 0);
			String address = data.getStringExtra(ValueKey.ADDRESS);
			String imagePath = data.getStringExtra(ValueKey.IMAGE_URL);
			if (null != IMChattingHelper.getInstance().getChatManager()) {
				IMChattingHelper.getInstance().sendLocationMsg(mClientUser, latitude, longitude,
						address, imagePath);
			}
		} else if (resultCode == RESULT_OK && requestCode == SEND_RED_PACKET) {
			ToastUtil.showMessage("已发送");
			if (null != IMChattingHelper.getInstance().getChatManager()) {
				IMChattingHelper.getInstance().sendRedPacketMsg(
						mClientUser, data.getStringExtra(ValueKey.DATA));
			}
		}
	}

	/**
	 * 跳转到图预览
	 */
	private void toImagePreview(Uri uri) {
		Intent intent = new Intent();
		intent.setClass(this, ImagePreviewActivity.class);
		intent.setData(uri);
		startActivityForResult(intent, PREVIEW_IMAGE_RESULT);
	}

	/**
	 * 表情模式
	 */
	private void setEmojiconMode() {
		if (mEmoticonLay.getVisibility() == View.GONE) {
			mEmoticonLay.postDelayed(new Runnable() {

				@Override
				public void run() {
					hideSoftKeyboard();
					getWindow()
							.setSoftInputMode(
									WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
					if (mKeyboardHeightView.getVisibility() == View.VISIBLE) {
						mKeyboardHeightView.setVisibility(View.GONE);
					}
					mEmoticonLay.setVisibility(View.VISIBLE);
				}
			}, 80);
		} else {
			setInputMode();
		}
	}

	/**
	 * 键盘文本模式
	 */
	private void setInputMode() {
		if (mEmoticonLay.getVisibility() == View.VISIBLE) {
			if (mEmoticonLay.getVisibility() == View.VISIBLE) {
				mEmoticonLay.setVisibility(View.GONE);
			}
			if (mKeyboardHeightView.getVisibility() == View.GONE) {
				mKeyboardHeightView.setVisibility(View.VISIBLE);
			}

			showSoftKeyboard(mContentInput);
			mContentInput.postDelayed(new Runnable() {

				@Override
				public void run() {
					mKeyboardHeightView.setVisibility(View.GONE);
					getWindow()
							.setSoftInputMode(
									WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
					mContentInput.requestFocus();
				}
			}, 100);
		}
	}

	/**
	 * 默认模式
	 */
	private void setDefaultMode() {
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		mKeyboardHeightView.setVisibility(View.GONE);
		hideSoftKeyboard();
		if (mEmoticonLay.getVisibility() == View.VISIBLE) {
			mEmoticonLay.setVisibility(View.GONE);
		}
		if (!TextUtils.isEmpty(mContentInput.getText())) {
			mInputVoiceAndText.setImageResource(R.drawable.send_icon_selector);
			mInputVoiceAndText.setTag(R.drawable.send_icon_selector);
		}
	}

	/**
	 * 跳到分享位置界面
	 */
	private void toShareLocation() {
		Intent intent = new Intent(this, ShareLocationActivity.class);
		startActivityForResult(intent, SHARE_LOCATION_RESULT);
	}

	/**
	 * 跳到发红包界面
	 */
	private void toRedPakcet() {
		Intent intent = new Intent(this, RedPacketActivity.class);
		startActivityForResult(intent, SEND_RED_PACKET);
	}

	@Override
	public void onEmojiItemClick(Emoticon emoticon) {
		int index = mContentInput.getSelectionStart();
		Editable edit = mContentInput.getEditableText();
		Drawable drawable;
		if (Build.VERSION.SDK_INT >= 22) {
			drawable = getResources().getDrawable(emoticon.reId, null);
		} else {
			drawable = getResources().getDrawable(emoticon.reId);
		}
		int wh = (int) getResources().getDimension(R.dimen.chat_emoji_wh);
		drawable.setBounds(0, 0, wh, wh);
		ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
		SpannableString spannableString = new SpannableString(
				emoticon.emojiCode);
		spannableString.setSpan(imageSpan, 0, emoticon.emojiCode.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		if (index < 0 || index >= edit.length()) {
			edit.append(spannableString);
		} else {
			edit.insert(index, spannableString);
		}
	}

	@Override
	public void onEmojiDelClick() {
		KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0,
				0, KeyEvent.KEYCODE_ENDCALL);
		mContentInput.dispatchKeyEvent(event);
	}

	@Override
	public void onRefresh() {
		long lastTime = System.currentTimeMillis();
		if (mMessageAdapter != null) {
			if (mIMessages.size() <= 0) {
				mSwipeRefresh.setRefreshing(false);
				return;
			}
			IMessage message = mIMessages.get(0);
			if (message != null) {
				lastTime = message.create_time;
			}
		}
		getMessage(String.valueOf(lastTime));
	}

	@Override
	public void onPushMessage(IMessage message) {
		if (mIMessages.isEmpty() ||//主动发送
				//在某一个用户会话界面，但是另一个用户发来消息
				(!mIMessages.isEmpty() && mIMessages.get(0).conversationId == message.conversationId)) {
			boolean isScrollBottom = isScrollBottom();
			mIMessages.add(message);
			mMessageAdapter.notifyItemInserted(mIMessages.size() - 1);
			if (isScrollBottom || message.isSend == IMessage.MessageIsSend.SEND) {
				scrollToBottom();
			}
		}
	}

	@Override
	public void onIMessageItemChange(String msgId) {

	}

	@Override
	public void onNotifyDataSetChanged(int conversationId) {

	}

	@Override
	protected void onNewIntent(Intent intent) {
		mIMessages.clear();
	}

	/**
	 * 是否滑动到最底部
	 */
	private boolean isScrollBottom() {
		try {
			int lastVisibleItemPosition = mLinearLayoutManager
					.findLastVisibleItemPosition();
			if (lastVisibleItemPosition < 0) {
				return false;
			}
			if (lastVisibleItemPosition < mIMessages.size()) {
				if (mIMessages.get(lastVisibleItemPosition).id == mIMessages
						.get(mIMessages.size() - 1).id) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 滚动到底部
	 */
	private void scrollToBottom() {
		mMessageRecyclerView
				.scrollToPosition(mMessageAdapter.getItemCount() - 1);
	}

	@Override
	public void onNotifyMessageStatusReport(IMessage msg) {
		onMessageStatusReport(msg);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == CAMERA_RESULT) {
			// 拒绝授权
			if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
				// 勾选了不再提示
				if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
					showOpenCameraDialog();
				} else {
				}
			} else if (isWritePersimmion) {
				openCamera();
				setInputMode();
			}
		} else if (requestCode == REQUEST_PERMISSION_WRITE) {//读写文件夹权限
			// 拒绝授权
			if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
				// 勾选了不再提示
				if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
					showWriteDialog();
				}
			} else {
				isWritePersimmion = true;
				AppManager.checkPermission(this, Manifest.permission.CAMERA, CAMERA_RESULT);
			}
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	private void showOpenCameraDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.open_camera_permission);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				Uri uri = Uri.fromParts("package", getPackageName(), null);
				intent.setData(uri);
				startActivityForResult(intent, REQUEST_PERMISSION_SETTING);

			}
		});
		builder.show();
	}

	private void showWriteDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.open_write_external);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				Uri uri = Uri.fromParts("package", getPackageName(), null);
				intent.setData(uri);
				startActivityForResult(intent, REQUEST_PERMISSION_SETTING);

			}
		});
		builder.show();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void showSnackBar(SnackBarEvent event) {
		if (!TextUtils.isEmpty(event.content)) {
			Snackbar.make(findViewById(R.id.message_recycler_view), event.content, Snackbar.LENGTH_LONG)
					.setActionTextColor(Color.RED)
					.setAction("点击查看", new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(ChatActivity.this, MoneyPacketActivity.class);
							startActivity(intent);
						}
					}).show();
		}
	}
}

