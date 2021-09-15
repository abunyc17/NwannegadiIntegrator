package com.neptunesoftware.quickloan.data;

import com.neptunesoftware.reuseableClasses.ResponseModel;

public class LoanLoginResponse extends ResponseModel{

	private String customerName;
	private String custNo;	
	private String accountNumber;
	private String pin;
	private String phoneNumber;
	

	public LoanLoginResponse() {
	}
	
	public LoanLoginResponse(String customerName, String accountNumber, String responseCode, String responseMessage) {
		super(responseCode, responseMessage);
		this.customerName = customerName;
		this.accountNumber = accountNumber;
	}
	
	public LoanLoginResponse(String customerName, String accountNumber) {
		super();
		this.customerName = customerName;
		this.accountNumber = accountNumber;
	}
	
	public String getCustomerName() {
		return customerName;
	}
	
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	
	public String getCustNo() {
		return custNo;
	}

	public void setCustNo(String custNo) {
		this.custNo = custNo;
	}

	
	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
}
