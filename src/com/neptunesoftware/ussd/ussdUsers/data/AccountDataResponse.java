package com.neptunesoftware.ussd.ussdUsers.data;

import java.util.List;

import com.neptunesoftware.reuseableClasses.ResponseModel;

public class AccountDataResponse extends ResponseModel{

	private List<String> accountNumber;
	private String accountName;
	private String contact;
	
	public AccountDataResponse() {}
	
	public AccountDataResponse(String responseCode, String responseMessage) {
		super(responseCode, responseMessage);
	}
	
	public List<String> getAccountNumber() {
		return accountNumber;
	}
	
	public void setAccountNumber(List<String> accountNumber) {
		this.accountNumber = accountNumber;
	}
	
	public String getAccountName() {
		return accountName;
	}
	
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	
	public String getContact() {
		return contact;
	}
	
	public void setContact(String contact) {
		this.contact = contact;
	}
	
	
	
}
