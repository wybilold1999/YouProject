package com.youdo.karma;

import android.graphics.Bitmap;
import android.support.multidex.MultiDexApplication;
import android.util.SparseIntArray;

import com.youdo.karma.config.AppConstants;
import com.youdo.karma.helper.AppActivityLifecycleCallbacks;
import com.youdo.karma.helper.CrashHandler;
import com.youdo.karma.manager.AppManager;
import com.youdo.karma.net.DynamicService;
import com.youdo.karma.net.FollowService;
import com.youdo.karma.net.LoveService;
import com.youdo.karma.net.PictureService;
import com.youdo.karma.net.UserService;
import com.youdo.karma.net.VideoService;
import com.youdo.karma.net.base.RetrofitManager;
import com.youdo.karma.utils.FileAccessorUtils;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.listener.RequestListener;
import com.facebook.imagepipeline.listener.RequestLoggingListener;
import com.facebook.imagepipeline.memory.PoolConfig;
import com.facebook.imagepipeline.memory.PoolFactory;
import com.facebook.imagepipeline.memory.PoolParams;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.util.FileDownloadHelper;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.util.HashSet;
import java.util.Set;

import cn.smssdk.SMSSDK;
import okhttp3.OkHttpClient;

/**
 * 
 * @ClassName:CSApplication
 * @Description:全局Application
 * @author wangyb
 * @Date:2015年5月3日上午10:39:37
 *
 */
public class CSApplication extends MultiDexApplication {

	private static CSApplication sApplication;

	// IWXAPI 是第三方app和微信通信的openapi接口
	public static IWXAPI api;

	public static synchronized CSApplication getInstance() {
		return sApplication;
	}


	@Override
	public void onCreate() {
		super.onCreate();
		sApplication = this;
		RetrofitManager.getInstance();//初始化retrofit
		initNetInterface();
		AppManager.setContext(sApplication);
		AppManager.setUserInfo();
		//初始化短信sdk
		SMSSDK.initSDK(this, AppConstants.SMS_INIT_KEY, AppConstants.SMS_INIT_SECRET);
		initFresco();

		FileDownloader.init(sApplication, new FileDownloadHelper.OkHttpClientCustomMaker() {
			@Override
			public OkHttpClient customMake() {
				return RetrofitManager.getInstance().getOkHttpClient();
			}
		});

		CrashHandler.getInstance().init(this);

		registerActivityLifecycleCallbacks(AppActivityLifecycleCallbacks.getInstance());

		registerWeiXin();

		/*Stetho.initialize(Stetho
				.newInitializerBuilder(this)
				.enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
				.enableWebKitInspector(
						Stetho.defaultInspectorModulesProvider(this)).build());*/
	}

	private void registerWeiXin() {
		// 通过WXAPIFactory工厂，获取IWXAPI的实例
		api = WXAPIFactory.createWXAPI(this, AppConstants.WEIXIN_ID, true);
		api.registerApp(AppConstants.WEIXIN_ID);
	}

	private void initFresco() {
		DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(this)
				.setBaseDirectoryPath(FileAccessorUtils.getCachePathName())
				.setBaseDirectoryName("you_love")
				.setMaxCacheSize(500*1024*1024)//500MB
				.setMaxCacheSizeOnLowDiskSpace(10 * 1024 * 1024)
				.setMaxCacheSizeOnVeryLowDiskSpace(5 * 1024 * 1024)
				.build();

		Set<RequestListener> listeners = new HashSet<>();
		listeners.add(new RequestLoggingListener());

		int MaxRequestPerTime = 64;
		SparseIntArray defaultBuckets = new SparseIntArray();
		defaultBuckets.put(16 * ByteConstants.KB, MaxRequestPerTime);
		PoolParams smallByteArrayPoolParams = new PoolParams(
				16 * ByteConstants.KB * MaxRequestPerTime,
				2 * ByteConstants.MB,
				defaultBuckets);
		PoolFactory factory = new PoolFactory(
				PoolConfig.newBuilder()
						. setSmallByteArrayPoolParams(smallByteArrayPoolParams)
						.build());

		ImagePipelineConfig config = OkHttpImagePipelineConfigFactory
				.newBuilder(this, RetrofitManager.getInstance().getOkHttpClient())
				.setBitmapsConfig(Bitmap.Config.RGB_565)
				.setDownsampleEnabled(true)
				.setPoolFactory(factory)
				.setMainDiskCacheConfig(diskCacheConfig)
				.setRequestListeners(listeners).build();
		Fresco.initialize(this, config);
	}

	/**
	 * 初始化网络接口
	 */
	private void initNetInterface(){
		/**
		 * 用户
		 */
		UserService userService = RetrofitManager.getInstance().getRetrofitInstance().create(UserService.class);
		AppManager.setUserService(userService);
		/**
		 * 图片
		 */
		PictureService pictureService = RetrofitManager.getInstance().getRetrofitInstance().create(PictureService.class);
		AppManager.setPictureService(pictureService);
		/**
		 * 关注
		 */
		FollowService followService = RetrofitManager.getInstance().getRetrofitInstance().create(FollowService.class);
		AppManager.setFollowService(followService);
		/**
		 * 喜欢
		 */
		LoveService loveService = RetrofitManager.getInstance().getRetrofitInstance().create(LoveService.class);
		AppManager.setLoveService(loveService);
		/**
		 * 视频
		 */
		VideoService videoService = RetrofitManager.getInstance().getRetrofitInstance().create(VideoService.class);
		AppManager.setVideoService(videoService);
		/**
		 * 动态
		 */
		DynamicService dynamicService = RetrofitManager.getInstance().getRetrofitInstance().create(DynamicService.class);
		AppManager.setDynamicService(dynamicService);
	}
}
