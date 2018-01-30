package com.youdo.karma.activity;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.youdo.karma.R;
import com.youdo.karma.activity.base.BaseActivity;
import com.youdo.karma.adapter.FMessageAdapter;
import com.youdo.karma.config.ValueKey;
import com.youdo.karma.db.FConversationSqlManager;
import com.youdo.karma.entity.Conversation;
import com.youdo.karma.entity.FConversation;
import com.youdo.karma.eventtype.DataEvent;
import com.youdo.karma.ui.widget.DividerItemDecoration;
import com.youdo.karma.ui.widget.WrapperLinearLayoutManager;
import com.youdo.karma.utils.DensityUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by wangyb on 2017/10/23.
 * 描述：真实用户发给假用户的会话信息
 */

public class FConversationActivity extends BaseActivity {

    @BindView(R.id.message_recycler_view)
    RecyclerView mMessageRecyclerView;

    private FMessageAdapter mAdapter;
    private List<FConversation> mConversations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_f_message);
        Toolbar toolbar = getActionBarToolbar();
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.mipmap.ic_up);
        }
        ButterKnife.bind(this);
        setupViews();
        setupEvent();
        initData();
    }

    private void setupViews(){
        LinearLayoutManager layoutManager = new WrapperLinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mMessageRecyclerView.setLayoutManager(layoutManager);
        mMessageRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mMessageRecyclerView.addItemDecoration(new DividerItemDecoration(
                this, LinearLayoutManager.VERTICAL, DensityUtil
                .dip2px(this, 12), DensityUtil.dip2px(
                this, 12)));
    }

    private void setupEvent(){
        EventBus.getDefault().register(this);
    }

    private void initData() {
        Conversation conversation = (Conversation) getIntent().getSerializableExtra(ValueKey.CONVERSATION);
        if (null != conversation) {
            mConversations = FConversationSqlManager.getInstance(this).queryConversations(conversation.id);
            if (mConversations != null && !mConversations.isEmpty()) {
                mAdapter = new FMessageAdapter(this, conversation, mConversations);
                mMessageRecyclerView.setAdapter(mAdapter);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateConversation(DataEvent event) {
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
