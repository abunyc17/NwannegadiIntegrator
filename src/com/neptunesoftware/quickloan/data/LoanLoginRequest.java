package com.neptunesoftware.quickloan.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LoanLoginRequest {

	private String username;
	private String password;
	
	public LoanLoginRequest() {
	}
	
	public LoanLoginRequest(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username == null ? "" : username;
	}

	@XmlElement(required = true)
	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password == null ? "" : password;
	}

	@XmlElement(required = true)
	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
