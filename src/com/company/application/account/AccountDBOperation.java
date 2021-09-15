package com.company.application.account;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.company.application.account.data.AccountHistory;
import com.company.application.account.data.AccountResponse;
import com.company.application.account.data.Beneficiary;
import com.company.application.account.data.MiniStatement;
import com.company.application.account.data.MultiAccountResponse;
import com.company.application.balanceEnquiry.BalanceEnquiryDBOperation;
import com.neptunesoftware.reuseableClasses.CommonMethods;
import com.neptunesoftware.reuseableClasses.ResponseConstants;
import com.neptunesoftware.reuseableClasses.ResponseModel;
import com.neptunesoftware.reuseableClasses.Database.SybaseDatabase;
import com.neptunesoftware.reuseableClasses.Database.config.ValueDatatypePair;

public class AccountDBOperation extends SybaseDatabase {

	public AccountDBOperation() {
		super();
	}

	public AccountDBOperation(final String databaseName) {
		super(databaseName);
		
	}

	public AccountDBOperation(final String driver, final String connectionURL, final String username,
			final String password, final String databaseType) {
		super(driver, connectionURL, username, password, databaseType);
	}

	public List<MiniStatement> selectMiniStatement(final String accountNo) {

		String sql = "SELECT TO_CHAR(SYS_CREATE_TS, 'DD/MM/YYYY') \"DATE\", DR_CR_IND CR_DR, TXN_AMT AMOUNT, TRAN_REF_TXT REF_NO \r\n"
				+ "FROM DEPOSIT_ACCOUNT_HISTORY \r\n"
				+ "WHERE TO_CHAR(SYS_CREATE_TS, 'MM') = TO_CHAR(SYSDATE, 'MM') \r\n"
				+ "AND DEPOSIT_ACCT_ID = (SELECT ACCT_ID FROM ACCOUNT WHERE ACCT_NO = ?)";

		// input parameters in the order needed in the query
		List<ValueDatatypePair> params = new ArrayList<ValueDatatypePair>();
		params.add(new ValueDatatypePair(accountNo, Types.VARCHAR));

		// collect the result
		HashMap<Integer, HashMap<String, String>> records = executeSelect(sql, params);

		// Null is returned when an exception is thrown.
		if (records == null) {
			return null;
		}

		List<MiniStatement> miniStatementLst = new ArrayList<MiniStatement>();

		// Loop through each row returned
		for (Map.Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {
			// collect the columns and access its value by
			// first the column alias or column names as returned by the query

			MiniStatement miniStatement = new MiniStatement();
			miniStatement.setDate(rowEntry.getValue().get("DATE".toUpperCase()));
			miniStatement.setCreditDebit(rowEntry.getValue().get("CR_DR".toUpperCase()));
			miniStatement.setAmount(rowEntry.getValue().get("amount".toUpperCase()));
			miniStatement.setRefNo(rowEntry.getValue().get("REF_NO".toUpperCase()));

			miniStatementLst.add(miniStatement);
		}

		return miniStatementLst;
	}

	public List<AccountResponse> selectMultiAccount2(final String accountNo) {

		// IN parameter values in the order received by the procedure
		LinkedHashMap<Integer, ValueDatatypePair> inParam = new LinkedHashMap<Integer, ValueDatatypePair>();
		inParam.put(1, new ValueDatatypePair(accountNo, Types.VARCHAR));

		// Execute procedure
		HashMap<Integer, HashMap<String, String>> records = executeProcedureReturningSelectQuery(
				"csp_mapp_MultiAccountInfo", inParam);

		// Null is returned when an exception is thrown.
		if (records == null) {
			return null;
		}

		List<AccountResponse> accounts = new ArrayList<AccountResponse>();

		// Loop through each row returned
		for (int rowIndex = 1; rowIndex <= records.size(); rowIndex++) {
			// collect the columns and access its value by
			// first the column alias or column names as returned by the query

			AccountResponse accountResponse = new AccountResponse();

			accountResponse.setAccountNumber(records.get(rowIndex).get("acct_no".toUpperCase()));
			accountResponse.setAccountName(records.get(rowIndex).get("title_1".toUpperCase()));

			String accountStatus = records.get(rowIndex).get("acct_status".toUpperCase());
			accountStatus = accountStatus.trim().toUpperCase().equals("ACTIVE") ? "A" : "I";

			accountResponse.setAccountStatus(accountStatus);
			accountResponse.setAccountType(records.get(rowIndex).get("acct_type".toUpperCase()));
			
			String balance = records.get(rowIndex).get("avail_bal".toUpperCase());
			BigDecimal availableBalance  = new BigDecimal(balance).setScale(2, BigDecimal.ROUND_HALF_EVEN);
			accountResponse.setLedgerBalance(availableBalance+"");
			
			accountResponse.setCurrencyCode(records.get(rowIndex).get("crncy_iso_code".toUpperCase()));
			accountResponse.setPhoneNumber(records.get(rowIndex).get("phone_no".toUpperCase()));
			accountResponse.setAccessPin(records.get(rowIndex).get("access_pin".toUpperCase()));

			accounts.add(accountResponse);
		}

		return accounts;
	}

	public List<AccountHistory> selectAccountHistory(final String accountNo, final String startDate,
			final String endDate) {

		// IN parameter values in the order received by the procedure
		LinkedHashMap<Integer, ValueDatatypePair> inParam = new LinkedHashMap<Integer, ValueDatatypePair>();
		inParam.put(1, new ValueDatatypePair(accountNo, Types.VARCHAR));
		inParam.put(2, new ValueDatatypePair(startDate, Types.VARCHAR));
		inParam.put(3, new ValueDatatypePair(endDate, Types.VARCHAR));

		// Execute procedure
		HashMap<Integer, HashMap<String, String>> records = executeProcedureReturningSelectQuery(
				"csp_mapp_AccountHistory", inParam);

		// Null is returned when an exception is thrown.
		if (records == null) {
			return null;
		}

		List<AccountHistory> accountHistory = new ArrayList<AccountHistory>();

		// Loop through each row returned
		for (Map.Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {
			// collect the columns and access its value by
			// first the column alias or column names as returned by the query

			String transactionAmountTemp = rowEntry.getValue().get("tran_amount".toUpperCase());
			String balanceAfterTemp = rowEntry.getValue().get("balance".toUpperCase());
			
			BigDecimal transactionAmount = new BigDecimal(transactionAmountTemp)
					.setScale(2, BigDecimal.ROUND_HALF_EVEN);

			BigDecimal balanceAfter = new BigDecimal(balanceAfterTemp).setScale(2, BigDecimal.ROUND_HALF_EVEN);

			String transactionDate = CommonMethods
					.getCurrentDateAsString(rowEntry.getValue().get("create_dt".toUpperCase()), "yyyy-MM-dd");

			
			AccountHistory acctHist = new AccountHistory();

			acctHist.setAccountType(rowEntry.getValue().get("acct_type".toUpperCase()));
			acctHist.setTransactionDesc(rowEntry.getValue().get("tran_desc".toUpperCase()));
			acctHist.setTransactionAmount(transactionAmount+"");
			acctHist.setBalanceAfter(balanceAfter+"");
			acctHist.setTransactionDate(transactionDate);

			accountHistory.add(acctHist);
		}

		return accountHistory;

	}

	public List<Beneficiary> selectBeneficiary(final String senderAcctNo, final String moduleId) {

		String sql = "select Ben_Id, Ben_Acct_No, Ben_Acct_Name, Ben_Bank_Code, Ben_Bank_Name\r\n"
				+ "from alt_quickteller_beneficiary\r\n" + "where Sender_Acct_No = ? and Module_Id = ?";

		// input parameters in the order needed in the query
		List<ValueDatatypePair> params = new ArrayList<ValueDatatypePair>();
		params.add(new ValueDatatypePair(senderAcctNo, Types.VARCHAR));
		params.add(new ValueDatatypePair(moduleId, Types.VARCHAR));

		// collect the result
		HashMap<Integer, HashMap<String, String>> records = executeSelect(sql, params);

		// Null is returned when an exception is thrown.
		if (records == null) {
			return null;
		}

		List<Beneficiary> beneficiaries = new ArrayList<Beneficiary>();

		// must be included based on Vincent's request
		// beneficiaryList.add(new Beneficiary("0", "null", "Choose Beneficiary",
		// "null", "null"));

		// Loop through each row returned
		for (int rowIndex = 1; rowIndex <= records.size(); rowIndex++) {
			// collect the columns and access its value by
			// first the column alias or column names as returned by the query

			Beneficiary beneficiary = new Beneficiary();

			beneficiary.setBeneficiaryId(records.get(rowIndex).get("Ben_Id".toUpperCase()));
			beneficiary.setBeneficiaryAcctNo(records.get(rowIndex).get("Ben_Acct_No".toUpperCase()));
			beneficiary.setBeneficiaryAcctName(records.get(rowIndex).get("Ben_Acct_Name".toUpperCase()));
			beneficiary.setBankCode(records.get(rowIndex).get("Ben_Bank_Code".toUpperCase()));
			beneficiary.setBankName(records.get(rowIndex).get("Ben_Bank_Name".toUpperCase()));

			beneficiaries.add(beneficiary);
		}

		return beneficiaries;
	}

	public AccountResponse selectAccountInfo2(final String accountNumber) {

		String sql = "select a.acct_no, a.title_1, (case upper(rtrim(ltrim(a.status))) when 'ACTIVE' then 'A' when 'CLOSED' then 'C' when 'DORMANT' then 'D' \r\n" 
				+ "when 'INCOMPLETE' then 'I' when 'RENEWPENDING' then 'R' when 'UNFUNDED' then 'U' else 'F' end) status, \r\n"
				+ "rtrim(ltrim(a.acct_type))acct_type, b.cur_bal, c.iso_code, d.phone_3, e.accesspin\r\n"
				+ "from 	dp_acct a\r\n"
				+ "join dp_display b on a.acct_no = b.acct_no and	a.acct_type = b.acct_type\r\n"
				+ "join ad_gb_crncy c on b.crncy_id = c.crncy_id\r\n"
				+ "join rm_address d on b.rim_no = d.rim_no and	d.addr_id = 1\r\n"
				+ "left join alt_mapp_device e on a.acct_no = e.acct_num\r\n" + " where 	a.acct_no = ?";
				
		// input parameters in the order needed in the query
		List<ValueDatatypePair> params = new ArrayList<ValueDatatypePair>();
		params.add(new ValueDatatypePair(accountNumber, Types.VARCHAR));

		// collect the result
		HashMap<Integer, HashMap<String, String>> records = executeSelect(sql, params);

		// Null is returned when an exception is thrown.
		if (records == null) {
			return null;
		}

		AccountResponse accountResponse = new AccountResponse();
		accountResponse.setResponseCode(ResponseConstants.NOT_FOUND_CODE);
		accountResponse.setResponseMessage(ResponseConstants.NOT_FOUND_MESSAGE);

		// Loop through each row returned
		for (Map.Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {
			// collect the columns and access its value by
			// first the column alias or column names as returned by the query

			accountResponse.setAccountNumber(accountNumber);
			accountResponse.setAccountName(rowEntry.getValue().get("title_1".toUpperCase()));
			accountResponse.setAccountStatus(rowEntry.getValue().get("status".toUpperCase()));
			accountResponse.setAccountType(rowEntry.getValue().get("acct_type".toUpperCase()));
			
			String availableBalance = new BalanceEnquiryDBOperation().accountBalance(accountNumber);
			accountResponse.setLedgerBalance(availableBalance);
			accountResponse.setCurrencyCode(rowEntry.getValue().get("iso_code".toUpperCase()));
			accountResponse.setPhoneNumber(rowEntry.getValue().get("phone_3".toUpperCase()));
			accountResponse.setAccessPin(rowEntry.getValue().get("accesspin".toUpperCase()));

			accountResponse.setResponseCode(ResponseConstants.SUCCEESS_CODE);
			accountResponse.setResponseMessage(ResponseConstants.SUCCEESS_MESSAGE);
		}

		return accountResponse;
	}

	
	public List<AccountResponse> selectMultiAccount(final String accountNumber) {
		
		String sql = "select  acct_no,\r\n" + 
				"	acct_type,\r\n" + 
				"	title_1,\r\n" + 
				"	status, rim_no \r\n" + 
				"from  	dp_acct \r\n" + 
				"where  rim_no = (select rim_no from dp_acct where acct_no = ?)";

		// input parameters in the order needed in the query
		List<ValueDatatypePair> params = new ArrayList<ValueDatatypePair>();
		params.add(new ValueDatatypePair(accountNumber, Types.VARCHAR));

		// collect the result
		HashMap<Integer, HashMap<String, String>> records = executeSelect(sql, params);
		
		return selectAccountDetails(records).getAccounts();
	}
	
	public AccountResponse selectAccountInfo(final String accountNumber) {
		
		String sql = "select  acct_no,\r\n" + 
				"	acct_type,\r\n" + 
				"	title_1,\r\n" + 
				"	status, rim_no \r\n" + 
				"from  	dp_acct \r\n" + 
				"where  	acct_no = ?";

		// input parameters in the order needed in the query
		List<ValueDatatypePair> params = new ArrayList<ValueDatatypePair>();
		params.add(new ValueDatatypePair(accountNumber, Types.VARCHAR));

		// collect the result
		HashMap<Integer, HashMap<String, String>> records = executeSelect(sql, params);
		
		if (records == null) {
			return null;
		}
		
		AccountResponse accountResponse = new AccountResponse();
		accountResponse.setResponseCode(ResponseConstants.NOT_FOUND_CODE);
		accountResponse.setResponseMessage(ResponseConstants.NOT_FOUND_MESSAGE);
		
		accountResponse = selectAccountDetails(records)
				.getAccounts()
				.stream()
				.findFirst()
				.map(account->{
					AccountResponse acctRsp = account;
					acctRsp.setResponseCode(ResponseConstants.SUCCEESS_CODE);
					acctRsp.setResponseMessage(ResponseConstants.SUCCEESS_MESSAGE);
					
					return acctRsp;
				})
				.orElse(accountResponse);
		
		return accountResponse;
	}
	
	private MultiAccountResponse selectAccountDetails(HashMap<Integer, HashMap<String, String>> records) {
				
		MultiAccountResponse multipleAccounts = new MultiAccountResponse();
		multipleAccounts.setResponseCode(ResponseConstants.NOT_FOUND_CODE);
		multipleAccounts.setResponseMessage(ResponseConstants.NOT_FOUND_MESSAGE);
		
		List<AccountResponse> accounts = new ArrayList<AccountResponse>();
		
		if (records == null) {
			multipleAccounts.setAccounts(accounts);
			return multipleAccounts;
		}
		
		for (Map.Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {
			
			AccountResponse accountResponse = new AccountResponse();

			String accountNumber = rowEntry.getValue().get("acct_no".toUpperCase()).trim();
			String accountName = rowEntry.getValue().get("title_1".toUpperCase()).trim();
			String accountType = rowEntry.getValue().get("acct_type".toUpperCase()).trim();
			String accountStatus = rowEntry.getValue().get("status".toUpperCase()).trim();
			int rimNo = Integer.parseInt(rowEntry.getValue().get("rim_no".toUpperCase()).trim());
			
			switch(accountStatus.trim().toUpperCase()) {
				case "ACTIVE": accountStatus = "A"; break;			//Active        
				case "CLOSED": accountStatus = "C"; break;			//Closed        
				case "DORMANT": accountStatus = "D"; break;			//Dormant       
				case "INCOMPLETE": accountStatus = "I"; break;		//Incomplete    
				case "RENEWPENDING": accountStatus = "R"; break;	//RenewPending  
				case "UNFUNDED": accountStatus = "U"; break;		//Unfunded      
				default: accountStatus = "F"; break;				//INACTIVE
			}
			
			String phoneNumber = selectPhoneNumber(rimNo);
			String currencyCode = selectCurrencyCode(accountNumber, accountType);
			String accessPin = selectAccessPin(accountNumber);
			String availableBalance = new BalanceEnquiryDBOperation().accountBalance(accountNumber);
			
			
			accountResponse.setAccountNumber(accountNumber);
			accountResponse.setAccountName(accountName.trim());
			accountResponse.setAccountStatus(accountStatus.trim());
			accountResponse.setAccountType(accountType.trim());
			accountResponse.setLedgerBalance(availableBalance.trim());
			accountResponse.setCurrencyCode(currencyCode.trim());
			accountResponse.setPhoneNumber(phoneNumber.trim());
			accountResponse.setAccessPin(accessPin.trim());
			

			accounts.add(accountResponse);
			
			multipleAccounts.setResponseCode(ResponseConstants.SUCCEESS_CODE);
			multipleAccounts.setResponseMessage(ResponseConstants.SUCCEESS_MESSAGE);
		}
		
		multipleAccounts.setAccounts(accounts);
		return multipleAccounts;
	}
	
	private String selectPhoneNumber(final int rimNo) {

		String sql = "select	phone_3 \r\n" + 
				"from	rm_address \r\n" + 
				"where	rim_no = ? \r\n" + 
				"and	addr_id = 1";

		// input parameters in the order needed in the query
		List<ValueDatatypePair> params = new ArrayList<ValueDatatypePair>();
		params.add(new ValueDatatypePair(rimNo, Types.INTEGER));

		// collect the result
		HashMap<Integer, HashMap<String, String>> records = executeSelect(sql, params);
		
		String phoneNumber = "";
		if (records == null) {
			return phoneNumber;
		}
		
		// Loop through each row returned
		for (Map.Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {
			phoneNumber = rowEntry.getValue().get("phone_3".toUpperCase());
		}

		return phoneNumber;
	}

	private String selectCurrencyCode(final String accountNumber, final String accountType) {

		// select currency id
		String sql = "select	crncy_id \r\n" + 
				"from  dp_display	\r\n" + 
				"where	acct_no = ? \r\n" + 
				"and	acct_type = ?";

		// input parameters in the order needed in the query
		List<ValueDatatypePair> params = new ArrayList<ValueDatatypePair>();
		params.add(new ValueDatatypePair(accountNumber, Types.VARCHAR));
		params.add(new ValueDatatypePair(accountType, Types.VARCHAR));

		// collect the result
		HashMap<Integer, HashMap<String, String>> records = executeSelect(sql, params);
		
		String currencyCode = "";
		if (records == null) {
			return currencyCode;
		}
		
		int currencyId = -1;
		// Loop through each row returned
		for (Map.Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {
			currencyId = Integer.parseInt(rowEntry.getValue().get("crncy_id".toUpperCase()));
		}

		
		// select currency code
		sql = "select	iso_code\r\n" + 
				"	from	ad_gb_crncy\r\n" + 
				"	where	crncy_id = ?";

		// input parameters in the order needed in the query
		List<ValueDatatypePair> parameter = new ArrayList<ValueDatatypePair>();
		parameter.add(new ValueDatatypePair(currencyId, Types.INTEGER));
		
		records = executeSelect(sql, parameter);
		
		if (records == null) {
			return currencyCode;
		}
		
		// Loop through each row returned
		for (Map.Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {
			currencyCode = rowEntry.getValue().get("iso_code".toUpperCase());
		}
		
		return currencyCode;
	}
	
	private String selectAccessPin(final String accountNumber) {

		// select currency id
		String sql = "select	accesspin \r\n" + 
				"	from	alt_mapp_device\r\n" + 
				"	where	acct_num = ?";

		// input parameters in the order needed in the query
		List<ValueDatatypePair> params = new ArrayList<ValueDatatypePair>();
		params.add(new ValueDatatypePair(accountNumber, Types.VARCHAR));

		// collect the result
		HashMap<Integer, HashMap<String, String>> records = executeSelect(sql, params);
		
		String accessPin = "";
		if (records == null) {
			return accessPin;
		}
		
		// Loop through each row returned
		for (Map.Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {
			accessPin = rowEntry.getValue().get("accesspin".toUpperCase());
		}
		
		return accessPin;
	}
	
	public String selectEndDate() {

		String sql = "select dateadd(day,1,last_to_dt) endDate from ov_control";
		
		// Execute procedure
		HashMap<Integer, HashMap<String, String>> records = executeSelect(sql);

		String endDate = CommonMethods.getCurrentDateAsString("", "yyyy-MM-dd");
		if (records == null) {
			return endDate;
		}
		
		for (Map.Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {
			endDate = rowEntry.getValue().get("endDate".toUpperCase()).trim();;
		}

		return endDate;
	}
	
	public String selectStartDate(String endDate) {

		String sql = "select	start_dt from	gl_calendar_period \r\n" + 
				"where convert(datetime, ?) between start_dt and end_dt";
		
		// input parameters in the order needed in the query
		List<ValueDatatypePair> params = new ArrayList<ValueDatatypePair>();
		params.add(new ValueDatatypePair(endDate, Types.VARCHAR));
				
		// Execute procedure
		HashMap<Integer, HashMap<String, String>> records = executeSelect(sql, params);

		String startDate = CommonMethods.getCurrentDateAsString("", "yyyy-MM-dd");
		if (records == null) {
			return startDate;
		}
		
		for (Map.Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {
			startDate = rowEntry.getValue().get("start_dt".toUpperCase()).trim();;
		}

		return startDate;
	}
	
	
	
	public String selectDailyTranxDone(final String accountNo, final String transactionMethod) {

		String sql = "select isnull(sum(tran_amount),0) txn_done from alt_quickteller\r\n"
				+ "where from_acct_num = ? and tran_nethod = ? \r\n"
				+ "and convert(varchar, system_ts, 105) = convert(varchar, getdate(), 105)";

		// input parameters in the order needed in the query
		List<ValueDatatypePair> params = new ArrayList<ValueDatatypePair>();
		params.add(new ValueDatatypePair(accountNo, Types.VARCHAR));
		params.add(new ValueDatatypePair(transactionMethod, Types.VARCHAR));

		// collect the result
		HashMap<Integer, HashMap<String, String>> records = executeSelect(sql, params);

		// Null is returned when an exception is thrown.
		if (records == null) {
			return null;
		}

		String doneTransaction = "0";

		// Loop through each row returned
		for (int rowIndex = 1; rowIndex <= records.size(); rowIndex++) {
			// collect the columns and access its value by
			// first the column alias or column names as returned by the query

			doneTransaction = records.get(rowIndex).get("txn_done".toUpperCase());
		}

		return doneTransaction;
	}

	public String accountName(final String accountNo) {

		// String sql = "select acct_no, acct_nm from account \r\n" + "where acct_no =
		// ?";
		String sql = "select b.acct_no, a.cbn_bvn_no bvn, b.title_1 acct_nm \r\n" + "from rm_acct a, dp_acct b \r\n"
				+ "where a.rim_no = b.rim_no \r\n" + "and acct_no = ?";

		// input parameters in the order needed in the query
		List<ValueDatatypePair> params = new ArrayList<ValueDatatypePair>();
		params.add(new ValueDatatypePair(accountNo, Types.VARCHAR));

		// collect the result
		HashMap<Integer, HashMap<String, String>> records = executeSelect(sql, params);

		// Null is returned when an exception is thrown.
		if (records == null) {
			return null;
		}

		String accountName = "";

		// Loop through each row returned
		for (int rowIndex = 1; rowIndex <= records.size(); rowIndex++) {
			// collect the columns and access its value by
			// first the column alias or column names as returned by the query

			accountName = records.get(rowIndex).get("acct_nm".toUpperCase());
		}

		return accountName;
	}

	public ResponseModel deleteBeneficiary(final String senderAcctNo, final String moduleId, final int beneficiaryId) {

		String sql = " delete from alt_quickteller_beneficiary\r\n" + " where Ben_Id = ?";

		// input parameters in the order needed in the query
		List<ValueDatatypePair> inParam = new ArrayList<ValueDatatypePair>();
		inParam.add(new ValueDatatypePair(beneficiaryId, Types.INTEGER));

		int numOfRows = executeDML(sql, inParam);

		if (beneficiaryId == 0) {
			sql = " delete from alt_quickteller_beneficiary where Sender_Acct_No = ? " + "and Module_id = ?";

			List<ValueDatatypePair> inParameter = new ArrayList<ValueDatatypePair>();
			inParameter.add(new ValueDatatypePair(senderAcctNo, Types.VARCHAR));
			inParameter.add(new ValueDatatypePair(moduleId, Types.VARCHAR));

			numOfRows = executeDML(sql, inParameter);
		}

		ResponseModel responseModel = new ResponseModel();
		responseModel.setResponseCode(ResponseConstants.NOT_FOUND_CODE);
		responseModel.setResponseMessage(ResponseConstants.NOT_FOUND_MESSAGE);

		if (numOfRows >= 0) {
			System.out.println("Beneficiary delete successful");

			responseModel.setResponseCode(ResponseConstants.SUCCEESS_CODE);
			responseModel.setResponseMessage(ResponseConstants.SUCCEESS_MESSAGE);
		}

		return responseModel;
	}

	public ResponseModel saveBeneficiary(String moduleId, String senderAcctNo, String beneficiaryAcctNo,
			String beneficiaryAcctName, String beneficiaryBankCode, String beneficiaryBankName) {

		// query to check whether beneficiary has been saved previously
		String sql = "select Ben_Id, Ben_Acct_No, Ben_Acct_Name, Ben_Bank_Code, Ben_Bank_Name\r\n"
				+ "from alt_quickteller_beneficiary\r\n"
				+ "where Sender_Acct_No = ? and Module_Id = ? and Ben_Acct_No = ?";

		// input parameters in the order needed in the query
		List<ValueDatatypePair> params = new ArrayList<ValueDatatypePair>();
		params.add(new ValueDatatypePair(senderAcctNo, Types.VARCHAR));
		params.add(new ValueDatatypePair(moduleId, Types.VARCHAR));
		params.add(new ValueDatatypePair(beneficiaryAcctNo, Types.VARCHAR));

		// collect the result
		HashMap<Integer, HashMap<String, String>> records = executeSelect(sql, params);

		ResponseModel responseModel = new ResponseModel();

		// Null is returned when an exception is thrown.
		if (records == null) {
			responseModel.setResponseCode(ResponseConstants.EXCEPTION_CODE);
			responseModel.setResponseMessage(ResponseConstants.EXCEPTION_MESSAGE);

			return responseModel;
		}

		// return already exist
		if (!records.isEmpty()) {
			responseModel.setResponseCode(ResponseConstants.ALREADY_EXIST_CODE);
			responseModel.setResponseMessage(ResponseConstants.ALREADY_EXIST_MESSAGE);
		}

		// save when beneficiary does not exist
		if (records.isEmpty()) {

			String query = "insert into \r\n"
					+ "alt_quickteller_beneficiary(Module_id, Sender_Acct_No, Ben_Acct_No, Ben_Acct_Name, Ben_Bank_Code, Ben_Bank_Name)\r\n"
					+ "values(?,?,?,?,?,?)";

			List<ValueDatatypePair> inParameter = new ArrayList<ValueDatatypePair>();
			inParameter.add(new ValueDatatypePair(moduleId, Types.VARCHAR));
			inParameter.add(new ValueDatatypePair(senderAcctNo, Types.VARCHAR));
			inParameter.add(new ValueDatatypePair(beneficiaryAcctNo, Types.VARCHAR));
			inParameter.add(new ValueDatatypePair(beneficiaryAcctName, Types.VARCHAR));
			inParameter.add(new ValueDatatypePair(beneficiaryBankCode, Types.VARCHAR));
			inParameter.add(new ValueDatatypePair(beneficiaryBankName, Types.VARCHAR));

			executeDML(query, inParameter);

			responseModel.setResponseCode(ResponseConstants.SUCCEESS_CODE);
			responseModel.setResponseMessage(ResponseConstants.SUCCEESS_MESSAGE);
		}

		return responseModel;
	}

	
	public static void main(String[] args) {
		//System.out.println(CommonMethods.ObjectToJsonString(new AccountDBOperation().selectMultiAccount("001204000008")));
		System.out.println(CommonMethods.ObjectToJsonString(new AccountDBOperation().selectAccountInfo2("001204000008")));
		
	}
	
	
}
