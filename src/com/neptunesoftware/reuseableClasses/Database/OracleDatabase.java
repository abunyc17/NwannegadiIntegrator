package com.neptunesoftware.reuseableClasses.Database;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.neptunesoftware.reuseableClasses.Database.config.Database;
import com.neptunesoftware.reuseableClasses.Database.config.ValueDatatypePair;

public class OracleDatabase extends BaseDatabase {
	
	public OracleDatabase() {
		super();		
	}
	
	public OracleDatabase(final String databaseAliasOrUsername) {
		super(databaseAliasOrUsername);		
	}
	
	public OracleDatabase(final String driver, final String connectionURL, final String username, final String password, final String databaseType) {
		super(driver, connectionURL, username, password, databaseType);
	}
	

	@Override
	public String selectProcessingDate() {
		// default date
		String processingDate = "01/01/1900";
		
		// select query
		String query = "SELECT to_char(to_date(DISPLAY_VALUE, 'DD/MM/YYYY'), 'DD-MM-YYYY') processingDate FROM CTRL_PARAMETER WHERE PARAM_CD = ?";
		
		// input parameters to the query
		List<ValueDatatypePair> params = new ArrayList<ValueDatatypePair>();
		params.add(new ValueDatatypePair("S65", Types.VARCHAR));
		
		// collect the result
		HashMap<Integer, HashMap<String, String>> records = executeSelect(query, params);
		
		// Null is returned when an exception is thrown.
		// Map is empty when no record is returned from query
		if(records == null) {
			return processingDate;
		}
				
		// Loop through each row returned
		for(Map.Entry<Integer, HashMap<String, String>> rowEntrySet : records.entrySet()) {
			// collect the column Map and access its value by column alias/ name as used in the query
			processingDate = rowEntrySet.getValue().get("processingDate".toUpperCase()); // processingDate
		}
				
		return processingDate;
	}

	@Override
	public Database readConfig() {
		/* To read database config file containing database credentials*/
		return DatabaseUtil.readConfig("OracleDatabaseInfo.xml");
	}
	
	
	
	public static void main(String [] args) {
		new OracleDatabase();
		//System.out.println(new OracleDatabase().selectProcessingDate());
		System.out.println(OracleDatabase.tableExist("account"));
	}
	
	
}
