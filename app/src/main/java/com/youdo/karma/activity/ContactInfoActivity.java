package com.youdo.karma.activity;

import android.arch.lifecycle.Lifecycle;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;
import com.umeng.analytics.MobclickAgent;
import com.youdo.karma.R;
import com.youdo.karma.activity.base.BaseActivity;
import com.youdo.karma.adapter.PhotosAdapter;
import com.youdo.karma.config.ValueKey;
import com.youdo.karma.db.ContactSqlManager;
import com.youdo.karma.entity.ClientUser;
import com.youdo.karma.entity.Contact;
import com.youdo.karma.listener.ModifyContactsListener;
import com.youdo.karma.manager.AppManager;
import com.youdo.karma.net.IUserPictureApi;
import com.youdo.karma.net.base.RetrofitFactory;
import com.youdo.karma.ui.widget.NoScrollGridView;
import com.youdo.karma.utils.JsonUtils;
import com.youdo.karma.utils.ToastUtil;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import mehdi.sakout.fancybuttons.FancyButton;

/**
 * 
 * @ClassName:ContactInfoActivity.java
 * @Description:通讯录信息
 * @author Administrator
 * @Date:2015年5月19日上午9:33:28
 *
 */
public class ContactInfoActivity extends BaseActivity implements OnClickListener {

	private SimpleDraweeView mPortrait;
	private TextView mUserName;
	private TextView mUserId;
	private TextView mAge;
	private TextView mConstellation;
	private TextView mSignatrue;
	private FancyButton mSendMessage;
	private FancyButton mAddFriend;
	private NoScrollGridView mGridView;
	private PhotosAdapter mAdapter;

	private Contact mContact;// 平台通讯录
	private String imagePath;
	private int pageNo = 1;
	private int pageSize = 6;
	private String from;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_info);
		Toolbar toolbar = getActionBarToolbar();
		if (toolbar != null) {
			toolbar.setNavigationIcon(R.mipmap.ic_up);
		}
		setupViews();
		setupEvent();
		setupData();
	}

	/**
	 * 设置视图
	 */
	private void setupViews() {
		mPortrait = (SimpleDraweeView) findViewById(R.id.portrait);
		mUserName = (TextView) findViewById(R.id.user_name);
		mUserId = (TextView) findViewById(R.id.user_id);
		mAge = (TextView) findViewById(R.id.age);
		mConstellation = (TextView) findViewById(R.id.constellation);
		mSignatrue = (TextView) findViewById(R.id.signatrue);
		mSendMessage = (FancyButton) findViewById(R.id.send_message);
		mAddFriend = (FancyButton) findViewById(R.id.add_friend);
		mGridView = (NoScrollGridView) findViewById(R.id.img_contents);
	}

	/**
	 * 设置事件
	 */
	private void setupEvent() {
		mSendMessage.setOnClickListener(this);
		mPortrait.setOnClickListener(this);
		mAddFriend.setOnClickListener(this);
	}

	/**
	 * 设置数据
	 */
	private void setupData() {
		mContact = (Contact) getIntent().getSerializableExtra(
				ValueKey.CONTACT);
		from = getIntent().getStringExtra(ValueKey.FROM_ACTIVITY);
		if (!TextUtils.isEmpty(from)) {
			mAddFriend.setVisibility(View.VISIBLE);
		} else {
			mAddFriend.setVisibility(View.GONE);
		}
		if (mContact != null) {
			getUserPicRequest(mContact.userId, pageNo, pageSize);
			setContactInfo(mContact);
		}
	}

	private void getUserPicRequest(String userId, int pageNo, int pageSize) {
		ArrayMap<String, String> params = new ArrayMap<>();
		params.put("pageNo", String.valueOf(pageNo));
		params.put("pageSize", String.valueOf(pageSize));
		params.put("uid", userId);
		RetrofitFactory.getRetrofit().create(IUserPictureApi.class)
				.getUserPictureList(AppManager.getClientUser().sessionId, params)
				.subscribeOn(Schedulers.io())
				.map(responseBody -> JsonUtils.parseJsonPics(responseBody.string()))
				.observeOn(AndroidSchedulers.mainThread())
				.as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY)))
				.subscribe(pics -> {
					if (pics != null && pics.size() > 0) {
						mGridView.setVisibility(View.VISIBLE);
						mAdapter = new PhotosAdapter(ContactInfoActivity.this, pics, mGridView);
						mGridView.setAdapter(mAdapter);
					} else {
						mGridView.setVisibility(View.GONE);
					}
				}, throwable -> {
				});
	}

	/**
	 * 设置通讯录信息
	 */
	private void setContactInfo(Contact contact) {
		imagePath = contact.face_url;
		mPortrait.setImageURI(Uri.parse(contact.face_url));
		if (contact.sex.equals(Contact.Gender.FEMALE)) {
			mUserName.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					R.mipmap.contact_female, 0);
		} else {
			mUserName.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					R.mipmap.contact_male, 0);
		}
		if (!TextUtils.isEmpty(contact.user_name)) {
			mUserName.setText(contact.user_name);
		}
		if (!TextUtils.isEmpty(contact.userId)) {
			mUserId.setText("ID:" + contact.userId);
		}
		if (!TextUtils.isEmpty(contact.birthday)) {
			mAge.setText(contact.birthday);
		}
		if (!TextUtils.isEmpty(contact.constellation)) {
			mConstellation.setText(contact.constellation);
		}
		if (!TextUtils.isEmpty(contact.signature)) {
			mSignatrue.setText(contact.signature);
		}
	}


	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
			case R.id.send_message:
				ClientUser clientUser = new ClientUser();
				clientUser.userId = mContact.userId;
				clientUser.user_name = mContact.user_name;
				clientUser.face_url = mContact.face_url;
				intent.putExtra(ValueKey.USER, clientUser);
				intent.setClass(this, ChatActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
				this.finish();
				break;
			case R.id.add_friend:
				ModifyContactsListener.getInstance().notifyAddDataChanged(mContact);
				ModifyContactsListener.getInstance().notifyDataChanged(mContact);
				ToastUtil.showMessage(R.string.action_add_friends_success);
				finish();
				break;
			case R.id.portrait:
				intent.setClass(this, PhotoViewActivity.class);
				intent.putExtra(ValueKey.IMAGE_URL, imagePath);
				startActivity(intent);
				break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (TextUtils.isEmpty(from)) {
			getMenuInflater().inflate(R.menu.contact_info_menu, menu);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_delete_friends) {
			ContactSqlManager.getInstance(this).deleteContactById(mContact.userId);
			ModifyContactsListener.getInstance().notifyDeleteDataChanged(mContact.userId);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(this.getClass().getName());
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(this.getClass().getName());
		MobclickAgent.onPause(this);
	}
}
