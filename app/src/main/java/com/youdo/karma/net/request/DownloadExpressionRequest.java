package com.youdo.karma.net.request;

import android.content.Context;

import com.youdo.karma.entity.ExpressionGroup;
import com.youdo.karma.listener.DownloadListener;
import com.youdo.karma.listener.DownloadProgressExpressionListener;
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
 * @Description:表情下载
 * @author zxj
 * @Date:2015年7月22日下午5:37:48
 */
@SuppressWarnings("deprecation")
public class DownloadExpressionRequest extends ResultPostExecute<String> {

	/**
	 * 下载请求
	 * 
	 * @param context
	 * @param url
	 *            请求地址
	 * @param savePath
	 *            保存地址
	 */
	public void request(Context context, String url, String savePath,
                        String fileName, final ExpressionGroup expressionGroup) {
		final File file = new File(savePath, fileName);
		RetrofitFactory.getRetrofit().create(IDownLoadApi.class)
				.downloadFileWithDynamicUrlSync(url)
				.enqueue(new Callback<ResponseBody>() {
					@Override
					public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
						FileUtils.writeResponseBodyToDisk(response.body(), file.getAbsolutePath(), new DownloadListener(){
							@Override
							public void progress(int progress) {
								if (expressionGroup != null)
									DownloadProgressExpressionListener.getInstance()
											.notifyExpressionProgressChanged(expressionGroup,
													progress, true);
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
