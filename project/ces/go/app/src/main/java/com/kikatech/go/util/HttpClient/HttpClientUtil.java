package com.kikatech.go.util.HttpClient;

/**
 * @author wangskeeter Created on 16/7/9.
 */
public class HttpClientUtil
{
    public enum Action
    {
        GET, GET_BINARY, PUT, DELETE, POST, PATCH
    }

    public static final int DEFAULT_READ_TIMEOUT = 10000;  //ms
    public static final int DEFAULT_CONNECT_TIMEOUT = 7000;  //ms

    public static final String HTTP_GET = "GET";
    public static final String HTTP_PUT = "PUT";
    public static final String HTTP_DELETE = "DELETE";
    public static final String HTTP_POST = "POST";
    public static final String HTTP_PATCH = "PATCH";
    public static final String HTTP_ENCODING_UTF8 = "UTF-8";

    public static final String HTTP_DELIMITER = "--";
    public static final String HTTP_BOUNDARY = "******";
    public static final String HTTP_CRLF = "\r\n";
    public static final String HTTP_HEADER_CONNECTION = "Connection";
    public static final String HTTP_HEADER_KEEP_ALIVE = "Keep-Alive";
    public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HTTP_HEADER_MIME_JSON = "application/json";
    public static final String HTTP_HEADER_MIME_MULTIPART = "multipart/form-data;boundary=" + HTTP_BOUNDARY;
    public static final String HTTP_HEADER_CONTENT_DISPOSITION_START = HTTP_DELIMITER + HTTP_BOUNDARY + HTTP_CRLF;
    public static final String HTTP_HEADER_CONTENT_DISPOSITION_DATA = "Content-Disposition: form-data; name=\"data\";filename=\"data\"" + HTTP_CRLF + HTTP_CRLF;
    public static final String HTTP_HEADER_CONTENT_DISPOSITION_END = HTTP_DELIMITER + HTTP_BOUNDARY + HTTP_DELIMITER + HTTP_CRLF;
    public static final String HTTP_HEADER_METHOD_OVERRIDE = "X-HTTP-Method-Override";

    public static String HTTP_HEADER_CONTENT_DISPOSITION_KEY( String paramKey )
    {
        return "Content-Disposition: form-data; name=\"" + paramKey + "\"" + HTTP_CRLF + HTTP_CRLF;
    }

    public static String HTTP_HEADER_CONTENT_DISPOSITION_VALUE( String paramValue )
    {
        return paramValue + HTTP_CRLF;
    }

    public static final String KEY_RESULT = "http_result";
    public static final String KEY_RESULT_BINARY = "http_result_binary";
    public static final String RESULT_BINARY_OK = "result_binary_ok";
    public static final String ERROR_TIMEOUT = "error_timeout";
    public static final String ERROR_FAILED = "error_failed";
    public static final String ERROR_FAILED_BAD_REQUEST = "error_failed_bad_request";
}
