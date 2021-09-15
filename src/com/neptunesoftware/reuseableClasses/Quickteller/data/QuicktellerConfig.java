package com.neptunesoftware.reuseableClasses.Quickteller.data;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "QuicktellerInfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class QuicktellerConfig {

	@XmlElement(name = "Quickteller")
	private List<QuicktellerCredential> quicktellerCredential;
	
	@XmlTransient
	private String responseCode;


	public List<QuicktellerCredential> getQuicktellerCredential() {
		return quicktellerCredential;
	}

	public void setQuicktellerCredential(List<QuicktellerCredential> quicktellerCredential) {
		this.quicktellerCredential = quicktellerCredential;
	}
	
	public String getResponseCode() {
		return responseCode == null ? "" : responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
	
}
