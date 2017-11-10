package com.youdo.karma.utils;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @ClassName:处理字符串工具类
 * @Description:
 * @author wangyb
 * @Date:2015年5月24日下午9:35:12
 *
 */
public class StringUtil {

	/**
	 * 将list转换为字符串
	 * 
	 * @param list
	 * @return
	 */
	public static String listToString(List<String> list) {
		StringBuffer sb = new StringBuffer();
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				sb.append(list.get(i));
				if (list.size() != (i + 1)) {
					sb.append(";");
				}
			}
		}

		return sb.toString();
	}

	/**
	 * 将字符串转为list
	 *
	 * @param parame
	 * @return
	 */
	public static List<String> stringToIntList(String parame) {
		try {
			if (TextUtils.isEmpty(parame)) {
				return new ArrayList<String>();
			}
			String[] s = parame.split(";");
			List<String> list = new ArrayList<String>();
			for (int i = 0; i < s.length; i++) {
				list.add(s[i]);
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<String>();
	}

	/**
	 * 将字符串转为list
	 *
	 * @param param
	 * @return
	 */
	public static List<String> stringToIntListByFormat(String param, String format) {
		try {
			if (TextUtils.isEmpty(param)) {
				return new ArrayList<String>();
			}
			String[] s = param.split(format);
			List<String> list = new ArrayList<String>();
			for (int i = 0; i < s.length; i++) {
				list.add(s[i]);
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<String>();
	}

	/**
	 * 验证第一个字符是否为字母
	 *
	 * @param s
	 * @return
	 */
	public static boolean checkLetter(String s) {
		char c = s.charAt(0);
		int i = (int) c;
		if ((i >= 65 && i <= 90) || (i >= 97 && i <= 122)) {
			return true;
		} else {
			return false;
		}
	}
}
