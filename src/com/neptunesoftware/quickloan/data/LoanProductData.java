package com.neptunesoftware.quickloan.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LoanProductData{

	private String productId;
	private String productCode;
	private String productDesc;
	private String currencyCode;
	private String termCode;
	private String termDesc;
	private String termValue;
	private String minAmount;
	private String maxAmount;
	private String applicationDate;
	private String expiryDate;
	
	public LoanProductData() {
	}
	
	public LoanProductData(String productId, String productCode, String productDesc, String currencyCode,
			String termCode, String termDesc, String termValue, String minAmount, String maxAmount,
			String applicationDate, String expiryDate, String responseCode, String responseMessage) {
		this.productId = productId;
		this.productCode = productCode;
		this.productDesc = productDesc;
		this.currencyCode = currencyCode;
		this.termCode = termCode;
		this.termDesc = termDesc;
		this.termValue = termValue;
		this.minAmount = minAmount;
		this.maxAmount = maxAmount;
		this.applicationDate = applicationDate;
		this.expiryDate = expiryDate;
	}

	public LoanProductData(String productId, String productCode, String productDesc, String currencyCode,
			String termCode, String termDesc, String termValue, String minAmount, String maxAmount) {
		super();
		this.productId = productId;
		this.productCode = productCode;
		this.productDesc = productDesc;
		this.currencyCode = currencyCode;
		this.termCode = termCode;
		this.termDesc = termDesc;
		this.termValue = termValue;
		this.minAmount = minAmount;
		this.maxAmount = maxAmount;
	}

	public String getProductId() {
		return productId == null ? "" : productId;
	}

	@XmlElement
	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getProductCode() {
		return productCode == null ? "" : productCode;
	}

	@XmlElement
	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public String getProductDesc() {
		return productDesc == null ? "" : productDesc;
	}

	@XmlElement
	public void setProductDesc(String productDesc) {
		this.productDesc = productDesc;
	}

	public String getCurrencyCode() {
		return currencyCode == null ? "" : currencyCode;
	}

	@XmlElement
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public String getTermCode() {
		return termCode == null ? "" : termCode;
	}

	@XmlElement
	public void setTermCode(String termCode) {
		this.termCode = termCode;
	}

	public String getTermDesc() {
		return termDesc == null ? "" : termDesc;
	}

	@XmlElement
	public void setTermDesc(String termDesc) {
		this.termDesc = termDesc;
	}

	public String getTermValue() {
		return termValue == null ? "" : termValue;
	}

	@XmlElement
	public void setTermValue(String termValue) {
		this.termValue = termValue;
	}

	public String getMinAmount() {
		return minAmount == null ? "" : minAmount;
	}

	@XmlElement
	public void setMinAmount(String minAmount) {
		this.minAmount = minAmount;
	}

	public String getMaxAmount() {
		return maxAmount == null ? "" : maxAmount;
	}

	@XmlElement
	public void setMaxAmount(String maxAmount) {
		this.maxAmount = maxAmount;
	}
	
	public String getApplicationDate() {
		return applicationDate;
	}

	@XmlElement
	public void setApplicationDate(String applicationDate) {
		this.applicationDate = applicationDate;
	}

	public String getExpiryDate() {
		return expiryDate;
	}

	@XmlElement
	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}

}
