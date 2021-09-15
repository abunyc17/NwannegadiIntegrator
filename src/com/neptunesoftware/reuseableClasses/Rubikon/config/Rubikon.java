package com.neptunesoftware.reuseableClasses.Rubikon.config;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "RubikonInfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class Rubikon {

	@XmlElement(name = "Rubikon")
	private List<RubikonCredential> rubikonCredential;

	@XmlTransient
	private String responseCode;
	
	public List<RubikonCredential> getRubikonCredential() {
		return rubikonCredential;
	}

	public void setRubikonCredential(List<RubikonCredential> rubikonCredential) {
		this.rubikonCredential = rubikonCredential;
	}
	
	public String getResponseCode() {
		return responseCode == null ? "" : responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
	
		
}
