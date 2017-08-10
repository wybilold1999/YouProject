package com.youdo.karma.net.request;

import android.text.TextUtils;

import com.youdo.karma.CSApplication;
import com.youdo.karma.R;
import com.youdo.karma.entity.FederationToken;
import com.youdo.karma.manager.AppManager;
import com.youdo.karma.net.base.ResultPostExecute;
import com.youdo.karma.utils.AESOperator;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * 获取oss鉴权
 * Created by Administrator on 2016/3/14.
 */
public class GetOSSTokenRequest extends ResultPostExecute<FederationToken> {

    public void request() {
        Call<ResponseBody> call = AppManager.getUserService().getOSSToken();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    try {
                        parserData(response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                        onErrorExecute(CSApplication.getInstance()
                                .getResources()
                                .getString(R.string.network_requests_error));
                    } finally {
                        response.body().close();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                onErrorExecute(CSApplication.getInstance()
                        .getResources()
                        .getString(R.string.network_requests_error));
            }
        });
    }

    /**
     * 解析数据
     *
     * @param result
     */
    private void parserData(String result) {
        try {
            if (!TextUtils.isEmpty(result)) {
                String data = AESOperator.getInstance().decrypt(result);
                Gson gson = new Gson();
                FederationToken token = gson.fromJson(data, FederationToken.class);
                if (token != null && !TextUtils.isEmpty(token.accessKeySecret) && !TextUtils.isEmpty(token.accessKeyId) && !TextUtils.isEmpty(token.bucketName) && !TextUtils.isEmpty(token.imagesEndpoint)) {
                    onPostExecute(token);
                } else {
                    onErrorExecute("");
                }
            } else {
                onErrorExecute("");
            }
        } catch (Exception e) {
            onErrorExecute(CSApplication.getInstance().getResources()
                    .getString(R.string.data_parser_error));
        }
    }

}
