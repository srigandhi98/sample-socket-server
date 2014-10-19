package org.gandhi.sample.socket.server.dto;

import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.gandhi.sample.socket.server.util.Status;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class ResponseDTO {
	private Status status;
	
	private List<ConnectionInfo> connectionInfos;
	
	private String message;

	public ResponseDTO(Status status,List<ConnectionInfo> connectionInfos, String message){
		this.status = status;
		this.connectionInfos = connectionInfos;
		this.message=message;
	}
	
	public ResponseDTO(Status status,List<ConnectionInfo> connectionInfos){
		this.status = status;
		this.connectionInfos = connectionInfos;
	}
	
	public ResponseDTO(Status status, String message){
		this.status = status;
		this.message = message;
	}
	
	public ResponseDTO(Status status){
		this.status = status;
	}
	
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public List<ConnectionInfo> getConnectionInfos() {
		return connectionInfos;
	}

	public void setConnectionInfos(List<ConnectionInfo> connectionInfos) {
		this.connectionInfos = connectionInfos;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}