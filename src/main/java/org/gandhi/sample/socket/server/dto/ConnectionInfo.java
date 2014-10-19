package org.gandhi.sample.socket.server.dto;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public final class ConnectionInfo {
	
	private final String connId;

	private Integer timeOut;
	
	private Long inTime;
	
	private Long expectedOutTime;
	
	private Integer timeLeft;
	
	private Long actualOutTime;
	
	public ConnectionInfo(String connId, Integer timeOut, Long inTime, Long expectedOutTime, Integer timeLeft, Long actualOutTime){
		this.connId = connId;
		this.timeOut = timeOut;
		this.inTime = inTime;
		this.expectedOutTime = expectedOutTime;
		this.timeLeft = timeLeft;
		this.actualOutTime = actualOutTime;
	}
	
	public ConnectionInfo(String connId, Integer timeOut, Long inTime, Long expectedOutTime){
		this.connId = connId;
		this.timeOut = timeOut;
		this.inTime = inTime;
		this.expectedOutTime = expectedOutTime;
	}
	
	public ConnectionInfo(String connectionId){
		this.connId = connectionId;
	}
	
	//FOR JSON Deserialization ONLY
	@Deprecated
	public ConnectionInfo(){
		this.connId = null;
	}
	
	public String getConnId() {
		return connId;
	}
	
	public Integer getTimeOut() {
		return timeOut;
	}

	public Long getInTime() {
		return inTime;
	}

	public Long getExpectedOutTime() {
		return expectedOutTime;
	}
	
	public void setTimeOut(Integer timeOut) {
		this.timeOut = timeOut;
	}

	public void setInTime(Long inTime) {
		this.inTime = inTime;
	}

	public void setExpectedOutTime(Long expectedOutTime) {
		this.expectedOutTime = expectedOutTime;
	}

	public Integer getTimeLeft() {
		return timeLeft;
	}

	public void setTimeLeft(Integer timeLeft) {
		this.timeLeft = timeLeft;
	}

	public Long getActualOutTime() {
		return actualOutTime;
	}

	public void setActualOutTime(Long actualOutTime) {
		this.actualOutTime = actualOutTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((connId == null) ? 0 : connId.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConnectionInfo other = (ConnectionInfo) obj;
		if (connId == null) {
			if (other.connId != null)
				return false;
		} else if (!connId.equals(other.connId))
			return false;
		return true;
	}
}