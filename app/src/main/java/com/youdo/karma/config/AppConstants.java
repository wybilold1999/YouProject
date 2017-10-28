package com.youdo.karma.config;

import com.xiaomi.account.openauth.XiaomiOAuthConstants;

/**
 * 
 * @ClassName:Constants
 * @Description:定义全局常量
 * @Author:wangyb
 * @Date:2015年5月12日上午9:26:45
 *
 */
public class AppConstants {
	
	public static final String BASE_URL = "http://112.74.85.193/YouLoveServer/";
//	public static final String BASE_URL = "http://192.168.1.100/YouLoveServer/";
//	public static final String BASE_URL = "http://10.0.108.198:8080/YouLoveServer/";

	/**
	 * 密码加密密匙
	 */
	public static final String SECURITY_KEY = "ABCD1234abcd5678";

	/**
	 * 请求位置的高德web api的key
	 */
	public static final String WEB_KEY = "d64c0240c9790d4c56498b152a4ca193";

	/**
	 *容联云IM
	 */
	public static String YUNTONGXUN_ID = "8aaf07085dcad420015dcb8739920097";
	public static String YUNTONGXUN_TOKEN = "aaa7d3c2510dfeb27e8f13b45fc93f5b";

	/**
	 * QQ登录的appid和appkey
	 */
	public static String mAppid = "1106273929";

	/**
	 * 微信登录
	 */
	public static String WEIXIN_ID = "wxf6c70fcd5522d4d3";

	/**
	 * 微信登录
	 */
	public static String WEIXIN_PAY_ID = "wxf6c70fcd5522d4d3";

	/**
	 * 短信
	 */
	public static final String SMS_INIT_KEY = "2015595f1e9d8";
	public static final String SMS_INIT_SECRET = "04b28a4b4aa3e37a3f16148c155a35c1";

	/**
	 * 小米推送appid
	 */
	public static String MI_PUSH_APP_ID = "2882303761517606814";
	/**
	 * 小米推送appkey
	 */
	public static String MI_PUSH_APP_KEY = "5731760661814";

	public static final String MI_ACCOUNT_REDIRECT_URI = "http://www.cyanbirds.cn";

	public static final int[] MI_SCOPE = new int[]{XiaomiOAuthConstants.SCOPE_PROFILE, XiaomiOAuthConstants.SCOPE_OPEN_ID};

	/**
	 * 阿里图片节点
	 */
	public static final String OSS_IMG_ENDPOINT = "http://real-love-server.img-cn-shenzhen.aliyuncs.com/";

	public static final String WX_PAY_PLATFORM = "wxpay";

	public static final String ALI_PAY_PLATFORM = "alipay";

	public static final int CHAT_LIMIT = 3;

}
