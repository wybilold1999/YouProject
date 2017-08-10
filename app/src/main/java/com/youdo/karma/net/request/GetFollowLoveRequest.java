package com.youdo.karma.net.request;
import com.youdo.karma.CSApplication;
import com.youdo.karma.R;
import com.youdo.karma.entity.FollowLoveModel;
import com.youdo.karma.manager.AppManager;
import com.youdo.karma.net.base.ResultPostExecute;
import com.youdo.karma.utils.AESOperator;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author Cloudsoar(wangyb)
 * @datetime 2016-05-03 16:48 GMT+8
 * @email 395044952@qq.com
 * 在“我”界面获取关注和喜欢我的用户信息
 */
public class GetFollowLoveRequest extends ResultPostExecute<FollowLoveModel> {
    public void request(final String userId){
        Call<ResponseBody> call = AppManager.getFollowService().getFollowAndLoveInfo(AppManager.getClientUser().sessionId, userId);
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
                    onErrorExecute(CSApplication.getInstance()
                            .getResources()
                            .getString(R.string.network_requests_error));
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

    private void parseJson(String json) {
        try {
            String decryptData = AESOperator.getInstance().decrypt(json);
            JsonObject obj = new JsonParser().parse(decryptData).getAsJsonObject();
            int code = obj.get("code").getAsInt();
            if (code == 1) {
                onErrorExecute(obj.get("msg").getAsString());
                return;
            }
            Gson gson = new Gson();
            FollowLoveModel model = gson.fromJson(obj.get("data").getAsJsonObject(), FollowLoveModel.class);
            onPostExecute(model);
        } catch (Exception e) {
            onErrorExecute(CSApplication.getInstance().getResources()
                    .getString(R.string.data_parser_error));
        }
    }
}
