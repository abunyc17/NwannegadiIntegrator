package com.neptunesoftware.reuseableClasses.Quickteller.PostingDB;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import com.company.application.airtimeRecharge.data.AirtimeRechargeRequest;
import com.company.application.billsPayment.data.BillPaymentRequest;
import com.company.application.fundTransfer.data.ExternalFTRequest;
import com.neptunesoftware.reuseableClasses.CommonMethods;
import com.neptunesoftware.reuseableClasses.ResponseConstants;
import com.neptunesoftware.reuseableClasses.ResponseModel;
import com.neptunesoftware.reuseableClasses.Database.DatabaseUtil;
import com.neptunesoftware.reuseableClasses.Database.SybaseDatabase;
import com.neptunesoftware.reuseableClasses.Database.config.DBRequest;
import com.neptunesoftware.reuseableClasses.Database.config.ValueDatatypePair;
import com.neptunesoftware.reuseableClasses.Quickteller.QuicktellerConstants;
import com.neptunesoftware.reuseableClasses.Rubikon.config.RubikonCredential;
import com.neptunesoftware.reuseableClasses.Rubikon.config.RubikonUtil;

public class CommonPostingDBSybase extends SybaseDatabase {


	public CommonPostingDBSybase() {
		super();
	}
	
	public CommonPostingDBSybase(String databaseName) {
		super(databaseName);
	}
		
	public CommonPostingDBSybase(String driver, String connectionURL, String username, String password, String databaseType) {
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
				"GL", narration, chargeAmount+"", taxAmount+"", airtimeRchgeXfer.getInitiatingApp());
		
		
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
				"GL", narration, chargeAmount+"", taxAmount+"", billPayment.getInitiatingApp());
		
		
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
				"GL", narration, chargeAmount+"", taxAmount+"",
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
						
		ResponseModel procedureResp = postTransaction(
				dbRequest.getSenderAcctNo(),  					// fromAccountNumber
				dbRequest.getBeneficiaryAcctNo(), 				// toAccountNumber
				new BigDecimal(dbRequest.getAmount()),			// transAmount
				new BigDecimal(dbRequest.getChargeAmount()),	// chargeAmount
				new BigDecimal(dbRequest.getTaxAmount()),		// taxAmount
				dbRequest.getNarration(),					// transactionDescription
				transactionRef,								// transactionReference
				false); 
				
		System.out.println("callProdecure_csp_mapp_TranPosting Response: " + procedureResp.getResponseCode());
		
		return procedureResp.getResponseCode();
	}
	
	public ResponseModel postTransaction(String fromAccountNumber, String toAccountNumber, 
			BigDecimal transAmount, BigDecimal chargeAmount, BigDecimal taxAmount,
			String transDescription, String transactionReference, boolean isReversal
			) {
		
		RubikonCredential rubikonCredential = RubikonUtil.readRubikonConfig();	
		String chargeCode = rubikonCredential.getChargeCode();
		
		String reversalFlag = isReversal ? "Y" : "N", debitCrditFlag = "DR";
		
		// IN parameter values in the order received by the procedure
		LinkedHashMap<Integer, ValueDatatypePair> inParam = new LinkedHashMap<Integer, ValueDatatypePair>();
		inParam.put(1,  new ValueDatatypePair("97", Types.INTEGER)); 				//initiatingApp
		inParam.put(2,  new ValueDatatypePair(fromAccountNumber, Types.VARCHAR));	//fromAccountNumber
		inParam.put(3,  new ValueDatatypePair(toAccountNumber, Types.VARCHAR));		//toAccountNumber
		inParam.put(4,  new ValueDatatypePair(transAmount, Types.DECIMAL));			//transactionAmount
		inParam.put(5,  new ValueDatatypePair(transDescription, Types.VARCHAR));	//transactionDescription
		inParam.put(6,  new ValueDatatypePair(transactionReference, Types.VARCHAR));//transactionReference
		inParam.put(7,  new ValueDatatypePair(chargeCode, Types.INTEGER));			//chargeCode
		inParam.put(8,  new ValueDatatypePair(chargeAmount, Types.DECIMAL));		//chargeAmount
		inParam.put(9,  new ValueDatatypePair(taxAmount, Types.DECIMAL));			//taxAmount
		inParam.put(10,  new ValueDatatypePair(debitCrditFlag, Types.VARCHAR));		//credit/ debit flag
		inParam.put(11,  new ValueDatatypePair(reversalFlag, Types.VARCHAR));		//reversalFlg
		
		// OUT parameter values in the order received by the procedure
		LinkedHashMap<Integer, ValueDatatypePair> outParam = new LinkedHashMap<Integer, ValueDatatypePair>();
		outParam.put(12,  new ValueDatatypePair(Types.INTEGER));					//errorCode
		outParam.put(13,  new ValueDatatypePair(Types.VARCHAR));					//errorMessage
		
		// Execute procedure
		LinkedHashMap<Integer, ValueDatatypePair> resultParam = executeProcedure("csp_mapp_TranPosting", inParam, outParam);
		
		String errorCode = "", errorMessage = "";
		if (resultParam.get(0).getValue().equals(ResponseConstants.SUCCEESS_CODE)) {
			errorCode = resultParam.get(12).getValue(); 	//errorCode
			errorMessage = resultParam.get(13).getValue(); 	//errorMessage
		}
		
		ResponseModel response = new ResponseModel();
		response.setResponseCode(ResponseConstants.SUCCEESS_CODE);
		response.setResponseMessage(ResponseConstants.SUCCEESS_MESSAGE);
		
		// procedure returns 0 for errorCode if successful
		if(!errorCode.equals("0")) {
			response.setResponseCode(errorCode);
			response.setResponseMessage(errorMessage);
		}
				
		return response;
		
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
			param.add(new ValueDatatypePair(dbRequest.getChargeAmount(), Types.DOUBLE));
			param.add(new ValueDatatypePair(dbRequest.getTaxAmount(), Types.DOUBLE));
			
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
	
	

}
