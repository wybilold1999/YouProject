package com.youdo.karma.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.youdo.karma.CSApplication;
import com.youdo.karma.R;
import com.youdo.karma.activity.base.BaseActivity;
import com.youdo.karma.config.AppConstants;
import com.youdo.karma.config.ValueKey;
import com.youdo.karma.entity.ClientUser;
import com.youdo.karma.eventtype.LocationEvent;
import com.youdo.karma.eventtype.WeinXinEvent;
import com.youdo.karma.eventtype.XMEvent;
import com.youdo.karma.helper.IMChattingHelper;
import com.youdo.karma.manager.AppManager;
import com.youdo.karma.net.request.CheckIsRegisterByPhoneRequest;
import com.youdo.karma.net.request.DownloadFileRequest;
import com.youdo.karma.net.request.GetMiAccessTokenRequest;
import com.youdo.karma.net.request.QqLoginRequest;
import com.youdo.karma.net.request.WXLoginRequest;
import com.youdo.karma.net.request.XMLoginRequest;
import com.youdo.karma.utils.CheckUtil;
import com.youdo.karma.utils.FileAccessorUtils;
import com.youdo.karma.utils.Md5Util;
import com.youdo.karma.utils.PreferencesUtils;
import com.youdo.karma.utils.ProgressDialogUtils;
import com.youdo.karma.utils.ToastUtil;
import com.youdo.karma.utils.Util;
import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.umeng.analytics.MobclickAgent;
import com.xiaomi.account.openauth.XiaomiOAuthFuture;
import com.xiaomi.account.openauth.XiaomiOAuthResults;
import com.xiaomi.account.openauth.XiaomiOAuthorize;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.smssdk.SMSSDK;
import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Created by Administrator on 2016/4/23.
 */
public class RegisterActivity extends BaseActivity {
    @BindView(R.id.phone_num)
    EditText phoneNum;
    @BindView(R.id.next)
    FancyButton next;
    @BindView(R.id.qq_login)
    ImageView qqLogin;
    @BindView(R.id.weixin_login)
    ImageView weiXinLogin;
    @BindView(R.id.select_man)
    ImageView mSelectMan;
    @BindView(R.id.select_lady)
    ImageView mSelectLady;
    @BindView(R.id.xm_login)
    ImageView xmLogin;

    /**
     * 相册返回
     */
    public final static int ALBUMS_RESULT = 102;

    public static Tencent mTencent;
    private UserInfo mInfo;
    private String token;
    private String openId;

    private ClientUser mClientUser;
    private String channelId;
    private boolean activityIsRunning;
    private String mCurrrentCity;//定位到的城市
    private String curLat;
    private String curLon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        Toolbar toolbar = getActionBarToolbar();
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.mipmap.ic_up);
        }
        if (mTencent == null) {
            mTencent = Tencent.createInstance(AppConstants.mAppid, this);
        }

        channelId = CheckUtil.getAppMetaData(this, "UMENG_CHANNEL");
        mCurrrentCity = getIntent().getStringExtra(ValueKey.LOCATION);
        curLat = getIntent().getStringExtra(ValueKey.LATITUDE);
        curLon = getIntent().getStringExtra(ValueKey.LONGITUDE);
    }


    @OnClick({R.id.next, R.id.qq_login,
            R.id.select_man, R.id.select_lady, R.id.weixin_login, R.id.xm_login})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.next:
                if(checkInput()){
                    new CheckPhoneIsRegisterTask().request(
                            phoneNum.getText().toString().trim());
                }
                break;
            case R.id.portrait :
                openAlbums();
                break;
            case R.id.qq_login:
                ProgressDialogUtils.getInstance(this).show(R.string.wait);
                if (!mTencent.isSessionValid() &&
                        mTencent.getQQToken().getOpenId() == null) {
                    mTencent.login(this, "all", loginListener);
                }
                break;
            case R.id.select_man :
                showSelectSexDialog(R.id.select_man);
                break;
            case R.id.select_lady :
                showSelectSexDialog(R.id.select_lady);
                break;
            case R.id.weixin_login:
                ProgressDialogUtils.getInstance(this).show(R.string.wait);
                SendAuth.Req req = new SendAuth.Req();
                req.scope = "snsapi_userinfo";
                req.state = "wechat_sdk_demo_test";
                CSApplication.api.sendReq(req);
                break;
            case R.id.xm_login:
                XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
                        .setAppId(Long.parseLong(AppConstants.MI_PUSH_APP_ID))
                        .setRedirectUrl(AppConstants.MI_ACCOUNT_REDIRECT_URI)
                        .setScope(AppConstants.MI_SCOPE)
                        .startGetAccessToken(this);
                new GetMiAccessTokenRequest(this, future).execute();
                break;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void xmLogin(XMEvent event) {
        ProgressDialogUtils.getInstance(RegisterActivity.this).show(R.string.dialog_request_login);
        new XMLoginTask().request(event.xmOAuthResults, channelId, mCurrrentCity);
    }

    public class XMLoginTask extends XMLoginRequest {
        @Override
        public void onPostExecute(ClientUser clientUser) {
            MobclickAgent.onProfileSignIn(String.valueOf(AppManager
                    .getClientUser().userId));
            if(!new File(FileAccessorUtils.FACE_IMAGE,
                    Md5Util.md5(clientUser.face_url) + ".jpg").exists()
                    && !TextUtils.isEmpty(clientUser.face_url)){
                new DownloadPortraitTask().request(clientUser.face_url,
                        FileAccessorUtils.FACE_IMAGE,
                        Md5Util.md5(clientUser.face_url) + ".jpg");
            }
            clientUser.currentCity = mCurrrentCity;
            clientUser.latitude = curLat;
            clientUser.longitude = curLon;
            AppManager.setClientUser(clientUser);
            AppManager.saveUserInfo();
            AppManager.getClientUser().loginTime = System.currentTimeMillis();
            PreferencesUtils.setLoginTime(RegisterActivity.this, System.currentTimeMillis());
            IMChattingHelper.getInstance().sendInitLoginMsg();
            Intent intent = new Intent();
            intent.setClass(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finishAll();
        }

        @Override
        public void onErrorExecute(String error) {
            ProgressDialogUtils.getInstance(RegisterActivity.this).dismiss();
            ToastUtil.showMessage(error);
        }
    }
    
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void weiXinLogin(WeinXinEvent event) {
        ProgressDialogUtils.getInstance(RegisterActivity.this).show(R.string.dialog_request_login);
        new WXLoginTask().request(event.code, channelId, mCurrrentCity);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getCity(LocationEvent event) {
        mCurrrentCity = event.city;
    }
    
    class WXLoginTask extends WXLoginRequest {
        @Override
        public void onPostExecute(ClientUser clientUser) {
            MobclickAgent.onProfileSignIn(String.valueOf(AppManager
                    .getClientUser().userId));
            if(!new File(FileAccessorUtils.FACE_IMAGE,
                    Md5Util.md5(clientUser.face_url) + ".jpg").exists()
                    && !TextUtils.isEmpty(clientUser.face_url)){
                new RegisterActivity.DownloadPortraitTask().request(clientUser.face_url,
                        FileAccessorUtils.FACE_IMAGE,
                        Md5Util.md5(clientUser.face_url) + ".jpg");
            }
            clientUser.currentCity = mCurrrentCity;
            clientUser.latitude = curLat;
            clientUser.longitude = curLon;
            AppManager.setClientUser(clientUser);
            AppManager.saveUserInfo();
            AppManager.getClientUser().loginTime = System.currentTimeMillis();
            PreferencesUtils.setLoginTime(RegisterActivity.this, System.currentTimeMillis());
            IMChattingHelper.getInstance().sendInitLoginMsg();
            Intent intent = new Intent();
            intent.setClass(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finishAll();
        }
        
        @Override
        public void onErrorExecute(String error) {
            ProgressDialogUtils.getInstance(RegisterActivity.this).dismiss();
            ToastUtil.showMessage(error);
        }
    }

    class CheckPhoneIsRegisterTask extends CheckIsRegisterByPhoneRequest {
        @Override
        public void onPostExecute(Boolean s) {
            if(s){
                ToastUtil.showMessage(R.string.phone_already_register);
            } else {
                //获取验证码
                String phone_num = phoneNum.getText().toString().trim();
                mClientUser.mobile = phone_num;
                mClientUser.currentCity = mCurrrentCity;
                SMSSDK.getVerificationCode("86", phone_num);
                Intent intent = new Intent(RegisterActivity.this, RegisterCaptchaActivity.class);
                intent.putExtra(ValueKey.PHONE_NUMBER, phone_num);
                intent.putExtra(ValueKey.INPUT_PHONE_TYPE, 0);
                intent.putExtra(ValueKey.USER, mClientUser);
                startActivity(intent);
            }
        }

        @Override
        public void onErrorExecute(String error) {
        }
    }

    /**
     * 打开相册
     */
    private void openAlbums() {
        Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
        openAlbumIntent.setType("image/*");
        startActivityForResult(openAlbumIntent, ALBUMS_RESULT);
    }

    IUiListener loginListener = new BaseUiListener() {
        @Override
        protected void doComplete(JSONObject values) {
            initOpenidAndToken(values);
            updateUserInfo();
        }
    };

    private class BaseUiListener implements IUiListener {

        @Override
        public void onComplete(Object response) {
            if (null == response) {
                ToastUtil.showMessage("登录失败");
                return;
            }
            JSONObject jsonResponse = (JSONObject) response;
            if (null != jsonResponse && jsonResponse.length() == 0) {
                ToastUtil.showMessage("登录失败");
                return;
            }
            doComplete((JSONObject)response);
        }

        protected void doComplete(JSONObject values) {

        }

        @Override
        public void onError(UiError e) {
            Util.toastMessage(RegisterActivity.this, "onError: " + e.errorDetail);
            Util.dismissDialog();
        }

        @Override
        public void onCancel() {
            Util.toastMessage(RegisterActivity.this, "取消授权");
        }
    }

    public void initOpenidAndToken(JSONObject jsonObject) {
        try {
            token = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN);
            String expires = jsonObject.getString(Constants.PARAM_EXPIRES_IN);
            openId = jsonObject.getString(Constants.PARAM_OPEN_ID);
            if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires)
                    && !TextUtils.isEmpty(openId)) {
                mTencent.setAccessToken(token, expires);
                mTencent.setOpenId(openId);
            }
        } catch(Exception e) {
        }
    }

    private void updateUserInfo() {
        if (mTencent != null && mTencent.isSessionValid()) {
            IUiListener listener = new IUiListener() {
                @Override
                public void onError(UiError e) {
                }
                @Override
                public void onComplete(final Object response) {
                    if (activityIsRunning) {
                        ProgressDialogUtils.getInstance(RegisterActivity.this).show(R.string.dialog_request_login);
                    }
                    new QqLoginTask().request(token, openId, channelId, mCurrrentCity);
                }

                @Override
                public void onCancel() {

                }
            };
            mInfo = new UserInfo(this, mTencent.getQQToken());
            mInfo.getUserInfo(listener);
        } else {
        }
    }

    class QqLoginTask extends QqLoginRequest {
        @Override
        public void onPostExecute(ClientUser clientUser) {
            if(!new File(FileAccessorUtils.FACE_IMAGE,
                    Md5Util.md5(clientUser.face_url) + ".jpg").exists()
                    && !TextUtils.isEmpty(clientUser.face_url)){
                new DownloadPortraitTask().request(clientUser.face_url,
                        FileAccessorUtils.FACE_IMAGE,
                        Md5Util.md5(clientUser.face_url) + ".jpg");
            }
            clientUser.currentCity = mCurrrentCity;
            clientUser.latitude = curLat;
            clientUser.longitude = curLon;
            AppManager.setClientUser(clientUser);
            AppManager.saveUserInfo();
            AppManager.getClientUser().loginTime = System.currentTimeMillis();
            PreferencesUtils.setLoginTime(RegisterActivity.this, System.currentTimeMillis());
            IMChattingHelper.getInstance().sendInitLoginMsg();
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finishAll();
        }

        @Override
        public void onErrorExecute(String error) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_LOGIN ||
                requestCode == Constants.REQUEST_APPBAR) {
            Tencent.onActivityResultData(requestCode, resultCode, data, loginListener);
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
        builder.setNegativeButton(getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mSelectMan.setEnabled(false);
                        mSelectLady.setEnabled(false);
                        if(mClientUser == null){
                            mClientUser = new ClientUser();
                        }
                        if (sexId == R.id.select_man) {
                            mClientUser.sex = "男";
                            mSelectMan.setImageResource(R.mipmap.radio_men_focused_bg);
                        } else {
                            mClientUser.sex = "女";
                            mSelectLady.setImageResource(R.mipmap.radio_women_focused_bg);
                        }
                        dialog.dismiss();
                        mClientUser.age = 20;
                    }
                });
        builder.show();
    }



    class DownloadPortraitTask extends DownloadFileRequest {
        @Override
        public void onPostExecute(String s) {
            AppManager.getClientUser().face_local = s;
            PreferencesUtils.setFaceLocal(RegisterActivity.this, s);
        }

        @Override
        public void onErrorExecute(String error) {
        }
    }

    /**
     * 验证输入
     */
    private boolean checkInput() {
        String message = "";
        boolean bool = true;
        if(mSelectMan.isEnabled() && mSelectLady.isEnabled()){
            message = getResources().getString(R.string.please_select_sex);
            bool = false;
        } else if (TextUtils.isEmpty(phoneNum.getText().toString())) {
            message = getResources().getString(R.string.input_phone);
            bool = false;
        } else if (!CheckUtil.isMobileNO(phoneNum.getText().toString())) {
            message = getResources().getString(
                    R.string.input_phone_number_error);
            bool = false;
        }
        if (!bool)
            ToastUtil.showMessage(message);
        return bool;
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityIsRunning = true;
        ProgressDialogUtils.getInstance(this).dismiss();
        MobclickAgent.onPageStart(this.getClass().getName());
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityIsRunning = false;
        MobclickAgent.onPageEnd(this.getClass().getName());
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ProgressDialogUtils.getInstance(this).dismiss();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
