package com.neptunesoftware.quickloan.data;

import java.util.List;

import com.neptunesoftware.reuseableClasses.ResponseModel;


public class LoanCreditTypeResponse extends ResponseModel {
	
	private List<LoanCreditTypeData> creditType;

	public LoanCreditTypeResponse() {
	}
	
	public LoanCreditTypeResponse(List<LoanCreditTypeData> creditType, String responseCode, String responseMessage) {
		super(responseCode, responseMessage);
		this.creditType = creditType;
	}

	public List<LoanCreditTypeData> getCreditType() {
		return creditType;
	}

	public void setCreditType(List<LoanCreditTypeData> creditType) {
		this.creditType = creditType;
	}
	
}
