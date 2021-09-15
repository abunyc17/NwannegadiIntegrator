package com.neptunesoftware.quickloan.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LoanRegistrationRequest {
	private String email;
	private String password;
	private String pin;
	private String accountNumber;
	private String phoneNumber;
	
	public LoanRegistrationRequest() {		
	}
	
	public LoanRegistrationRequest(String email, String password, String pin, String accountNumber,
			String phoneNumber) {
		super();
		this.email = email;
		this.password = password;
		this.pin = pin;
		this.accountNumber = accountNumber;
		this.phoneNumber = phoneNumber;
	}

	public String getEmail() {
		return email == null ? "" : email;
	}

	@XmlElement(required = true)
	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password == null ? "" : password;
	}

	@XmlElement(required = true)
	public void setPassword(String password) {
		this.password = password;
	}

	public String getPin() {
		return pin == null ? "" : pin;
	}

	@XmlElement(required = true)
	public void setPin(String pin) {
		this.pin = pin;
	}

	public String getAccountNumber() {
		return accountNumber == null ? "" : accountNumber;
	}

	@XmlElement(required = true)
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getPhoneNumber() {
		return phoneNumber == null ? "" : phoneNumber;
	}

	@XmlElement
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	
	
	
	
}
