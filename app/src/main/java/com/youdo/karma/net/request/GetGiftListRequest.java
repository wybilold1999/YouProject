package com.youdo.karma.net.request;

import com.youdo.karma.CSApplication;
import com.youdo.karma.R;
import com.youdo.karma.entity.ApkInfo;
import com.youdo.karma.entity.Gift;
import com.youdo.karma.entity.IdentifyCard;
import com.youdo.karma.manager.AppManager;
import com.youdo.karma.net.base.ResultPostExecute;
import com.youdo.karma.utils.AESOperator;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by Administrator on 2016/5/1.
 */
public class GetGiftListRequest extends ResultPostExecute<List<Gift>> {
    public void request() {
        Call<ResponseBody> call = AppManager.getUserService().getGift(AppManager.getClientUser().sessionId);
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
            if (code != 0) {
                onErrorExecute(CSApplication.getInstance().getResources()
                        .getString(R.string.data_load_error));
                return;
            }
            String data = obj.get("data").getAsString();
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Gift>>() {
            }.getType();
            List<Gift> gifts = gson.fromJson(data, listType);
            onPostExecute(gifts);
        } catch (Exception e) {
            onErrorExecute(CSApplication.getInstance().getResources()
                    .getString(R.string.data_parser_error));
        }
    }
}
