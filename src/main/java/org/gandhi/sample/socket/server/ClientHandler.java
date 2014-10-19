package org.gandhi.sample.socket.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.gandhi.sample.socket.server.dto.ConnectionInfo;
import org.gandhi.sample.socket.server.dto.RequestDTO;
import org.gandhi.sample.socket.server.dto.ResponseDTO;
import org.gandhi.sample.socket.server.reply.ExceptionReply;
import org.gandhi.sample.socket.server.reply.SuccessReply;
import org.gandhi.sample.socket.server.util.ServerConstants;
import org.gandhi.sample.socket.server.util.Status;

public class ClientHandler extends Thread {

	private final Socket CLIENT_SOCKET;

	private final long inTime;

	private final Server server;

	private final ObjectMapper objectMapper;

	public ClientHandler(Socket clientSocket, Server server) {
		this.inTime = System.currentTimeMillis();
		this.CLIENT_SOCKET = clientSocket;
		this.server = server;
		objectMapper = new ObjectMapper();
	}

	public Socket getCLIENT_SOCKET() {
		return CLIENT_SOCKET;
	}

	public long getInTime() {
		return inTime;
	}

	public Server getServer() {
		return server;
	}

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public void run() {
		try{
			RequestDTO requestDTO = extractAndPrepareRequestDTO();
			String protocolWithVersion = requestDTO.getProtocolWithVersion();
			String method = requestDTO.getMethod();
			String url = requestDTO.getUrl();
			ConcurrentHashMap<ConnectionInfo, Thread> currentRequests = server.getCurrentRequests();
			
			if (method.equals(ServerConstants.HTTP_GET) && url.equals(ServerConstants.ADD_CONNECTION_URL)) {
				ConnectionInfo addConnectionInfo = requestDTO.getConnectionInfo();
				if (!currentRequests.containsKey(addConnectionInfo)) {
					currentRequests.put(addConnectionInfo, this);
					try {
						long timeLeftToSleep = addConnectionInfo.getExpectedOutTime() - (System.currentTimeMillis());
						if(timeLeftToSleep>0)sleep(timeLeftToSleep);
						
						addConnectionInfo.setTimeLeft(0);
						addConnectionInfo.setActualOutTime(System.currentTimeMillis());
						ResponseDTO responseDTO = new ResponseDTO(Status.OK, Arrays.asList(addConnectionInfo));
						sendSucessReplyToClient(new SuccessReply(protocolWithVersion, ServerConstants.HTTP_OK_RESPONSE_CODE, ServerConstants.HTTP_OK_RESPONSE, ServerConstants.CONTENT_TYPE_JSON, serialize(responseDTO)));
						currentRequests.remove(addConnectionInfo);
					} catch (InterruptedException ie) {
						addConnectionInfo.setTimeLeft((int)(addConnectionInfo.getExpectedOutTime()-System.currentTimeMillis()));
						addConnectionInfo.setActualOutTime(System.currentTimeMillis());
						ResponseDTO responseDTO = new ResponseDTO(Status.KILLED, Arrays.asList(addConnectionInfo));
						sendSucessReplyToClient(new SuccessReply(protocolWithVersion, ServerConstants.HTTP_OK_RESPONSE_CODE, ServerConstants.HTTP_OK_RESPONSE, ServerConstants.CONTENT_TYPE_JSON, serialize(responseDTO)));
						currentRequests.remove(addConnectionInfo);
					}
				} else {
					ResponseDTO responseDTO = new ResponseDTO(Status.ERROR, "client with connection id " + addConnectionInfo.getConnId() + " is already connected to the server.");
					throw new ExceptionReply(protocolWithVersion, ServerConstants.HTTP_BAD_REQUEST_RESPONSE_CODE, ServerConstants.HTTP_BAD_REQUEST_RESPONSE, ServerConstants.CONTENT_TYPE_JSON, serialize(responseDTO));
				}
			} else if (method.equals(ServerConstants.HTTP_GET) && url.equals(ServerConstants.GET_CONNECTIONS_URL)) {
				ResponseDTO responseDTO = new ResponseDTO(Status.OK, new ArrayList<ConnectionInfo>(currentRequests.keySet()));
				for(ConnectionInfo ci : responseDTO.getConnectionInfos() ){
					ci.setTimeLeft((int)(ci.getExpectedOutTime()-System.currentTimeMillis()));
				}
				sendSucessReplyToClient(new SuccessReply(protocolWithVersion, ServerConstants.HTTP_OK_RESPONSE_CODE, ServerConstants.HTTP_OK_RESPONSE, ServerConstants.CONTENT_TYPE_JSON, serialize(responseDTO)));
			} else if (method.equals(ServerConstants.HTTP_PUT) && url.equals(ServerConstants.KILL_CONNECTION_URL)) {
				ConnectionInfo killConnectionInfo = requestDTO.getConnectionInfo();
				if (currentRequests.containsKey(killConnectionInfo)) {
					for(ConnectionInfo ci : currentRequests.keySet())if(killConnectionInfo.equals(ci))killConnectionInfo=ci;
					Thread threadToKill = currentRequests.get(killConnectionInfo);
					threadToKill.interrupt();
					try {
						threadToKill.join();
						ResponseDTO responseDTO = new ResponseDTO(Status.OK, Arrays.asList(killConnectionInfo));
						sendSucessReplyToClient(new SuccessReply(protocolWithVersion, ServerConstants.HTTP_OK_RESPONSE_CODE, ServerConstants.HTTP_OK_RESPONSE, ServerConstants.CONTENT_TYPE_JSON, serialize(responseDTO)));
					} catch (InterruptedException ie) {
						ResponseDTO responseDTO = new ResponseDTO(Status.ERROR, "interrupted exception " + ie.getLocalizedMessage().replaceAll("[\\\n]", "") + " occurred while trying to kill the client with connection id - "+killConnectionInfo.getConnId()+".");
						throw new ExceptionReply(protocolWithVersion, ServerConstants.HTTP_SERVER_FAIL_RESPONSE_CODE, ServerConstants.HTTP_SERVER_FAIL_RESPONSE, ServerConstants.CONTENT_TYPE_JSON, serialize(responseDTO));
					}
				} else {
					ResponseDTO responseDTO = new ResponseDTO(Status.ERROR, "client with connection id " + killConnectionInfo.getConnId() + " is currently not connected to the server.");
					throw new ExceptionReply(protocolWithVersion, ServerConstants.HTTP_BAD_REQUEST_RESPONSE_CODE, ServerConstants.HTTP_BAD_REQUEST_RESPONSE,  ServerConstants.CONTENT_TYPE_JSON, serialize(responseDTO));
				}
			}
		}catch(ExceptionReply er){
			sendExceptionReplyToClient(er);
		} finally{
			closeClientSocketQuietly(CLIENT_SOCKET);
		}
	}

	private RequestDTO extractAndPrepareRequestDTO() throws ExceptionReply {
		try {
			RequestDTO clientRequest = null;

			String requestDetailsHeader = null;
			String[] requestDetails = null;
			List<String> otherHeaders = null;

			String payloadDataType = null;
			int payloadDataLength = -1;
			String payloadData = "";

			BufferedReader in = new BufferedReader(new InputStreamReader(CLIENT_SOCKET.getInputStream()));

			// 1st Line - Method Type, URL, Protocol - Version
			String currentLine;
			currentLine = in.readLine();
			requestDetailsHeader = currentLine;
			requestDetails = requestDetailsHeader.split(" ");

			// Other Headers - looks for payloadDataLength, payloadDataType, payloadData
			currentLine = "";
			otherHeaders = new ArrayList<String>();
			while ((currentLine = in.readLine()) != null && (currentLine.length() != 0)) {
				otherHeaders.add(currentLine);
				if (currentLine.indexOf(ServerConstants.CONTENT_LENGTH_HEADER) > -1) {
					String contentLength = currentLine.substring(currentLine.indexOf(ServerConstants.CONTENT_LENGTH_HEADER) + 16, currentLine.length());
					try{
						payloadDataLength = new Integer(contentLength).intValue();
					}catch(NumberFormatException nfe){
						ResponseDTO responseDTO = new ResponseDTO(Status.ERROR, ServerConstants.CONTENT_LENGTH_HEADER+" - "+contentLength+" passed is not a valid integer");
						throw new ExceptionReply(ServerConstants.DEFAULT_PROTOCOL_WITH_VERSION, ServerConstants.HTTP_BAD_REQUEST_RESPONSE_CODE, ServerConstants.HTTP_BAD_REQUEST_RESPONSE_CODE, ServerConstants.CONTENT_TYPE_JSON, serialize(responseDTO));
					}
				}
				if (currentLine.indexOf(ServerConstants.CONTENT_TYPE_HEADER) > -1) {
					payloadDataType = currentLine.substring(currentLine.indexOf(ServerConstants.CONTENT_TYPE_HEADER) + 14);
				}
			}
			if (payloadDataLength > 0 && payloadDataType.equals(ServerConstants.CONTENT_TYPE_JSON)) {
				char[] charArray = new char[payloadDataLength];
				in.read(charArray, 0, payloadDataLength);
				payloadData = new String(charArray);
			}
			CLIENT_SOCKET.shutdownInput();

			if (requestDetails.length == 3) {
				String httpMethodType = requestDetails[0];
				String url = requestDetails[1];
				String protocolWithVersion = requestDetails[2];

				Map<String, String> queryParams = null;
				String actualURL = null;
				if (ServerConstants.VALID_PROTOCOL_WITH_VERSIONS.contains(protocolWithVersion)) {
					if (httpMethodType.equals(ServerConstants.HTTP_GET) && url.startsWith(ServerConstants.ADD_CONNECTION_URL+ServerConstants.QUERY_PARAMS_STARTER)) {
						int indexOfQueryParamsStarter = url.indexOf(ServerConstants.QUERY_PARAMS_STARTER);
						actualURL = url.substring(0, indexOfQueryParamsStarter);
						String queryParamsURL = url.substring(indexOfQueryParamsStarter + 1);
						queryParams = extractQueryParams(protocolWithVersion, queryParamsURL);

						// Now search for the required keys in queryParams Map
						if (queryParams.containsKey(ServerConstants.QUERY_PARAM_CONN_ID) && queryParams.containsKey(ServerConstants.QUERY_PARAM_TIME_OUT)) {
							String connId = queryParams.get(ServerConstants.QUERY_PARAM_CONN_ID);
							String tOut = queryParams.get(ServerConstants.QUERY_PARAM_TIME_OUT);
							try {
								int timeOut = Integer.parseInt(tOut);
								ConnectionInfo ac = new ConnectionInfo(connId, timeOut, inTime, inTime + timeOut);
								clientRequest = new RequestDTO(protocolWithVersion, ServerConstants.HTTP_GET, actualURL, ac);
							} catch (NumberFormatException nfe) {
								ResponseDTO responseDTO = new ResponseDTO(Status.ERROR, ServerConstants.QUERY_PARAM_TIME_OUT+" - "+tOut+" passed is not a valid integer");
								throw new ExceptionReply(ServerConstants.DEFAULT_PROTOCOL_WITH_VERSION, ServerConstants.HTTP_BAD_REQUEST_RESPONSE_CODE, ServerConstants.HTTP_BAD_REQUEST_RESPONSE_CODE, ServerConstants.CONTENT_TYPE_JSON, serialize(responseDTO));
							}
						} else {
							ResponseDTO responseDTO = new ResponseDTO(Status.ERROR, "mandatory params ("+ServerConstants.QUERY_PARAM_CONN_ID+" or "+ServerConstants.QUERY_PARAM_TIME_OUT+") is/are missing. queryParamsReceived - "+queryParamsURL);
							throw new ExceptionReply(protocolWithVersion, ServerConstants.HTTP_BAD_REQUEST_RESPONSE_CODE, ServerConstants.HTTP_BAD_REQUEST_RESPONSE, ServerConstants.CONTENT_TYPE_JSON, serialize(responseDTO));
						}
					} else if (httpMethodType.equals(ServerConstants.HTTP_GET) && url.equals(ServerConstants.GET_CONNECTIONS_URL)) {
						actualURL = url;
						clientRequest = new RequestDTO(protocolWithVersion, ServerConstants.HTTP_GET, actualURL, null);
					} else if (httpMethodType.equals(ServerConstants.HTTP_PUT) && url.equals(ServerConstants.KILL_CONNECTION_URL)) {
						if (!payloadData.isEmpty()) {
							actualURL = url;
							ConnectionInfo killDTO = deSerializeKillConnectionInfo(protocolWithVersion, payloadData);
							clientRequest = new RequestDTO(protocolWithVersion, ServerConstants.HTTP_PUT, actualURL, killDTO);
						} else {
							ResponseDTO responseDTO = new ResponseDTO(Status.ERROR, "payload is either not specified or specified with wrong contentType - "+payloadDataType+" & wrong contentLength - "+payloadDataLength+".");
							throw new ExceptionReply(protocolWithVersion, ServerConstants.HTTP_BAD_REQUEST_RESPONSE_CODE, ServerConstants.HTTP_BAD_REQUEST_RESPONSE, ServerConstants.CONTENT_TYPE_JSON, serialize(responseDTO));
						}
					} else {
						ResponseDTO responseDTO = new ResponseDTO(Status.ERROR, httpMethodType + " - " + url + " is currently not supported on this server.");
						throw new ExceptionReply(protocolWithVersion, ServerConstants.HTTP_NOT_FOUND_RESPONSE_CODE, ServerConstants.HTTP_NOT_FOUND_RESPONSE, ServerConstants.HTTP_NOT_FOUND_RESPONSE_CODE, serialize(responseDTO));
					}
				} else {
					ResponseDTO responseDTO = new ResponseDTO(Status.ERROR, "unsupported protocol - "+protocolWithVersion+".");
					throw new ExceptionReply(ServerConstants.DEFAULT_PROTOCOL_WITH_VERSION, ServerConstants.HTTP_BAD_REQUEST_RESPONSE_CODE, ServerConstants.HTTP_BAD_REQUEST_RESPONSE, ServerConstants.CONTENT_TYPE_JSON, serialize(responseDTO));
				}
			} else {
				ResponseDTO responseDTO = new ResponseDTO(Status.ERROR, "malformed request details header - "+requestDetailsHeader+".");
				throw new ExceptionReply(ServerConstants.DEFAULT_PROTOCOL_WITH_VERSION, ServerConstants.HTTP_BAD_REQUEST_RESPONSE_CODE, ServerConstants.HTTP_BAD_REQUEST_RESPONSE, ServerConstants.CONTENT_TYPE_JSON, serialize(responseDTO));
			}
			return clientRequest;
		} catch (IOException ioe) {
			ResponseDTO responseDTO = new ResponseDTO(Status.ERROR, "io exception " + ioe.getLocalizedMessage().replaceAll("[\\\n]", "") + " occurred while trying to read the client request");
			throw new ExceptionReply(ServerConstants.DEFAULT_PROTOCOL_WITH_VERSION, ServerConstants.HTTP_SERVER_FAIL_RESPONSE_CODE, ServerConstants.HTTP_SERVER_FAIL_RESPONSE_CODE, ServerConstants.CONTENT_TYPE_JSON, serialize(responseDTO));
		}
	}
	
	private Map<String, String> extractQueryParams(String protocolWithVersion, String queryParamsURL) throws ExceptionReply {
		String actualQueryParamsURL = queryParamsURL;
		Map<String, String> queryParams = new HashMap<String, String>();
		String currentKeyValuePair = null;

		while (queryParamsURL.contains(ServerConstants.QUERY_PARAMS_SEPARATOR)) {
			int indexOfQueryParamsSeperator = queryParamsURL.indexOf(ServerConstants.QUERY_PARAMS_SEPARATOR);
			currentKeyValuePair = queryParamsURL.substring(0,indexOfQueryParamsSeperator);
			queryParamsURL = queryParamsURL.substring(indexOfQueryParamsSeperator + 1);
			if (currentKeyValuePair.contains(ServerConstants.QUERY_PARAMS_KEY_VALUE_SEPARATOR)) {
				int indexOfKeyValueSeperator = currentKeyValuePair.indexOf(ServerConstants.QUERY_PARAMS_KEY_VALUE_SEPARATOR);
				String key = currentKeyValuePair.substring(0, indexOfKeyValueSeperator);
				String value = currentKeyValuePair.substring(indexOfKeyValueSeperator+1);
				queryParams.put(key, value);
			} else {
				ResponseDTO responseDTO = new ResponseDTO(Status.ERROR, "malformed url - "+actualQueryParamsURL);
				throw new ExceptionReply(protocolWithVersion, ServerConstants.HTTP_BAD_REQUEST_RESPONSE_CODE, ServerConstants.HTTP_BAD_REQUEST_RESPONSE, ServerConstants.CONTENT_TYPE_JSON, serialize(responseDTO));
			}
		}
		currentKeyValuePair = queryParamsURL;
		if (currentKeyValuePair.contains(ServerConstants.QUERY_PARAMS_KEY_VALUE_SEPARATOR)) {
			int indexOfKeyValueSeperator = currentKeyValuePair.indexOf(ServerConstants.QUERY_PARAMS_KEY_VALUE_SEPARATOR);
			String key = currentKeyValuePair.substring(0, indexOfKeyValueSeperator);
			String value = currentKeyValuePair.substring(indexOfKeyValueSeperator+1);
			queryParams.put(key, value);
		} else {
			ResponseDTO responseDTO = new ResponseDTO(Status.ERROR, "malformed url - "+actualQueryParamsURL);
			throw new ExceptionReply(protocolWithVersion, ServerConstants.HTTP_BAD_REQUEST_RESPONSE_CODE, ServerConstants.HTTP_BAD_REQUEST_RESPONSE, ServerConstants.CONTENT_TYPE_JSON, serialize(responseDTO));

		}
		return queryParams;
	}
	
	private ConnectionInfo deSerializeKillConnectionInfo(String protocolWithVersion, String payload) throws ExceptionReply {
		ConnectionInfo killDTO = null;
		try {
			killDTO = objectMapper.readValue(payload, ConnectionInfo.class);
		} catch (JsonMappingException jme) {
			ResponseDTO responseDTO = new ResponseDTO(Status.ERROR, "jsonmapping exception " + jme.getLocalizedMessage().replaceAll("[\\\n]", "")  + " occurred while trying parse the request payload.");
			throw new ExceptionReply(protocolWithVersion, ServerConstants.HTTP_BAD_REQUEST_RESPONSE_CODE, ServerConstants.HTTP_BAD_REQUEST_RESPONSE, ServerConstants.CONTENT_TYPE_JSON, serialize(responseDTO));		
		} catch (JsonGenerationException jge) {
			ResponseDTO responseDTO = new ResponseDTO(Status.ERROR, "jsonmapping exception " + jge.getLocalizedMessage().replaceAll("[\\\n]", "")  + " occurred while trying parse the request payload.");
			throw new ExceptionReply(protocolWithVersion, ServerConstants.HTTP_BAD_REQUEST_RESPONSE_CODE, ServerConstants.HTTP_BAD_REQUEST_RESPONSE, ServerConstants.CONTENT_TYPE_JSON, serialize(responseDTO));		
		} catch (IOException ioe) {
			ResponseDTO responseDTO = new ResponseDTO(Status.ERROR, "jsonmapping exception " + ioe.getLocalizedMessage().replaceAll("[\\\n]", "")  + " occurred while trying parse the request payload.");
			throw new ExceptionReply(protocolWithVersion, ServerConstants.HTTP_BAD_REQUEST_RESPONSE_CODE, ServerConstants.HTTP_BAD_REQUEST_RESPONSE, ServerConstants.CONTENT_TYPE_JSON, serialize(responseDTO));		
		}
		return killDTO;
	}
	
	private String serialize(Object o){
		String response = null;
		try {
			response = objectMapper.writeValueAsString(o);
		} catch (JsonMappingException jme) {
			response = "{\"message\":\"jsonmapping exception - " + jme.getLocalizedMessage().replaceAll("[\\\n]", "")  + " occurred while trying generate the response payload.\"}";
		} catch (JsonGenerationException jge) {
			response = "{\"message\":\"jsonmapping exception - " + jge.getLocalizedMessage().replaceAll("[\\\n]", "")  + " occurred while trying generate the response payload.\"}";
		} catch (IOException ioe) {
			response = "{\"message\":\"jsonmapping exception - " + ioe.getLocalizedMessage().replaceAll("[\\\n]", "")  + " occurred while trying generate the response payload.\"}";
		}
		return response;
	}

	private void sendExceptionReplyToClient(ExceptionReply er) {
		try {
			PrintWriter out = new PrintWriter(CLIENT_SOCKET.getOutputStream(), true);
			out.println(er.getProtocolWithVersion() + " " + er.getResponseCode() + " " + er.getResponseStatus());
			out.println(ServerConstants.CONTENT_TYPE_HEADER + " "+ er.getResponseContentType());
			if(er.getResponse() != null){
				out.println(ServerConstants.CONTENT_LENGTH_HEADER + " "+er.getResponse().length());
				out.println("");
				out.print(er.getResponse());
			}
			out.flush();
			out.close();
			CLIENT_SOCKET.close();
		} catch (IOException ioe) {
			System.err.println("IOException while writing the exceptionResponse to the client - " + ioe.getLocalizedMessage());
		}
	}

	private void sendSucessReplyToClient(SuccessReply sr) {
		try {
			PrintWriter out = new PrintWriter(CLIENT_SOCKET.getOutputStream(), true);
			out.println(sr.getProtocolWithVersion() + " " + sr.getResponseCode() + " " + sr.getResponseStatus());
			out.println("Content-Type: " + sr.getResponseContentType());
			out.println("");
			out.print(sr.getResponse());
			out.flush();
			out.close();
			CLIENT_SOCKET.close();
		} catch (IOException ioe) {
			System.err.println("IOException while writing the successResponse to the client - " + ioe.getLocalizedMessage());
		}
	}
	
	private void closeClientSocketQuietly(Socket c) {
		try {
			if (c != null && !c.isClosed())
				c.close();
		} catch (IOException ioe) {
			System.err.println("IOException occured while closing a client socket - " + ioe.getLocalizedMessage());
		}
	}
}