package com.youdo.karma.net.request;

import com.youdo.karma.CSApplication;
import com.youdo.karma.R;
import com.youdo.karma.manager.AppManager;
import com.youdo.karma.net.base.ResultPostExecute;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author Cloudsoar(wangyb)
 * @datetime 2016-05-03 10:38 GMT+8
 * @email 395044952@qq.com
 */
public class UploadCrashRequest extends ResultPostExecute<String> {
    public void request(String crashInfo){
        Call<ResponseBody> call = AppManager.getUserService().uploadCrash(crashInfo);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if(response.isSuccessful()){

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
}
