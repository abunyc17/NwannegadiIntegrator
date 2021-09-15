package com.company.application.newCustomer;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.company.application.account.data.MiniStatement;
import com.company.application.newCustomer.data.CustomerData;
import com.neptunesoftware.reuseableClasses.ResponseConstants;
import com.neptunesoftware.reuseableClasses.Database.SybaseDatabase;
import com.neptunesoftware.reuseableClasses.Database.config.ValueDatatypePair;

public class NewCustomerDBOperation extends SybaseDatabase{

	public NewCustomerDBOperation() {
		super();
	}
	
	public NewCustomerDBOperation(final String databaseName) {
		super(databaseName);
	}
		
	public NewCustomerDBOperation(final String driver, final String connectionURL, final String username, final String password, final String databaseType) {
		super(driver, connectionURL, username, password, databaseType);
	}
	
	
	public CustomerData getCustomerInfo(final String accountNumber) {
					
		String query = "select cust_id, cust_no, cust_nm from customer \r\n"
				+ "where cust_id = (select cust_id from account where acct_no = ?)";

		// input parameters in the order needed in the query
		List<ValueDatatypePair> params = new ArrayList<ValueDatatypePair>();
		params.add(new ValueDatatypePair(accountNumber, Types.VARCHAR));

		// collect the result
		HashMap<Integer, HashMap<String, String>> records = executeSelect(query, params);


		// Null is returned when an exception is thrown.
		if (records == null) {
			return null;
		}

		CustomerData customerData = new CustomerData();
		customerData.setResponseCode(ResponseConstants.NOT_FOUND_CODE);
		customerData.setResponseMessage(ResponseConstants.NOT_FOUND_MESSAGE);
		
		// Loop through each row returned
		for (Map.Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {
			// collect the columns and access its value by
			// first the column alias or column names as returned by the query

			customerData.setCustomerId(rowEntry.getValue().get("cust_id".toUpperCase()));
			customerData.setCustomerNumber(rowEntry.getValue().get("cust_no".toUpperCase()));
			customerData.setCustomerName(rowEntry.getValue().get("cust_nm".toUpperCase()));

			customerData.setResponseCode(ResponseConstants.SUCCEESS_CODE);
			customerData.setResponseMessage(ResponseConstants.SUCCEESS_MESSAGE);

			MiniStatement miniStatement = new MiniStatement();
			miniStatement.setDate(rowEntry.getValue().get("DATE".toUpperCase()));
			miniStatement.setCreditDebit(rowEntry.getValue().get("CR_DR".toUpperCase()));
			miniStatement.setAmount(rowEntry.getValue().get("amount".toUpperCase()));
			miniStatement.setRefNo(rowEntry.getValue().get("REF_NO".toUpperCase()));

		}

		return customerData;
		
	}
	
	
	
}
