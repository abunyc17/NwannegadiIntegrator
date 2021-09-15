package com.neptunesoftware.quickloan.data;

import java.util.List;

import com.neptunesoftware.reuseableClasses.ResponseModel;


public class LoanReasonResponse extends ResponseModel{

	private List<LoanReasonData> reasonCode;
	
	public LoanReasonResponse() {
	}

	public LoanReasonResponse(List<LoanReasonData> reasonCode, String responseCode, String responseMessage) {
		super(responseCode, responseMessage);
		this.reasonCode = reasonCode;
	}

	public List<LoanReasonData> getReasonCode() {
		return reasonCode;
	}

	public void setReasonCode(List<LoanReasonData> reasonCode) {
		this.reasonCode = reasonCode;
	}
	
}
