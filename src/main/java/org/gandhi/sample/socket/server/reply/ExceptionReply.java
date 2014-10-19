package org.gandhi.sample.socket.server.reply;

public class ExceptionReply extends Exception{

	private static final long serialVersionUID = -5384055825978284889L;
	
	private final String protocolWithVersion;
	
	private final String responseCode;
	
	private final String responseStatus;
	
	private final String responseContentType;
	
	private final String response;

	public ExceptionReply(String protocolWithVersion, String responseCode, String responseStatus, String responseContentType, String response) {
		super();
		this.protocolWithVersion = protocolWithVersion;
		this.responseCode = responseCode;
		this.responseStatus = responseStatus;
		this.responseContentType = responseContentType;
		this.response = response;
	}

	public String getProtocolWithVersion() {
		return protocolWithVersion;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public String getResponseStatus() {
		return responseStatus;
	}

	public String getResponseContentType() {
		return responseContentType;
	}

	public String getResponse() {
		return response;
	}
}