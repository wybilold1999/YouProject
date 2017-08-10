package com.youdo.karma.net.request;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.youdo.karma.config.AppConstants;
import com.youdo.karma.entity.IDKey;
import com.youdo.karma.manager.AppManager;
import com.youdo.karma.net.base.ResultPostExecute;
import com.youdo.karma.utils.AESOperator;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by wangyb on 2017/5/17.
 * 描述：获取微信id
 */

public class GetIDKeyRequest extends ResultPostExecute<List<IDKey>> {

    public void request() {
        Call<ResponseBody> call = AppManager.getUserService().getIdKey();
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
                Type listType = new TypeToken<ArrayList<IDKey>>() {
                }.getType();
                Gson gson = new Gson();
                ArrayList<IDKey> idKeys = gson.fromJson(decryptData, listType);
                if (idKeys != null && idKeys.size() > 0) {
                    for (IDKey idKey : idKeys) {
                        if ("xiaomi".equals(idKey.platform)) {
                            AppConstants.MI_PUSH_APP_ID = idKey.appId;
                            AppConstants.MI_PUSH_APP_KEY = idKey.appKey;
                        } else if ("wechat".equals(idKey.platform)) {
                            AppConstants.WEIXIN_ID = idKey.appId;
                        } else if ("qq".equals(idKey.platform)) {
                            AppConstants.mAppid = idKey.appId;
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }

}
