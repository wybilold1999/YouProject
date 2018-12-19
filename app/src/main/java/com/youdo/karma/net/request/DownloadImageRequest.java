package com.youdo.karma.net.request;

import com.youdo.karma.entity.IMessage;
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

/**
 * 
 * @Description:下载图片请求
 * @author wangyb
 * @Date:2015年7月10日下午5:06:04
 */

public class DownloadImageRequest extends ResultPostExecute<File> {

	/**
	 * 下载请求
	 *
	 * @param url
	 *            请求地址
	 * @param savePath
	 *            保存地址
	 * @param fileName 保存文件名
	 * @param message
	 *            当不是消息的时候可以为空
	 */
	public void request(String url, String savePath,
						String fileName,final IMessage message) {
		final File file = new File(savePath, fileName);
		RetrofitFactory.getRetrofit().create(IDownLoadApi.class)
				.downloadFileWithDynamicUrlSync(url)
				.enqueue(new Callback<ResponseBody>() {
					@Override
					public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
						FileUtils.writeResponseBodyToDisk(response.body(), file.getAbsolutePath(), new DownloadListener(){
							@Override
							public void progress(int progress) {
								if (progress >= 100) {
									if (message != null)
										message.status = IMessage.MessageStatus.RECEIVED;
								}
								if (message != null)
									FileProgressListener.getInstance()
											.notifyFileProgressChanged(message, progress);
							}

							@Override
							public void completed(String path) {
								onPostExecute(file);
								if (message != null) {
									message.localPath = file.getPath();
									FileProgressListener.getInstance()
											.notifyFileProgressChanged(message, 100);
								}
							}

							@Override
							public void error(String error) {
							}
						});
					}

					@Override
					public void onFailure(Call<ResponseBody> call, Throwable t) {

					}
				});
	}
}
