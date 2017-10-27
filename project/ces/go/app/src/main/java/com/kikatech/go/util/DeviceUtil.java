package com.kikatech.go.util;

import android.os.Build;

/**
 * @author SkeeterWang Created on 2017/10/26.
 */
public class DeviceUtil
{
	/** Android 4.0 (14) */
	public static boolean overIceCreamSandwich()
	{
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
	}

	/** Android 4.1 (16) */
	public static boolean overJellyBean()
	{
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
	}

	/** Android 4.4 (19) */
	public static boolean overKitKat()
	{
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
	}

	/** Android 5.0 (21) **/
	public static boolean overLollipop()
	{
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
	}

	/** Android 6.0 (23) **/
	public static boolean overM()
	{
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
	}

	/** Android 7.0 (24) **/
	public static boolean overN()
	{
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
	}
}
