package com.neptunesoftware.ussd.FirstTimeUser;

import com.neptunesoftware.reuseableClasses.CommonMethods;
import com.neptunesoftware.reuseableClasses.ResponseConstants;
import com.neptunesoftware.reuseableClasses.ResponseModel;
import com.neptunesoftware.ussd.FirstTimeUser.data.PinDataResponse;
import com.neptunesoftware.ussd.FirstTimeUser.data.RegistrationData;
import com.neptunesoftware.ussd.FirstTimeUser.data.UserDetailsData;

public class FirstTimeService {
	
	static {
		new FirstTimeDBOperation().executeScript();
	}
	
	public  ResponseModel registerPin(String body){
		
		RegistrationData data = (RegistrationData) CommonMethods.JSONStringToObject(body, RegistrationData.class);
				
		return registerPin(data);
	}
	
	public  ResponseModel registerPin(RegistrationData data){
		
		ResponseModel responseModel = new ResponseModel(ResponseConstants.SUCCEESS_CODE,
				ResponseConstants.SUCCEESS_MESSAGE);
		
		if(!data.getPin1().equals(data.getPin2())) {
			responseModel.setResponseCode(ResponseConstants.MISMATCH_CODE);
			responseModel.setResponseMessage(ResponseConstants.MISMATCH_MESSAGE);
			
			return responseModel;
		}
		
		if(!(new FirstTimeDBOperation().savePin(data))) {
			responseModel.setResponseCode(ResponseConstants.QUERY_CODE);
			responseModel.setResponseMessage(ResponseConstants.QUERY_MESSAGE);
		}
		
		return responseModel;
	}
	
	
	public ResponseModel newCustomer(String body){
		//saves details of new customer
		
		UserDetailsData data = (UserDetailsData) CommonMethods.JSONStringToObject(body, UserDetailsData.class);
		
		return newCustomer(data);	
	}
	
	public ResponseModel newCustomer(UserDetailsData data){
		//saves details of new customer
		ResponseModel responseModel = new ResponseModel(ResponseConstants.SUCCEESS_CODE,
				ResponseConstants.SUCCEESS_MESSAGE);
		
		if(!(new FirstTimeDBOperation().saveNewCustomer(data))) {
			responseModel.setResponseCode(ResponseConstants.QUERY_CODE);
			responseModel.setResponseMessage(ResponseConstants.QUERY_MESSAGE);
		}
		
		return responseModel;		
	}
	
	public PinDataResponse getPin(String mobileNumber) {
		return new FirstTimeDBOperation().fetchUssdUserPin(mobileNumber);
	}
	
	

}
