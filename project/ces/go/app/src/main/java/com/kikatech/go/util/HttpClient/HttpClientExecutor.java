package com.kikatech.go.util.HttpClient;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.kikatech.go.util.HttpClient.HttpClientTask.HttpClientCallback;
import com.kikatech.go.util.HttpClient.HttpClientUtil.Action;

/**
 * @author wangskeeter Created on 16/7/9.
 */
public class HttpClientExecutor
{
    public static final String TAG = "HttpClientExecutor";

    public static final int USE_DEFAULT_TIMEOUT = -1;

    private static HttpClientExecutor sIns;
    private ScheduledThreadPoolExecutor mExecutor;

    /**
     * Allocating Executor if needed.
     * <P> ScheduledThreadPoolExecutor( 5, new ScheduledThreadPoolExecutor.DiscardOldestPolicy() )</P>
     **/
    public static synchronized HttpClientExecutor getIns()
    {
        if( sIns == null )
            sIns = new HttpClientExecutor();
        return sIns;
    }

    private HttpClientExecutor()
    {
        mExecutor = new ScheduledThreadPoolExecutor( 5, new ScheduledThreadPoolExecutor.DiscardOldestPolicy() );
    }

    /**
     * @param isGetBinary true: Action.GET_BINARY, false: Action.GET
     **/
    public void asyncGET( String targetUrl, boolean isGetBinary, HttpClientCallback callback )
    {
        asyncGET( targetUrl, isGetBinary, USE_DEFAULT_TIMEOUT, USE_DEFAULT_TIMEOUT, callback );
    }

    public void asyncGET( String targetUrl, boolean isGetBinary, int readTimeout, int connectTimeout, HttpClientCallback callback )
    {
        HttpClientTask task = new HttpClientTask( isGetBinary ? Action.GET_BINARY : Action.GET, targetUrl, callback );
        task.setTimeout( readTimeout, connectTimeout );
        task.executeOnExecutor( mExecutor, targetUrl );
    }

    public void asyncPUT( String targetUrl, String jsonBody, HttpClientCallback callback )
    {
        asyncPUT( targetUrl, jsonBody, USE_DEFAULT_TIMEOUT, USE_DEFAULT_TIMEOUT, callback );
    }

    public void asyncPUT( String targetUrl, String jsonBody, int readTimeout, int connectTimeout, HttpClientCallback callback )
    {
        HttpClientTask task = new HttpClientTask( Action.PUT, targetUrl, jsonBody, callback );
        task.setTimeout( readTimeout, connectTimeout );
        task.executeOnExecutor( mExecutor, targetUrl );
    }

    public void asyncDELETE( String targetUrl, HttpClientCallback callback )
    {
        asyncDELETE( targetUrl, USE_DEFAULT_TIMEOUT, USE_DEFAULT_TIMEOUT, callback );
    }

    public void asyncDELETE( String targetUrl, int readTimeout, int connectTimeout, HttpClientCallback callback )
    {
        HttpClientTask task = new HttpClientTask( Action.DELETE, targetUrl, callback );
        task.setTimeout( readTimeout, connectTimeout );
        task.executeOnExecutor( mExecutor, targetUrl );
    }

    public void asyncPOST( String targetUrl, String jsonBody, HttpClientCallback callback )
    {
        asyncPOST( targetUrl, jsonBody, USE_DEFAULT_TIMEOUT, USE_DEFAULT_TIMEOUT, callback );
    }

    public void asyncPOST( String targetUrl, String jsonBody, int readTimeout, int connectTimeout, HttpClientCallback callback )
    {
        HttpClientTask task = new HttpClientTask( Action.POST, targetUrl, jsonBody, callback );
        task.setTimeout( readTimeout, connectTimeout );
        task.executeOnExecutor( mExecutor, targetUrl );
    }

    public void asyncPATCH( String targetUrl, String jsonBody, HttpClientCallback callback )
    {
        asyncPATCH( targetUrl, jsonBody, USE_DEFAULT_TIMEOUT, USE_DEFAULT_TIMEOUT, callback );
    }

    public void asyncPATCH( String targetUrl, String jsonBody, int readTimeout, int connectTimeout, HttpClientCallback callback )
    {
        HttpClientTask task = new HttpClientTask( Action.PATCH, targetUrl, jsonBody, callback );
        task.setTimeout( readTimeout, connectTimeout );
        task.executeOnExecutor( mExecutor, targetUrl );
    }
}
