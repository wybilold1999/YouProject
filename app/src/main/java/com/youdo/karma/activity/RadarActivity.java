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
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.youdo.karma.R;
import com.youdo.karma.activity.base.BaseActivity;
import com.youdo.karma.config.ValueKey;
import com.youdo.karma.entity.YuanFenModel;
import com.youdo.karma.manager.AppManager;
import com.youdo.karma.net.request.GetYuanFenUserRequest;
import com.youdo.karma.utils.ToastUtil;
import com.facebook.drawee.view.SimpleDraweeView;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.util.List;

/**
 * 作者：wangyb
 * 时间：2016/9/18 19:54
 * 描述：请求缘分
 */
public class RadarActivity extends BaseActivity {

	private int pageNo = 0;
	private int pageSize = 200;

	private RelativeLayout mLoadingLay;
	private ImageView radarbttom;
	private ImageView radartop;
	private ImageView mAnnularImg;
	private AnimationSet grayAnimal;
	private SimpleDraweeView mPortrait;
	private Handler mHandler = new Handler();



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_radar_view);
		setupView();
	}

	private void setupView() {
		mLoadingLay = (RelativeLayout) findViewById(R.id.loading_lay);
		mAnnularImg = (ImageView) findViewById(R.id.radar_img);
		radartop = (ImageView) findViewById(R.id.radar_top_img);
		radarbttom = (ImageView) findViewById(R.id.radar_bttom_img);
		mPortrait = (SimpleDraweeView) findViewById(R.id.portrait);

		if (!TextUtils.isEmpty(AppManager.getClientUser().face_local)) {
			mPortrait.setImageURI(Uri.parse("file://" + AppManager.getClientUser().face_local));
		}

		startcircularAnima();
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				new GetYuanFenUserTask().request(pageNo, pageSize);
			}
		}, 3000);
	}

	private void startcircularAnima() {
		grayAnimal = playHeartbeatAnimation();
		radarbttom.startAnimation(grayAnimal);
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
		mAnnularImg.setVisibility(View.VISIBLE);
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
				mAnnularImg.setVisibility(View.GONE);
			}
		});
		mAnnularImg.startAnimation(annularAnimat);
	}

	private void startwhiteAnimal() {
		AnimationSet whiteAnimal = playHeartbeatAnimation();
		whiteAnimal.setRepeatCount(0);
		whiteAnimal.setDuration(600);
		radartop.setVisibility(View.VISIBLE);
		radartop.startAnimation(whiteAnimal);
		whiteAnimal.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mAnnularImg.setVisibility(View.GONE);
				radartop.setVisibility(View.GONE);
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

	class GetYuanFenUserTask extends GetYuanFenUserRequest {
		@Override
		public void onPostExecute(List<YuanFenModel> yuanFenModels) {
			mLoadingLay.setVisibility(View.GONE);
			Intent intent = new Intent(RadarActivity.this, CardActivity.class);
			intent.putExtra(ValueKey.USER, (Serializable) yuanFenModels);
			startActivity(intent);
			finish();
		}

		@Override
		public void onErrorExecute(String error) {
			ToastUtil.showMessage(error);
			finish();
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
}
