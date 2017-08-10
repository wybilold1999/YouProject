package com.youdo.karma.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.youdo.karma.R;
import com.youdo.karma.activity.base.BaseActivity;
import com.youdo.karma.adapter.CardAdapter;
import com.youdo.karma.config.ValueKey;
import com.youdo.karma.entity.CardModel;
import com.youdo.karma.entity.YuanFenModel;
import com.youdo.karma.manager.AppManager;
import com.youdo.karma.net.request.AddLoveRequest;
import com.youdo.karma.net.request.GetYuanFenUserRequest;
import com.youdo.karma.net.request.SendGreetRequest;
import com.youdo.karma.utils.ToastUtil;
import com.facebook.drawee.view.SimpleDraweeView;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 作者：wangyb
 * 时间：2016/9/18 20:07
 * 描述：卡片
 */
public class CardActivity extends BaseActivity implements SwipeFlingAdapterView.onFlingListener,
		SwipeFlingAdapterView.OnItemClickListener {

	@BindView(R.id.left)
	ImageView mLeft;
	@BindView(R.id.info)
	ImageView mInfo;
	@BindView(R.id.right)
	ImageView mRight;
	@BindView(R.id.frame)
	SwipeFlingAdapterView mFrame;
	@BindView(R.id.loading_lay)
	RelativeLayout mLoadingLay;
	@BindView(R.id.data_lay)
	FrameLayout mDataLay;
	@BindView(R.id.radar_img)
	ImageView mRadarImg;
	@BindView(R.id.radar_bttom_img)
	ImageView mRadarBttomImg;
	@BindView(R.id.radar_top_img)
	ImageView mRadarTopImg;
	@BindView(R.id.portrait)
	SimpleDraweeView mPortrait;

	private CardModel curModel;
	private int pageNo = 1;
	private int pageSize = 200;
	private Handler mHandler = new Handler();
	private AnimationSet grayAnimal;
	private List<CardModel> dataList = new ArrayList<CardModel>();
	private CardAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_card);
		ButterKnife.bind(this);
		Toolbar toolbar = getActionBarToolbar();
		if (toolbar != null) {
			toolbar.setNavigationIcon(R.mipmap.ic_up);
		}
		setupView();
		setupEvent();
		setupData();
	}

	private void setupView() {
		mLoadingLay.setVisibility(View.VISIBLE);
		mDataLay.setVisibility(View.GONE);
	}

	private void setupEvent() {
		mFrame.setOnItemClickListener(this);
		mFrame.setFlingListener(this);
	}

	private void setupData() {
		dataList = new ArrayList<>();
		mAdapter = new CardAdapter(this, dataList);
		mFrame.setAdapter(mAdapter);

		if (!TextUtils.isEmpty(AppManager.getClientUser().face_local)) {
			mPortrait.setImageURI(Uri.parse("file://" + AppManager.getClientUser().face_local));
		}

		startcircularAnima();
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				new GetYuanFenUserTask().request(pageNo, pageSize);
			}
		}, 3500);
	}

	@OnClick({R.id.left, R.id.info, R.id.right})
	public void onViewClicked(View view) {
		switch (view.getId()) {
			case R.id.left:
				mFrame.getTopCardListener().selectLeft();
				break;
			case R.id.info:
				if (curModel != null) {
					Intent intent = new Intent(this, PersonalInfoActivity.class);
					intent.putExtra(ValueKey.USER_ID, String.valueOf(curModel.userId));
					startActivity(intent);
				}
				break;
			case R.id.right:
				mFrame.getTopCardListener().selectRight();
				break;
		}
	}

	@Override
	public void onItemClicked(int itemPosition, Object dataObject) {
		CardModel model = (CardModel) dataObject;
		Intent intent = new Intent(this, PersonalInfoActivity.class);
		intent.putExtra(ValueKey.USER_ID, String.valueOf(model.userId));
		startActivity(intent);
	}

	@Override
	public void removeFirstObjectInAdapter() {
		if (dataList != null && dataList.size() > 0) {
			dataList.remove(0);
			mAdapter.notifyDataSetChanged();
			if (dataList.size() > 0) {
				curModel = dataList.get(0);
			}
		}
	}

	@Override
	public void onLeftCardExit(Object dataObject) {

	}

	@Override
	public void onRightCardExit(Object dataObject) {
		CardModel cardModel = (CardModel) dataObject;
		new SenderGreetTask().request(String.valueOf(cardModel.userId));
		new AddLoveRequest().request(String.valueOf(cardModel.userId));
	}

	@Override
	public void onAdapterAboutToEmpty(int itemsInAdapter) {
		if (itemsInAdapter == 0) {
			mLoadingLay.setVisibility(View.VISIBLE);
			mDataLay.setVisibility(View.GONE);
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					new GetYuanFenUserTask().request(pageNo, pageSize);
				}
			}, 3500);
		}
	}

	@Override
	public void onScroll(float scrollProgressPercent) {

	}

	class GetYuanFenUserTask extends GetYuanFenUserRequest {
		@Override
		public void onPostExecute(List<YuanFenModel> yuanFenModels) {
			mLoadingLay.setVisibility(View.GONE);
			mDataLay.setVisibility(View.VISIBLE);
			if (yuanFenModels != null && yuanFenModels.size() > 0) {
				dataList.clear();
				for (YuanFenModel model : yuanFenModels) {
					CardModel dataItem = new CardModel();
					dataItem.userId = model.uid;
					dataItem.userName = model.nickname;
					dataItem.imagePath = model.faceUrl;
					dataItem.city = model.city;
					dataItem.age = model.age;
					dataItem.constellation = model.constellation;
					dataItem.distance = model.distance == null ? 0.00 : model.distance;
					dataItem.signature = model.signature;
					dataItem.pictures = model.pictures;
					dataList.add(dataItem);
				}
				curModel = dataList.get(0);
				mAdapter.notifyDataSetChanged();
			}
		}

		@Override
		public void onErrorExecute(String error) {
			ToastUtil.showMessage(error);
		}
	}

	class SenderGreetTask extends SendGreetRequest {
		@Override
		public void onPostExecute(String s) {
			ToastUtil.showMessage(s);
		}

		@Override
		public void onErrorExecute(String error) {
		}
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

	private void startcircularAnima() {
		grayAnimal = playHeartbeatAnimation();
		mRadarBttomImg.startAnimation(grayAnimal);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				startwhiteAnimal();
			}
		}, 500);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				startannularAnimat();
			}
		}, 700);
	}

	private AnimationSet playHeartbeatAnimation() {
		AnimationSet animationSet = new AnimationSet(true);
		ScaleAnimation sa = new ScaleAnimation(0.3f, 1.0f, 0.3f, 1.0f,
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
		sa.setDuration(800);
		sa.setFillAfter(true);
		sa.setRepeatCount(0);
		sa.setInterpolator(new LinearInterpolator());
		animationSet.addAnimation(sa);
		return animationSet;
	}

	private void startannularAnimat() {
		mRadarImg.setVisibility(View.VISIBLE);
		AnimationSet annularAnimat = getAnimAnnular();
		annularAnimat.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mRadarImg.setVisibility(View.GONE);
			}
		});
		mRadarImg.startAnimation(annularAnimat);
	}

	private void startwhiteAnimal() {
		AnimationSet whiteAnimal = playHeartbeatAnimation();
		whiteAnimal.setRepeatCount(0);
		whiteAnimal.setDuration(600);
		mRadarTopImg.setVisibility(View.VISIBLE);
		mRadarTopImg.startAnimation(whiteAnimal);
		whiteAnimal.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mRadarImg.setVisibility(View.GONE);
				mRadarTopImg.setVisibility(View.GONE);
				startcircularAnima();
			}
		});

	}

	private AnimationSet getAnimAnnular() {
		AnimationSet animationSet = new AnimationSet(true);
		ScaleAnimation sa = new ScaleAnimation(1.0f, 1.5f, 1.0f, 1.5f,
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
		animationSet.addAnimation(new AlphaAnimation(1.0f, 0.1f));
		animationSet.setDuration(400);
		sa.setDuration(500);
		sa.setFillAfter(true);
		sa.setRepeatCount(0);
		sa.setInterpolator(new LinearInterpolator());
		animationSet.addAnimation(sa);
		return animationSet;
	}
}
