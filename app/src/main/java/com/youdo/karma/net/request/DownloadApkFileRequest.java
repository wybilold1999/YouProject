package com.youdo.karma.net.request;


import com.youdo.karma.listener.DownloadListener;
import com.youdo.karma.listener.FileProgressListener;
import com.youdo.karma.net.IDownLoadApi;
import com.youdo.karma.net.base.ResultPostExecute;
import com.youdo.karma.net.base.RetrofitFactory;
import com.youdo.karma.utils.FileUtils;

import java.io.File;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/***
 * @author wangyb
 * @ClassName:DownloadFileRequest
 * @Description:下载文件请求
 * @Date:2015年6月9日下午9:00:21
 */

public class DownloadApkFileRequest extends ResultPostExecute<String> {

    /**
     * 下载请求
     *
     * @param url          请求地址
     * @param savePath     保存地址
     * @param fileName     保存文件名
     */
    public void request(String url, final String savePath, String fileName) {
        final File file = new File(savePath, fileName);
        RetrofitFactory.getRetrofit().create(IDownLoadApi.class)
                .downloadFileWithDynamicUrlSync(url)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        FileUtils.writeResponseBodyToDisk(response.body(), file.getAbsolutePath(), new DownloadListener(){
                            @Override
                            public void progress(int progress) {
                                FileProgressListener.getInstance().notifyFileProgressChanged(null, progress);
                            }

                            @Override
                            public void completed(String path) {
                                onPostExecute(file.getPath());
                            }

                            @Override
                            public void error(String error) {
                                onErrorExecute("下载失败");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        onErrorExecute("下载失败");
                    }
                });
    }
}
