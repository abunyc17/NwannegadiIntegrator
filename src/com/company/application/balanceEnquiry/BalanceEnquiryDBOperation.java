package com.company.application.balanceEnquiry;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.neptunesoftware.reuseableClasses.Database.SybaseDatabase;
import com.neptunesoftware.reuseableClasses.Database.config.ValueDatatypePair;

public class BalanceEnquiryDBOperation extends SybaseDatabase {

	public BalanceEnquiryDBOperation() {
		super();
	}
	
	public BalanceEnquiryDBOperation(String databaseName) {
		super(databaseName);
	}
		
	public BalanceEnquiryDBOperation(String driver, String connectionURL, String username, String password, String databaseType) {
		super(driver, connectionURL, username, password, databaseType);
	}
	
	public String accountBalance2(String accountNumber) {
		
		// IN parameter values in the order received by the procedure
		LinkedHashMap<Integer, ValueDatatypePair> inParam = new LinkedHashMap<Integer, ValueDatatypePair>();
		inParam.put(1,  new ValueDatatypePair(accountNumber, Types.VARCHAR));
		
		// OUT parameter values in the order received by the procedure
		LinkedHashMap<Integer, ValueDatatypePair> outParam = new LinkedHashMap<Integer, ValueDatatypePair>();
		outParam.put(2,  new ValueDatatypePair(Types.DECIMAL));
		
		// Execute procedure
		LinkedHashMap<Integer, ValueDatatypePair> resultParam = executeProcedure("csp_mapp_BalanceEnquiry", inParam, outParam);
		
		String balance = BigDecimal.ZERO + "";
		if (resultParam.get(0).getValue().equals("00")) {
			balance = resultParam.get(2).getValue().equalsIgnoreCase("null")
					? balance : resultParam.get(2).getValue();
		}

		BigDecimal availableBalance = new BigDecimal(balance).setScale(2, BigDecimal.ROUND_HALF_EVEN);
		
		return availableBalance + "";
	}
	
	
	public String accountBalance(String accountNumber) {
		String balance = executeBalanceEnquiry(accountNumber) + "";
		return balance;
	}
	
	
	private BigDecimal executeBalanceEnquiry(String accountNumber) {
		
	    // Step 1: fetch account type	
		String accountType = selectAccountType(accountNumber);
		
		// Step 2: select various balances for calculation
		String sql = "select	acct_type,\r\n" + 
				"	cur_bal,\r\n" + 
				"	col_bal,\r\n" + 
				"	hold_bal,\r\n" + 
				"	memo_dr,\r\n" + 
				"	memo_cr,\r\n" + 
				"	od_limit,\r\n" + 
				"	Float_bal_1,\r\n" + 
				"	Float_bal_2,\r\n" + 
				"	memo_float\r\n" + 
				"from	dp_display\r\n" + 
				"where	acct_no = ltrim(rtrim(?)) \r\n" + 
				"and      acct_type =  ltrim(rtrim(?))";
		
		List<ValueDatatypePair> parameter = new ArrayList<ValueDatatypePair>();
		parameter.add(new ValueDatatypePair(accountNumber, Types.VARCHAR));
		parameter.add(new ValueDatatypePair(accountType, Types.VARCHAR));
		
		// collect the result
		HashMap<Integer, HashMap<String, String>> records = executeSelect(sql, parameter);
		
		BigDecimal curBal = BigDecimal.ZERO;
		//BigDecimal colBal = BigDecimal.ZERO;
		BigDecimal holdBal = BigDecimal.ZERO;
		BigDecimal memoDR = BigDecimal.ZERO;
		BigDecimal memoCR = BigDecimal.ZERO;
		//BigDecimal odLimit = BigDecimal.ZERO;
		BigDecimal floatBal1 = BigDecimal.ZERO;
		BigDecimal floatBal2 = BigDecimal.ZERO;
		BigDecimal memoFloat = BigDecimal.ZERO;
		
		BigDecimal balance = BigDecimal.ZERO;
		if (records == null) {
			return balance;
		}
		
		for (Map.Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {			
			curBal = new BigDecimal(rowEntry.getValue().get("cur_bal".toUpperCase()));
			//colBal = new BigDecimal(rowEntry.getValue().get("col_bal".toUpperCase()));
			holdBal = new BigDecimal(rowEntry.getValue().get("hold_bal".toUpperCase()));
			memoDR = new BigDecimal(rowEntry.getValue().get("memo_dr".toUpperCase()));
			memoCR = new BigDecimal(rowEntry.getValue().get("memo_cr".toUpperCase()));
			//odLimit = new BigDecimal(rowEntry.getValue().get("od_limit".toUpperCase()));
			floatBal1 = new BigDecimal(rowEntry.getValue().get("Float_bal_1".toUpperCase()));
			floatBal2 = new BigDecimal(rowEntry.getValue().get("Float_bal_2".toUpperCase()));
			memoFloat = new BigDecimal(rowEntry.getValue().get("memo_float".toUpperCase()));		
		}
		
		
		
		// Step 3: Execute procedure kini
		int postingDef = executeCsp_get_dp_posting_def();		
		
		// Step 4: calculate  available balance based on step 3 above
		BigDecimal availableBalance = BigDecimal.ZERO;
		
		switch (postingDef) {
		case 0:
		case 1:
			availableBalance = curBal;
			break;
		case 2:
			availableBalance = curBal.subtract(floatBal1);
			break;
		case 3:
			availableBalance = curBal.subtract(floatBal2);
			break;
		case 6:
			availableBalance = curBal.add(memoCR).subtract(memoDR).subtract(memoFloat);
			break;
		case 8:
			availableBalance = curBal.subtract(floatBal1).add(memoCR).subtract(memoDR).subtract(memoFloat);
			break;
		case 9:
			availableBalance = curBal.subtract(floatBal2).add(memoCR).subtract(memoDR).subtract(memoFloat);
			break;
		}
		
		// Step 5: Execute System Hold procedure
		BigDecimal systemHold = executeCsp_get_system_hold(accountType, accountNumber);

		// Step 6: Calculate available balance
		holdBal = holdBal.add(systemHold);
		availableBalance = availableBalance.subtract(holdBal);
		
		// Step 7: Set balance to 2 decimal places		
		availableBalance = availableBalance.setScale(2, BigDecimal.ROUND_HALF_EVEN);
		
		return availableBalance;
	}
	
	private String selectAccountType(String accountNumber) {
		
		String sql = "select   acct_type from    dp_acct \r\n" + 
				" where    acct_no = ltrim(rtrim(?))";
		
		List<ValueDatatypePair> parameter = new ArrayList<ValueDatatypePair>();
		parameter.add(new ValueDatatypePair(accountNumber, Types.VARCHAR));
		
		// collect the result
		HashMap<Integer, HashMap<String, String>> records = executeSelect( sql, parameter);
		
		String accountType = "SA";
		if (records == null) {
			// Exception was thrown
			return accountType;
		}
		
		for (Map.Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {			
			accountType = rowEntry.getValue().get("acct_type".toUpperCase());
		}
		
		return accountType;
	}
	
	private int executeCsp_get_dp_posting_def() {
		
		String sql = "Select \r\n" + 
				"		AVAIL_BAL,  \r\n" + 
				"		COLL_BAL,  \r\n" + 
				"		POST_BAL  \r\n" + 
				"	From AD_DP_CONTROL";
				
		// collect the result
		HashMap<Integer, HashMap<String, String>> records = executeSelect(sql);
		
		int result = -2;
		if (records == null) {
			// Exception was thrown
			result = -1;
			return result;
		}
		
		if(records.isEmpty()) {
			return result;
		}
			
		
		String availBal = "";
		String collBal = "";
		String postBal = "";
		
		for (Map.Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {			
			availBal = rowEntry.getValue().get("AVAIL_BAL".toUpperCase());
			collBal = rowEntry.getValue().get("COLL_BAL".toUpperCase());
			postBal = rowEntry.getValue().get("POST_BAL".toUpperCase());	
		}
		
		// available balance definition
		int availBalDef = -1;
		switch (availBal.trim()) {
		case "Current - Float 1":
			availBalDef = 2;
			break;
		case "Current - Float 2":
			availBalDef = 3;
			break;
		case "Current + Memo Posted":
			availBalDef = 6;
			break;
		case "Current - Float 1 + Memo Posted": 
			availBalDef = 8;
			break;
		case "Current - Float 2 + Memo Posted":
			availBalDef = 9;
			break;
		case "Current - Holds":
		case "Current - Holds - Float 1":
		case "Current - Holds - Float 2":
		case "Current - Holds + Memo Posted":
		case "Current - Holds - Float 1 + Memo Posted":
		case "Current - Holds - Float 2 + Memo Posted":
			result = -3;
			break;
		default : availBalDef = 1;
		}
		if(result == -3) return result;
		
		
		// collected balance definition
		int collBalDef = -1;
		switch (collBal.trim()) {
		case "Current - Float 1":
			collBalDef = 2;
			break;
		case "Current - Float 2":
			collBalDef = 3;
			break;
		case "Current - Holds":
		case "Current - Holds - Float 1":
		case "Current - Holds - Float 2":
		case "Current + Memo Posted":
		case "Current - Holds + Memo Posted":
		case "Current - Float 1 + Memo Posted":
		case "Current - Float 2 + Memo Posted":
		case "Current - Holds - Float 1 + Memo Posted":
		case "Current - Holds - Float 2 + Memo Posted":
			result = -4;
			break;
		default : collBalDef = 1;
		}
		if(result == -3) return result;
		
		
		// posting balance definition
		int postingBalDef = -20;
		switch (postBal.trim()) {
		case "Available":
			postingBalDef = availBalDef;
			break;
		case "Collected":
			postingBalDef = collBalDef;
			break;
		case "Current":
		default : postingBalDef = 0;
		}
				
		return postingBalDef;
	}
	
	private BigDecimal executeCsp_get_system_hold(String accountType, String accountNumber) {
		
		String sql = "Select isnull(HOLD_PENDING_CC,'N') checker From DP_ACCT \r\n" + 
				"    Where acct_no = ? And acct_type = ?";
		
		List<ValueDatatypePair> param = new ArrayList<ValueDatatypePair>();
		param.add(new ValueDatatypePair(accountNumber, Types.VARCHAR));
		param.add(new ValueDatatypePair(accountType, Types.VARCHAR));
		
		// collect the result
		HashMap<Integer, HashMap<String, String>> records = executeSelect(sql, param);
		
		BigDecimal systemHold = BigDecimal.ZERO;
		if (records == null) {
			// Exception was thrown
			return systemHold;
		}
		
		String checkerValue = "N";		
		for (Map.Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {			
			checkerValue = rowEntry.getValue().get("checker".toUpperCase());
		}
		
		if(checkerValue.equalsIgnoreCase("Y")) {
			
			// pending cc
			sql = "Select Isnull( \r\n" + 
					"        Sum( Isnull((Select convert(decimal(21,6),0) where p.CHARGE_AMT = -1),\r\n" + 
					"        p.CHARGE_AMT) ), 0) value \r\n" + 
					"        From cc_pending p\r\n" + 
					"    Where p.acct_no = ?\r\n" + 
					"    And p.acct_type = ?";
						
			// collect the result
			records = executeSelect(sql, param);
			
			String value = "";
			for (Map.Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {			
				value = rowEntry.getValue().get("value".toUpperCase());
			}			
			systemHold = new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_EVEN).add(systemHold);
			
						
			// state tax amount
			sql = "Select Isnull(\r\n" + 
					"        Sum( Isnull((Select convert(decimal(21,6),0) where p.CHARGE_AMT = -1),\r\n" + 
					"        p.state_tax_amt) ), 0) value \r\n" + 
					"        From cc_pending p \r\n" + 
					"    Where p.acct_no = ? \r\n" + 
					"    And p.acct_type = ?";
						
			// collect the result
			records = executeSelect(sql, param);
			
			for (Map.Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {			
				value = rowEntry.getValue().get("value".toUpperCase());
			}			
			systemHold = new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_EVEN).add(systemHold);
			
			
			// local tax amount
			sql = "Select Isnull(\r\n" + 
					"        Sum( Isnull((Select convert(decimal(21,6),0) where p.CHARGE_AMT = -1),\r\n" + 
					"        p.local_tax_amt) ), 0) value  \r\n" + 
					"        From cc_pending p\r\n" + 
					"    Where p.acct_no = ? \r\n" + 
					"    And p.acct_type = ?";
						
			// collect the result
			records = executeSelect(sql, param);
			
			for (Map.Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {			
				value = rowEntry.getValue().get("value".toUpperCase());
			}			
			systemHold = new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_EVEN).add(systemHold);
						
		}
		
		// debit interest
		sql = "Select isnull(Hold_accr_dr,'N') checker From DP_ACCT_INT_OPT\r\n" + 
				"    Where acct_no = ?\r\n" + 
				"    And acct_type = ?\r\n" + 
				"    And Debit_credit = 'DR'";
		
		records = executeSelect(sql, param);
		
		for (Map.Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {			
			checkerValue = rowEntry.getValue().get("checker".toUpperCase());
		}
		
		if(checkerValue.equalsIgnoreCase("Y")) {
			sql = "Select Isnull( ACCR_DR,0) value  \r\n" + 
					"    From DP_Display \r\n" + 
					"    Where acct_no =  ? \r\n" + 
					"    And  acct_type = ?";
						
			// collect the result
			records = executeSelect(sql, param);
			
			String value = "";
			for (Map.Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {			
				value = rowEntry.getValue().get("value".toUpperCase());
			}			
			systemHold = new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_EVEN).add(systemHold);
			
		}
				
		return systemHold;
	}
	
	
	
}
