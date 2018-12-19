package com.youdo.karma.activity;

import android.arch.lifecycle.Lifecycle;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.youdo.karma.R;
import com.youdo.karma.activity.base.BaseActivity;
import com.youdo.karma.adapter.MyAttentionAdapter;
import com.youdo.karma.config.ValueKey;
import com.youdo.karma.entity.FollowModel;
import com.youdo.karma.manager.AppManager;
import com.youdo.karma.net.IUserFollowApi;
import com.youdo.karma.net.base.RetrofitFactory;
import com.youdo.karma.ui.widget.CircularProgress;
import com.youdo.karma.ui.widget.DividerItemDecoration;
import com.youdo.karma.ui.widget.WrapperLinearLayoutManager;
import com.youdo.karma.utils.DensityUtil;
import com.youdo.karma.utils.JsonUtils;
import com.youdo.karma.utils.ToastUtil;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;
import com.umeng.analytics.MobclickAgent;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Cloudsoar(wangyb)
 * @datetime 2016-01-13 22:17 GMT+8
 * @email 395044952@qq.com
 */
public class MyAttentionActivity extends BaseActivity {
    private RecyclerView mRecyclerView;
    private CircularProgress mCircularProgress;
    private TextView mNoUserInfo;
    private MyAttentionAdapter mAdapter;

    private int pageNo = 1;
    private int pageSize = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_attention);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.my_attention);
        setupView();
        setupEvent();
        setupData();
    }

    private void setupView(){
        mCircularProgress = (CircularProgress) findViewById(R.id.progress_bar);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mNoUserInfo = (TextView) findViewById(R.id.info);
        LinearLayoutManager manager = new WrapperLinearLayoutManager(this);
        manager.setOrientation(LinearLayout.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                this, LinearLayoutManager.VERTICAL, DensityUtil
                .dip2px(this, 12), DensityUtil.dip2px(
                this, 12)));
    }

    private void setupEvent(){

    }

    private void setupData(){
        mAdapter = new MyAttentionAdapter(MyAttentionActivity.this);
        mAdapter.setOnItemClickListener(mOnItemClickListener);
        mRecyclerView.setAdapter(mAdapter);
        mCircularProgress.setVisibility(View.VISIBLE);
        requestFollowList("followList", pageNo, pageSize);
    }

    private MyAttentionAdapter.OnItemClickListener mOnItemClickListener = new MyAttentionAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            FollowModel followModel = mAdapter.getItem(position);
            Intent intent = new Intent(MyAttentionActivity.this, PersonalInfoActivity.class);
            intent.putExtra(ValueKey.USER_ID, String.valueOf(followModel.userId));
            startActivity(intent);
        }
    };

    private void requestFollowList(String url, int pageNo, int pageSize) {
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("uid", AppManager.getClientUser().userId);
        params.put("pageNo", String.valueOf(pageNo));
        params.put("pageSize", String.valueOf(pageSize));
        RetrofitFactory.getRetrofit().create(IUserFollowApi.class)
                .getFollowList(url, AppManager.getClientUser().sessionId, params)
                .subscribeOn(Schedulers.io())
                .map(responseBody -> JsonUtils.parseJsonFollows(responseBody.string()))
                .observeOn(AndroidSchedulers.mainThread())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(followModels -> {
                    mCircularProgress.setVisibility(View.GONE);
                    if(null != followModels && followModels.size() > 0){
                        mNoUserInfo.setVisibility(View.GONE);
                        mAdapter.setFollowModels(followModels);
                    } else {
                        mNoUserInfo.setVisibility(View.VISIBLE);
                    }
                }, throwable -> {
                    mCircularProgress.setVisibility(View.GONE);
                    ToastUtil.showMessage(R.string.network_requests_error);
                });
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
