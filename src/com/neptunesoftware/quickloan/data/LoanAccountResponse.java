package com.neptunesoftware.quickloan.data;

import java.util.List;

import com.neptunesoftware.reuseableClasses.ResponseModel;

public class LoanAccountResponse extends ResponseModel {

	private List<LoanAccountData> accounts;

	public LoanAccountResponse() {
	}
	
	public LoanAccountResponse(List<LoanAccountData> accounts) {
		super();
		this.accounts = accounts;
	}

	public List<LoanAccountData> getAccounts() {
		return accounts;
	}

	public void setAccounts(List<LoanAccountData> accounts) {
		this.accounts = accounts;
	}
	
}
