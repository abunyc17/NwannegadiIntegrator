package com.neptunesoftware.quickloan.data;

import com.neptunesoftware.reuseableClasses.ResponseModel;

public class LoanRegistrationResponse extends ResponseModel {

	private String email;
	private String password;
	private String pin;
	private String accountNumber;
	private String phoneNumber;
	
	public LoanRegistrationResponse() {
	}
	
	public LoanRegistrationResponse(String email, String password, String pin, String accountNumber,
			String phoneNumber) {
		super();
		this.email = email;
		this.password = password;
		this.pin = pin;
		this.accountNumber = accountNumber;
		this.phoneNumber = phoneNumber;
	}
	
	public LoanRegistrationResponse(String email, String password, String pin, String accountNumber,
			String phoneNumber, String responseCode, String responseMessage) {
		super(responseCode, responseMessage);
		this.email = email;
		this.password = password;
		this.pin = pin;
		this.accountNumber = accountNumber;
		this.phoneNumber = phoneNumber;
	}

	public String getEmail() {
		return email == null ? "" : email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password == null ? "" : password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPin() {
		return pin == null ? "" : pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public String getAccountNumber() {
		return accountNumber == null ? "" : accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getPhoneNumber() {
		return phoneNumber == null ? "" : phoneNumber;
	}
	
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
}
