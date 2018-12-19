package com.youdo.karma.activity;

import android.Manifest;
import android.arch.lifecycle.Lifecycle;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;
import com.umeng.analytics.MobclickAgent;
import com.youdo.karma.CSApplication;
import com.youdo.karma.R;
import com.youdo.karma.activity.base.BaseActivity;
import com.youdo.karma.adapter.ViewPagerAdapter;
import com.youdo.karma.db.ConversationSqlManager;
import com.youdo.karma.entity.ClientUser;
import com.youdo.karma.fragment.ContactsFragment;
import com.youdo.karma.fragment.FoundFragment;
import com.youdo.karma.fragment.FoundNewFragment;
import com.youdo.karma.fragment.HomeLoveFragment;
import com.youdo.karma.fragment.MessageFragment;
import com.youdo.karma.fragment.MyPersonalFragment;
import com.youdo.karma.fragment.PersonalFragment;
import com.youdo.karma.helper.BottomNavigationViewHelper;
import com.youdo.karma.helper.SDKCoreHelper;
import com.youdo.karma.listener.MessageUnReadListener;
import com.youdo.karma.manager.AppManager;
import com.youdo.karma.net.IUserApi;
import com.youdo.karma.net.base.RetrofitFactory;
import com.youdo.karma.ui.widget.CustomViewPager;
import com.youdo.karma.utils.CheckUtil;
import com.youdo.karma.utils.DensityUtil;
import com.youdo.karma.utils.PreferencesUtils;
import com.youdo.karma.utils.Utils;
import com.yuntongxun.ecsdk.ECInitParams;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

public class MainNewActivity extends BaseActivity implements MessageUnReadListener.OnMessageUnReadListener, AMapLocationListener {

	private CustomViewPager viewPager;
	private BottomNavigationView bottomNavigationView;

	private final int REQUEST_LOCATION_PERMISSION = 1000;

	private AMapLocationClientOption mLocationOption;
	private AMapLocationClient mlocationClient;
	private boolean isSecondAccess = false;

	private String curLat;
	private String curLon;

	private Badge mBadgeView;
	private QBadgeView mQBadgeView;
	private RxPermissions rxPermissions;

	private ViewPagerAdapter mViewPagerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_new);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		setupViews();
		setupEvent();
		updateConversationUnRead();

		locationSuccess();

		SDKCoreHelper.init(CSApplication.getInstance(), ECInitParams.LoginMode.FORCE_LOGIN);

	}

	/**
	 * 判断是否定位成功。成功就不定位了，直接上传城市
	 */
	private void locationSuccess() {
		String currentCity = AppManager.getClientUser().currentCity;
		curLat = AppManager.getClientUser().latitude;
		curLon = AppManager.getClientUser().longitude;
		boolean isLocSuc = PreferencesUtils.getIsLocationSuccess(this);
		if (isLocSuc && !TextUtils.isEmpty(currentCity) && !TextUtils.isEmpty(curLat) && !TextUtils.isEmpty(curLon)) {
			uploadCityInfoRequest(currentCity, curLat, curLon);
		} else {
			initLocationClient();
			requestLocationPermission();
		}
	}


	/**
	 * 初始化定位
	 */
	private void initLocationClient() {
		mlocationClient = new AMapLocationClient(this);
		//初始化定位参数
		mLocationOption = new AMapLocationClientOption();
		//设置定位监听
		mlocationClient.setLocationListener(this);
		//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
		mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
		//获取最近3s内精度最高的一次定位结果：
		mLocationOption.setOnceLocationLatest(true);
	}

	/**
	 * 开始定位
	 */
	private void startLocation() {
		//设置定位参数
		mlocationClient.setLocationOption(mLocationOption);
		//启动定位
		mlocationClient.startLocation();
	}

	/**
	 * 停止定位
	 */
	private void stopLocation(){
		// 停止定位
		mlocationClient.stopLocation();
	}

	/**
	 * 销毁定位
	 */
	private void destroyLocation(){
		if (null != mlocationClient) {
			/**
			 * 如果AMapLocationClient是在当前Activity实例化的，
			 * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
			 */
			mlocationClient.onDestroy();
			mlocationClient = null;
			mLocationOption = null;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);// 必须要调用这句(信鸽推送)
	}

	@Override
	public void onLocationChanged(AMapLocation aMapLocation) {
		if (aMapLocation != null && !TextUtils.isEmpty(aMapLocation.getCity())) {
			ClientUser clientUser = AppManager.getClientUser();
			clientUser.latitude = String.valueOf(aMapLocation.getLatitude());
			clientUser.longitude = String.valueOf(aMapLocation.getLongitude());
			AppManager.setClientUser(clientUser);
			curLat = clientUser.latitude;
			curLon = clientUser.longitude;

			uploadCityInfoRequest(aMapLocation.getCity(), String.valueOf(aMapLocation.getLatitude()),
					String.valueOf(aMapLocation.getLongitude()));

			PreferencesUtils.setCurrentCity(this, aMapLocation.getCity());
			PreferencesUtils.setCurrentProvince(this, aMapLocation.getProvince());
			PreferencesUtils.setLatitude(this, curLat);
			PreferencesUtils.setLongitude(this, curLon);
			PreferencesUtils.setIsLocationSuccess(this, true);

			if (!TextUtils.isEmpty(aMapLocation.getCity())) {
				stopLocation();
			}
		}

	}

	private void uploadCityInfoRequest(String city, String lat, String lon) {
		ArrayMap<String, String> params = new ArrayMap<>();
		params.put("channel", CheckUtil.getAppMetaData(CSApplication.getInstance(), "UMENG_CHANNEL"));
		params.put("currentCity", city);
		params.put("latitude", lat);
		params.put("longitude", lon);
		RetrofitFactory.getRetrofit().create(IUserApi.class)
				.uploadCityInfo(params, AppManager.getClientUser().sessionId)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY)))
				.subscribe(responseBody -> {} , throwable -> {});
	}

	/**
	 * 设置视图
	 */
	private void setupViews() {
		viewPager = findViewById(R.id.viewpager);
		viewPager.setNoScroll(true);
		bottomNavigationView = findViewById(R.id.bottom_navigation);
		//默认 >3 的选中效果会影响ViewPager的滑动切换时的效果，故利用反射去掉
		BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
		bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
			switch (item.getItemId()) {
				case R.id.item_news:
					viewPager.setCurrentItem(0);
					break;
				case R.id.item_lib:
					viewPager.setCurrentItem(1);
					break;
				case R.id.item_find:
					viewPager.setCurrentItem(2);
					break;
				case R.id.item_more:
					viewPager.setCurrentItem(3);
					break;
			}
			return false;
		});

		setupViewPager(viewPager);

		viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				if(bottomNavigationView.getMenu().getItem(position).isChecked()){
					bottomNavigationView.getMenu().getItem(position).setChecked(false);
				}
				String title = getResources().getString(R.string.tab_message);
				if (position == 1) {
					title = getResources().getString(R.string.tab_contacts);
				} else if (position == 2) {
					title = getResources().getString(R.string.tab_found);
				} else if (position == 3) {
					title = getResources().getString(R.string.tab_personal);
				}
				if (mViewPagerAdapter != null && mViewPagerAdapter.getItem(position) !=null &&
						mViewPagerAdapter.getItem(position).getActivity() != null) {
					mViewPagerAdapter.getItem(position).getActivity().setTitle(title);
				}
			}

			@Override
			public void onPageSelected(int position) {
				bottomNavigationView.getMenu().getItem(position).setChecked(true);
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});

		BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
		if (menuView != null) {
			mQBadgeView = new QBadgeView(this);
			mBadgeView = mQBadgeView.setGravityOffset((float) (DensityUtil.getWidthInPx(this) / 1.25), 2, false)
					.bindTarget(menuView);
		}
	}

	private void setupViewPager(ViewPager viewPager) {
		mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

		mViewPagerAdapter.addFragment(new MessageFragment());
		mViewPagerAdapter.addFragment(new ContactsFragment());
		mViewPagerAdapter.addFragment(new FoundNewFragment());
		mViewPagerAdapter.addFragment(new MyPersonalFragment());
		viewPager.setAdapter(mViewPagerAdapter);
	}


	private void setupEvent(){
		MessageUnReadListener.getInstance().setMessageUnReadListener(this);
	}


	@Override
	public void notifyUnReadChanged(int type) {
		updateConversationUnRead();
	}

	/**
	 * 更新会话未读消息总数
	 */
	private void updateConversationUnRead() {
		if (mBadgeView != null) {
			int total = ConversationSqlManager.getInstance(this)
					.getAnalyticsUnReadConversation();
			if (total > 0) {
				mQBadgeView.setVisibility(View.VISIBLE);
				if (total >= 100) {
					mBadgeView.setBadgeText("99+");
				} else {
					mBadgeView.setBadgeText(String.valueOf(total));
				}
			} else {
				mQBadgeView.setVisibility(View.GONE);
			}
		}
	}

	private void requestLocationPermission() {
		if (!CheckUtil.isGetPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ||
				!CheckUtil.isGetPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
			if (rxPermissions == null) {
				rxPermissions = new RxPermissions(this);
			}
			rxPermissions.requestEachCombined(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
					.subscribe(permission -> {// will emit 1 Permission object
						if (permission.granted) {
							// All permissions are granted !
							startLocation();
						} else if (permission.shouldShowRequestPermissionRationale) {
							// At least one denied permission without ask never again
							if (!isSecondAccess) {
								showAccessLocationDialog();
							}
						} else {
							// At least one denied permission with ask never again
							// Need to go to the settings
							if (!isSecondAccess) {
								showAccessLocationDialog();
							}
						}
					}, throwable -> {

					});
		} else {
			startLocation();
		}
	}

	private void showAccessLocationDialog() {
		isSecondAccess = true;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.permission_request);
		builder.setMessage(R.string.access_location);
		builder.setPositiveButton(R.string.ok, (dialog, i) -> {
			dialog.dismiss();
			Utils.goToSetting(MainNewActivity.this, REQUEST_LOCATION_PERMISSION);
		});
		builder.show();
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		destroyLocation();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			showQuitDialog();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void showQuitDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.quit_app_name);
		builder.setMessage(R.string.quit_are_you_sure);
		builder.setNegativeButton(R.string.cancel, (dialog, i) -> {
			dialog.dismiss();
		});
		builder.setPositiveButton(R.string.ok, (dialog, i) -> {
			dialog.dismiss();
			exitApp();
		});
		builder.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_LOCATION_PERMISSION) {
			isSecondAccess = false;
			requestLocationPermission();
		}
	}

}
