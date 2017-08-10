package com.youdo.karma.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.youdo.karma.R;
import com.youdo.karma.adapter.MessageAdapter;
import com.youdo.karma.db.ConversationSqlManager;
import com.youdo.karma.entity.Conversation;
import com.youdo.karma.listener.MessageChangedListener;
import com.youdo.karma.ui.widget.DividerItemDecoration;
import com.youdo.karma.ui.widget.WrapperLinearLayoutManager;
import com.youdo.karma.utils.DensityUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

/**
 * @author: wangyb
 * @datetime: 2015-12-20 11:34 GMT+8
 * @email: 395044952@qq.com
 * @description:
 */
public class MessageFragment extends Fragment implements MessageChangedListener.OnMessageChangedListener {
    private View rootView;
    private RecyclerView mMessageRecyclerView;
    private MessageAdapter mAdapter;
    private List<Conversation> mConversations;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_message, null);
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
                R.string.tab_message);
        return rootView;
    }

    private void setupViews(){
        mMessageRecyclerView = (RecyclerView) rootView.findViewById(R.id.message_recycler_view);
        LinearLayoutManager layoutManager = new WrapperLinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mMessageRecyclerView.setLayoutManager(layoutManager);
        mMessageRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mMessageRecyclerView.addItemDecoration(new DividerItemDecoration(
                getActivity(), LinearLayoutManager.VERTICAL, DensityUtil
                .dip2px(getActivity(), 12), DensityUtil.dip2px(
                getActivity(), 12)));
    }

    private void setupEvent(){
        MessageChangedListener.getInstance().setMessageChangedListener(this);
    }

    private void setupData(){
        mAdapter = new MessageAdapter(getActivity(), mConversations);
        mMessageRecyclerView.setAdapter(mAdapter);
        getData();
    }

    private void getData(){
        mConversations = ConversationSqlManager.getInstance(getActivity()).queryConversations();
        if(mConversations != null && !mConversations.isEmpty()){
            mAdapter.setConversations(mConversations);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onMessageChanged(String conversationId) {
        getData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MessageChangedListener.getInstance().setMessageChangedListener(null);
        MessageChangedListener.getInstance().clearAllMessageChangedListener();
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
