package com.neptunesoftware.quickloan;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.neptunesoftware.quickloan.data.CreditApplicationData;
import com.neptunesoftware.quickloan.data.CreditApplicationResponse;
import com.neptunesoftware.quickloan.data.LoanAccountData;
import com.neptunesoftware.quickloan.data.LoanAccountResponse;
import com.neptunesoftware.quickloan.data.LoanCreditTypeData;
import com.neptunesoftware.quickloan.data.LoanCreditTypeResponse;
import com.neptunesoftware.quickloan.data.LoanLoginRequest;
import com.neptunesoftware.quickloan.data.LoanLoginResponse;
import com.neptunesoftware.quickloan.data.LoanProductData;
import com.neptunesoftware.quickloan.data.LoanProductResponse;
import com.neptunesoftware.quickloan.data.LoanReasonData;
import com.neptunesoftware.quickloan.data.LoanReasonResponse;
import com.neptunesoftware.quickloan.data.LoanRegistrationRequest;
import com.neptunesoftware.quickloan.data.LoanRegistrationResponse;
import com.neptunesoftware.reuseableClasses.ResponseConstants;
import com.neptunesoftware.reuseableClasses.Database.DatabaseUtil;
import com.neptunesoftware.reuseableClasses.Database.OracleDatabase;
import com.neptunesoftware.reuseableClasses.Database.config.DBResponse;
import com.neptunesoftware.reuseableClasses.Database.config.ValueDatatypePair;

public class QuickloanDBOperation extends OracleDatabase{
	
	public LoanLoginResponse login(LoanLoginRequest loanLoginRequest){

		LoanLoginResponse loanLoginResponse = new LoanLoginResponse(loanLoginRequest.getUsername(), loanLoginRequest.getPassword());
		loanLoginResponse.setResponseCode(ResponseConstants.NOT_FOUND_CODE);
		loanLoginResponse.setResponseMessage(ResponseConstants.NOT_FOUND_MESSAGE);
		
		String sql = "SELECT T2.ACCT_NM, T1.ACCOUNT_NUMBER, T1.PIN, T1.PHONE_NUMBER, T3.CUST_NO   \r\n"
				+ "FROM QUICK_LOAN_INFO T1 join ACCOUNT T2 on T2.ACCT_NO = T1.ACCOUNT_NUMBER\r\n"
				+ "join CUSTOMER T3 on T3.CUST_ID = T2.CUST_ID \r\n"
				+ "WHERE (EMAIL_ADDRESS = ? OR PHONE_NUMBER = ? ) AND PASSWORD = ?";

		List<ValueDatatypePair> inputParameter = new ArrayList<ValueDatatypePair>();
		inputParameter.add(new ValueDatatypePair(loanLoginRequest.getUsername(), Types.VARCHAR));
		inputParameter.add(new ValueDatatypePair(loanLoginRequest.getUsername(), Types.VARCHAR));
		inputParameter.add(new ValueDatatypePair(loanLoginRequest.getPassword(), Types.VARCHAR));

		HashMap<Integer, HashMap<String, String>> records = executeSelect(sql, inputParameter);

		if (records == null) {
			loanLoginResponse.setResponseCode(ResponseConstants.EXCEPTION_CODE);
			loanLoginResponse.setResponseMessage(ResponseConstants.EXCEPTION_MESSAGE);

			return loanLoginResponse;
		}

		for (Map.Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {

			loanLoginResponse.setCustomerName(rowEntry.getValue().get("ACCT_NM".toUpperCase()));
			loanLoginResponse.setAccountNumber(rowEntry.getValue().get("ACCOUNT_NUMBER".toUpperCase()));
			loanLoginResponse.setPin(rowEntry.getValue().get("PIN".toUpperCase()));
			loanLoginResponse.setPhoneNumber(rowEntry.getValue().get("PHONE_NUMBER".toUpperCase()));
			loanLoginResponse.setCustNo(rowEntry.getValue().get("CUST_NO".toUpperCase()));

			loanLoginResponse.setResponseCode(ResponseConstants.SUCCEESS_CODE);
			loanLoginResponse.setResponseMessage(ResponseConstants.SUCCEESS_MESSAGE);
		}

		return loanLoginResponse;			
	}
	
	public LoanRegistrationResponse registration(LoanRegistrationRequest loanRegistrationRequest) {

		// initialize the response object
		LoanRegistrationResponse loanRegistrationResponse = new LoanRegistrationResponse(
				loanRegistrationRequest.getEmail(), loanRegistrationRequest.getPassword(),
				loanRegistrationRequest.getPin(), loanRegistrationRequest.getAccountNumber(),
				loanRegistrationRequest.getPhoneNumber());

		String query = "SELECT T2.ACCT_NM, T1.ACCOUNT_NUMBER, T1.PIN, T1.PHONE_NUMBER \r\n"
				+ "FROM QUICK_LOAN_INFO T1 join ACCOUNT T2 on T2.ACCT_NO = T1.ACCOUNT_NUMBER\r\n"
				+ "WHERE EMAIL_ADDRESS = ?";

		List<ValueDatatypePair> inputParameter = new ArrayList<ValueDatatypePair>();
		inputParameter.add(new ValueDatatypePair(loanRegistrationRequest.getEmail(), Types.VARCHAR));

		HashMap<Integer, HashMap<String, String>> records = executeSelect(query, inputParameter);

		if (records == null) {
			loanRegistrationResponse.setResponseCode(ResponseConstants.EXCEPTION_CODE);
			loanRegistrationResponse.setResponseMessage(ResponseConstants.EXCEPTION_MESSAGE);

			return loanRegistrationResponse;
		}

		if (!records.isEmpty()) {
			loanRegistrationResponse.setResponseCode(ResponseConstants.ALREADY_EXIST_CODE);
			loanRegistrationResponse.setResponseMessage(ResponseConstants.ALREADY_EXIST_MESSAGE);

			return loanRegistrationResponse;
		}

		String sql = "INSERT INTO QUICK_LOAN_INFO(EMAIL_ADDRESS, PASSWORD, PIN, ACCOUNT_NUMBER, PHONE_NUMBER) VALUES(?,?,?,?,?)";

		List<ValueDatatypePair> inputParam = new ArrayList<ValueDatatypePair>();
		inputParam.add(new ValueDatatypePair(loanRegistrationRequest.getEmail(), Types.VARCHAR));
		inputParam.add(new ValueDatatypePair(loanRegistrationRequest.getPassword(), Types.VARCHAR));
		inputParam.add(new ValueDatatypePair(loanRegistrationRequest.getPin(), Types.VARCHAR));
		inputParam.add(new ValueDatatypePair(loanRegistrationRequest.getAccountNumber(), Types.VARCHAR));
		inputParam.add(new ValueDatatypePair(loanRegistrationRequest.getPhoneNumber(), Types.VARCHAR));

		loanRegistrationResponse.setResponseCode(ResponseConstants.QUERY_CODE);
		loanRegistrationResponse.setResponseMessage(ResponseConstants.QUERY_MESSAGE);

		if (executeDML(sql, inputParam) > 0) {
			loanRegistrationResponse.setResponseCode(ResponseConstants.SUCCEESS_CODE);
			loanRegistrationResponse.setResponseMessage(ResponseConstants.SUCCEESS_MESSAGE);
		}

		return loanRegistrationResponse;		
	}
	
	public LoanCreditTypeResponse creditType() {

		LoanCreditTypeResponse loanCreditTypeResponse = new LoanCreditTypeResponse();
		loanCreditTypeResponse.setResponseCode(ResponseConstants.NOT_FOUND_CODE);
		loanCreditTypeResponse.setResponseMessage(ResponseConstants.NOT_FOUND_MESSAGE);

		String sql = "select CR_TY_CD, CR_TY_DESC from credit_type order by 2";

		HashMap<Integer, HashMap<String, String>> records = executeSelect(sql);

		if (records == null) {
			loanCreditTypeResponse.setResponseCode(ResponseConstants.EXCEPTION_CODE);
			loanCreditTypeResponse.setResponseMessage(ResponseConstants.EXCEPTION_MESSAGE);

			return loanCreditTypeResponse;
		}

		List<LoanCreditTypeData> loanCreditTypeDataList = new ArrayList<LoanCreditTypeData>();
		for (Map.Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {

			LoanCreditTypeData loanCreditTypeData = new LoanCreditTypeData();
			loanCreditTypeData.setCreditTypeCode(rowEntry.getValue().get("CR_TY_CD".toUpperCase()));
			loanCreditTypeData.setCreditTypeDesc(rowEntry.getValue().get("CR_TY_DESC".toUpperCase()));

			loanCreditTypeDataList.add(loanCreditTypeData);

			loanCreditTypeResponse.setResponseCode(ResponseConstants.SUCCEESS_CODE);
			loanCreditTypeResponse.setResponseMessage(ResponseConstants.SUCCEESS_MESSAGE);
		}

		loanCreditTypeResponse.setCreditType(loanCreditTypeDataList);

		return loanCreditTypeResponse;
	}
	
	public LoanReasonResponse reason() {

		LoanReasonResponse loanReasonResponse = new LoanReasonResponse();
		loanReasonResponse.setResponseCode(ResponseConstants.NOT_FOUND_CODE);
		loanReasonResponse.setResponseMessage(ResponseConstants.NOT_FOUND_MESSAGE);

		String sql = "select RSN_CD, RSN_DESC " + " from reason_ref where RSN_CAT_CD = 'POC' " + 
					"order  by 2";

		HashMap<Integer, HashMap<String, String>> records = executeSelect(sql);

		if (records == null) {
			loanReasonResponse.setResponseCode(ResponseConstants.EXCEPTION_CODE);
			loanReasonResponse.setResponseMessage(ResponseConstants.EXCEPTION_MESSAGE);

			return loanReasonResponse;
		}

		List<LoanReasonData> LoanReasonDataList = new ArrayList<LoanReasonData>();
		for (Map.Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {
			LoanReasonData loanReasonData = new LoanReasonData();

			loanReasonData.setReasonCode(rowEntry.getValue().get("RSN_CD".toUpperCase()));
			loanReasonData.setReasonDesc(rowEntry.getValue().get("RSN_DESC".toUpperCase()));

			LoanReasonDataList.add(loanReasonData);

			loanReasonResponse.setResponseCode(ResponseConstants.SUCCEESS_CODE);
			loanReasonResponse.setResponseMessage(ResponseConstants.SUCCEESS_MESSAGE);
		}

		loanReasonResponse.setReasonCode(LoanReasonDataList);

		return loanReasonResponse;
	}
	
	public LoanProductResponse loanProduct() {

		LoanProductResponse loanProductResponse = new LoanProductResponse();
		loanProductResponse.setResponseCode(ResponseConstants.NOT_FOUND_CODE);
		loanProductResponse.setResponseMessage(ResponseConstants.NOT_FOUND_MESSAGE);
	
		String sql = "select a.prod_id, b.PROD_CD, b.PROD_DESC, c.CRNCY_CD, \r\n" + 
				"a.TERM_FREQ_CD, d.REF_DESC, a.TERM_FREQ_VALUE, nvl(to_Char(a.MIN_LOAN_AMT), 'No Limit') min_amount, nvl(to_Char(a.MAX_LOAN_AMT), 'No Limit') max_amount \r\n" + 
				",to_char(sysdate, 'dd/MM/yyyy') application_date, case a.TERM_FREQ_CD when 'D' then to_char((sysdate + TERM_FREQ_VALUE), 'dd/MM/yyyy') when 'W' then to_char((sysdate + (7*a.TERM_FREQ_VALUE)), 'dd/MM/yyyy') \r\n" + 
				"when 'M' then to_char(add_months(sysdate, a.TERM_FREQ_VALUE), 'dd/MM/yyyy') when 'Q' then to_char(add_months(sysdate, (3 * a.TERM_FREQ_VALUE)), 'dd/MM/yyyy') when 'H' then to_char(add_months(sysdate, (6 * a.TERM_FREQ_VALUE)), 'dd/MM/yyyy')\r\n" + 
				"when 'Y' then to_char(add_months(sysdate, (12 * a.TERM_FREQ_VALUE)), 'dd/MM/yyyy') end expiry_date \r\n" + 
				"from loan_product_basic_info a\r\n" + 
				"join product b on b.prod_id = a.prod_id and b.PROD_CAT_TY = 'LN'\r\n" + 
				"join currency c on c.CRNCY_ID = b.CRNCY_ID\r\n" + 
				"join FREQUENCY_REF d on d.REF_KEY = a.TERM_FREQ_CD\r\n" + 
				"where b.REC_ST = 'A'\r\n" + 
				"order by 3";
		
		HashMap<Integer, HashMap<String, String>> records = executeSelect(sql);

		if (records == null) {
			loanProductResponse.setResponseCode(ResponseConstants.EXCEPTION_CODE);
			loanProductResponse.setResponseMessage(ResponseConstants.EXCEPTION_MESSAGE);

			return loanProductResponse;
		}

		List<LoanProductData> loanProductDataList = new ArrayList<LoanProductData>();
		for (Map.Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {
			LoanProductData loanProductData = new LoanProductData();
			
			loanProductData.setProductId(rowEntry.getValue().get("prod_id".toUpperCase()));
			loanProductData.setProductCode(rowEntry.getValue().get("PROD_CD".toUpperCase()));
			loanProductData.setProductDesc(rowEntry.getValue().get("PROD_DESC".toUpperCase()));
			loanProductData.setCurrencyCode(rowEntry.getValue().get("CRNCY_CD".toUpperCase()));
			loanProductData.setTermCode(rowEntry.getValue().get("TERM_FREQ_CD".toUpperCase()));
			loanProductData.setTermDesc(rowEntry.getValue().get("REF_DESC".toUpperCase()));
			loanProductData.setTermValue(rowEntry.getValue().get("TERM_FREQ_VALUE".toUpperCase()));
			loanProductData.setMinAmount(rowEntry.getValue().get("min_amount".toUpperCase()));
			loanProductData.setMaxAmount(rowEntry.getValue().get("max_amount".toUpperCase()));
			loanProductData.setApplicationDate(rowEntry.getValue().get("application_date".toUpperCase()));
			loanProductData.setExpiryDate(rowEntry.getValue().get("expiry_date".toUpperCase()));
			
			loanProductDataList.add(loanProductData);
			
			loanProductResponse.setResponseCode(ResponseConstants.SUCCEESS_CODE);
			loanProductResponse.setResponseMessage(ResponseConstants.SUCCEESS_MESSAGE);
			
		}
		
		loanProductResponse.setLoanProduct(loanProductDataList);
		
		return loanProductResponse;
	}

	public CreditApplicationResponse creditApplicationProcedure(CreditApplicationData creditApplicationData) {

		DBResponse dbResponse = new DBResponse();
		
		dbResponse = procedureCIICreditApplication(
				creditApplicationData.getMainOperation(),
				creditApplicationData.getRefNo(),
				creditApplicationData.getCreditType(),
				creditApplicationData.getCustNo(),
				creditApplicationData.getReasonCode(),
				creditApplicationData.getApplicationDate(),
				creditApplicationData.getCurrencyCode(),
				creditApplicationData.getPrimeLimitAmount(),
				creditApplicationData.getPortfolioCode(),
				creditApplicationData.getCreditRequestDate(),				
				creditApplicationData.getTermCode(),
				creditApplicationData.getTermValue(),
				creditApplicationData.getExpiryDate(),
				creditApplicationData.getRepaymentSourceAccount(),
				creditApplicationData.getApplicationProductCode(),
				creditApplicationData.getOverdraftAccountNo(),
				creditApplicationData.getRecordStatus(),
				creditApplicationData.getVersionNo(),
				creditApplicationData.getRowTimeStamp(),
				creditApplicationData.getUserId(),
				
				creditApplicationData.getCreateDate(),
				creditApplicationData.getSysCreateDate(),
				creditApplicationData.getCreatedBy(),
				creditApplicationData.getRequestPWD(),
				creditApplicationData.getLogrepository(),
				creditApplicationData.getTestCallLevel(),
				creditApplicationData.getTraceLevel(),
				creditApplicationData.getErrorCode(), 
				creditApplicationData.getErrorSeverity(),
				creditApplicationData.getErrorMessage()			   
				);

		CreditApplicationResponse creditApplicationResponse = new CreditApplicationResponse();
		
		if (dbResponse.PV_ERROR_SEVERITY == null || dbResponse.PV_ERROR_SEVERITY.equals("0")) {

			creditApplicationResponse.setReferenceNo(dbResponse.PV_REF_NO);
			creditApplicationResponse.setResponseCode(ResponseConstants.SUCCEESS_CODE);
			creditApplicationResponse.setResponseMessage(ResponseConstants.SUCCEESS_MESSAGE);
		} else {
			String message = ResponseConstants.PROCEDURE_MESSAGE + dbResponse.PV_ERROR_SEVERITY;

			creditApplicationResponse.setResponseCode(ResponseConstants.PROCEDURE_CODE);
			creditApplicationResponse.setResponseMessage(message);
		}
		
		System.out.println("procedureCIICreditApplication Response: " + dbResponse.PV_ERROR_SEVERITY);

		//return response
		return creditApplicationResponse;
	}
	
	private DBResponse procedureCIICreditApplication(String PV_MAIN_OPERATION,  
			String PV_REF_NO, String PV_CR_TY_CD, String PV_CUST_NO, String PV_RSN_CD,
			String PV_APPL_DT, String PV_CRNCY_CD,int PV_PRIME_LIMIT_AMT, String PV_PORTFOLIO_CD,  
			String PV_CR_REQD_DT, String PV_TERM_CD, int PV_TERM_VALUE, String PV_EXPIRY_DT,
			String PV_REPAY_SRC_ACCT_NO, String PV_APPL_PROD_CD, String PV_OVERDRAFT_ACCT_NO,
			String PV_REC_ST, int PV_VERSION_NO, String PV_ROW_TS, String PV_USER_ID,
			String PV_CREATE_DT, String PV_SYS_CREATE_TS, String PV_CREATED_BY,	String PV_REQUEST_PWD,   
			String PV_LOG_REPOSITORY, int PV_TEST_CALL_LEVEL, int PV_TRACE_LEVEL, String PV_ERROR_CODE,  
			int PV_ERROR_SEVERITY, String PV_ERROR_MESSAGE
			) {
		
		// IN parameter values in the order received by the procedure
		LinkedHashMap<Integer, ValueDatatypePair> inParam = new LinkedHashMap<Integer, ValueDatatypePair>();
		inParam.put(1, new ValueDatatypePair(PV_MAIN_OPERATION, Types.VARCHAR));							// PV_MAIN_OPERATION
		inParam.put(2, new ValueDatatypePair(PV_REF_NO, Types.VARCHAR));									// PV_REF_NO
		inParam.put(3, new ValueDatatypePair(PV_CR_TY_CD, Types.VARCHAR));									// PV_CR_TY_CD
		inParam.put(4, new ValueDatatypePair(PV_CUST_NO, Types.VARCHAR));									// PV_CUST_NO
		inParam.put(5, new ValueDatatypePair(PV_RSN_CD, Types.VARCHAR));									// PV_RSN_CD
		inParam.put(6, new ValueDatatypePair(DatabaseUtil.getCurrentDate(PV_APPL_DT, "dd/MM/yyyy"), Types.DATE));		// PV_APPL_DT
		inParam.put(7, new ValueDatatypePair(PV_CRNCY_CD, Types.VARCHAR));									// PV_CRNCY_CD
		inParam.put(8, new ValueDatatypePair(PV_PRIME_LIMIT_AMT, Types.NUMERIC));							// PV_PRIME_LIMIT_AMT
		inParam.put(9, new ValueDatatypePair(PV_PORTFOLIO_CD, Types.NULL));									// PV_PORTFOLIO_CD
		inParam.put(10, new ValueDatatypePair(DatabaseUtil.getCurrentDate(PV_CR_REQD_DT, "dd/MM/yyyy"), Types.DATE));	// PV_CR_REQD_DT
		inParam.put(11, new ValueDatatypePair(PV_TERM_CD, Types.VARCHAR));									// PV_TERM_CD
		
		inParam.put(12, new ValueDatatypePair(PV_TERM_VALUE, Types.INTEGER));								// PV_TERM_VALUE
		inParam.put(13, new ValueDatatypePair(DatabaseUtil.getCurrentDate(PV_EXPIRY_DT,"dd/MM/yyyy"), Types.DATE));		// PV_EXPIRY_DT
		inParam.put(14, new ValueDatatypePair(PV_REPAY_SRC_ACCT_NO, Types.VARCHAR));						// PV_REPAY_SRC_ACCT_NO
		inParam.put(15, new ValueDatatypePair(PV_APPL_PROD_CD, Types.VARCHAR));								// PV_APPL_PROD_CD
		inParam.put(16, new ValueDatatypePair(PV_OVERDRAFT_ACCT_NO, Types.NULL));							// PV_OVERDRAFT_ACCT_NO
					
		inParam.put(17, new ValueDatatypePair(PV_REC_ST, Types.VARCHAR));									// PV_REC_ST
		inParam.put(18, new ValueDatatypePair(PV_VERSION_NO, Types.INTEGER));								// PV_VERSION_NO
		inParam.put(19, new ValueDatatypePair(DatabaseUtil.getCurrentDate(PV_ROW_TS, "dd/MM/yyyy"), Types.DATE));		// PV_ROW_TS
		inParam.put(20, new ValueDatatypePair(PV_USER_ID, Types.VARCHAR));									// PV_USER_ID
		inParam.put(21, new ValueDatatypePair(DatabaseUtil.getCurrentDate(PV_CREATE_DT, "dd/MM/yyyy"), Types.DATE));		// PV_CREATE_DT
		inParam.put(22, new ValueDatatypePair(DatabaseUtil.getCurrentDate(PV_SYS_CREATE_TS, "dd/MM/yyyy"), Types.DATE));	// PV_SYS_CREATE_TS
		inParam.put(23, new ValueDatatypePair(PV_CREATED_BY, Types.VARCHAR));								// PV_CREATED_BY
					
		inParam.put(24, new ValueDatatypePair(PV_REQUEST_PWD, Types.VARCHAR));								// PV_REQUEST_PWD
		inParam.put(25, new ValueDatatypePair(PV_LOG_REPOSITORY, Types.VARCHAR));							// PV_LOG_REPOSITORY
		inParam.put(26, new ValueDatatypePair(PV_TEST_CALL_LEVEL, Types.NUMERIC));							// PV_TEST_CALL_LEVEL
		inParam.put(27, new ValueDatatypePair(PV_TRACE_LEVEL, Types.NUMERIC));								// PV_TRACE_LEVEL
		inParam.put(28, new ValueDatatypePair(PV_ERROR_CODE, Types.VARCHAR));								// PV_ERROR_CODE
		inParam.put(29, new ValueDatatypePair(PV_ERROR_SEVERITY, Types.NUMERIC));							// PV_ERROR_SEVERITY
		inParam.put(30, new ValueDatatypePair(PV_ERROR_MESSAGE, Types.VARCHAR));							// PV_ERROR_MESSAGE
					
		// OUT parameter values in the order received by the procedure
		LinkedHashMap<Integer, ValueDatatypePair> outParam = new LinkedHashMap<Integer, ValueDatatypePair>();
		outParam.put(25, new ValueDatatypePair(Types.VARCHAR));	// PV_LOG_REPOSITORY
		outParam.put(26, new ValueDatatypePair(Types.NUMERIC));	// PV_TEST_CALL_LEVEL
		outParam.put(27, new ValueDatatypePair(Types.NUMERIC));	// PV_TRACE_LEVEL
		outParam.put(28, new ValueDatatypePair(Types.VARCHAR));	// PV_ERROR_CODE
		outParam.put(29, new ValueDatatypePair(Types.NUMERIC));	// PV_ERROR_SEVERITY
		outParam.put(30, new ValueDatatypePair(Types.VARCHAR));	// PV_ERROR_MESSAGE
		outParam.put(2, new ValueDatatypePair(Types.VARCHAR));	// PV_REF_NO
		
		// Execute procedure
		LinkedHashMap<Integer, ValueDatatypePair> resultParam = executeProcedure("CII_CREDIT_APPLICATION", inParam, outParam);
		
		DBResponse dbResponse = new DBResponse();
		
		if (resultParam.get(0).getValue().equals("00")) {
			dbResponse.PV_LOG_REPOSITORY =	resultParam.get(25).getValue();	//PV_LOG_REPOSITORY
			dbResponse.PV_TEST_CALL_LEVEL =	resultParam.get(26).getValue(); //PV_TEST_CALL_LEVEL
			dbResponse.PV_TRACE_LEVEL = resultParam.get(27).getValue();		//PV_TRACE_LEVEL
			dbResponse.PV_ERROR_CODE = resultParam.get(28).getValue();		//PV_ERROR_CODE
			dbResponse.PV_ERROR_SEVERITY = resultParam.get(29).getValue();	//PV_ERROR_SEVERITY
			dbResponse.PV_ERROR_MESSAGE = resultParam.get(30).getValue();	//PV_ERROR_MESSAGE
			dbResponse.PV_REF_NO = resultParam.get(2).getValue();			//PV_REF_NO
		}
		
		return dbResponse;
			
	}

	public LoanAccountResponse multiAccount(String accountNo) {
		
		LoanAccountResponse loanAccountResponse = new LoanAccountResponse();
		loanAccountResponse.setResponseCode(ResponseConstants.NOT_FOUND_CODE);
		loanAccountResponse.setResponseMessage(ResponseConstants.NOT_FOUND_MESSAGE);
		
		String sql = "SELECT T1.CUST_ID, T2.ACCT_NO, T2.ACCT_NM,T2.REC_ST,T2.PROD_CAT_TY, T3.LEDGER_BAL, T4.CRNCY_CD_ISO,T5.CONTACT,T8.REF_DESC, ROWNUM  \r\n" + 
				"FROM CUSTOMER T1 join ACCOUNT T2 on T1.CUST_ID = T2.CUST_ID and T2.PROD_CAT_TY = 'DP' and T2.REC_ST = 'A' \r\n" + 
				"join DEPOSIT_ACCOUNT_SUMMARY T3 on T2.ACCT_ID = T3.DEPOSIT_ACCT_ID\r\n" + 
				"join CURRENCY T4 on T2.CRNCY_ID = T4.CRNCY_ID AND T4.CRNCY_CD_ISO = 'NGN' \r\n" + 
				"left join CUSTOMER_CONTACT_MODE T5 on T1.CUST_ID = T5.CUST_ID AND REGEXP_LIKE (T5.CONTACT, '^(\\+|[0-9])') \r\n" + 
				"left join CONTACT_MODE_REF T6 on T5.CONTACT_MODE_ID = T6.CONTACT_MODE_ID \r\n" + 
				"and T6.CONTACT_MODE_ID IN (237,231,236)\r\n" + 
				"left join PRODUCT_CATEGORY_REF T8 on T8.REF_KEY = T2.PROD_CAT_TY\r\n" + 
				"where T1.CUST_ID IN (SELECT CUST_ID FROM ACCOUNT WHERE ACCT_NM = \r\n" + 
				"(SELECT ACCT_NM FROM ACCOUNT WHERE ACCT_NO = ?))\r\n" + 
				"order by 8 desc";
		
		List<ValueDatatypePair> inputParameter = new ArrayList<ValueDatatypePair>();
		inputParameter.add(new ValueDatatypePair(accountNo, Types.VARCHAR));
		
		HashMap<Integer, HashMap<String, String>> records = executeSelect(sql, inputParameter);

		if (records == null) {
			loanAccountResponse.setResponseCode(ResponseConstants.EXCEPTION_CODE);
			loanAccountResponse.setResponseMessage(ResponseConstants.EXCEPTION_MESSAGE);

			return loanAccountResponse;
		}

		List<LoanAccountData> loanAccountDataLst = new ArrayList<LoanAccountData>();
		for (Map.Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {
			LoanAccountData loanAccountData = new LoanAccountData();
			
			loanAccountData.setAccountId(rowEntry.getValue().get("ROWNUM".toUpperCase()));
			loanAccountData.setAccountNumber(rowEntry.getValue().get("ACCT_NO".toUpperCase()));
			loanAccountData.setAccountName(rowEntry.getValue().get("ACCT_NM".toUpperCase()));
			loanAccountData.setAccountStatus(rowEntry.getValue().get("REC_ST".toUpperCase()));
			loanAccountData.setAccountType(rowEntry.getValue().get("REF_DESC".toUpperCase()));
			loanAccountData.setLedgerBalance(rowEntry.getValue().get("LEDGER_BAL".toUpperCase()));
			loanAccountData.setCurrencyCode(rowEntry.getValue().get("CRNCY_CD_ISO".toUpperCase()));
			loanAccountData.setPhoneNumber(rowEntry.getValue().get("CONTACT".toUpperCase()));
			
			loanAccountDataLst.add(loanAccountData);
			
			loanAccountResponse.setResponseCode(ResponseConstants.SUCCEESS_CODE);
			loanAccountResponse.setResponseMessage(ResponseConstants.SUCCEESS_MESSAGE);
		}
		
		loanAccountResponse.setAccounts(loanAccountDataLst);
		
		return loanAccountResponse;			
	}
	
	public String getCustomerNumber(String accountNumber) {
		
		String sql = "SELECT t2.cust_no FROM ACCOUNT T1, CUSTOMER T2 \r\n" + 
				"WHERE T1.CUST_ID=T2.CUST_ID AND T1.REC_ST='A'\r\n" + 
				"AND t2.cust_cat='PER' AND t1.acct_no = ?";

		List<ValueDatatypePair> inputParameter = new ArrayList<ValueDatatypePair>();
		inputParameter.add(new ValueDatatypePair(accountNumber, Types.VARCHAR));
		
		HashMap<Integer, HashMap<String, String>> records = executeSelect(sql, inputParameter);

		String customerNumber = "";
		if (records == null) {
			return customerNumber;
		}
		
		for (Map.Entry<Integer, HashMap<String, String>> rowEntry : records.entrySet()) {
			customerNumber = rowEntry.getValue().get("cust_no".toUpperCase());
		}

		return customerNumber;
	}
	
}
