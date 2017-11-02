package com.kikatech.go.util.HttpClient;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import com.kikatech.go.util.HttpClient.HttpClientUtil.Action;
import com.kikatech.go.util.LogUtil;

/**
 * @author wangskeeter Created on 16/7/9.
 */
public class HttpClientTask extends AsyncTask< String, Void, Bundle >
{
    private static final String TAG = "HttpClientTask";


    private Action mAction;
    private String mApiUrl;
    private String mJsonBody;
    private HttpClientCallback mCallback;

    private int mReadTimeout = HttpClientUtil.DEFAULT_READ_TIMEOUT;
    private int mConnectTimeout = HttpClientUtil.DEFAULT_CONNECT_TIMEOUT;

    /**
     * Constructor for Actions { GET / GET_BINARY / DELETE }
     */
    public HttpClientTask( Action action, String apiUrl, HttpClientCallback callback )
    {
        this( action, apiUrl, null, callback );
    }

    /**
     * Constructor for Action { PUT / POST / POST_BINARY }
     */
    public HttpClientTask( Action action, String apiUrl, String jsonBody , HttpClientCallback callback )
    {
        this.mAction = action;
        this.mApiUrl = apiUrl;
        this.mJsonBody = jsonBody;
        this.mCallback = callback;
    }

    /**
     * Setup timeout BEFORE calling executeHttp_METHOD
     * @param readTimeout    http read timeout in ms (-1 to use default read timeout)
     * @param connectTimeout http connect timeout in ms (-1 to use default connecting timeout)
     **/
    public void setTimeout( int readTimeout, int connectTimeout )
    {
        this.mReadTimeout = ( readTimeout == -1 ) ? HttpClientUtil.DEFAULT_READ_TIMEOUT : readTimeout;
        this.mConnectTimeout = ( connectTimeout == -1 ) ? HttpClientUtil.DEFAULT_CONNECT_TIMEOUT : connectTimeout;
    }


    public Bundle executeHttp_GET( String targetUrl, boolean isGetBinary )
    {
        if( LogUtil.DEBUG ) LogUtil.logd( TAG , "executeHttp_GET targetUrl: " + targetUrl );

        Bundle resultBundle = new Bundle();
        HttpURLConnection httpURLConnection = null;
        byte[] data = null;
        String response = "";

        try
        {
            httpURLConnection = getHttpConnection( targetUrl, HttpClientUtil.HTTP_GET );
            if( httpURLConnection != null )
            {
                int responseCode = httpURLConnection.getResponseCode();
                if( LogUtil.DEBUG ) LogUtil.logv( TAG , "Response code: " + responseCode );
                switch( responseCode )
                {
                    case HttpURLConnection.HTTP_OK:
                        if( isGetBinary )
                        {
                            DataInputStream dataInputStream = new DataInputStream( httpURLConnection.getInputStream() );
                            byte[] buffer = new byte[ 4096 ];
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            int length;
                            while( ( length = dataInputStream.read( buffer ) ) != -1 )
                            {
                                byteArrayOutputStream.write( buffer, 0, length );
                            }
                            data = byteArrayOutputStream.toByteArray();
                            response = HttpClientUtil.RESULT_BINARY_OK;
                            break;
                        }
                    case HttpURLConnection.HTTP_FORBIDDEN:
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( httpURLConnection.getInputStream() ) );
                        String line;
                        while( ( line = bufferedReader.readLine() ) != null )
                            response += line;
                        break;
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        resultBundle.putString( HttpClientUtil.KEY_RESULT, HttpClientUtil.ERROR_FAILED_BAD_REQUEST );
                    default:
                        break;
                }
            }
        }
        catch( SocketTimeoutException e )
        {
            e.printStackTrace();
            resultBundle.putString( HttpClientUtil.KEY_RESULT, HttpClientUtil.ERROR_TIMEOUT );
            return resultBundle;
        }
        catch( Exception e )
        {
            e.printStackTrace();
            resultBundle.putString( HttpClientUtil.KEY_RESULT, HttpClientUtil.ERROR_FAILED );
            return resultBundle;
        }
        finally
        {
            if( httpURLConnection != null )
                httpURLConnection.disconnect();
        }

        if( !TextUtils.isEmpty( response ) )
            resultBundle.putString( HttpClientUtil.KEY_RESULT, response );
        if( data != null && data.length > 0 )
            resultBundle.putByteArray( HttpClientUtil.KEY_RESULT_BINARY, data );

        return resultBundle;
    }

    public Bundle executeHttp_PUT( String targetUrl )
    {
        if( LogUtil.DEBUG ) LogUtil.logd( TAG , "executeHttp_PUT targetUrl: " + targetUrl );

        Bundle resultBundle = new Bundle();
        HttpURLConnection httpURLConnection = null;
        String response = "";

        try
        {
            httpURLConnection = getHttpConnection( targetUrl, HttpClientUtil.HTTP_PUT );
            if( httpURLConnection != null )
            {
                httpURLConnection.setRequestProperty( HttpClientUtil.HTTP_HEADER_CONTENT_TYPE, HttpClientUtil.HTTP_HEADER_MIME_JSON );
                if( !TextUtils.isEmpty( mJsonBody ) )
                {
                    if( LogUtil.DEBUG ) LogUtil.logw( TAG , "mJsonBody: " + mJsonBody );
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( outputStream, HttpClientUtil.HTTP_ENCODING_UTF8 ) );
                    writer.write( mJsonBody );
                    writer.flush();
                    writer.close();
                    outputStream.close();
                }
                if( LogUtil.DEBUG ) LogUtil.logw( TAG , "response code: " + httpURLConnection.getResponseCode() );
                switch( httpURLConnection.getResponseCode() )
                {
                    case HttpURLConnection.HTTP_OK:
                    case HttpURLConnection.HTTP_FORBIDDEN:
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( httpURLConnection.getInputStream() ) );
                        String line;
                        while( ( line = bufferedReader.readLine() ) != null )
                            response += line;
                        break;
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        resultBundle.putString( HttpClientUtil.KEY_RESULT, HttpClientUtil.ERROR_FAILED_BAD_REQUEST );
                    default:
                        break;
                }
            }
        }
        catch( SocketTimeoutException e )
        {
            e.printStackTrace();
            resultBundle.putString( HttpClientUtil.KEY_RESULT, HttpClientUtil.ERROR_TIMEOUT );
            return resultBundle;
        }
        catch( Exception e )
        {
            e.printStackTrace();
            resultBundle.putString( HttpClientUtil.KEY_RESULT, HttpClientUtil.ERROR_FAILED );
            return resultBundle;
        }
        finally
        {
            if( httpURLConnection != null )
                httpURLConnection.disconnect();
        }

        if( !TextUtils.isEmpty( response ) )
            resultBundle.putString( HttpClientUtil.KEY_RESULT, response );

        return resultBundle;
    }

    public Bundle executeHttp_DELETE( String targetUrl )
    {
        if( LogUtil.DEBUG ) LogUtil.logd( TAG , "executeHttp_DELETE targetUrl: " + targetUrl );

        Bundle resultBundle = new Bundle();
        HttpURLConnection httpURLConnection = null;
        String response = "";

        try
        {
            httpURLConnection = getHttpConnection( targetUrl, HttpClientUtil.HTTP_DELETE );
            if( httpURLConnection != null )
            {
                httpURLConnection.setRequestProperty( HttpClientUtil.HTTP_HEADER_CONTENT_TYPE, HttpClientUtil.HTTP_HEADER_MIME_JSON );
                if( LogUtil.DEBUG ) LogUtil.logw( TAG , "response code: " + httpURLConnection.getResponseCode() );
                switch( httpURLConnection.getResponseCode() )
                {
                    case HttpURLConnection.HTTP_OK:
                    case HttpURLConnection.HTTP_FORBIDDEN:
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( httpURLConnection.getInputStream() ) );
                        String line;
                        while( ( line = bufferedReader.readLine() ) != null )
                            response += line;
                        break;
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        resultBundle.putString( HttpClientUtil.KEY_RESULT, HttpClientUtil.ERROR_FAILED_BAD_REQUEST );
                    default:
                        break;
                }
            }
        }
        catch( SocketTimeoutException e )
        {
            e.printStackTrace();
            resultBundle.putString( HttpClientUtil.KEY_RESULT, HttpClientUtil.ERROR_TIMEOUT );
            return resultBundle;
        }
        catch( Exception e )
        {
            e.printStackTrace();
            resultBundle.putString( HttpClientUtil.KEY_RESULT, HttpClientUtil.ERROR_FAILED );
            return resultBundle;
        }
        finally
        {
            if( httpURLConnection != null )
                httpURLConnection.disconnect();
        }

        if( !TextUtils.isEmpty( response ) )
            resultBundle.putString( HttpClientUtil.KEY_RESULT, response );

        return resultBundle;
    }

    public Bundle executeHttp_POST( String targetUrl )
    {
        if( LogUtil.DEBUG ) LogUtil.logd( TAG , "executeHttp_POST targetUrl: " + targetUrl );

        Bundle resultBundle = new Bundle();
        HttpURLConnection httpURLConnection = null;
        String response = "";

        try
        {
            httpURLConnection = getHttpConnection( targetUrl, HttpClientUtil.HTTP_POST );
            if( httpURLConnection != null )
            {
                httpURLConnection.setRequestProperty( HttpClientUtil.HTTP_HEADER_CONTENT_TYPE, HttpClientUtil.HTTP_HEADER_MIME_JSON );
                if( !TextUtils.isEmpty( mJsonBody ) )
                {
                    if( LogUtil.DEBUG ) LogUtil.logw( TAG , "mJsonBody: " + mJsonBody );
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( outputStream, HttpClientUtil.HTTP_ENCODING_UTF8 ) );
                    writer.write( mJsonBody );
                    writer.flush();
                    writer.close();
                    outputStream.close();
                }
                if( LogUtil.DEBUG ) LogUtil.logw( TAG , "response code: " + httpURLConnection.getResponseCode() );
                switch( httpURLConnection.getResponseCode() )
                {
                    case HttpURLConnection.HTTP_OK:
                    case HttpURLConnection.HTTP_FORBIDDEN:
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( httpURLConnection.getInputStream() ) );
                        String line;
                        while( ( line = bufferedReader.readLine() ) != null )
                            response += line;
                        break;
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        resultBundle.putString( HttpClientUtil.KEY_RESULT, HttpClientUtil.ERROR_FAILED_BAD_REQUEST );
                    default:
                        break;
                }
            }
        }
        catch( SocketTimeoutException e )
        {
            e.printStackTrace();
            resultBundle.putString( HttpClientUtil.KEY_RESULT, HttpClientUtil.ERROR_TIMEOUT );
            return resultBundle;
        }
        catch( Exception e )
        {
            e.printStackTrace();
            resultBundle.putString( HttpClientUtil.KEY_RESULT, HttpClientUtil.ERROR_FAILED );
            return resultBundle;
        }
        finally
        {
            if( httpURLConnection != null )
                httpURLConnection.disconnect();
        }

        if( !TextUtils.isEmpty( response ) )
            resultBundle.putString( HttpClientUtil.KEY_RESULT, response );

        return resultBundle;
    }

    public Bundle executeHttp_PATCH( String targetUrl )
    {
        if( LogUtil.DEBUG ) LogUtil.logd( TAG , "executeHttp_PATCH targetUrl: " + targetUrl );

        Bundle resultBundle = new Bundle();
        HttpURLConnection httpURLConnection = null;
        String response = "";

        try
        {
            httpURLConnection = getHttpConnection( targetUrl, HttpClientUtil.HTTP_PATCH );
            if( httpURLConnection != null )
            {
                httpURLConnection.setRequestProperty( HttpClientUtil.HTTP_HEADER_CONTENT_TYPE, HttpClientUtil.HTTP_HEADER_MIME_JSON );
                httpURLConnection.setRequestProperty( HttpClientUtil.HTTP_HEADER_METHOD_OVERRIDE, HttpClientUtil.HTTP_PATCH );
                if( !TextUtils.isEmpty( mJsonBody ) )
                {
                    if( LogUtil.DEBUG ) LogUtil.logw( TAG, "mJsonBody: " + mJsonBody );
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( outputStream, HttpClientUtil.HTTP_ENCODING_UTF8 ) );
                    writer.write( mJsonBody );
                    writer.flush();
                    writer.close();
                    outputStream.close();
                }
                if( LogUtil.DEBUG ) LogUtil.logw( TAG , "response code: " + httpURLConnection.getResponseCode() );
                switch( httpURLConnection.getResponseCode() )
                {
                    case HttpURLConnection.HTTP_OK:
                    case HttpURLConnection.HTTP_FORBIDDEN:
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( httpURLConnection.getInputStream() ) );
                        String line;
                        while( ( line = bufferedReader.readLine() ) != null )
                            response += line;
                        break;
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        resultBundle.putString( HttpClientUtil.KEY_RESULT, HttpClientUtil.ERROR_FAILED_BAD_REQUEST );
                    default:
                        break;
                }
            }
        }
        catch( SocketTimeoutException e )
        {
            e.printStackTrace();
            resultBundle.putString( HttpClientUtil.KEY_RESULT, HttpClientUtil.ERROR_TIMEOUT );
            return resultBundle;
        }
        catch( Exception e )
        {
            e.printStackTrace();
            resultBundle.putString( HttpClientUtil.KEY_RESULT, HttpClientUtil.ERROR_FAILED );
            return resultBundle;
        }
        finally
        {
            if( httpURLConnection != null )
                httpURLConnection.disconnect();
        }

        if( !TextUtils.isEmpty( response ) )
            resultBundle.putString( HttpClientUtil.KEY_RESULT, response );

        return resultBundle;
    }

    /**
     * Return HttpURLConnection instance with specific url and method
     * @param method HttpClientUtil.HTTP_GET or HttpClientUtil.HTTP_POST
     **/
    private HttpURLConnection getHttpConnection( String targetUrl, String method )
    {
        HttpURLConnection httpURLConnection;
        try
        {
            URL url = new URL( targetUrl );
            httpURLConnection = ( HttpURLConnection ) url.openConnection();

            httpURLConnection.setConnectTimeout( mConnectTimeout );
            httpURLConnection.setReadTimeout( mReadTimeout );

            switch( method )
            {
                case HttpClientUtil.HTTP_PATCH:
                    // ought to use X-HTTP-Method-Override cause of there's no PATCH method in HttpURLConnection
                    method = HttpClientUtil.HTTP_POST;
                case HttpClientUtil.HTTP_PUT:
                case HttpClientUtil.HTTP_POST:
                    httpURLConnection.setDoOutput( true );
                case HttpClientUtil.HTTP_DELETE:
                    httpURLConnection.setDoInput( true );
                    break;
            }

            httpURLConnection.setRequestMethod( method );
        }
        catch( Exception e )
        {
            httpURLConnection = null;
            e.printStackTrace();
        }
        return httpURLConnection;
    }


    @Override
    protected Bundle doInBackground( String... strings )
    {
        Bundle result = null;
        switch( mAction )
        {
            case GET:
                result = executeHttp_GET( mApiUrl, false );
                break;
            case GET_BINARY:
                result = executeHttp_GET( mApiUrl, true );
                break;
            case PUT:
                result = executeHttp_PUT( mApiUrl );
                break;
            case DELETE:
                result = executeHttp_DELETE( mApiUrl );
                break;
            case POST:
                result = executeHttp_POST( mApiUrl );
                break;
            case PATCH:
                result = executeHttp_PATCH( mApiUrl );
                break;
        }
        return result;
    }

    @Override
    protected void onPostExecute( Bundle result )
    {
        if( result != null )
        {
            String resultString = result.getString( HttpClientUtil.KEY_RESULT );
            if( !TextUtils.isEmpty( resultString ) )
            {
                switch( resultString )
                {
                    case HttpClientUtil.ERROR_TIMEOUT:
                        if( LogUtil.DEBUG ) LogUtil.logd( TAG , "ERROR_TIMEOUT" );
                        if( mCallback != null )
                            mCallback.onError( HttpErrorMessages.ERR_TIMEOUT );
                        break;
                    case HttpClientUtil.ERROR_FAILED:
                        if( LogUtil.DEBUG ) LogUtil.logd( TAG , "ERROR_FAILED" );
                        if( mCallback != null )
                            mCallback.onError( HttpErrorMessages.ERR_FAILED );
                        break;
                    case HttpClientUtil.ERROR_FAILED_BAD_REQUEST:
                        if( LogUtil.DEBUG ) LogUtil.logd( TAG , "ERROR_FAILED" );
                        if( mCallback != null )
                            mCallback.onError( HttpErrorMessages.ERR_FAILED_BAD_REQUEST );
                        break;
                    default:
                        if( LogUtil.DEBUG ) pintResultString( result );
                        if( mCallback != null )
                            mCallback.onResponse( result );
                }
            }
            else if( mCallback != null )
                mCallback.onError( HttpErrorMessages.ERR_FAILED );
        }
    }

    public interface HttpClientCallback
    {
        void onResponse( Bundle result );

        void onError( String err );
    }

    private static void pintResultString( Bundle result )
    {
        if( result != null )
        {
            String resultString = "";
            for( String key : result.keySet() )
                resultString += key + ": " + result.get( key ) + ", ";
            LogUtil.logd( TAG, resultString.substring( 0, resultString.lastIndexOf( "," ) ) );
        }
    }
}