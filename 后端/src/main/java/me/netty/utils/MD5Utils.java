package me.netty.utils;

import org.apache.commons.codec.binary.Base64;

import java.security.MessageDigest;

public class MD5Utils {

	/**
	 * @Description: 对字符串进行md5加密 
	 */
	public static String getMD5Str(String strValue) throws Exception {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		String newstr = Base64.encodeBase64String(md5.digest(strValue.getBytes()));
		return newstr;
	}

	public static void main(String[] args) {
		try {
			String md51 = getMD5Str("main");
			String md52 = getMD5Str("test1");
			String md53 = getMD5Str("test2");
			System.out.println(md51);
			System.out.println(md52);
			System.out.println(md53);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
