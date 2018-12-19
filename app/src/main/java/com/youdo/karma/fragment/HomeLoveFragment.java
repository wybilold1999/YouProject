package com.youdo.karma.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.youdo.karma.R;
import com.youdo.karma.activity.BandPhoneActivity;
import com.youdo.karma.adapter.HomeTabFragmentAdapter;
import com.youdo.karma.manager.AppManager;
import com.youdo.karma.service.ConnectServerService;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 作者：wangyb
 * 时间：2016/9/26 14:30
 * 描述：
 */
public class HomeLoveFragment extends Fragment {

	@BindView(R.id.tabs)
	TabLayout mTabLayout;
	@BindView(R.id.viewpager)
	ViewPager mViewpager;

	private View rootView;

	private List<String> tabList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_homelove, null);
			ButterKnife.bind(this, rootView);
			setupView();
			setupData();
			setHasOptionsMenu(true);
		}
		ViewGroup parent = (ViewGroup) rootView.getParent();
		if (parent != null) {
			parent.removeView(rootView);
		}
		return rootView;
	}

	private void setupView() {
		tabList = new ArrayList<>();
		tabList.add("颜值");
		tabList.add("同城");
		tabList.add("全国");
		mTabLayout.setTabMode(TabLayout.MODE_FIXED);//设置tab模式，当前为系统默认模式
		mTabLayout.addTab(mTabLayout.newTab().setText(tabList.get(0)));//添加tab选项卡
		mTabLayout.addTab(mTabLayout.newTab().setText(tabList.get(1)));
		mTabLayout.addTab(mTabLayout.newTab().setText(tabList.get(2)));

		HomeTabFragmentAdapter fragmentAdapter = new HomeTabFragmentAdapter(
				getChildFragmentManager(), tabList);
		mViewpager.setAdapter(fragmentAdapter);//给ViewPager设置适配器
		mTabLayout.setupWithViewPager(mViewpager);//将TabLayout和ViewPager关联起来。
		mTabLayout.setTabsFromPagerAdapter(fragmentAdapter);
	}

	private void setupData() {
		startConnectServerService();
		if (AppManager.getClientUser().isShowVip &&
				AppManager.getClientUser().isShowTd &&
				!AppManager.getClientUser().isCheckPhone) {//显示vip，并且isShowTd为true且未绑定号码的时候
			showBandPhoneDialog();
		}
	}

	private void startConnectServerService() {
		Intent intent = new Intent(getActivity(), ConnectServerService.class);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			getActivity().startForegroundService(intent);
		} else {
			getActivity().startService(intent);
		}
	}

	private void showBandPhoneDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.bangding_phone);
		builder.setMessage(R.string.band_phone_for_u);
		builder.setPositiveButton(getResources().getString(R.string.bangding_phone),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						Intent intent = new Intent(getActivity(), BandPhoneActivity.class);
						getActivity().startActivity(intent);
					}
				});
		builder.setCancelable(false);
		builder.show();
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
