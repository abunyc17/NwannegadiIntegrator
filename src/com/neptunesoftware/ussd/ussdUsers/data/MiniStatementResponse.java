package com.neptunesoftware.ussd.ussdUsers.data;

import java.util.List;

import com.neptunesoftware.reuseableClasses.ResponseModel;

public class MiniStatementResponse extends ResponseModel{
	
	List<MiniStatement> transactions;
	
	
	public MiniStatementResponse() {
		super();
	}
	
	public MiniStatementResponse(String responseCode, String responseMessage) {
		super(responseCode, responseMessage);
	}

	public List<MiniStatement> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<MiniStatement> transactions) {
		this.transactions = transactions;
	}

}
