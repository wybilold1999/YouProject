package com.youdo.karma.activity;

import android.arch.lifecycle.Lifecycle;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.youdo.karma.R;
import com.youdo.karma.activity.base.BaseActivity;
import com.youdo.karma.config.AppConstants;
import com.youdo.karma.entity.ClientUser;
import com.youdo.karma.manager.AppManager;
import com.youdo.karma.net.IUserApi;
import com.youdo.karma.net.base.RetrofitFactory;
import com.youdo.karma.utils.AESEncryptorUtil;
import com.youdo.karma.utils.ProgressDialogUtils;
import com.youdo.karma.utils.ToastUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;
import com.umeng.analytics.MobclickAgent;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import mehdi.sakout.fancybuttons.FancyButton;

/**
 * @author Cloudsoar(wangyb)
 * @datetime 2015-12-26 14:55 GMT+8
 * @email 395044952@qq.com
 */
public class ModifyPwdActivity extends BaseActivity implements View.OnClickListener{
    private FancyButton mSure;
    private EditText mPassword;
    private EditText mPwdAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_pwd);
        Toolbar toolbar = getActionBarToolbar();
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.mipmap.ic_up);
        }
        getSupportActionBar().setTitle(R.string.modify_pwd);

        setupView();
        setupEvent();
    }

    private void setupView(){
        mSure = findViewById(R.id.btn_sure);
        mPassword = findViewById(R.id.new_pwd);
        mPwdAgain = findViewById(R.id.new_pwd_again);
    }

    private void setupEvent(){
        mSure.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_sure :
                if(inputCheck()){
                    String cryptPwd = AESEncryptorUtil.crypt(
                            mPassword.getText().toString().trim(), AppConstants.SECURITY_KEY);
                    ProgressDialogUtils.getInstance(this).show(R.string.dialog_modifying);
                    modifyPwd(cryptPwd);
                }
                break;
        }
    }

    private void modifyPwd(String newPwd) {
        RetrofitFactory.getRetrofit().create(IUserApi.class)
                .modifyPwd(AppManager.getClientUser().sessionId, newPwd)
                .subscribeOn(Schedulers.io())
                .map(responseBody -> {
                    ClientUser clientUser = AppManager.getClientUser();
                    clientUser.userPwd = AESEncryptorUtil.crypt(mPwdAgain.getText().toString().trim(), AppConstants.SECURITY_KEY);
                    AppManager.setClientUser(clientUser);
                    AppManager.saveUserInfo();
                    JsonObject obj = new JsonParser().parse(responseBody.string()).getAsJsonObject();
                    int code = obj.get("code").getAsInt();
                    return code;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(integer -> {
                    ProgressDialogUtils.getInstance(ModifyPwdActivity.this).dismiss();
                    if (integer == 0) {
                        ToastUtil.showMessage(R.string.modify_success);
                    } else {
                        ToastUtil.showMessage(R.string.modify_faiure);
                    }
                    finish();
                }, throwable -> {
                    ProgressDialogUtils.getInstance(ModifyPwdActivity.this).dismiss();
                    ToastUtil.showMessage(R.string.network_requests_error);
                });

    }

    /**
     * 输入验证
     */
    private boolean inputCheck() {
        String message = "";
        boolean bool = true;
        if (TextUtils.isEmpty(mPwdAgain.getText().toString())) {
            message = getResources().getString(
                    R.string.again_input_password_tips);
            bool = false;
        } if (TextUtils.isEmpty(mPassword.getText().toString())) {
            message = getResources().getString(
                    R.string.input_password);
            bool = false;
        } else if(!mPwdAgain.getText().toString().trim().equals(
                mPassword.getText().toString().trim())){
                message = getResources().getString(
                    R.string.input_password_different);
            bool = false;
        }
        if (!bool)
            ToastUtil.showMessage(message);
        return bool;
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
