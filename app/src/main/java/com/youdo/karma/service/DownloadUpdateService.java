package com.youdo.karma.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;

import com.youdo.karma.R;
import com.youdo.karma.config.ValueKey;
import com.youdo.karma.entity.IMessage;
import com.youdo.karma.listener.FileProgressListener;
import com.youdo.karma.manager.AppManager;
import com.youdo.karma.net.request.DownloadApkFileRequest;
import com.youdo.karma.utils.FileAccessorUtils;
import com.youdo.karma.utils.ToastUtil;

import java.io.File;

/**
 * 作者：wangyb
 * 时间：2016/9/24 17:42
 * 描述：
 */
public class DownloadUpdateService extends Service implements FileProgressListener.OnFileProgressChangedListener {

	private NotificationManager manager = null;
	private NotificationCompat.Builder builder = null;
	private Notification notification = null;

	private String apkUrl = null;

	public DownloadUpdateService() {
		/*manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		builder = new NotificationCompat.Builder(DownloadUpdateService.this);
		builder.setSmallIcon(R.mipmap.ic_launcher);
		builder.setContentTitle(getString(R.string.app_name));
		builder.setTicker(getString(R.string.downloading));
		builder.setContentText(String.format(getString(R.string.download_progress), 0f));

		notification = builder.build();
		notification.vibrate = new long[]{500, 500};
		notification.defaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND;
		manager.notify(0, notification);*/
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		FileProgressListener.getInstance().addOnFileProgressChangedListener(this);
		manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		builder = new NotificationCompat.Builder(DownloadUpdateService.this);
		builder.setSmallIcon(R.mipmap.ic_launcher);
		builder.setContentTitle(getString(R.string.app_name));
		builder.setTicker(getString(R.string.downloading));
		builder.setContentText(String.format(getString(R.string.download_progress), 0f));

		notification = builder.build();
//		notification.vibrate = new long[]{500, 500};
//		notification.defaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND;
		notification.defaults = Notification.DEFAULT_SOUND;
		manager.notify(0, notification);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		apkUrl = intent.getStringExtra(ValueKey.APK_URL);
		File apkFile = new File(FileAccessorUtils.APK_PATH, "tanlove.apk");
		if (apkFile.exists()) {
			apkFile.delete();
		}
		new DownloadApkFileTask().request(apkUrl, FileAccessorUtils.APK_PATH, "tanlove.apk");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onFileProgressChanged(IMessage message, int progress) {
		float fprogress = progress;
		builder.setContentText(String.format(getString(R.string.download_progress), fprogress));
		builder.setProgress(100, progress, false);
		manager.notify(0, builder.build());
	}

	class DownloadApkFileTask extends DownloadApkFileRequest {
		@Override
		public void onPostExecute(String s) {
			FileProgressListener.getInstance().removeOnFileProgressChangedListener(DownloadUpdateService.this);
			File apkFile = new File(s);

			/**
			 * 创建通知栏的intent
			 */
			Intent notifyIntent = new Intent(Intent.ACTION_VIEW);
			Uri uri = Uri.fromFile(apkFile);
			//设置intent的类型
			notifyIntent.setDataAndType(uri,
					"application/vnd.android.package-archive");
			PendingIntent pendingIntent = PendingIntent.getActivity(DownloadUpdateService.this, 0, notifyIntent, 0);
			builder.setContentIntent(pendingIntent);
			builder.setContentText(getString(R.string.download_finish));
			builder.setProgress(100, 100, false);
			manager.notify(0, builder.build());

			/**
			 * 弹出app的安装界面
			 */
			AppManager.installApk(apkFile);

			stopSelf();
		}

		@Override
		public void onErrorExecute(String error) {
			ToastUtil.showMessage(error);
			builder.setContentText(getString(R.string.download_fail));
			manager.notify(0, builder.build());
			showVersionInfo();
		}
	}

	private void showVersionInfo() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.download_fail_retry);
		builder.setPositiveButton(getResources().getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						/**
						 * 开始下载apk文件
						 */
						new DownloadApkFileTask().request(apkUrl, FileAccessorUtils.APK_PATH, "tanlove.apk");
					}
				});
		builder.show();
	}

}

/**
 * 使用DownloadManager下载文件
 */
/*
public class DownloadUpdateService extends Service {
	private DownloadManager dm;
	private long enqueue;
	private BroadcastReceiver receiver;

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String apkUrl = intent.getStringExtra(ValueKey.APK_URL);
		File apkFile = new File(FileAccessorUtils.APK_PATH, "tanlove.apk");
		if (apkFile.exists()) {
			apkFile.delete();
		}
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setDataAndType(Uri.fromFile(new File(FileAccessorUtils.APK_PATH, "tanlove.apk")),
						"application/vnd.android.package-archive");
				startActivity(intent);
				stopSelf();
			}
		};

		registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		DownloadApkFile(apkUrl);
		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	private void DownloadApkFile(String apkUrl) {
		DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
		DownloadManager.Request request = new DownloadManager.Request(
				Uri.parse(apkUrl));
		request.setMimeType("application/vnd.android.package-archive");
		request.setDestinationInExternalPublicDir(FileAccessorUtils.APK_PATH, "tanlove.apk");
		dm.enqueue(request);
	}
}*/
