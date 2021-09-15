package com.neptunesoftware.ussd.FirstTimeUser;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.neptunesoftware.reuseableClasses.ResponseConstants;
import com.neptunesoftware.reuseableClasses.Database.SybaseDatabase;
import com.neptunesoftware.reuseableClasses.Database.config.ValueDatatypePair;
import com.neptunesoftware.ussd.FirstTimeUser.data.PinDataResponse;
import com.neptunesoftware.ussd.FirstTimeUser.data.RegistrationData;
import com.neptunesoftware.ussd.FirstTimeUser.data.UserDetailsData;

public class FirstTimeDBOperation extends SybaseDatabase {

	public void executeScript() {
		
		if (!tableExist("USSD_PIN")) {
			
			String query = "CREATE TABLE USSD_PIN (\r\n" + 
					"	 ACCT_NO VARCHAR(15), \r\n" +
					"    MOBILE_NO VARCHAR(15), \r\n" + 
					"    PIN_1 VARCHAR(10),\r\n" + 
					"    PIN_2 VARCHAR(10),\r\n" + 
					"    ENQUIRY_DT DATETIME DEFAULT getdate()\r\n" + 
					")";
			
			isDatabaseObjectCreated(query);
		}
		
		if (!tableExist("USSD_USER")) {
			
			String query = "CREATE TABLE USSD_USER(\r\n" + 
					"    first_name VARCHAR(25),\r\n" + 
					"    last_name VARCHAR(25),\r\n" + 
					"    gender CHAR(1),\r\n" + 
					"    mobile_no VARCHAR(15),\r\n" + 
					"    ENQUIRY_DT DATETIME DEFAULT getdate()\r\n" + 
					")";
			
			isDatabaseObjectCreated(query);
		}
	}
	
	public boolean savePin(RegistrationData data) {
	
		boolean response = false;
		
			String sql= "INSERT INTO USSD_PIN (acct_no, mobile_no, pin_1, pin_2) values (?,?,?,?)";
			
			List<ValueDatatypePair> inputParameter = new ArrayList<ValueDatatypePair>();
			inputParameter.add(new ValueDatatypePair(data.getAccountNumber(), Types.VARCHAR));
			inputParameter.add(new ValueDatatypePair(data.getMobileNumber(), Types.VARCHAR));
			inputParameter.add(new ValueDatatypePair(data.getPin1(), Types.VARCHAR));
			inputParameter.add(new ValueDatatypePair(data.getPin2(), Types.VARCHAR));
			
			response = executeDML(sql, inputParameter) > 0 ? true: false;
		
		return response;		
	}
	
	public boolean saveNewCustomer(UserDetailsData data){
		
		boolean response = false;
		
		String sql = "INSERT INTO USSD_USER(first_name, last_name, gender, mobile_no) values (?,?,?,?)";
		
		List<ValueDatatypePair> inputParameter = new ArrayList<ValueDatatypePair>();
		inputParameter.add(new ValueDatatypePair(data.getFirstName(), Types.VARCHAR));
		inputParameter.add(new ValueDatatypePair(data.getLastName(), Types.VARCHAR));
		inputParameter.add(new ValueDatatypePair(data.getGender(), Types.VARCHAR));
		inputParameter.add(new ValueDatatypePair(data.getContact(), Types.VARCHAR));
		
		response = executeDML(sql, inputParameter) > 0 ? true: false;
		
		return response;
	}

	public PinDataResponse fetchUssdUserPin(String mobileNumber) {
		
		String query = "SELECT acct_no,pin_1 FROM USSD_PIN  WHERE mobile_no = ?";
		
		List<ValueDatatypePair> inputParameter = new ArrayList<ValueDatatypePair>();
		inputParameter.add(new ValueDatatypePair(mobileNumber, Types.VARCHAR));
		
		HashMap<Integer, HashMap<String, String>> records = executeSelect(query, inputParameter);
		
		PinDataResponse response = new PinDataResponse();
		response.setResponseCode(ResponseConstants.NOT_FOUND_CODE);
		response.setResponseMessage(ResponseConstants.NOT_FOUND_MESSAGE);
		
		if(records == null) {
			return response;
		}
		
		for (Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {
			
			response.setPin(rowEntry.getValue().get("pin_1".toUpperCase()));
			
			response.setResponseCode(ResponseConstants.SUCCEESS_CODE);
			response.setResponseMessage(ResponseConstants.SUCCEESS_MESSAGE);
		}
		
		return response;
	}
	
}
