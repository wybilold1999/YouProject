package com.youdo.karma.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.youdo.karma.R;
import com.youdo.karma.activity.base.BaseActivity;
import com.youdo.karma.entity.ClientUser;
import com.youdo.karma.manager.AppManager;
import com.youdo.karma.net.request.DownloadFileRequest;
import com.youdo.karma.presenter.UserLoginPresenterImpl;
import com.youdo.karma.utils.FileAccessorUtils;
import com.youdo.karma.utils.Md5Util;
import com.youdo.karma.utils.PreferencesUtils;
import com.youdo.karma.utils.ProgressDialogUtils;
import com.youdo.karma.utils.ToastUtil;
import com.youdo.karma.view.IUserLoginLogOut;
import com.umeng.analytics.MobclickAgent;

import java.io.File;

import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Created by Administrator on 2016/4/23.
 */
public class SelectSexActivity extends BaseActivity<IUserLoginLogOut.Presenter> implements View.OnClickListener, IUserLoginLogOut.View{

    ImageView mSelectMan;
    ImageView mSelectLady;

    private String mWeChatCode;
    private String mQQToken;
    private String mQQOpenId;
    private String mHWOpenId;

    private FancyButton mBtLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_sex);
        Toolbar toolbar = getActionBarToolbar();
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.mipmap.ic_up);
        }
        setupView();
        setupEvent();
        setupData();
    }

    private void setupView() {
        mSelectMan = findViewById(R.id.select_man);
        mSelectLady = findViewById(R.id.select_lady);
        mBtLogin = findViewById(R.id.btn_login);
    }

    private void setupEvent() {
        mSelectMan.setOnClickListener(this);
        mSelectLady.setOnClickListener(this);
        mBtLogin.setOnClickListener(this);
    }

    private void setupData(){
        mWeChatCode = getIntent().getStringExtra("code");
        mQQToken = getIntent().getStringExtra("token");
        mQQOpenId = getIntent().getStringExtra("openId");
        mHWOpenId = getIntent().getStringExtra("hw_open_id");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.select_man :
                showSelectSexDialog(R.id.select_man);
                break;
            case R.id.select_lady :
                showSelectSexDialog(R.id.select_lady);
                break;
            case R.id.btn_login :
                if (!TextUtils.isEmpty(AppManager.getClientUser().sex)) {
                    loginType();
                } else {
                    ToastUtil.showMessage(R.string.please_select_sex);
                }
                break;
        }
    }

    private void showSelectSexDialog(final int sexId){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_sex);
        if(R.id.select_man == sexId) {
            builder.setMessage(String.format(getResources().getString(R.string.select_sex_tips), "男生"));
        } else {
            builder.setMessage(String.format(getResources().getString(R.string.select_sex_tips), "女生"));
        }
        builder.setNegativeButton(getResources().getString(R.string.cancel), ((dialog, i) -> dialog.dismiss()));
        builder.setPositiveButton(getResources().getString(R.string.ok), ((dialog, i) -> {
            mSelectMan.setEnabled(false);
            mSelectLady.setEnabled(false);
            if (sexId == R.id.select_man) {
                AppManager.getClientUser().sex = "1";
                mSelectMan.setImageResource(R.mipmap.radio_men_focused_bg);
            } else {
                AppManager.getClientUser().sex = "0";
                mSelectLady.setImageResource(R.mipmap.radio_women_focused_bg);
            }
            dialog.dismiss();
        }));
        builder.show();
    }

    private void loginType () {
        onShowLoading();
        if (!TextUtils.isEmpty(mWeChatCode)) {
            presenter.onWXLogin(mWeChatCode);
        } else if (!TextUtils.isEmpty(mQQToken) && !TextUtils.isEmpty(mQQOpenId)){
            presenter.onQQLogin(mQQToken, mQQOpenId);
        } else if (!TextUtils.isEmpty(mHWOpenId)) {
            presenter.onHWLogin(mHWOpenId);
        }
    }

    @Override
    public void onShowLoading() {
        ProgressDialogUtils.getInstance(SelectSexActivity.this).show(R.string.dialog_request_login);
    }

    @Override
    public void onHideLoading() {
        ProgressDialogUtils.getInstance(SelectSexActivity.this).dismiss();
    }

    @Override
    public void onShowNetError() {
        onHideLoading();
        ToastUtil.showMessage(R.string.network_requests_error);
    }

    @Override
    public void loginLogOutSuccess(ClientUser clientUser) {
        onHideLoading();
        if (clientUser != null) {
            hideSoftKeyboard();
            File faceLocalFile = new File(FileAccessorUtils.FACE_IMAGE,
                    Md5Util.md5(clientUser.face_url) + ".jpg");
            if(!faceLocalFile.exists()
                    && !TextUtils.isEmpty(clientUser.face_url)){
                new DownloadPortraitTask().request(clientUser.face_url,
                        FileAccessorUtils.FACE_IMAGE,
                        Md5Util.md5(clientUser.face_url) + ".jpg");
            }

            Intent intent = new Intent();
            intent.setClass(SelectSexActivity.this, MainActivity.class);
            startActivity(intent);
            finishAll();
        } else {
            ToastUtil.showMessage(R.string.phone_pwd_error);
        }
    }

    @Override
    public void setPresenter(IUserLoginLogOut.Presenter presenter) {
        if (presenter == null) {
            this.presenter = new UserLoginPresenterImpl(this);
        }
    }

    class DownloadPortraitTask extends DownloadFileRequest {
        @Override
        public void onPostExecute(String s) {
            AppManager.getClientUser().face_local = s;
            PreferencesUtils.setFaceLocal(SelectSexActivity.this, s);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
