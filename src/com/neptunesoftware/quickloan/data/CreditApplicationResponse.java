package com.neptunesoftware.quickloan.data;

import com.neptunesoftware.reuseableClasses.ResponseModel;

public class CreditApplicationResponse extends ResponseModel {

	private String referenceNo;
	
	public CreditApplicationResponse() {
		super();
	}
	public CreditApplicationResponse(String responseCode, String responseMessage) {
		super(responseCode, responseMessage);
	}
	
	public String getReferenceNo() {
		return referenceNo == null ? "" : referenceNo;
	}
	
	public void setReferenceNo(String referenceNo) {
		this.referenceNo = referenceNo;
	}
	
}
