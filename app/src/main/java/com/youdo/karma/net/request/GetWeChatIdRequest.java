package com.youdo.karma.net.request;

import android.text.TextUtils;

import com.youdo.karma.config.AppConstants;
import com.youdo.karma.entity.CityInfo;
import com.youdo.karma.manager.AppManager;
import com.youdo.karma.net.base.ResultPostExecute;
import com.youdo.karma.utils.AESOperator;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by wangyb on 2017/5/17.
 * 描述：获取微信id
 */

public class GetWeChatIdRequest extends ResultPostExecute<String> {

    public void request() {
        Call<ResponseBody> call = AppManager.getUserService().getWeChatId();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    try {
                        parseJson(response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        response.body().close();
                    }
                } else {
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    private void parseJson(String json){
        try {
            String decryptData = AESOperator.getInstance().decrypt(json);
            if (!TextUtils.isEmpty(decryptData)) {
                AppConstants.WEIXIN_ID = decryptData;
                onPostExecute(decryptData);
            }
        } catch (Exception e) {
        }
    }

}
