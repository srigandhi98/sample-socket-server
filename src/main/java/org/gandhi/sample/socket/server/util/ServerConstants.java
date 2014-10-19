package org.gandhi.sample.socket.server.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ServerConstants {
	public static final Set<String> VALID_PROTOCOL_WITH_VERSIONS = new HashSet<String>(Arrays.asList("HTTP/1.0", "HTTP/1.1", "HTTP/1.2"));

	public static final String DEFAULT_PROTOCOL_WITH_VERSION = "HTTP/1.2";
	
	public static final String HTTP_GET = "GET";
	public static final String HTTP_PUT = "PUT";
	
	public static final String CONTENT_LENGTH_HEADER="Content-Length:";
	public static final String CONTENT_TYPE_HEADER="Content-Type:";
	public static final String CONTENT_TYPE_JSON = "application/json";
	
	public static final String ADD_CONNECTION_URL="/api/request";
	public static final String GET_CONNECTIONS_URL="/api/serverStatus";
	public static final String KILL_CONNECTION_URL="/api/kill";
	
	public static final String QUERY_PARAM_CONN_ID="connId";
	public static final String QUERY_PARAM_TIME_OUT="timeOut";
	
	public static final String QUERY_PARAMS_STARTER="?";
	public static final String QUERY_PARAMS_SEPARATOR="&";
	public static final String QUERY_PARAMS_KEY_VALUE_SEPARATOR="=";

	public static final String HTTP_OK_RESPONSE_CODE = "200";
	public static final String HTTP_OK_RESPONSE = "OK";
	public static final String HTTP_BAD_REQUEST_RESPONSE_CODE="400";
	public static final String HTTP_BAD_REQUEST_RESPONSE="BAD_REQUEST";
	public static final String HTTP_SERVER_FAIL_RESPONSE_CODE="500";
	public static final String HTTP_SERVER_FAIL_RESPONSE="FAIL";
	public static final String HTTP_NOT_FOUND_RESPONSE_CODE="404";
	public static final String HTTP_NOT_FOUND_RESPONSE="NOT_FOUND";
	public static final String HTTP_FORBIDDEN_RESPONSE_CODE="403";
	public static final String HTTP_FORBIDDEN_RESPONSE="FORBIDDEN";
}
