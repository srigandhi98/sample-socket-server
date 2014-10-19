package org.gandhi.sample.socket.server.dto;


public class RequestDTO {
	
	private String protocolWithVersion;
	
	private String method;
	
	private String url;
	
	private ConnectionInfo connectionInfo;
	
	public RequestDTO(String protocolWithVersion, String method, String url, ConnectionInfo connectionInfo){
		this.protocolWithVersion = protocolWithVersion;
		this.method = method;
		this.url = url;
		this.connectionInfo = connectionInfo;
	}

	public String getProtocolWithVersion() {
		return protocolWithVersion;
	}

	public void setProtocolWithVersion(String protocolWithVersion) {
		this.protocolWithVersion = protocolWithVersion;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public ConnectionInfo getConnectionInfo() {
		return connectionInfo;
	}

	public void setConnectionInfo(ConnectionInfo connectionInfo) {
		this.connectionInfo = connectionInfo;
	}
}