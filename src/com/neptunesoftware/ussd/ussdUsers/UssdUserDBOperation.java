package com.neptunesoftware.ussd.ussdUsers;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.neptunesoftware.reuseableClasses.ResponseConstants;
import com.neptunesoftware.reuseableClasses.Database.SybaseDatabase;
import com.neptunesoftware.reuseableClasses.Database.config.ValueDatatypePair;
import com.neptunesoftware.ussd.ussdUsers.data.AccountDataResponse;
import com.neptunesoftware.ussd.ussdUsers.data.MiniStatement;
import com.neptunesoftware.ussd.ussdUsers.data.MiniStatementResponse;
import com.neptunesoftware.ussd.ussdUsers.data.RegistrationDataResponse;

public class UssdUserDBOperation extends SybaseDatabase {

	public RegistrationDataResponse fetchUssdUser(String mobileNumber) {
		
		String query = "SELECT acct_no,pin_1 FROM USSD_PIN  WHERE mobile_no = ?";
		
		List<ValueDatatypePair> inputParameter = new ArrayList<ValueDatatypePair>();
		inputParameter.add(new ValueDatatypePair(mobileNumber, Types.VARCHAR));
		
		HashMap<Integer, HashMap<String, String>> records = executeSelect(query, inputParameter);
		
		RegistrationDataResponse response = new RegistrationDataResponse();
		response.setResponseCode(ResponseConstants.NOT_FOUND_CODE);
		response.setResponseMessage(ResponseConstants.NOT_FOUND_MESSAGE);
		
		if(records == null) {
			return response;
		}
		
		for (Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {
			
			response.setAccountNumber(rowEntry.getValue().get("acct_no".toUpperCase()));
			response.setMobileNumber(mobileNumber);
			response.setPin(rowEntry.getValue().get("pin_1".toUpperCase()));
			
			response.setResponseCode(ResponseConstants.SUCCEESS_CODE);
			response.setResponseMessage(ResponseConstants.SUCCEESS_MESSAGE);
		}
		
		return response;
	}

	public AccountDataResponse fetchCustomerAccount(String mobileNumber){
		//first check for account associated with the mobile number

//		String query = "SELECT A.acct_nm, A.acct_no, B.contact "
//				+ "FROM account A "
//				+ "JOIN customer_contact_mode B "
//				+"ON A.cust_id = B.cust_id WHERE B.contact = ? AND A.PROD_CAT_TY = 'DP'";
		
		String query = "select 	a.acct_no, a.title_1 acct_nm, (case upper(rtrim(ltrim(a.status))) when 'ACTIVE' then 'A' else 'I' end) status, \r\n" + 
				"rtrim(ltrim(a.acct_type))acct_type, b.cur_bal, c.iso_code, d.phone_3 contact, e.accesspin\r\n" + 
				"from 	dp_acct a\r\n" + 
				"join dp_display b on a.acct_no = b.acct_no and	a.acct_type = b.acct_type\r\n" + 
				" and	a.rim_no = b.rim_no\r\n" + 
				"join ad_gb_crncy c on b.crncy_id = c.crncy_id\r\n" + 
				"join rm_address d on b.rim_no = d.rim_no and	d.addr_id = 1\r\n" + 
				"left join alt_mapp_device e on a.acct_no = e.acct_num\r\n" + 
				"where 	where d.phone_3 = ? and upper(rtrim(ltrim(a.status))) = 'ACTIVE'";
		
		
		List<ValueDatatypePair> inputParameter = new ArrayList<ValueDatatypePair>();
		inputParameter.add(new ValueDatatypePair(mobileNumber, Types.VARCHAR));
		
		HashMap<Integer, HashMap<String, String>> records = executeSelect(query, inputParameter);
		
		AccountDataResponse response = new AccountDataResponse();
		response.setResponseCode(ResponseConstants.NOT_FOUND_CODE);
		response.setResponseMessage(ResponseConstants.NOT_FOUND_MESSAGE);
		
		if(records == null) {
			return response;
		}
		
		List<String> accountNumberLst = new ArrayList<String>();
		for (Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {
			
			accountNumberLst.add(rowEntry.getValue().get("acct_no".toUpperCase()));
			
			response.setAccountName(rowEntry.getValue().get("acct_nm".toUpperCase()));
			response.setContact(rowEntry.getValue().get("contact".toUpperCase()));
			
			response.setResponseCode(ResponseConstants.SUCCEESS_CODE);
			response.setResponseMessage(ResponseConstants.SUCCEESS_MESSAGE);
		}
		
		response.setAccountNumber(accountNumberLst);
		
		return response;		
	}
	
	public MiniStatementResponse miniStatement(String accountNumber) {
		
		String query = "SELECT to_char(sys_create_ts, 'DD/MM/YYYY') \"DATE\", dr_cr_ind, txn_amt,tran_ref_txt\r\n" + 
				"FROM deposit_account_history\r\n" + 
				"WHERE acct_no = ? \r\n" + 
				"AND to_char(sys_create_ts, 'MM') = to_char(TO_DATE(sysdate, 'DD/MM/YYYY'), 'MM')";
		
		List<ValueDatatypePair> inputParameter = new ArrayList<ValueDatatypePair>();
		inputParameter.add(new ValueDatatypePair(accountNumber, Types.VARCHAR));
		
		HashMap<Integer, HashMap<String, String>> records = executeSelect(query, inputParameter);
		
		MiniStatementResponse response = new MiniStatementResponse();
		response.setResponseCode(ResponseConstants.NOT_FOUND_CODE);
		response.setResponseMessage(ResponseConstants.NOT_FOUND_MESSAGE);
		
		if(records == null) {
			return response;
		}
		
		List<MiniStatement> miniDataLst = new ArrayList<MiniStatement>();
		for (Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {
			MiniStatement miniData = new MiniStatement();
			
			miniData.setTransactionDate(rowEntry.getValue().get("DATE".toUpperCase()));
			miniData.setTransactionType(rowEntry.getValue().get("dr_cr_ind".toUpperCase()));
			miniData.setTransactionAmount(rowEntry.getValue().get("txn_amt".toUpperCase()));
			miniData.setTransactionRefNo(rowEntry.getValue().get("tran_ref_txt".toUpperCase()));
			
			miniDataLst.add(miniData);
			
			response.setResponseCode(ResponseConstants.SUCCEESS_CODE);
			response.setResponseMessage(ResponseConstants.SUCCEESS_MESSAGE);
		}
		
		response.setTransactions(miniDataLst);
		
		return response;
	}
	
	public String totalDailyTransaction(String accountNumber) {
		
		String query = "select nvl(sum(tran_amount), 0) total\r\n" + 
				"from alt_quickteller\r\n" + 
				"where upper(tran_nethod) in ('INTERNALFUNDTRANSFER', 'EXTERNALFUNDTRANSFER')\r\n" + 
				"and upper(tran_appl) = 'MAPP' and to_char(system_ts, 'MM') = to_char(TO_DATE(sysdate, 'DD/MM/YYYY'), 'MM')\r\n" + 
				"and charge_amount != 0 " +
				"and tran_status in ('90000', '90010', '90011', '90016', '90009', '900A0', '70022', '10001', 'E18', '90E18', 'E20', '90E20', 'E21', '90E21') " +
				" and from_acct_num = ?";
		
		List<ValueDatatypePair> inputParameter = new ArrayList<ValueDatatypePair>();
		inputParameter.add(new ValueDatatypePair(accountNumber, Types.VARCHAR));
		
		HashMap<Integer, HashMap<String, String>> records = executeSelect(query, inputParameter);
		
		String totalAmount = "0";
		if(records == null) {
			return totalAmount;
		}		
		
		for (Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {
			totalAmount = rowEntry.getValue().get("total".toUpperCase());
		}
		
		return totalAmount;
	}
	
}
