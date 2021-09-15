package com.neptunesoftware.quickloan;

import com.neptunesoftware.quickloan.data.CreditApplicationData;
import com.neptunesoftware.quickloan.data.CreditApplicationResponse;
import com.neptunesoftware.quickloan.data.LoanAccountResponse;
import com.neptunesoftware.quickloan.data.LoanCreditTypeResponse;
import com.neptunesoftware.quickloan.data.LoanLoginRequest;
import com.neptunesoftware.quickloan.data.LoanLoginResponse;
import com.neptunesoftware.quickloan.data.LoanProductResponse;
import com.neptunesoftware.quickloan.data.LoanReasonResponse;
import com.neptunesoftware.quickloan.data.LoanRegistrationRequest;
import com.neptunesoftware.quickloan.data.LoanRegistrationResponse;
import com.neptunesoftware.reuseableClasses.CommonMethods;

public class QuickloanService {
  
    public String login(String body){
    	System.out.println("\n**** login ****");
    	
    	LoanLoginRequest loanLoginRequest = (LoanLoginRequest) CommonMethods.JSONStringToObject(body, LoanLoginRequest.class);
		
		//connect to rubikon database
		QuickloanDBOperation oracleDB = new QuickloanDBOperation();
		
		LoanLoginResponse loanLoginResponse = oracleDB.login(loanLoginRequest);
		
		String response = CommonMethods.ObjectToJsonString(loanLoginResponse);
		System.out.println("response: \n" + response);
		
		return response;
    }
        
    public String registration(String body) {
    	System.out.println("\n**** registration ****");
    	
    	LoanRegistrationRequest loanRegistrationRequest = (LoanRegistrationRequest) CommonMethods.JSONStringToObject(body, LoanRegistrationRequest.class);
	
		//connect to rubikon database
		QuickloanDBOperation oracleDB = new QuickloanDBOperation();
		
		LoanRegistrationResponse loanRegistrationResponse = oracleDB.registration(loanRegistrationRequest);
		
		String response = CommonMethods.ObjectToJsonString(loanRegistrationResponse);
		System.out.println("response: \n" + response);
		
		return response;
    }
    
    public String creditType() {
    	System.out.println("\n**** creditType ****");
    	
		//connect to rubikon database
		QuickloanDBOperation oracleDB = new QuickloanDBOperation();
		
		LoanCreditTypeResponse loanCreditTypeResponse = oracleDB.creditType();
		
		String response = CommonMethods.ObjectToJsonString(loanCreditTypeResponse);
		System.out.println("response: \n" + response);
		
		return response;

    }
	    
    public String reason() {
    	System.out.println("\n**** reason ****");
    	
		//connect to rubikon database
		QuickloanDBOperation oracleDB = new QuickloanDBOperation();
		
		LoanReasonResponse lanReasonResponse = oracleDB.reason();
		
		String response = CommonMethods.ObjectToJsonString(lanReasonResponse);
		System.out.println("response: \n" + response);
		
		return response;
    } 
        
    public String loanProduct() {
    	System.out.println("\n**** loanProduct ****");
    	
		//connect to rubikon database
		QuickloanDBOperation oracleDB = new QuickloanDBOperation();
		
		LoanProductResponse loanProductResponse = oracleDB.loanProduct();
		
		String response = CommonMethods.ObjectToJsonString(loanProductResponse);
		System.out.println("response: \n" + response);
		
		return response;
    } 
    
    public String account(String accountNumber) {
    	System.out.println("\n**** account ****");
    	
		//connect to rubikon database
		QuickloanDBOperation oracleDB = new QuickloanDBOperation();
		
		LoanAccountResponse loanAccountResponse = oracleDB.multiAccount(accountNumber);
		
		String response = CommonMethods.ObjectToJsonString(loanAccountResponse);
		System.out.println("response: \n" + response);
		
		return response;
    }
    
    public String creditApplication(String body) {
    	System.out.println("\n**** creditApplication ****");

		//connect to rubikon database
		QuickloanDBOperation oracleDB = new QuickloanDBOperation();
		
    	CreditApplicationData creditApplicationData = (CreditApplicationData) CommonMethods.JSONStringToObject(body, CreditApplicationData.class);
    	
		creditApplicationData.setMainOperation("Create");
		creditApplicationData.setRefNo("ref/FPL/" + System.currentTimeMillis());
		//creditApplicationData.getCreditType("");
		creditApplicationData.setCustNo(oracleDB.getCustomerNumber(creditApplicationData.getRepaymentSourceAccount()));
		//creditApplicationData.getReasonCode("");
		creditApplicationData.setApplicationDate("");
		//creditApplicationData.setCurrencyCode("");
		//creditApplicationData.setPrimeLimitAmount("");
		//creditApplicationData.setPortfolioCode(""); //null
		creditApplicationData.setCreditRequestDate("");				
		//creditApplicationData.setTermCode("");
		//creditApplicationData.setTermValue("");
		//creditApplicationData.setExpiryDate("");
		//creditApplicationData.setRepaymentSourceAccount("");
		//creditApplicationData.setApplicationProductCode("");
		//creditApplicationData.setOverdraftAccountNo(""); //null
		creditApplicationData.setRecordStatus("I");
		creditApplicationData.setVersionNo(1);
		creditApplicationData.setRowTimeStamp("");
		creditApplicationData.setUserId("SYSTEM");
		creditApplicationData.setCreateDate("");
		creditApplicationData.setSysCreateDate("");
		creditApplicationData.setCreatedBy("SYSTEM");
		creditApplicationData.setRequestPWD("123456789");
		creditApplicationData.setLogrepository("DB");
		creditApplicationData.setTestCallLevel(0);
		creditApplicationData.setTraceLevel(3);
		creditApplicationData.setErrorCode("");
		creditApplicationData.setErrorSeverity(0);
		creditApplicationData.setErrorMessage("");
		
		
		CreditApplicationResponse creditApplicationResponse = oracleDB.creditApplicationProcedure(creditApplicationData);
		
		String response = CommonMethods.ObjectToJsonString(creditApplicationResponse);
		System.out.println("response: \n" + response);
		
		return response;
    }
  

    
}
