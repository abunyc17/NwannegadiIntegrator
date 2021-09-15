package com.neptunesoftware.quickloan.data;

import java.util.List;


import com.neptunesoftware.reuseableClasses.ResponseModel;



public class LoanProductResponse extends ResponseModel{

	private List<LoanProductData> loanProduct;

	public LoanProductResponse() {
	}

	public LoanProductResponse(List<LoanProductData> loanProduct, String responseCode, String responseMessage) {
		super(responseCode, responseMessage);
		this.loanProduct = loanProduct;
	}
	
	public LoanProductResponse(List<LoanProductData> loanProduct) {
		super();
		this.loanProduct = loanProduct;
	}
	
	public List<LoanProductData> getLoanProduct() {
		return loanProduct;
	}
	
	public void setLoanProduct(List<LoanProductData> loanProduct) {
		this.loanProduct = loanProduct;
	}

	
}
