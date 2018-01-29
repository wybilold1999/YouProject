package com.youdo.karma.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.youdo.karma.R;
import com.youdo.karma.activity.AboutActivity;
import com.youdo.karma.activity.AttentionMeActivity;
import com.youdo.karma.activity.BetweenLoversActivity;
import com.youdo.karma.activity.IdentifyActivity;
import com.youdo.karma.activity.LoveFormeActivity;
import com.youdo.karma.activity.MakeMoneyActivity;
import com.youdo.karma.activity.MoneyPacketActivity;
import com.youdo.karma.activity.MyAppointmentActivity;
import com.youdo.karma.activity.MyAttentionActivity;
import com.youdo.karma.activity.MyGiftsActivity;
import com.youdo.karma.activity.MyGoldActivity;
import com.youdo.karma.activity.NearPartyActivity;
import com.youdo.karma.activity.PersonalInfoActivity;
import com.youdo.karma.activity.SettingActivity;
import com.youdo.karma.activity.SuccessCaseActivity;
import com.youdo.karma.activity.VideoListActivity;
import com.youdo.karma.activity.VipCenterActivity;
import com.youdo.karma.config.ValueKey;
import com.youdo.karma.entity.ClientUser;
import com.youdo.karma.entity.FollowLoveModel;
import com.youdo.karma.eventtype.UserEvent;
import com.youdo.karma.manager.AppManager;
import com.youdo.karma.net.request.DownloadFileRequest;
import com.youdo.karma.net.request.GetFollowLoveRequest;
import com.youdo.karma.utils.FileAccessorUtils;
import com.youdo.karma.utils.Md5Util;
import com.youdo.karma.utils.PreferencesUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * @author: wangyb
 * @datetime: 2015-12-20 11:34 GMT+8
 * @email: 395044952@qq.com
 * @description:
 */
public class PersonalFragment extends Fragment {

	@BindView(R.id.user_name)
	TextView userName;
	@BindView(R.id.signature)
	TextView signature;
	@BindView(R.id.user_info)
	LinearLayout userInfo;
	@BindView(R.id.is_vip)
	ImageView isVip;
	@BindView(R.id.head_portrait_lay)
	RelativeLayout headPortraitLay;
	@BindView(R.id.vip_img)
	ImageView vipImg;
	@BindView(R.id.vip_lay)
	RelativeLayout vipLay;
	@BindView(R.id.my_attention_img)
	ImageView myAttentionImg;
	@BindView(R.id.my_attention)
	RelativeLayout myAttention;
	@BindView(R.id.attentioned_user_img)
	ImageView attentionedUserImg;
	@BindView(R.id.attention_count)
	TextView attentionCount;
	@BindView(R.id.attentioned_user)
	RelativeLayout attentionedUser;
	@BindView(R.id.good_user_img)
	ImageView goodUserImg;
	@BindView(R.id.love_count)
	TextView loveCount;
	@BindView(R.id.good_user)
	RelativeLayout goodUser;
	@BindView(R.id.setting_img)
	ImageView settingImg;
	@BindView(R.id.setting)
	RelativeLayout setting;
	@BindView(R.id.about)
	RelativeLayout about;
	@BindView(R.id.my_gold)
	RelativeLayout mMyGold;
	@BindView(R.id.portrait)
	SimpleDraweeView mPortrait;
	@BindView(R.id.download_layout)
	RelativeLayout mDownloadlayout;
	@BindView(R.id.lovers_card)
	CardView mLoversCard;
	@BindView(R.id.lovers_lay)
	RelativeLayout mLoversLay;
	@BindView(R.id.success_case)
	RelativeLayout mSuccessCase;
	@BindView(R.id.near_party)
	RelativeLayout mNearParty;
	@BindView(R.id.identify_card)
	CardView mIdentifyCard;
	@BindView(R.id.identify_lay)
	RelativeLayout mIdentifyLay;
	@BindView(R.id.my_gifts)
	RelativeLayout mMyGifts;
	@BindView(R.id.gifts_count)
	TextView giftsCount;
	@BindView(R.id.vip_card)
	CardView mVipCard;
	@BindView(R.id.gift_red_point)
	ImageView mGiftRedPoint;
	@BindView(R.id.attention_red_point)
	ImageView mAttentionRedPoint;
	@BindView(R.id.love_red_point)
	ImageView mLoveRedPoint;
	@BindView(R.id.money_card)
	CardView mMoneyCard;
	@BindView(R.id.money_lay)
	RelativeLayout mMoneyLay;
	@BindView(R.id.video_show_card)
	CardView mVideoShowCard;
	@BindView(R.id.video_show_lay)
	RelativeLayout mVideoShowLay;
	@BindView(R.id.my_appointment_lay)
	RelativeLayout mAppointmentLay;

	Unbinder unbinder;

	private View rootView;

	private ClientUser clientUser;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_personal, null);
			unbinder = ButterKnife.bind(this, rootView);
			EventBus.getDefault().register(this);
			setupViews();
			setupEvent();
			setupData();
			setHasOptionsMenu(true);
		}
		ViewGroup parent = (ViewGroup) rootView.getParent();
		if (parent != null) {
			parent.removeView(rootView);
		}
		return rootView;
	}

	private void setupViews() {
	}

	private void setupEvent() {
	}

	private void setupData() {
		mLoveRedPoint.setVisibility(View.VISIBLE);
		mAttentionRedPoint.setVisibility(View.VISIBLE);
		mGiftRedPoint.setVisibility(View.VISIBLE);
		setUserInfo();
		new GetFollowLoveTask().request(AppManager.getClientUser().userId);
	}

	class GetFollowLoveTask extends GetFollowLoveRequest {
		@Override
		public void onPostExecute(FollowLoveModel followLoveModel) {
			if (null != followLoveModel) {
				if (followLoveModel.followCount > 0) {
					attentionCount.setVisibility(View.VISIBLE);
					attentionCount.setText(String.valueOf(followLoveModel.followCount));
				}
				if (followLoveModel.loveCount > 0) {
					loveCount.setVisibility(View.VISIBLE);
					loveCount.setText(String.valueOf(followLoveModel.loveCount));
				}
				if (followLoveModel.giftsCount > 0) {
					giftsCount.setVisibility(View.VISIBLE);
					giftsCount.setText(String.valueOf(followLoveModel.giftsCount));
				}
			}
		}

		@Override
		public void onErrorExecute(String error) {
		}
	}

	/**
	 * 设置用户信息
	 */
	private void setUserInfo() {
		clientUser = AppManager.getClientUser();
		if (clientUser != null) {
			if (!TextUtils.isEmpty(clientUser.face_local) && new File(clientUser.face_local).exists()) {
				mPortrait.setImageURI(Uri.parse("file://" + clientUser.face_local));
			} else if (!TextUtils.isEmpty(clientUser.face_url)) {
				mPortrait.setImageURI(Uri.parse(clientUser.face_url));
				new DownloadPortraitTask().request(clientUser.face_url,
						FileAccessorUtils.FACE_IMAGE,
						Md5Util.md5(clientUser.face_url) + ".jpg");
			}
			if (!TextUtils.isEmpty(clientUser.signature)) {
				signature.setText(clientUser.signature);
			}
			if (!TextUtils.isEmpty(clientUser.user_name)) {
				userName.setText(clientUser.user_name);
			}
			if (clientUser.isShowVip && clientUser.is_vip) {
				isVip.setVisibility(View.VISIBLE);
			} else {
				isVip.setVisibility(View.GONE);
			}
			if (clientUser.isShowVip) {
				mVipCard.setVisibility(View.VISIBLE);
				mIdentifyCard.setVisibility(View.VISIBLE);
				vipLay.setVisibility(View.VISIBLE);
			} else {
				mVipCard.setVisibility(View.GONE);
				mIdentifyCard.setVisibility(View.GONE);
				vipLay.setVisibility(View.GONE);
			}
			if (clientUser.isShowDownloadVip) {
				mDownloadlayout.setVisibility(View.VISIBLE);
			} else {
				mDownloadlayout.setVisibility(View.GONE);
			}
			if (clientUser.isShowGold) {
				mMyGold.setVisibility(View.VISIBLE);
			} else {
				mMyGold.setVisibility(View.GONE);
			}
			if (clientUser.isShowLovers) {
				mLoversCard.setVisibility(View.VISIBLE);
			} else {
				mLoversCard.setVisibility(View.GONE);
			}
			if (clientUser.isShowRpt) {
				mMoneyCard.setVisibility(View.VISIBLE);
			} else {
				mMoneyCard.setVisibility(View.GONE);
			}
			if (clientUser.isShowVideo) {
				mVideoShowCard.setVisibility(View.VISIBLE);
			} else {
				mVideoShowCard.setVisibility(View.GONE);
			}
			if (clientUser.isShowAppointment) {
				mAppointmentLay.setVisibility(View.VISIBLE);
			} else {
				mAppointmentLay.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * 下载头像
	 */
	class DownloadPortraitTask extends DownloadFileRequest {
		@Override
		public void onPostExecute(String s) {
			if (!TextUtils.isEmpty(s)) {
				AppManager.getClientUser().face_local = s;
				PreferencesUtils.setFaceLocal(getActivity(), s);
			}
		}

		@Override
		public void onErrorExecute(String error) {
		}
	}

	@OnClick({
			R.id.head_portrait_lay, R.id.vip_lay, R.id.my_attention,
			R.id.attentioned_user, R.id.good_user, R.id.setting, R.id.about, R.id.my_gold,
			R.id.download_layout, R.id.lovers_lay, R.id.success_case,
			R.id.near_party, R.id.identify_lay, R.id.my_gifts, R.id.money_lay,
			R.id.video_show_lay, R.id.my_appointment_lay})
	public void onClick(View view) {
		Intent intent = new Intent();
		switch (view.getId()) {
			case R.id.head_portrait_lay:
				intent.setClass(getActivity(), PersonalInfoActivity.class);
				intent.putExtra(ValueKey.USER_ID, AppManager.getClientUser().userId);
				startActivity(intent);
				break;
			case R.id.vip_lay:
				intent.setClass(getActivity(), VipCenterActivity.class);
				startActivity(intent);
				break;
			case R.id.my_attention:
				intent.setClass(getActivity(), MyAttentionActivity.class);
				startActivity(intent);
				break;
			case R.id.my_gifts:
				mGiftRedPoint.setVisibility(View.GONE);
				intent.setClass(getActivity(), MyGiftsActivity.class);
				startActivity(intent);
				break;
			case R.id.attentioned_user:
				mAttentionRedPoint.setVisibility(View.GONE);
				intent.setClass(getActivity(), AttentionMeActivity.class);
				startActivity(intent);
				break;
			case R.id.good_user:
				mLoveRedPoint.setVisibility(View.GONE);
				intent.setClass(getActivity(), LoveFormeActivity.class);
				startActivity(intent);
				break;
			case R.id.setting:
				intent.setClass(getActivity(), SettingActivity.class);
				startActivity(intent);
				break;
			case R.id.about:
				intent.setClass(getActivity(), AboutActivity.class);
				startActivity(intent);
				break;
			case R.id.my_gold:
				intent.setClass(getActivity(), MyGoldActivity.class);
				startActivity(intent);
				break;
			case R.id.download_layout:
				intent.setClass(getActivity(), MakeMoneyActivity.class);
				startActivity(intent);
				break;
			case R.id.lovers_lay:
				intent.setClass(getActivity(), BetweenLoversActivity.class);
				startActivity(intent);
				break;
			case R.id.success_case:
				intent.setClass(getActivity(), SuccessCaseActivity.class);
				startActivity(intent);
				break;
			case R.id.near_party:
				intent.setClass(getActivity(), NearPartyActivity.class);
				startActivity(intent);
				break;
			case R.id.identify_lay:
				intent.setClass(getActivity(), IdentifyActivity.class);
				startActivity(intent);
				break;
			case R.id.money_lay:
				intent.setClass(getActivity(), MoneyPacketActivity.class);
				startActivity(intent);
				break;
			case R.id.video_show_lay:
				intent.setClass(getActivity(), VideoListActivity.class);
				startActivity(intent);
				break;
			case R.id.my_appointment_lay:
				intent.setClass(getActivity(), MyAppointmentActivity.class);
				startActivity(intent);
				break;
		}
	}


	@Subscribe(threadMode = ThreadMode.MAIN)
	public void changeUserInfo(UserEvent event) {
		ClientUser clientUser = AppManager.getClientUser();
		if (clientUser != null) {
			if (!TextUtils.isEmpty(clientUser.face_url)) {
				mPortrait.setImageURI(Uri.parse(clientUser.face_url));
			}
			if (!TextUtils.isEmpty(clientUser.signature)) {
				signature.setText(clientUser.signature);
			}
			if (!TextUtils.isEmpty(clientUser.user_name)) {
				userName.setText(clientUser.user_name);
			}
			if (clientUser.isShowVip && clientUser.is_vip) {
				isVip.setVisibility(View.VISIBLE);
			} else {
				isVip.setVisibility(View.GONE);
			}
			if (clientUser.isShowVip) {
				mIdentifyCard.setVisibility(View.VISIBLE);
				vipLay.setVisibility(View.VISIBLE);
			} else {
				mIdentifyCard.setVisibility(View.GONE);
				vipLay.setVisibility(View.GONE);
			}
			if (clientUser.isShowDownloadVip) {
				mDownloadlayout.setVisibility(View.VISIBLE);
			} else {
				mDownloadlayout.setVisibility(View.GONE);
			}
			if (clientUser.isShowGold) {
				mMyGold.setVisibility(View.VISIBLE);
			} else {
				mMyGold.setVisibility(View.GONE);
			}
			if (clientUser.isShowLovers) {
				mLoversCard.setVisibility(View.VISIBLE);
			} else {
				mLoversCard.setVisibility(View.GONE);
			}
			if (clientUser.isShowVideo) {
				mVideoShowCard.setVisibility(View.VISIBLE);
			} else {
				mVideoShowCard.setVisibility(View.GONE);
			}
			if (clientUser.isShowAppointment) {
				mAppointmentLay.setVisibility(View.VISIBLE);
			} else {
				mAppointmentLay.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (unbinder != null) {
			unbinder.unbind();
		}
		EventBus.getDefault().unregister(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(this.getClass().getName());
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(this.getClass().getName());
	}
}
