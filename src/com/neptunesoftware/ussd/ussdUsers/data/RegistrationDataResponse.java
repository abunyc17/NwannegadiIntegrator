package com.neptunesoftware.ussd.ussdUsers.data;

import com.neptunesoftware.reuseableClasses.ResponseModel;

public class RegistrationDataResponse extends ResponseModel {

	private String pin;
	private String mobileNumber;
	private String accountNumber;
	
	public String getPin() {
		return pin;
	}
	
	public void setPin(String pin) {
		this.pin = pin;
	}
		
	public String getMobileNumber() {
		return mobileNumber;
	}
	
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	
	public String getAccountNumber() {
		return accountNumber;
	}
	
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	
	
}
