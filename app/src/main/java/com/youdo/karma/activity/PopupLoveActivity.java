package com.youdo.karma.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.youdo.karma.R;
import com.youdo.karma.activity.base.BaseActivity;
import com.youdo.karma.config.ValueKey;
import com.youdo.karma.entity.ClientUser;
import com.youdo.karma.entity.LoveModel;
import com.youdo.karma.manager.AppManager;
import com.facebook.drawee.view.SimpleDraweeView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mehdi.sakout.fancybuttons.FancyButton;

/**
 * 作者：wangyb
 * 时间：2017/7/3 00:30
 * 描述：
 */
public class PopupLoveActivity extends BaseActivity {
    @BindView(R.id.my_portrait)
    SimpleDraweeView mMyPortrait;
    @BindView(R.id.other_portrait)
    SimpleDraweeView mOtherPortrait;
    @BindView(R.id.love_name)
    TextView mLoveName;
    @BindView(R.id.send_msg)
    FancyButton mSendMsg;
    @BindView(R.id.going_find)
    FancyButton mGoingFind;

    private LoveModel mLoveModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_love);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        mLoveModel = (LoveModel) getIntent().getSerializableExtra(ValueKey.DATA);
        if (!TextUtils.isEmpty(AppManager.getClientUser().face_url)) {
            mMyPortrait.setImageURI(AppManager.getClientUser().face_url);
        }
        if (mLoveModel != null) {
            mOtherPortrait.setImageURI(mLoveModel.faceUrl);
            mLoveName.setText(Html.fromHtml(String.format(getResources().getString(R.string.love_name), mLoveModel.nickname)));
        }
    }

    @OnClick({R.id.other_portrait, R.id.send_msg, R.id.going_find})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.other_portrait:
                if (mLoveModel != null) {
                    Intent intent = new Intent(this, PersonalInfoActivity.class);
                    intent.putExtra(ValueKey.USER_ID, String.valueOf(mLoveModel.userId));
                    startActivity(intent);
                    finish();
                }
                break;
            case R.id.send_msg:
                if (mLoveModel != null) {
                    Intent intent = new Intent(this, ChatActivity.class);
                    ClientUser clientUser = new ClientUser();
                    clientUser.face_local = mLoveModel.faceUrl;
                    clientUser.user_name = mLoveModel.nickname;
                    clientUser.userId = String.valueOf(mLoveModel.userId);
                    intent.putExtra(ValueKey.USER, clientUser);
                    startActivity(intent);
                }
                break;
            case R.id.going_find:
                finish();
                break;
        }
    }
}
