package com.company.application.airtimeRecharge;

import com.neptunesoftware.reuseableClasses.Quickteller.PostingDB.CommonPostingDBSybase;

public class AirtimeRechargeDBOperation extends CommonPostingDBSybase{

	public AirtimeRechargeDBOperation() {
		super();
	}
	
	public AirtimeRechargeDBOperation(String databaseName) {
		super(databaseName);
	}
		
	public AirtimeRechargeDBOperation(String driver, String connectionURL, String username, String password, String databaseType) {
		super(driver, connectionURL, username, password, databaseType);
	}
	
	
	
	
}
