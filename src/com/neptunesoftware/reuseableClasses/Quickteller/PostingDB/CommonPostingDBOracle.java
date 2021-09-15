package com.neptunesoftware.reuseableClasses.Quickteller.PostingDB;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import com.company.application.airtimeRecharge.data.AirtimeRechargeRequest;
import com.company.application.billsPayment.data.BillPaymentRequest;
import com.company.application.fundTransfer.data.ExternalFTRequest;
import com.neptunesoftware.reuseableClasses.CommonMethods;
import com.neptunesoftware.reuseableClasses.Database.DatabaseUtil;
import com.neptunesoftware.reuseableClasses.Database.OracleDatabase;
import com.neptunesoftware.reuseableClasses.Database.config.DBRequest;
import com.neptunesoftware.reuseableClasses.Database.config.DBResponse;
import com.neptunesoftware.reuseableClasses.Database.config.ValueDatatypePair;
import com.neptunesoftware.reuseableClasses.Quickteller.QuicktellerConstants;
import com.neptunesoftware.reuseableClasses.Rubikon.config.RubikonCredential;
import com.neptunesoftware.reuseableClasses.Rubikon.config.RubikonUtil;

public class CommonPostingDBOracle extends OracleDatabase {

	public CommonPostingDBOracle() {
		super();
	}
	
	public CommonPostingDBOracle(String databaseName) {
		super(databaseName);
	}
		
	public CommonPostingDBOracle(String driver, String connectionURL, String username, String password, String databaseType) {
		super(driver, connectionURL, username, password, databaseType);
	}
	
	
	//*** START very specific methods.
	// not really reusable except you are implementing quickteller
	
	public boolean isTransactionSuccessful(final AirtimeRechargeRequest airtimeRchgeXfer, final boolean isReversal, final String responseCode, final String transactionRef) {

		RubikonCredential rubikonCredential = RubikonUtil.readRubikonConfig();
		String chargeCodeBillsPayment = rubikonCredential.getChargeCodeBillsPayment();
		String channelCode = rubikonCredential.getChannelCode();
		String currencyCode = rubikonCredential.getCurrencyCode();
		String billsPaymentCredit = rubikonCredential.getBillsPaymentCredit();
		String billsPaymentDebit = rubikonCredential.getBillsPaymentDebit();
		
		
		System.out.println("**Start callProcedure");
		
		String narration = "Airtime recharge for mobile no " + airtimeRchgeXfer.getCustomerId();
		
		double transactionAmount = CommonMethods.koboToNaira(Integer.parseInt(airtimeRchgeXfer.getTransactionAmount().trim()));
		double chargeAmount = CommonMethods.koboToNaira(Double.parseDouble(airtimeRchgeXfer.getChargeAmount().trim()));
		double taxAmount = CommonMethods.koboToNaira(Double.parseDouble(airtimeRchgeXfer.getTaxAmount().trim()));
		String serviceCode = billsPaymentDebit;
		String transactionType = "DR";
		String reveralFlag = "N";
		
		if(isReversal) {
			narration = "(Reversal) " + narration;
			
			double reversalCharges = Double.sum(chargeAmount, taxAmount);
			transactionAmount =		Double.sum(transactionAmount, reversalCharges);
			chargeAmount = 0;
			taxAmount = 0;
			serviceCode = billsPaymentCredit;
			transactionType = "CR";
			//reveralFlag = "Y";
		}
		
		
		DBRequest dbRequest = new DBRequest(airtimeRchgeXfer.getFromAccountNumber(), transactionAmount+"", "0",
				airtimeRchgeXfer.getMobileNumber(), narration, chargeAmount+"", taxAmount+"", airtimeRchgeXfer.getInitiatingApp());
		
		
		String newTransactionRef = transactionRef.isEmpty() 
						? new QuicktellerConstants().getTransferCodePrefix() + "|" + new Date() + "|" + new Date().getTime()
						: transactionRef;
		
		String dbResponse = callProcedure(dbRequest, "", serviceCode, newTransactionRef, reveralFlag, chargeCodeBillsPayment);
		
		String newResponseCode = responseCode.isEmpty() ? dbResponse : responseCode;
		
		// save to the db
		try {
			saveRecord(dbRequest, newResponseCode, newTransactionRef, "MobileRecharge", transactionType,
					channelCode, currencyCode);
		} catch (Exception e) {
			System.out.println("Failed to save record");
		}
		
		// return false if deduction was not possible
		if (!dbResponse.equals("00")) {
			System.out.println("**End callProcedure");
			return false;
		}
		
		System.out.println("**End callProcedure");
		return true;
		
	}
	
	public boolean isTransactionSuccessful(final BillPaymentRequest billPayment, final boolean isReversal, final String responseCode, final String transactionRef) {

		RubikonCredential rubikonCredential = RubikonUtil.readRubikonConfig();
		String chargeCodeBillsPayment = rubikonCredential.getChargeCodeBillsPayment();
		String channelCode = rubikonCredential.getChannelCode();
		String currencyCode = rubikonCredential.getCurrencyCode();
		String billsPaymentCredit = rubikonCredential.getBillsPaymentCredit();
		String billsPaymentDebit = rubikonCredential.getBillsPaymentDebit();
		
		
		System.out.println("**Start callProcedure");
		
		String narration = "Bills payment #" + billPayment.getCustomerId(); 
		narration = narration + "\nSub Desc: " + billPayment.getTransactionDescription();
		
		double transactionAmount = CommonMethods.koboToNaira(Integer.parseInt(billPayment.getTransactionAmount().trim()));
		double chargeAmount = CommonMethods.koboToNaira(Double.parseDouble(billPayment.getChargeAmount().trim()));
		double taxAmount = CommonMethods.koboToNaira(Double.parseDouble(billPayment.getTaxAmount().trim()));
		String serviceCode = billsPaymentDebit;
		String transactionType = "DR";
		String reveralFlag = "N";
		
		if(isReversal) {
			narration = "(Reversal) " + narration;
			
			double reversalCharges = Double.sum(chargeAmount, taxAmount);
			transactionAmount =		Double.sum(transactionAmount, reversalCharges);
			chargeAmount = 0;
			taxAmount = 0;
			serviceCode = billsPaymentCredit;
			transactionType = "CR";
			//reveralFlag = "Y";
		}
		
		
		DBRequest dbRequest = new DBRequest(billPayment.getFromAccountNumber(), transactionAmount+"", "0",
				billPayment.getCustomerId(), narration, chargeAmount+"", taxAmount+"", billPayment.getInitiatingApp());
		
		
		String newTransactionRef = transactionRef.isEmpty() 
						? new QuicktellerConstants().getTransferCodePrefix() + "|" + new Date() + "|" + new Date().getTime()
						: transactionRef;
		
		String dbResponse = callProcedure(dbRequest, "", serviceCode, transactionRef, reveralFlag, chargeCodeBillsPayment);
		
		String newResponseCode = responseCode.isEmpty() ? dbResponse : responseCode;
		
		// save to the db
		try {
			saveRecord(dbRequest, newResponseCode, newTransactionRef, "BillsPayment", transactionType,
					channelCode, currencyCode);
		} catch (Exception e) {
			System.out.println("Failed to save record");
		}
		
		// return false if deduction was not possible
		if (!dbResponse.equals("00")) {
			System.out.println("**End callProcedure");
			return false;
		}
		
		System.out.println("**End callProcedure");
		return true;
		
	}
		
	public boolean isTransactionSuccessful(final String senderName, final ExternalFTRequest externalTransfer, final boolean isReversal, final String responseCode, final String transactionRef) {

		RubikonCredential rubikonCredential = RubikonUtil.readRubikonConfig();	
		String chargeCode = rubikonCredential.getChargeCode();
		String channelCode = rubikonCredential.getChannelCode();
		String currencyCode = rubikonCredential.getCurrencyCode();
		String fundXferCredit = rubikonCredential.getFundTransferCredit();
		String fundXferDebit = rubikonCredential.getFundTransferDebit();
		
		
		System.out.println("**Start callProcedure");
		
		String narration = "External Transfer from " + senderName + ", #acctno " + externalTransfer.getFromAccountNumber() + 
				" to #acctno " + externalTransfer.getBeneficiaryAccountNumber(); 
		narration = narration + "\nSub Desc: " + externalTransfer.getTransactionDescription();	
		
		double transactionAmount = CommonMethods.koboToNaira(Integer.parseInt(externalTransfer.getTransactionAmount().trim()));
		double chargeAmount = CommonMethods.koboToNaira(Double.parseDouble(externalTransfer.getChargeAmount().trim()));
		double taxAmount = CommonMethods.koboToNaira(Double.parseDouble(externalTransfer.getTaxAmount().trim()));
		String serviceCode = fundXferDebit;
		String transactionType = "DR";
		String reversalFlag = "N";
		
		if(isReversal) {
			narration = "(Reversal) " + narration;
			
			double reversalCharges = Double.sum(chargeAmount, taxAmount);
			transactionAmount =		Double.sum(transactionAmount, reversalCharges);
			chargeAmount = 0;
			taxAmount = 0;
			serviceCode = fundXferCredit;
			transactionType = "CR";
		}
		
		
		DBRequest dbRequest = new DBRequest(externalTransfer.getFromAccountNumber(), transactionAmount+"", "0",
				externalTransfer.getBeneficiaryAccountNumber(), narration, chargeAmount+"", taxAmount+"",
				externalTransfer.getInitiatingApp());
		
		
		String newTransactionRef = transactionRef.isEmpty() 
						? new QuicktellerConstants().getTransferCodePrefix() + "|" + new Date() + "|" + new Date().getTime()
						: transactionRef;
		
		String dbResponse = callProcedure(dbRequest, senderName, serviceCode, newTransactionRef, reversalFlag, chargeCode);
		
		String newResponseCode = responseCode.isEmpty() ? dbResponse : responseCode;
		
		// save to the db
		try {
			saveRecord(dbRequest, newResponseCode, newTransactionRef, "ExternalFundTransfer", transactionType,
					channelCode, currencyCode);
		} catch (Exception e) {
			System.out.println("Failed to save record");
		}
		
		// return false if deduction was not possible
		if (!dbResponse.equals("00")) {
			System.out.println("**End callProcedure");
			return false;
		}
		
		System.out.println("**End callProcedure");
		return true;
		
	}
	
	public String callProcedure(final DBRequest dbRequest, final String senderName,
			String serviceCode, String transactionRef, String reversalFlag, String chargeCode) {
		
		RubikonCredential rubikonCredential = RubikonUtil.readRubikonConfig();		
		String channelCode = rubikonCredential.getChannelCode();
		String taxCode = rubikonCredential.getTaxCode();
		String currencyCode = rubikonCredential.getCurrencyCode();
		String appUsername = rubikonCredential.getApplicationUsername();
				
		
		String contraCurrencyCode = !dbRequest.getContraAmount().equals("0") ? currencyCode : "";
		transactionRef = transactionRef.isEmpty() 
				? "INT-TXN-|" + new Date() + "|" + new Date().getTime()
				: transactionRef;
				
		DBResponse dbResponse = new DBResponse();
		
		try {
			dbResponse = ProcedureXAPI_POSTING_SERVICE(
					channelCode, // PV_CHANNEL_CD
					"", // PV_CHANNEL_PWD
					"123456789", // PV_DEVICE_ID
					serviceCode, // PV_SERVICE_CD
					"", // PV_TRANS_TYPE_CD
					dbRequest.getSenderAcctNo(), // PV_ACCOUNT_NO,
					currencyCode, // PV_ACCOUNT_CURRENCY_CD,
					new BigDecimal(Math.abs(Double.valueOf(dbRequest.getAmount()))), // PV_ACCOUNT_AMOUNT,
					currencyCode, // PV_TRANS_CURRENCY_CD,
					new BigDecimal(Math.abs(Double.valueOf(dbRequest.getAmount()))), // PV_TRANS_AMOUNT,
					
					new Date(), // PV_VALUE_DATE,
					new Date(), // PV_TRANS_DATE,
					transactionRef, // PV_TRANS_REF,
					transactionRef, // PV_SUPPLEMENTARY_REF,
					dbRequest.getNarration(), // PV_NARRATIVE,
					dbRequest.getBeneficiaryAcctNo(), // PV_CONTRA_ACCOUNT_NO,
					contraCurrencyCode, // PV_CONTRA_CURRENCY_CD,
					new BigDecimal(Math.abs(Double.valueOf(dbRequest.getContraAmount()))), // PV_CONTRA_AMOUNT,
					new BigDecimal(0), // PV_EXCHANGE_RATE,
					chargeCode, // PV_CHARGE_CD,
					
					new BigDecimal(Math.abs(Double.valueOf(dbRequest.getChargeAmount()))), // PV_CHARGE_AMOUNT,
					taxCode, // PV_TAX_CD,
					new BigDecimal(Math.abs(Double.valueOf(dbRequest.getTaxAmount()))), // PV_TAX_AMOUNT,
					reversalFlag, // PV_REVERSAL_FLG,
					"", // PV_ORIGIN_BANK_CD,
					"", // PV_SERVICE_PROVIDER,
					"", // PV_SERVICE_PROVIDER_SVC,
					"", // PV_USER_TYPE,
					appUsername, // PV_USER_ID,
					"", // PV_USER_PWD,
					
				   "12:45:30", // PV_TRANS_TIME
				   "N", // PV_EXTERNAL_COMMIT
				   "Y", // PV_PREVENT_DUPLICATES
				   "123456789", //PV_REQUEST_PWD
				   "DB", // PV_LOG_REPOSITORY
				   0, // PV_TEST_CALL_LEVEL
				   3, // PV_TRACE_LEVEL
					"", // PV_ERROR_CODE,
					0, // PV_ERROR_SEVERITY,
					"" // PV_ERROR_MESSAGE,
					);
		} catch (Exception e) {
			return "";
		}

		String responseCode = dbResponse.PV_ERROR_SEVERITY == null || dbResponse.PV_ERROR_SEVERITY.equals("0") ? "00"
				: dbResponse.PV_ERROR_SEVERITY;
		
		System.out.println("callProdecure_XAPI_POSTING_SERVICE Response: " + responseCode);

		//return response
		return responseCode;
	}
	
	public DBResponse ProcedureXAPI_POSTING_SERVICE(final String PV_CHANNEL_CD,
			final String PV_CHANNEL_PWD, final String PV_DEVICE_ID, final String PV_SERVICE_CD,
			final String PV_TRANS_TYPE_CD, final String PV_ACCOUNT_NO,
			final String PV_ACCOUNT_CURRENCY_CD, final BigDecimal PV_ACCOUNT_AMOUNT,
			final String PV_TRANS_CURRENCY_CD, final BigDecimal PV_TRANS_AMOUNT,
			final Date PV_VALUE_DATE, final Date PV_TRANS_DATE, final String PV_TRANS_REF,
			final String PV_SUPPLEMENTARY_REF, final String PV_NARRATIVE,
			final String PV_CONTRA_ACCOUNT_NO, final String PV_CONTRA_CURRENCY_CD,
			final BigDecimal PV_CONTRA_AMOUNT, final BigDecimal PV_EXCHANGE_RATE,
			final String PV_CHARGE_CD, final BigDecimal PV_CHARGE_AMOUNT,
			final String PV_TAX_CD, final BigDecimal PV_TAX_AMOUNT,
			final String PV_REVERSAL_FLG, final String PV_ORIGIN_BANK_CD,
			final String PV_SERVICE_PROVIDER, final String PV_SERVICE_PROVIDER_SVC,
			final String PV_USER_TYPE, final String PV_USER_ID, final String PV_USER_PWD,
			final String PV_TRANS_TIME, final String PV_EXTERNAL_COMMIT, final String PV_PREVENT_DUPLICATES, 
			final String PV_REQUEST_PWD, final String PV_LOG_REPOSITORY, final int PV_TEST_CALL_LEVEL,
			final int PV_TRACE_LEVEL, final String PV_ERROR_CODE, final int PV_ERROR_SEVERITY,
			final String PV_ERROR_MESSAGE
			 
			) {
		
		
		// IN parameter values in the order received by the procedure
		LinkedHashMap<Integer, ValueDatatypePair> inParam = new LinkedHashMap<Integer, ValueDatatypePair>();
		inParam.put(1, new ValueDatatypePair(PV_CHANNEL_CD, Types.VARCHAR));			// PV_CHANNEL_CD
		inParam.put(2, new ValueDatatypePair(PV_CHANNEL_PWD, Types.VARCHAR));			// PV_CHANNEL_PWD
		inParam.put(3, new ValueDatatypePair(PV_DEVICE_ID, Types.VARCHAR));				// PV_DEVICE_ID
		inParam.put(4, new ValueDatatypePair(PV_SERVICE_CD, Types.VARCHAR));			// PV_SERVICE_CD
		inParam.put(5, new ValueDatatypePair(PV_TRANS_TYPE_CD, Types.VARCHAR));			// PV_TRANS_TYPE_CD
		inParam.put(6, new ValueDatatypePair(PV_ACCOUNT_NO, Types.VARCHAR));			// PV_ACCOUNT_NO
		inParam.put(7, new ValueDatatypePair(PV_ACCOUNT_CURRENCY_CD, Types.VARCHAR));	// PV_ACCOUNT_CURRENCY_CD
		inParam.put(8, new ValueDatatypePair(PV_ACCOUNT_AMOUNT, Types.NUMERIC));		// PV_ACCOUNT_AMOUNT
		inParam.put(9, new ValueDatatypePair(PV_TRANS_CURRENCY_CD, Types.VARCHAR));		// PV_TRANS_CURRENCY_CD
		inParam.put(10, new ValueDatatypePair(PV_TRANS_AMOUNT, Types.NUMERIC));			// PV_TRANS_AMOUNT
		
		inParam.put(11, new ValueDatatypePair(DatabaseUtil.getCurrentDate(), Types.DATE));			// PV_VALUE_DATE
		inParam.put(12, new ValueDatatypePair(DatabaseUtil.getCurrentDate(), Types.DATE));			// PV_TRANS_DATE
		inParam.put(13, new ValueDatatypePair(PV_TRANS_REF, Types.VARCHAR));			// PV_TRANS_REF
		inParam.put(14, new ValueDatatypePair(PV_SUPPLEMENTARY_REF, Types.VARCHAR));	// PV_SUPPLEMENTARY_REF
		inParam.put(15, new ValueDatatypePair(PV_NARRATIVE, Types.VARCHAR));			// PV_NARRATIVE
		inParam.put(16, new ValueDatatypePair(PV_CONTRA_ACCOUNT_NO, Types.VARCHAR));	// PV_CONTRA_ACCOUNT_NO
		inParam.put(17, new ValueDatatypePair(PV_CONTRA_CURRENCY_CD, Types.VARCHAR));	// PV_CONTRA_CURRENCY_CD
		inParam.put(18, new ValueDatatypePair(PV_CONTRA_AMOUNT, Types.NUMERIC));		// PV_CONTRA_AMOUNT
		inParam.put(19, new ValueDatatypePair(PV_EXCHANGE_RATE, Types.NUMERIC));		// PV_EXCHANGE_RATE
		inParam.put(20, new ValueDatatypePair(PV_CHARGE_CD, Types.VARCHAR));			// PV_CHARGE_CD
				
		inParam.put(21, new ValueDatatypePair(PV_CHARGE_AMOUNT, Types.NUMERIC));		// PV_CHARGE_AMOUNT
		inParam.put(22, new ValueDatatypePair(PV_TAX_CD, Types.VARCHAR));				// PV_TAX_CD
		inParam.put(23, new ValueDatatypePair(PV_TAX_AMOUNT, Types.NUMERIC));			// PV_TAX_AMOUNT
		inParam.put(24, new ValueDatatypePair(PV_REVERSAL_FLG, Types.VARCHAR));			// PV_REVERSAL_FLG
		inParam.put(25, new ValueDatatypePair(null, Types.NULL));						// PV_ORIGIN_BANK_CD
		inParam.put(26, new ValueDatatypePair(null, Types.NULL));						// PV_SERVICE_PROVIDER
		inParam.put(27, new ValueDatatypePair(null, Types.NULL));						// PV_SERVICE_PROVIDER_SVC
		inParam.put(28, new ValueDatatypePair(PV_USER_TYPE, Types.VARCHAR));			// PV_USER_TYPE
		inParam.put(29, new ValueDatatypePair(PV_USER_ID, Types.VARCHAR));				// PV_USER_ID
		inParam.put(30, new ValueDatatypePair(PV_USER_PWD, Types.VARCHAR));				// PV_USER_PWD
				
		inParam.put(31, new ValueDatatypePair(PV_TRANS_TIME, Types.VARCHAR));			// PV_TRANS_TIME
		inParam.put(32, new ValueDatatypePair(PV_EXTERNAL_COMMIT, Types.VARCHAR));		// PV_EXTERNAL_COMMIT
		inParam.put(33, new ValueDatatypePair(PV_PREVENT_DUPLICATES, Types.VARCHAR));	// PV_PREVENT_DUPLICATES
		inParam.put(34, new ValueDatatypePair(PV_REQUEST_PWD, Types.VARCHAR));			// PV_REQUEST_PWD
		inParam.put(35, new ValueDatatypePair(PV_LOG_REPOSITORY, Types.VARCHAR));		// PV_LOG_REPOSITORY
		inParam.put(36, new ValueDatatypePair(PV_TEST_CALL_LEVEL, Types.NUMERIC));		// PV_TEST_CALL_LEVEL
		inParam.put(37, new ValueDatatypePair(PV_TRACE_LEVEL, Types.NUMERIC));			// PV_TRACE_LEVEL
		inParam.put(38, new ValueDatatypePair(PV_ERROR_CODE, Types.VARCHAR));			// PV_ERROR_CODE
		inParam.put(39, new ValueDatatypePair(PV_ERROR_SEVERITY, Types.NUMERIC));		// PV_ERROR_SEVERITY
		inParam.put(40, new ValueDatatypePair(PV_ERROR_MESSAGE, Types.VARCHAR));		// PV_ERROR_MESSAGE
				
		// OUT parameter values in the order received by the procedure
		LinkedHashMap<Integer, ValueDatatypePair> outParam = new LinkedHashMap<Integer, ValueDatatypePair>();
		outParam.put(35, new ValueDatatypePair(null, Types.VARCHAR));	// PV_LOG_REPOSITORY
		outParam.put(36, new ValueDatatypePair(null, Types.NUMERIC));	// PV_TEST_CALL_LEVEL
		outParam.put(37, new ValueDatatypePair(null, Types.NUMERIC));	// PV_TRACE_LEVEL
		outParam.put(38, new ValueDatatypePair(null, Types.VARCHAR));	// PV_ERROR_CODE
		outParam.put(39, new ValueDatatypePair(null, Types.NUMERIC)); // PV_ERROR_SEVERITY
		outParam.put(40, new ValueDatatypePair(null, Types.VARCHAR)); // PV_ERROR_MESSAGE
		
		// Execute procedure
		LinkedHashMap<Integer, ValueDatatypePair> resultParam = executeProcedure("XAPI_POSTING_SERVICE_V2", inParam, outParam);
				
		DBResponse dbResponse = new DBResponse();
		
		if (resultParam.get(0).getValue().equals("00")) {
			dbResponse.PV_LOG_REPOSITORY =	resultParam.get(35).getValue();	//PV_LOG_REPOSITORY
			dbResponse.PV_TEST_CALL_LEVEL =	resultParam.get(36).getValue(); //PV_TEST_CALL_LEVEL
			dbResponse.PV_TRACE_LEVEL = resultParam.get(37).getValue();		//PV_TRACE_LEVEL
			dbResponse.PV_ERROR_CODE = resultParam.get(38).getValue();		//PV_ERROR_CODE
			dbResponse.PV_ERROR_SEVERITY = resultParam.get(39).getValue();	//PV_ERROR_SEVERITY
			dbResponse.PV_ERROR_MESSAGE = resultParam.get(40).getValue();	//PV_ERROR_MESSAGE
		}
		
		return dbResponse;
		
	}
	
	public int saveRecord(final DBRequest dbRequest, final String responseCd, final String paymentReference, final String trans_method_name,
			final String trans_type, final String channelCode, final String Curr) {
		
		String Isreversal = dbRequest.getNarration().startsWith("(Reversal)") ?  "True" : "False";
		
			String sql = "INSERT INTO  "
					+ "ALT_QUICKTELLER(TRAN_APPL,TRAN_REF,FROM_ACCT_NUM,TRAN_RECEIVER,TRAN_AMOUNT,TRAN_STATUS,TRAN_NETHOD,NARRATION,TRAN_PURPOSE,CHANNEL_NAME ,ISREVERSAL,TRAN_DATE ,TRAN_TYPE ,PAYMENT_CURR, CHARGE_AMOUNT,TAX_AMOUNT) "
					+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

			// create the parameter values in the order needed by the insert statement
			List<ValueDatatypePair> param = new ArrayList<ValueDatatypePair>();
			param.add(new ValueDatatypePair(dbRequest.getInitiatingApp(), Types.VARCHAR));
			param.add(new ValueDatatypePair(paymentReference, Types.VARCHAR));
			param.add(new ValueDatatypePair(dbRequest.getSenderAcctNo(), Types.VARCHAR));
			param.add(new ValueDatatypePair(dbRequest.getBeneficiaryAcctNo(), Types.VARCHAR));
			param.add(new ValueDatatypePair(dbRequest.getAmount(), Types.DOUBLE));
			param.add(new ValueDatatypePair(responseCd, Types.VARCHAR));
			param.add(new ValueDatatypePair(trans_method_name, Types.VARCHAR));
			param.add(new ValueDatatypePair(dbRequest.getNarration(), Types.VARCHAR));
			param.add(new ValueDatatypePair("", Types.VARCHAR));
			param.add(new ValueDatatypePair(channelCode, Types.VARCHAR));
			param.add(new ValueDatatypePair(Isreversal, Types.VARCHAR));
			param.add(new ValueDatatypePair(DatabaseUtil.getCurrentDate() + "", Types.DATE));
			param.add(new ValueDatatypePair(trans_type, Types.VARCHAR));
			param.add(new ValueDatatypePair(Curr, Types.VARCHAR));
			param.add(new ValueDatatypePair(dbRequest.getChargeAmount(), Types.VARCHAR));
			param.add(new ValueDatatypePair(dbRequest.getTaxAmount(), Types.VARCHAR));
			
			return executeDML(sql, param);

	}
	
	public int saveSupposedReversal(String senderAcctNo, String receiverAcctNo,
			double transactionAmount, String errorCode, String errorDesc, String transactionMethod, 
			double chargeAmount, double taxAmount){
		
		String transactionReference = new QuicktellerConstants().getTransferCodePrefix() + "|" + new Date() + "|" + new Date().getTime();
				
		String sql = "INSERT INTO  "
				+ "ALT_REJECTED_ITEM (TRANS_REF, SENDER_ACCT_NO, RECEIVER_ACCT_NO, AMOUNT, ERROR_CODE, ERROR_DESC, STATUS, TRANS_METHOD, CHARGE_AMOUNT, TAX_AMOUNT) "
				+ " VALUES(?,?,?,?,?,?,?,?,?,?)";

		// create the parameter values in the order needed by the insert statement
		List<ValueDatatypePair> param = new ArrayList<ValueDatatypePair>();
		param.add(new ValueDatatypePair(transactionReference, Types.VARCHAR));
		param.add(new ValueDatatypePair(senderAcctNo, Types.VARCHAR));
		param.add(new ValueDatatypePair(receiverAcctNo, Types.VARCHAR));
		param.add(new ValueDatatypePair(transactionAmount, Types.DOUBLE));
		param.add(new ValueDatatypePair(errorCode, Types.VARCHAR));
		param.add(new ValueDatatypePair(errorDesc, Types.VARCHAR));
		param.add(new ValueDatatypePair("R", Types.VARCHAR));
		param.add(new ValueDatatypePair(transactionMethod, Types.VARCHAR));
		param.add(new ValueDatatypePair(chargeAmount, Types.DOUBLE));
		param.add(new ValueDatatypePair(taxAmount, Types.DOUBLE));
		
		return executeDML(sql, param);
	}
	
	
	// older implementation
	public DBResponse _ProcedureXAPI_POSTING_SERVICE(final String PV_CHANNEL_CD,
			String PV_CHANNEL_PWD, String PV_DEVICE_ID, String PV_SERVICE_CD,
			String PV_TRANS_TYPE_CD, String PV_ACCOUNT_NO,
			String PV_ACCOUNT_CURRENCY_CD, BigDecimal PV_ACCOUNT_AMOUNT,
			String PV_TRANS_CURRENCY_CD, BigDecimal PV_TRANS_AMOUNT,
			Date PV_VALUE_DATE, Date PV_TRANS_DATE, String PV_TRANS_REF,
			String PV_SUPPLEMENTARY_REF, String PV_NARRATIVE,
			String PV_CONTRA_ACCOUNT_NO, String PV_CONTRA_CURRENCY_CD,
			BigDecimal PV_CONTRA_AMOUNT, BigDecimal PV_EXCHANGE_RATE,
			String PV_CHARGE_CD, BigDecimal PV_CHARGE_AMOUNT,
			String PV_TAX_CD, BigDecimal PV_TAX_AMOUNT,
			String PV_REVERSAL_FLG, String PV_ORIGIN_BANK_CD,
			String PV_SERVICE_PROVIDER, String PV_SERVICE_PROVIDER_SVC,
			String PV_USER_TYPE, String PV_USER_ID, String PV_USER_PWD,
			String PV_TRANS_TIME, String PV_EXTERNAL_COMMIT, String PV_PREVENT_DUPLICATES, 
			String PV_REQUEST_PWD, String PV_LOG_REPOSITORY, int PV_TEST_CALL_LEVEL,
			int PV_TRACE_LEVEL, String PV_ERROR_CODE, int PV_ERROR_SEVERITY,
			String PV_ERROR_MESSAGE
			 
			 
			) throws Exception {
		
		

		String XAPI_TRANS_SERVICE = "{call XAPI_POSTING_SERVICE_V2(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";

		Connection dbConnection = connect();

		CallableStatement callableStatement = null;
		DBResponse dbResponse = new DBResponse();
			
		try {
			callableStatement = dbConnection.prepareCall(XAPI_TRANS_SERVICE);
			
			callableStatement.setString(1, PV_CHANNEL_CD); // PV_CHANNEL_CD
			callableStatement.setString(2, PV_CHANNEL_PWD); // PV_CHANNEL_PWD
			callableStatement.setString(3, PV_DEVICE_ID); // PV_DEVICE_ID
			callableStatement.setString(4, PV_SERVICE_CD); // PV_SERVICE_CD
			callableStatement.setString(5, PV_TRANS_TYPE_CD); // PV_TRANS_TYPE_CD
			callableStatement.setString(6, PV_ACCOUNT_NO); // PV_ACCOUNT_NO
			callableStatement.setString(7, PV_ACCOUNT_CURRENCY_CD); // PV_ACCOUNT_CURRENCY_CD
			callableStatement.setBigDecimal(8, PV_ACCOUNT_AMOUNT); // PV_ACCOUNT_AMOUNT
			callableStatement.setString(9, PV_TRANS_CURRENCY_CD); // PV_TRANS_CURRENCY_CD
			callableStatement.setBigDecimal(10, PV_TRANS_AMOUNT); // PV_TRANS_AMOUNT

			callableStatement.setDate(11, DatabaseUtil.getCurrentDate()); // PV_VALUE_DATE
			callableStatement.setDate(12, DatabaseUtil.getCurrentDate());// (12,// getCurrentDate());// //PV_TRANS_DATE
			callableStatement.setString(13, PV_TRANS_REF); // PV_TRANS_REF
			callableStatement.setString(14, PV_SUPPLEMENTARY_REF); // PV_SUPPLEMENTARY_REF
			callableStatement.setString(15, PV_NARRATIVE); // PV_NARRATIVE
			callableStatement.setString(16, PV_CONTRA_ACCOUNT_NO);// (16, "");// //PV_CONTRA_ACCOUNT_NO
			callableStatement.setString(17, PV_CONTRA_CURRENCY_CD); // PV_CONTRA_CURRENCY_CD
			callableStatement.setBigDecimal(18, PV_CONTRA_AMOUNT); // PV_CONTRA_AMOUNT// BigDecimal
			callableStatement.setBigDecimal(19, PV_EXCHANGE_RATE); // PV_EXCHANGE_RATE// BigDecimal
			callableStatement.setString(20, PV_CHARGE_CD); // PV_CHARGE_CD

			callableStatement.setBigDecimal(21, PV_CHARGE_AMOUNT); // PV_CHARGE_AMOUNT // BigDecimal
			callableStatement.setString(22, PV_TAX_CD);	//PV_TAX_CD               IN STRING,    
			callableStatement.setBigDecimal(23, PV_TAX_AMOUNT);	//PV_TAX_AMOUNT           IN NUMBER,
			callableStatement.setString(24, PV_REVERSAL_FLG); // PV_REVERSAL_FLG
			callableStatement.setNull(25, Types.VARCHAR); // PV_ORIGIN_BANK_CD
			callableStatement.setNull(26, Types.VARCHAR); // PV_SERVICE_PROVIDER
			callableStatement.setNull(27, Types.VARCHAR); // PV_SERVICE_PROVIDER_SVC
			callableStatement.setString(28, PV_USER_TYPE); // PV_USER_TYPE
			callableStatement.setString(29, PV_USER_ID); // PV_USER_ID
			callableStatement.setString(30, PV_USER_PWD); // PV_USER_PWD
			
			callableStatement.setString(31, PV_TRANS_TIME); //PV_TRANS_TIME           IN STRING,
			callableStatement.setString(32, PV_EXTERNAL_COMMIT); //PV_EXTERNAL_COMMIT      IN STRING,
			callableStatement.setString(33, PV_PREVENT_DUPLICATES);	//PV_PREVENT_DUPLICATES   IN STRING,
			callableStatement.setString(34, PV_REQUEST_PWD);	//PV_REQUEST_PWD          IN STRING,    
			callableStatement.setString(35, PV_LOG_REPOSITORY);	//PV_REQUEST_PWD 
			
			callableStatement.registerOutParameter(35, java.sql.Types.VARCHAR);  //PV_LOG_REPOSITORY       IN OUT STRING,
			callableStatement.registerOutParameter(36, java.sql.Types.NUMERIC); 	//PV_TEST_CALL_LEVEL      IN OUT NUMBER,      
			callableStatement.registerOutParameter(37, java.sql.Types.NUMERIC); // PV_TRACE_LEVEL	PV_TRACE_LEVEL          IN OUT NUMBER,   
			callableStatement.registerOutParameter(38, java.sql.Types.VARCHAR); // PV_ERROR_CODE	PV_ERROR_CODE           IN OUT STRING,    
			callableStatement.registerOutParameter(39, java.sql.Types.NUMERIC); // PV_ERROR_SEVERITY
			callableStatement.registerOutParameter(40, java.sql.Types.VARCHAR); // PV_ERROR_MESSAGE

			callableStatement.executeUpdate();

			int PV_ERROR_SEVERITY_lc = -1;
			
			int PV_TEST_CALL_LEVEL_lc = callableStatement.getInt(36);
			String PV_ERROR_CODE_lc = callableStatement.getString(38);
			PV_ERROR_SEVERITY_lc = callableStatement.getInt(39);
			String PV_ERROR_MESSAGE_lc = callableStatement.getString(40);
			int PV_TRACE_LEVEL_lc = callableStatement.getInt(37);
			String PV_LOG_REPOSITORY_lc = callableStatement.getString(35);

			dbResponse.PV_TEST_CALL_LEVEL = Integer.toString(PV_TEST_CALL_LEVEL_lc);
			dbResponse.PV_ERROR_CODE = PV_ERROR_CODE_lc;
			dbResponse.PV_ERROR_SEVERITY = Integer.toString(PV_ERROR_SEVERITY_lc);
			dbResponse.PV_ERROR_MESSAGE = PV_ERROR_MESSAGE_lc;
			dbResponse.PV_TRACE_LEVEL = Integer.toString(PV_TRACE_LEVEL_lc);
			dbResponse.PV_LOG_REPOSITORY = PV_LOG_REPOSITORY_lc;

			return dbResponse;

		} catch (SQLException e) {
			return dbResponse;
		} finally {

			if (callableStatement != null) {
				callableStatement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
	}
	
	// older implementation
	public int _saveRecordOld(final DBRequest dbRequest, final String responseCd, final String paymentReference, final String trans_method_name,
			final String trans_type, final String channelCode, final String Curr) throws Exception {
		
		String Isreversal = dbRequest.getNarration().startsWith("(Reversal)") ?  "True" : "False";
		
		//String content = CommonMethods.getInfo("core_systeminfo.txt", IntegrationSoapImpl.class);
		//content = CypherCrypt.deCypher(content);
		
		//String[] ipAndPort = content.split(",");
		//String	channelCode = ipAndPort[4].split("=>")[1].trim();
		//String Curr = ipAndPort[8].split("=>")[1].trim();

		Connection dbConnection = connect();

		PreparedStatement pst = null;
		ResultSet rs = null;

		try {
			String sql = "INSERT INTO  "
					+ "ALT_QUICKTELLER(TRAN_APPL,TRAN_REF,FROM_ACCT_NUM,TRAN_RECEIVER,TRAN_AMOUNT,TRAN_STATUS,TRAN_NETHOD,NARRATION,TRAN_PURPOSE,CHANNEL_NAME ,ISREVERSAL,TRAN_DATE ,TRAN_TYPE ,PAYMENT_CURR, CHARGE_AMOUNT,TAX_AMOUNT) "
					+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

			pst = dbConnection.prepareStatement(sql);
			pst.setString(1, dbRequest.getInitiatingApp()); // TRAN_APPL
			pst.setString(2, paymentReference); // TRAN_REF
			pst.setString(3, dbRequest.getSenderAcctNo()); // FROM_ACCT_NUM
			pst.setString(4, dbRequest.getBeneficiaryAcctNo()); // TRAN_RECEIVER
			pst.setDouble(5, Double.valueOf(dbRequest.getAmount())); // TRAN_AMOUNT
			pst.setString(6, responseCd); // TRAN_STATUS
			pst.setString(7, trans_method_name); // TRAN_NETHOD
			pst.setString(8, dbRequest.getNarration()); // NARRATION
			pst.setString(9, ""); // TRAN_PURPOSE
			pst.setString(10, channelCode); // CHANNEL_NAME
			pst.setString(11, Isreversal); // ISREVERSAL
			pst.setDate(12, DatabaseUtil.getCurrentDate()); // TRAN_DATE
			pst.setString(13, trans_type); // TRAN_TYPE
			pst.setString(14, Curr); // PAYMENT_CURR	,		
			pst.setString(15, dbRequest.getChargeAmount()); //CHARGE_AMOUNT
			pst.setString(16, dbRequest.getTaxAmount()); //TAX_AMOUNT
			int result = pst.executeUpdate();
			
			System.out.println("Table ALT_QUICKTELLER Insert successful");
			
			return result;
		}

		catch (Exception e) {
			// E.printStackTrace();
			System.out.println("Table ALT_QUICKTELLER Insert failed");
			return -1;
		} finally {

			if (dbConnection != null) {
				dbConnection.close();
			}
			if (pst != null) {
				pst.close();
			}
			if (rs != null) {
				rs.close();
			}
		}

	}
	
	
	
	//*** END of very specific methods
	
	
}
