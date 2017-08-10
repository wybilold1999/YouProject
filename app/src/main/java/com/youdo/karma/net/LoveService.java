package com.youdo.karma.net;

import android.support.v4.util.ArrayMap;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * @author Cloudsoar(wangyb)
 * @datetime 2016-06-17 17:48 GMT+8
 * @description
 */
public interface LoveService {
    @FormUrlEncoded
    @POST("love/addLove")
    Call<ResponseBody> addLove(@Header("token") String token, @Field("loveId") String loveId);

    @FormUrlEncoded
    @POST("love/loveFormeList")
    Call<ResponseBody> getLoveFormeList(@Header("token") String token, @FieldMap ArrayMap<String, String> params);

    @FormUrlEncoded
    @POST("greet/sendGreet")
    Call<ResponseBody> sendGreet(@Header("token") String token, @Field("greetId") String greetId);
}
