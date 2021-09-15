package com.neptunesoftware.ussd.ussdUsers;

import java.math.BigDecimal;

import com.company.application.airtimeRecharge.AirtimeRechargeService;
import com.company.application.airtimeRecharge.data.AirtimeRechargeRequest;
import com.company.application.fundTransfer.FundTransferService;
import com.company.application.fundTransfer.data.ExternalFTRequest;
import com.company.application.fundTransfer.data.InternalFTRequest;
import com.neptunesoftware.reuseableClasses.CommonMethods;
import com.neptunesoftware.ussd.ussdUsers.data.AccountDataResponse;
import com.neptunesoftware.ussd.ussdUsers.data.MiniStatementResponse;
import com.neptunesoftware.ussd.ussdUsers.data.RegistrationDataResponse;

public class UssdUserService {
	
	public RegistrationDataResponse validateUssdUser(String mobileNumber) {		
		return new UssdUserDBOperation().fetchUssdUser(mobileNumber);
	}
	
	public AccountDataResponse validateCustomerAccount(String mobileNumber) {		
		return new UssdUserDBOperation().fetchCustomerAccount(mobileNumber);
	}
	
	public MiniStatementResponse miniStatement(String accountNumber) {
		return new UssdUserDBOperation().miniStatement(accountNumber);
	}
	
	public String internalFundTransfer(String body) {
		
		InternalFTRequest internalTransfer = (InternalFTRequest) CommonMethods.JSONStringToObject(body, InternalFTRequest.class);
		internalTransfer.setInitiatingApp("USSD");
		
//		if (isTransactionExceeded(internalTransfer.getFromAccountNumber(), internalTransfer.getTransactionAmount())) {
//			ResponseModel responseModel =  new ResponseModel(ResponseConstants.LIMIT_CODE, ResponseConstants.LIMIT_MESSAGE);
//			return CommonMethods.ObjectToJsonString(responseModel);
//		}
		
		return new FundTransferService().InternalFundTransfer(CommonMethods.ObjectToJsonString(internalTransfer));
	}
	
	public String externalFundTransfer(String body) {
		
		ExternalFTRequest externalTransfer = (ExternalFTRequest) CommonMethods.JSONStringToObject(body, ExternalFTRequest.class);
		externalTransfer.setInitiatingApp("USSD");
		
//		if (isTransactionExceeded(externalTransfer.getFromAccountNumber(), externalTransfer.getTransactionAmount())) {
//			ResponseModel responseModel =  new ResponseModel(ResponseConstants.LIMIT_CODE, ResponseConstants.LIMIT_MESSAGE);
//			return CommonMethods.ObjectToJsonString(responseModel);
//		}
		
		return new FundTransferService().ExternalFundTransfer(CommonMethods.ObjectToJsonString(externalTransfer));
	}

	public String airtimeRecharge(String body) {
		
		AirtimeRechargeRequest airtimeRchgeXfer = (AirtimeRechargeRequest) CommonMethods.JSONStringToObject(body, AirtimeRechargeRequest.class);
		airtimeRchgeXfer.setInitiatingApp("USSD");
		
		return new AirtimeRechargeService().AirtimeRecharge(CommonMethods.ObjectToJsonString(airtimeRchgeXfer));
	}
	
	
	
	@SuppressWarnings("unused")
	private boolean isTransactionExceeded(String accountNumber, String txnAmount) {
		BigDecimal DAILY_TRANSACTION_LIMIT = new BigDecimal("100000");
		
		BigDecimal dailyTransactionAlreadyDone = new BigDecimal(new UssdUserDBOperation().totalDailyTransaction(accountNumber));
		
		BigDecimal totalDailyTransactionDone = dailyTransactionAlreadyDone.add(new BigDecimal(txnAmount));
		
		boolean result = false;
		if(totalDailyTransactionDone.compareTo(DAILY_TRANSACTION_LIMIT) >= 0) {
			result = true;
		}
		
		return result;
	}
	
}
