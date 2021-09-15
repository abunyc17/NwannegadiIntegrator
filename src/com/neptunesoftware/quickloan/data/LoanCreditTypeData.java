package com.neptunesoftware.quickloan.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LoanCreditTypeData{
	private String creditTypeCode;
	private String creditTypeDesc;
	
	public LoanCreditTypeData() {
	}
	
	public LoanCreditTypeData(String creditTypeCode, String creditTypeDesc) {
		this.creditTypeCode = creditTypeCode;
		this.creditTypeDesc = creditTypeDesc;
	}
	
	public String getCreditTypeCode() {
		return creditTypeCode == null ? "" : creditTypeCode;
	}

	@XmlElement
	public void setCreditTypeCode(String creditTypeCode) {
		this.creditTypeCode = creditTypeCode;
	}

	public String getCreditTypeDesc() {
		return creditTypeDesc == null ? "" : creditTypeDesc;
	}

	@XmlElement
	public void setCreditTypeDesc(String creditTypeDesc) {
		this.creditTypeDesc = creditTypeDesc;
	}
}
