package com.youdo.karma.fragment;

import android.arch.lifecycle.Lifecycle;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;
import com.umeng.analytics.MobclickAgent;
import com.youdo.karma.R;
import com.youdo.karma.adapter.FoundNewAdapter;
import com.youdo.karma.db.ContactSqlManager;
import com.youdo.karma.entity.Contact;
import com.youdo.karma.listener.ModifyContactsListener;
import com.youdo.karma.manager.AppManager;
import com.youdo.karma.net.IUserApi;
import com.youdo.karma.net.base.RetrofitFactory;
import com.youdo.karma.ui.widget.CircularProgress;
import com.youdo.karma.ui.widget.DividerItemDecoration;
import com.youdo.karma.ui.widget.WrapperLinearLayoutManager;
import com.youdo.karma.utils.DensityUtil;
import com.youdo.karma.utils.JsonUtils;
import com.youdo.karma.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @author: wangyb
 * @datetime: 2015-12-20 11:33 GMT+8
 * @email: 395044952@qq.com
 * @description:
 */
public class FoundNewFragment extends Fragment implements ModifyContactsListener.OnDataChangedListener {
    private RecyclerView mRecyclerView;
    private CircularProgress mProgressBar;
    private View rootView;
    private FoundNewAdapter mAdapter;
    private LinearLayoutManager layoutManager;
    private List<Contact> mNetContacts;//网络请求的联系人
    private List<Contact> mContacts;//通讯录已存在的好友
    private int pageIndex = 1;
    private int pageSize = 50;
    private String GENDER = ""; //空表示查询和自己性别相反的用户
    /**
     * 0:同城 1：缘分 2：颜值  -1:就是全国
     */
    private String mUserScopeType = "0";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_found, null);
            setupViews();
            setupEvent();
            setupData();
            setHasOptionsMenu(true);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(
                R.string.tab_found);
        return rootView;
    }

    private void setupViews() {
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        mProgressBar = (CircularProgress) rootView.findViewById(R.id.progress_bar);
        layoutManager = new WrapperLinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                getActivity(), LinearLayoutManager.VERTICAL, DensityUtil
                .dip2px(getActivity(), 12), DensityUtil.dip2px(
                getActivity(), 12)));
    }

    private void setupEvent(){
        mRecyclerView.addOnScrollListener(mOnScrollListener);
        ModifyContactsListener.getInstance().addOnDataChangedListener(this);
    }

    private void setupData() {
        mNetContacts = new ArrayList<>();
        mAdapter = new FoundNewAdapter(mNetContacts, getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mProgressBar.setVisibility(View.VISIBLE);
        getContactsRequest(pageIndex, pageSize, GENDER, mUserScopeType);

        mContacts = ContactSqlManager.getInstance(getActivity()).queryAllContactsByFrom(true);
    }

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {

        private int lastVisibleItem;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            lastVisibleItem = layoutManager.findLastVisibleItemPosition();
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE
                    && lastVisibleItem + 1 == mAdapter.getItemCount()
                    && mAdapter.isShowFooter()) {
                //加载更多
                //请求数据
                getContactsRequest(++pageIndex, pageSize, GENDER, mUserScopeType);
            }
        }
    };

    @Override
    public void onDataChanged(Contact contact) {
        if (contact != null) {
            for (Contact clientUser : mNetContacts) {
                if (contact.userId.equals(clientUser.userId)) {
                    mNetContacts.remove(clientUser);
                    break;
                }
            }
            mAdapter.setClientUsers(mNetContacts);
        }
    }

    @Override
    public void onDeleteDataChanged(String userId) {
    }

    @Override
    public void onAddDataChanged(Contact contact) {

    }

    private void getContactsRequest(final int pageNo, final int pageSize,
                                    final String gender, final String mUserScopeTypen) {
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("pageNo", String.valueOf(pageNo));
        params.put("pageSize", String.valueOf(pageSize));
        params.put("gender", gender);
        params.put("user_scope_type", mUserScopeType);
        RetrofitFactory.getRetrofit().create(IUserApi.class)
                .getContactList(AppManager.getClientUser().sessionId, params)
                .subscribeOn(Schedulers.io())
                .map(responseBody -> JsonUtils.parseListContact(responseBody.string()))
                .observeOn(AndroidSchedulers.mainThread())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(contactList -> {
                    if (mContacts != null && mContacts.size() > 0) {
                        List<Contact> clientUsers = new ArrayList<>();
                        clientUsers.addAll(contactList);
                        for (Contact contact : mContacts) {
                            for (Contact clientUser : clientUsers) {
                                if (contact.userId.equals(clientUser.userId)) {
                                    contactList.remove(clientUser);
                                    break;
                                }
                            }
                        }
                    }

                    mProgressBar.setVisibility(View.GONE);
                    if (pageIndex == 1) {//进行筛选的时候，滑动到顶部
                        layoutManager.scrollToPositionWithOffset(0, 0);
                    }
                    if(contactList == null || contactList.size() == 0){
                        mAdapter.setIsShowFooter(false);
                        mAdapter.notifyDataSetChanged();
                        ToastUtil.showMessage(R.string.no_more_data);
                    } else {
                        mNetContacts.addAll(contactList);
                        mAdapter.setIsShowFooter(true);
                        mAdapter.setClientUsers(mNetContacts);
                    }
                }, throwable -> {
                    ToastUtil.showMessage(R.string.network_requests_error);
                    mProgressBar.setVisibility(View.GONE);
                    mAdapter.notifyDataSetChanged();
                });
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        ModifyContactsListener.getInstance().removeOnDataChangedListener(this);
    }
}
