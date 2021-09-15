package com.neptunesoftware.quickloan.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LoanReasonData {
	private String reasonCode;
	private String reasonDesc;
	
	public LoanReasonData() {
	}
	
	public LoanReasonData(String reasonCode, String reasonDesc) {
		this.reasonCode = reasonCode;
		this.reasonDesc = reasonDesc;
	}

	public String getReasonCode() {
		return reasonCode == null ? "" : reasonCode;
	}

	@XmlElement
	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}

	public String getReasonDesc() {
		return reasonDesc == null ? "" : reasonDesc;
	}

	@XmlElement
	public void setReasonDesc(String reasonDesc) {
		this.reasonDesc = reasonDesc;
	}

}
