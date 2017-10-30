package com.kikatech.go.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import java.util.Locale;
/**
 * Created by brad_chang on 2016/1/20.
 */
public class DeviceUtil
{
	private static final String TAG = "DeviceUtil";

    private static String sAndroidId;
    private static int sLastUUIDCharVal = -1;



	public enum OTLocale
	{
		TAIWAN,
		HONG_KONG,
		CHINA,
		JAPAN,
		KOREA,
		US,
		UK,
		AUSTRALIA,
		SPAIN,                  // 西班牙
		SPANISH_LATIN_AMERICA,  // 西班牙 (拉丁美洲)
		SPANISH_MEXICO,         // 西班牙 (墨西哥)
		PORTUGAL,               // 葡萄牙
		PORTUGUESE_BRAZIL,      // 葡萄牙 (巴西)
		RUSSIA,                 // 俄國
		GERMANY,                // 德國
		FRANCE,                 // 法國
		FRENCH_CANADA,          // 法國
		ITALY,                  // 義大利
		ARABIC,                 // 阿拉伯語系國家
		VIETNAM,                // 越南
		OTHERS;

		public static OTLocale getLocale()
		{
			try
			{
				Locale currentLocale = Locale.getDefault();
				String language = currentLocale.getLanguage().toLowerCase( Locale.ENGLISH );
				String currentCountry = currentLocale.getCountry();
				String isoCountryCode = currentCountry.toLowerCase( Locale.ENGLISH );
				if( isoCountryCode.contains( "hk" ) )
					return HONG_KONG;
				else if( isoCountryCode.contains( "cn" ) || currentLocale.equals( Locale.CHINA ) || currentLocale.equals( Locale.SIMPLIFIED_CHINESE ) )
					return CHINA;
				else if( isoCountryCode.contains( "tw" ) || currentLocale.equals( Locale.TAIWAN ) || currentLocale.equals( Locale.TRADITIONAL_CHINESE ) || currentLocale.equals( Locale.CHINESE ) )
					return TAIWAN;
				else if( language.contains( "ja" ) || currentLocale.equals( Locale.JAPAN ) || currentLocale.equals( Locale.JAPANESE ) )
					return JAPAN;
				else if( language.contains( "ko" ) || currentLocale.equals( Locale.KOREA ) || currentLocale.equals( Locale.KOREAN ) )
					return KOREA;
				else if( isoCountryCode.contains( "au" ) )
					return AUSTRALIA;
				else if( isoCountryCode.contains( "gb" ) || currentLocale.equals( Locale.UK ) )
					return UK;
				else if( language.contains( "en" ) || currentLocale.equals( Locale.US ) || currentLocale.equals( Locale.ENGLISH ) )
					return US;
				else if( language.contains( "es" ) )
				{
					if( isoCountryCode.contains( "mx" ) )
						return SPANISH_MEXICO;
					else if( isoCountryCode.contains( "es" ) )
						return SPAIN;
					else
						return SPANISH_LATIN_AMERICA;
				}
				else if( language.contains( "pt" ) )
				{
					if( isoCountryCode.contains( "br" ) )
						return PORTUGUESE_BRAZIL;
					else
						return PORTUGAL;
				}
				else if( language.contains( "ru" ) )
					return RUSSIA;
				else if( language.contains( "de" ) || currentLocale.equals( Locale.GERMAN ) || currentLocale.equals( Locale.GERMANY ) )
					return GERMANY;
				else if( language.contains( "fr" ) || currentLocale.equals( Locale.FRANCE ) || currentLocale.equals( Locale.FRENCH ) )
				{
					if( isoCountryCode.contains( "ca" ) )
						return FRENCH_CANADA;
					else
						return FRANCE;
				}
				else if( language.contains( "it" ) || currentLocale.equals( Locale.ITALIAN ) || currentLocale.equals( Locale.ITALY ) )
					return ITALY;
				else if( language.contains( "ar" ) )
					return ARABIC;
				else if( language.contains( "vi" ) )
					return VIETNAM;
				else
					return OTHERS;
			}
			catch( Exception e ) { if( LogUtil.DEBUG) LogUtil.printStackTrace( TAG, e.getMessage(), e ); }
			return OTHERS;
		}
	}

	public static void printLocale()
	{
		Locale currentLocale = Locale.getDefault();
		String language = currentLocale.getLanguage().toLowerCase( Locale.ENGLISH );
		String currentCountry = currentLocale.getCountry();
		String isoCountryCode = currentCountry.toLowerCase( Locale.ENGLISH );
		if( LogUtil.DEBUG ) LogUtil.logw( TAG, "Locale language = " + language
											   + ", isoCountryCode = " + isoCountryCode);
	}



	public static String getMCC(Context context)
	{
		final TelephonyManager tm = ( TelephonyManager ) context.getApplicationContext().getSystemService( Context.TELEPHONY_SERVICE );
		String mcc_mnc = tm.getSimOperator();
		if( null != mcc_mnc && mcc_mnc.length() >= 3 )
		{
			StringBuilder mcc = new StringBuilder();
			mcc.append( mcc_mnc, 0, 3 );
			return mcc.toString();
		}
		return "null";
	}

	private static boolean isUnitedStates(Context context)
	{
		String mcc = getMCC(context);
		return !TextUtils.isEmpty( mcc ) && ( mcc.equals( "310" ) || mcc.equals( "311" ) );
	}

	public static boolean isTaiwan(Context context)
	{
		String mcc = getMCC(context);
		if( !TextUtils.isEmpty( mcc ) && !mcc.equals( "null" ) )
		{
			return mcc.equals( "466" );
		}
		else
		{
			Locale currentLocale = Locale.getDefault();
			return ( currentLocale.equals( Locale.CHINESE ) || currentLocale.equals( Locale.TAIWAN ) || currentLocale.equals( Locale.TRADITIONAL_CHINESE ) );
		}
	}





	/**
	 * @return If mcc is in US, return true / false that stands for A / B test.
	 * Otherwise, return false
	 */
	public static boolean getUSOnlyABTest(Context context)
	{
		return isUnitedStates(context) && isEnabledByProbability( context, 50 );
	}

	/**
	 * Check probability by uuid
	 *
	 * @param percentage 6 : "1/16"  12: "2/16" 18: "3/16" 25: "4/16" 31: "5/16"  37: "6/16"
	 */
	private static boolean isEnabledByProbability( Context context, int percentage )
	{
		return isEnabledByProbability( context, percentage, false );
	}

	/**
	 * Check probability by uuid
	 *
	 * @param percentage 6 : "1/16"  12: "2/16" 18: "3/16" 25: "4/16" 31: "5/16"  37: "6/16"
	 * @param revert     count uuid from 0 or f
	 */
	private static boolean isEnabledByProbability( Context context, int percentage, boolean revert )
	{
		if( percentage <= 0 )
			return false;

		if( percentage == 100 )
			return true;

		if( sLastUUIDCharVal == -1 )
		{
			try
			{
				String UUID = getAndroidID(context);
				if( null != UUID && UUID.length() > 0 )
				{
					String lastUUIDChar = "" + UUID.charAt( UUID.length() - 1 );
					sLastUUIDCharVal = Integer.parseInt( lastUUIDChar, 16 );
				}
			}
			catch( Exception ignore ) {}
		}

		int maxValue = ( int ) ( ( percentage / 100.0f ) * 15 );
		int comparator = revert ? 16 - sLastUUIDCharVal : sLastUUIDCharVal;

		return ( sLastUUIDCharVal >= 0 && comparator <= maxValue );
	}






	public static String getAndroidID(Context context) {
		if (!TextUtils.isEmpty(sAndroidId)) return sAndroidId;

		try {
			ContentResolver cr = context.getApplicationContext().getContentResolver();
			sAndroidId = Settings.System.getString(cr, Settings.Secure.ANDROID_ID);
		} catch (Exception e) {
			sAndroidId = getSerialId();
		}
		return sAndroidId;
	}

	private static String getSerialId()
	{
		return Build.SERIAL;
	}

	public static String getManufacturer()
	{
		return Build.MANUFACTURER;
	}

	public static String getBrand()
	{
		return Build.BRAND;
	}

	public static String getModel()
	{
		return Build.MODEL;
	}

	public static void printDeviceInfo()
	{
		if( LogUtil.DEBUG )
		{
			try
			{
				LogUtil.log( TAG, LogUtil.getCheckedLogString( "Build.BOARD", Build.BOARD ) );
				LogUtil.log( TAG, LogUtil.getCheckedLogString( "Build.BOOTLOADER", Build.BOOTLOADER ) );
				LogUtil.log( TAG, LogUtil.getCheckedLogString( "Build.BRAND", Build.BRAND ) );
				LogUtil.log( TAG, LogUtil.getCheckedLogString( "Build.CPU_ABI", Build.CPU_ABI ) );
				LogUtil.log( TAG, LogUtil.getCheckedLogString( "Build.CPU_ABI2", Build.CPU_ABI2 ) );
				LogUtil.log( TAG, LogUtil.getCheckedLogString( "Build.DEVICE", Build.DEVICE ) );
				LogUtil.log( TAG, LogUtil.getCheckedLogString( "Build.DISPLAY", Build.DISPLAY ) );
				LogUtil.log( TAG, LogUtil.getCheckedLogString( "Build.FINGERPRINT", Build.FINGERPRINT ) );
				LogUtil.log( TAG, LogUtil.getCheckedLogString( "Build.HARDWARE", Build.HARDWARE ) );
				LogUtil.log( TAG, LogUtil.getCheckedLogString( "Build.HOST", Build.HOST ) );
				LogUtil.log( TAG, LogUtil.getCheckedLogString( "Build.ID", Build.ID ) );
				LogUtil.log( TAG, LogUtil.getCheckedLogString( "Build.MANUFACTURER", Build.MANUFACTURER ) );
				LogUtil.log( TAG, LogUtil.getCheckedLogString( "Build.MODEL", Build.MODEL ) );
				LogUtil.log( TAG, LogUtil.getCheckedLogString( "Build.PRODUCT", Build.PRODUCT ) );
				LogUtil.log( TAG, LogUtil.getCheckedLogString( "Build.RADIO", Build.RADIO ) );
				LogUtil.log( TAG, LogUtil.getCheckedLogString( "Build.SERIAL", Build.SERIAL ) );
				if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP )
				{
					for( String abi : Build.SUPPORTED_ABIS )
						LogUtil.log( TAG, LogUtil.getCheckedLogString( "abi", abi ) );
					for( String abi32 : Build.SUPPORTED_32_BIT_ABIS )
						LogUtil.log( TAG, LogUtil.getCheckedLogString( "abi32", abi32 ) );
					for( String abi64 : Build.SUPPORTED_64_BIT_ABIS )
						LogUtil.log( TAG, LogUtil.getCheckedLogString( "abi64", abi64 ) );
				}
				LogUtil.log( TAG, LogUtil.getCheckedLogString( "Build.TAGS", Build.TAGS ) );
				LogUtil.log( TAG, LogUtil.getCheckedLogString( "Build.TIME", "" + Build.TIME ) );
				LogUtil.log( TAG, LogUtil.getCheckedLogString( "Build.TYPE", Build.TYPE ) );
				LogUtil.log( TAG, LogUtil.getCheckedLogString( "Build.USER", Build.USER ) );
			}
			catch( Exception ignore ) {}
		}
	}




	public static boolean hasNavigationBar( Resources resources )
	{
		int id = resources.getIdentifier( "config_showNavigationBar", "bool", "android" );
		return id > 0 && resources.getBoolean( id );
	}

    public static int getScreenWidth( Context context )
    {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics( metrics );
        return metrics.widthPixels;
    }

    public static int getScreenHeight( Context context )
    {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics( metrics );
        return metrics.heightPixels;
    }





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





	public static boolean isRTL( Context context )
	{
		try
		{
			if( Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 )
			{
				int directionality = Character.getDirectionality( Locale.getDefault().getDisplayName().charAt(0) );
				return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
					   directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
			}
			else
				return context.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
		}
		catch( Exception ignore ) {}
		return false;
	}





	public static String getSMSPackageName( Context context )
	{
		if( !overKitKat() ) return null;
		try
		{
			return Telephony.Sms.getDefaultSmsPackage( context );
		}
		catch( Exception ignore ) {}
		return null;
	}
}
