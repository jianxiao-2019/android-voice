package com.kikatech.voice.util.log;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.Locale;

/**
 * Created by brad_chang on 2016/1/10.
 */
public class LogUtil
{
    final static public boolean DEBUG = Logger.DEBUG;
	final static public boolean ENABLE_FILE_LOG = DEBUG;
    private static int sProcessId = DEBUG ? 0 : android.os.Process.myPid();
	private static final int PARENT_NODE = 3, SELF_NODE = 2;
	private static final String PARENT_LOG_FORMAT = "[%s:%s:ln%d] ";
	private static final String LOG_FORMAT = "[%s:%s:ln%d] %s (pid: %d)";

	public final static String LOG_FOLDER = "kika_go/log";
	public final static String LOG_FILE = "%s_voice_sdk.txt";
	private static int mFileLoggerId = -1;

	private enum LogLabel
	{
		VERBOSE,
		DEBUG,
		INFO,
		WARN,
		ERROR,
		ASSERT,
		EXCEPTION
	}

    private static String getStackMsg( String log, int parentStacksCount )
    {
        String message = log;
        try
        {
            StackTraceElement[] stacks = ( new Throwable() ).getStackTrace();
			String parentStack = "";
            String className = null;
            String methodName = null;
            int lineNumber = 0;
			StackTraceElement stack;
			String[] classes;
			if( parentStacksCount > 0 )
			{
				if( stacks.length >= PARENT_NODE )
				{
					for( int i = PARENT_NODE; i <= PARENT_NODE + parentStacksCount && i < stacks.length ; i++ )
					{
						stack = stacks[ i ];
						className = stack.getClassName();
						classes = className.split( "\\." );
						if( classes.length > 0 ) className = classes[ classes.length - 1 ];
						methodName = stack.getMethodName();
						lineNumber = stack.getLineNumber();

						parentStack += String.format( Locale.ENGLISH, PARENT_LOG_FORMAT, className, methodName, lineNumber );
					}
				}
			}
			if( stacks.length >= SELF_NODE )
            {
				stack = stacks[ SELF_NODE ];
                className = stack.getClassName();
				classes = className.split( "\\." );
                if( classes.length > 0 ) className = classes[ classes.length - 1 ];
                methodName = stack.getMethodName();
                lineNumber = stack.getLineNumber();
            }

			message = String.format( Locale.ENGLISH, LOG_FORMAT, className, methodName, lineNumber, log, sProcessId );
			if( !TextUtils.isEmpty( parentStack ) ) message += ( "\n parent stacks: " + parentStack );
			return message;
        }
        catch( Exception ignore ) {}
		return message;
    }

    private static void log( LogLabel logLabel, String logTag, String log )
	{
		log( logLabel, logTag, log, null );
	}

	private static void log( LogLabel logLabel, String logTag, String log, Throwable throwable )
	{
        switch( logLabel )
        {
            case VERBOSE:
                Log.v( logTag, log );
                break;
            case DEBUG:
                Log.d( logTag, log );
                break;
            case INFO:
                Log.i( logTag, log );
                break;
            case WARN:
                Log.w( logTag, log );
                break;
            case ERROR:
                Log.e( logTag, log );
                break;
            case ASSERT:
                Log.wtf( logTag, log );
                break;
			case EXCEPTION:
				Log.e( logTag, log, throwable );
				break;
        }

		if (ENABLE_FILE_LOG) {
			if (mFileLoggerId == -1) {
				mFileLoggerId = FileLoggerUtil.getIns().configFileLogger(LOG_FOLDER, LOG_FILE);
			}
			FileLoggerUtil.getIns().writeLogToFile(mFileLoggerId, logTag + " " + log);
		}
    }



    public static void log(@NonNull String logTag, @NonNull String log )
    {
		if( DEBUG ) log( LogLabel.INFO, logTag, getStackMsg( log, 0 ) );
	}

    public static void logd( @NonNull String logTag, @NonNull String log )
    {
		if( DEBUG ) log( LogLabel.DEBUG, logTag, getStackMsg( log, 0 ) );
	}

    public static void logv( @NonNull String logTag, @NonNull String log )
    {
		if( DEBUG ) log( LogLabel.VERBOSE, logTag, getStackMsg( log, 0 ) );
	}

    public static void logw( @NonNull String logTag, @NonNull String log )
    {
		if( DEBUG ) log( LogLabel.WARN, logTag, getStackMsg( log, 0 ) );
	}

    public static void logwtf( @NonNull String logTag, @NonNull String log )
    {
		if( DEBUG ) log( LogLabel.ASSERT, logTag, getStackMsg( log, 0 ) );
	}

	public static void logParent( @NonNull String logTag, @NonNull String log, int parentStacksCount )
	{
		if( DEBUG ) log( LogLabel.INFO, logTag, getStackMsg( log, parentStacksCount ) );
	}

	public static void logdParent( @NonNull String logTag, @NonNull String log, int parentStacksCount )
	{
		if( DEBUG ) log( LogLabel.DEBUG, logTag, getStackMsg( log, parentStacksCount ) );
	}

	public static void logvParent( @NonNull String logTag, @NonNull String log, int parentStacksCount )
	{
		if( DEBUG ) log( LogLabel.VERBOSE, logTag, getStackMsg( log, parentStacksCount ) );
	}

	public static void logwParent( @NonNull String logTag, @NonNull String log, int parentStacksCount )
	{
		if( DEBUG ) log( LogLabel.WARN, logTag, getStackMsg( log, parentStacksCount ) );
	}

	public static void logwtfParent( @NonNull String logTag, @NonNull String log, int parentStacksCount )
	{
		if( DEBUG ) log( LogLabel.ASSERT, logTag, getStackMsg( log, parentStacksCount ) );
	}



    public static void printStackTrace( @NonNull String logTag, @NonNull String log, Throwable throwable )
	{
		if( DEBUG ) log( LogLabel.EXCEPTION, logTag, getStackMsg( log, 0 ), throwable );
	}



	public static void reportToFabric( Exception exception )
	{
//		try
//		{
//			Crashlytics.logException( exception );
//		}
//		catch( Exception ignore ) {}
	}



    /**
     * If value is not null or empty, return String formatted as "key: value"
     **/
    public static String getCheckedLogString( @NonNull String key , @NonNull @Nullable String value )
    {
        return key + ": " + ( TextUtils.isEmpty( value ) ? "" :  value );
    }
}
