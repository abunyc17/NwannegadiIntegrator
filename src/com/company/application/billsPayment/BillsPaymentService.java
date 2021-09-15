package com.company.application.billsPayment;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import com.company.application.balanceEnquiry.BalanceEnquiryService;
import com.company.application.billsPayment.data.BillPaymentRequest;
import com.neptunesoftware.reuseableClasses.CommonMethods;
import com.neptunesoftware.reuseableClasses.ResponseConstants;
import com.neptunesoftware.reuseableClasses.ResponseModel;
import com.neptunesoftware.reuseableClasses.Quickteller.Quickteller;
import com.neptunesoftware.reuseableClasses.Quickteller.QuicktellerConstants;
import com.neptunesoftware.reuseableClasses.Quickteller.QueryTransaction.QueryTransactionResponse;
import com.neptunesoftware.reuseableClasses.Quickteller.SendBillsPaymentAdvice.BillPaymentAdviceResponse;
import com.neptunesoftware.reuseableClasses.Rubikon.config.Errors;

public class BillsPaymentService {

	/*
	 * This Method works with the assumption that transaction amount received is in kobo
	 */
	
	public String BillsPayment(final String body){
		System.out.println("\n**** Start BillsPayment ****");

		BillPaymentRequest billPayment = (BillPaymentRequest) CommonMethods.JSONStringToObject(body, BillPaymentRequest.class);

		BillsPaymentDBOperation database = new BillsPaymentDBOperation();

		// convert transaction amount to Naira
		double tran_Amount = CommonMethods.koboToNaira(Integer.parseInt(billPayment.getTransactionAmount().trim()));
		double charge_Amount = CommonMethods.koboToNaira(Double.parseDouble(billPayment.getChargeAmount().trim()));
		double tax_Amount = CommonMethods.koboToNaira(Double.parseDouble(billPayment.getTaxAmount().trim()));
		
		// total charges
		double charges = Double.sum(charge_Amount, tax_Amount);
		// total amount
		double totalAmount = Double.sum(tran_Amount, charges);

		System.out.println("Total amount to be deducted: " + totalAmount);
		
		// check to make sure account has sufficient balance
		if (!new BalanceEnquiryService().hasSufficientFunds(billPayment.getFromAccountNumber(), totalAmount)) {

			// return insufficient funds when transaction amount is less than available
			// balance
			ResponseModel responseModel = new ResponseModel();
			responseModel.setResponseCode(ResponseConstants.INSUFFICIENT_CODE);
			responseModel.setResponseMessage(ResponseConstants.INSUFFICIENT_MESSAGE);

			String response = CommonMethods.ObjectToJsonString(responseModel);
			
			// log request and responses
			CommonMethods.logContent("*** BillsPayment Request ***" + body + "\r\n\r\n"
					+ "*** BillsPayment Request(Interswitch) *** \r\n\r\n"
					+ "*** BillsPayment Response(Interswitch) *** \r\n\r\n"
					+ "*** QueryTransaction Response *** \r\n\r\n"
					+ "*** BillsPayment Response ***" + response);
						
			return response;
		}

		// call procedure to deduct from the senders account
		System.out.println("\n\n Debit Customer");
		if (!database.isTransactionSuccessful(billPayment, false, "", "")) {

			// return error if deduction was not possible
			ResponseModel responseModel = new ResponseModel();
			responseModel.setResponseCode(ResponseConstants.PROCEDURE_CODE);
			responseModel.setResponseMessage(ResponseConstants.PROCEDURE_MESSAGE);

			String response = CommonMethods.ObjectToJsonString(responseModel);
			
			// log request and responses
			CommonMethods.logContent("*** BillsPayment Request ***" + body + "\r\n\r\n"
					+ "*** BillsPayment Request(Interswitch) *** \r\n\r\n"
					+ "*** BillsPayment Response(Interswitch) *** \r\n\r\n"
					+ "*** QueryTransaction Response *** \r\n\r\n"
					+ "*** BillsPayment Response ***" + response);
						
			return response;
		}

		// deduction was successful proceed

		// call Interswitch to carryout transfer
		Quickteller quickteller = new Quickteller();
		
		AtomicReference<String> requestReference = new AtomicReference<String>();
		
		String BPAdviceRespStr = quickteller.sendBillPaymentAdvice(billPayment.getPaymentCode(),
				billPayment.getCustomerId(), "", "", billPayment.getTransactionAmount(), requestReference);

		// Deserialize response string
		BillPaymentAdviceResponse BPAdviceResp = (BillPaymentAdviceResponse) CommonMethods.JSONStringToObject(BPAdviceRespStr, BillPaymentAdviceResponse.class);

		//Also deserialize response string to Error object incase error object was returned not BillPaymentAdviceResponse
		Errors errorResp = (Errors) CommonMethods.JSONStringToObject(BPAdviceRespStr, Errors.class);
		
		ResponseModel responseModel = new ResponseModel();
				
		// Return when neither BillPaymentAdviceResponse nor Error object was returned
		if (BPAdviceRespStr == null || BPAdviceRespStr.isEmpty()) {
			
			// call procedure to do a reversal (credit)
			System.out.println("\n\n Credit Customer");
			database.isTransactionSuccessful(billPayment, true, ResponseConstants.WEBSERVICE_UNAVAILABLE_CODE, "");

			String response = CommonMethods.ObjectToJsonString(responseModel);
			
			// and return if it is an error object
			responseModel.setResponseCode(ResponseConstants.WEBSERVICE_UNAVAILABLE_CODE);
			responseModel.setResponseMessage(ResponseConstants.WEBSERVICE_UNAVAILABLE_MESSAGE);

			// log request and responses
			CommonMethods.logContent("*** BillsPayment Request ***" + body + "\r\n\r\n"
					+ "*** BillsPayment Request(Interswitch) *** \r\n\r\n"
					+ "*** BillsPayment Response(Interswitch) ***" + BPAdviceRespStr + "\r\n\r\n"
					+ "*** QueryTransaction Response *** \r\n\r\n"
					+ "*** BillsPayment Response ***" + response);
						
			return response;
		}
		
		// When responseCodes from BillPaymentAdviceResponse object IS NOT one of the success/pending codes
		if (!(QuicktellerConstants.SUCCESS_CODES.toUpperCase().contains(BPAdviceResp.getResponseCode()))) {
			
			//set response object appropriately
			String message = "Interswitch ResponseCode: " + BPAdviceResp.getResponseCode() + "\r\n"
							+ "Interswitch ResponseMessage" + BPAdviceResp.getResponseMessage();
			
			responseModel.setResponseCode(ResponseConstants.WEBSERVICE_FAILED_RESPONSE_CODE);
			responseModel.setResponseMessage(message);
			
		}
		
		// When responseCodes from Error object IS NOT one of the success/pending codes
		if (!(QuicktellerConstants.SUCCESS_CODES.toUpperCase().contains(errorResp.error.getCode()))) {

			// set response object appropriately
			String message = "Interswitch ResponseCode: " + errorResp.error.getCode() + "\r\n"
					+ "Interswitch ResponseMessage" + errorResp.error.getMessage();

			responseModel.setResponseCode(ResponseConstants.WEBSERVICE_FAILED_RESPONSE_CODE);
			responseModel.setResponseMessage(message);

		}			

		// used when saving to the db
		String transferCode = requestReference.get() + "|" + new Date() + "|" + new Date().getTime();

		// call query transaction to confirm transaction
		System.out.println("ReferenceCode: " + requestReference.get());
		System.out.println("TransactionRef: " + BPAdviceResp.getTransactionRef());
		
		String queryTransRespStr = quickteller.queryTransaction(requestReference.get());
		
		// convert response to QueryTransactionResponse object
		QueryTransactionResponse queryTransactionResponse = (QueryTransactionResponse) CommonMethods.JSONStringToObject(queryTransRespStr, QueryTransactionResponse.class);
		
		// convert response to Error object
		Errors errorResponse = (Errors) CommonMethods.JSONStringToObject(queryTransRespStr, Errors.class);
				
		// if Error object was returned and response code is not one of the success codes
		if (!QuicktellerConstants.SUCCESS_CODES.contains(errorResponse.error.getCode().trim())) {
			
			// call procedure to do a reversal (credit)
			System.out.println("\n\n Credit Customer");
			database.isTransactionSuccessful(billPayment, true, ResponseConstants.WEBSERVICE_FAILED_RESPONSE_CODE, transferCode);
						
			//set response object appropriately
			String message = "Interswitch ResponseCode: " + errorResponse.error.getCode() + "\r\n"
							+ "Interswitch ResponseMessage" + errorResponse.error.getMessage();
			
			responseModel.setResponseCode(ResponseConstants.WEBSERVICE_FAILED_RESPONSE_CODE);
			responseModel.setResponseMessage(responseModel.getResponseMessage().isEmpty()
										? message
										: responseModel.getResponseMessage());
			
			String response = CommonMethods.ObjectToJsonString(responseModel);
			
			// log request and responses
			CommonMethods.logContent("*** ExternalFundsTransfer Request ***" + body + "\r\n\r\n"
					+ "*** BillsPayment Request(Interswitch) *** \r\n\r\n"
					+ "*** BillsPayment Response(Interswitch) ***" + BPAdviceRespStr + "\r\n\r\n"
					+ "*** QueryTransaction Response ***" + queryTransRespStr + "\r\n\r\n"
					+ "*** BillsPayment Response ***" + response);
			
			return response;
		}

		// successful, proceed
		// get transactionResponseCode
		String transactionResponseCode = queryTransactionResponse.getTransactionResponseCode();
		
		// if response code returned is not one of the success codes
		if (!(QuicktellerConstants.SUCCESS_CODES.toUpperCase().contains(transactionResponseCode))) {

			// call procedure to do a reversal (credit)
			System.out.println("\n\n Credit Customer");
			database.isTransactionSuccessful(billPayment, true, ResponseConstants.WEBSERVICE_FAILED_RESPONSE_CODE, transferCode);
			
			//set response object appropriately
			String message = "Interswitch ResponseCode: " + transactionResponseCode + "\r\n"
					+ "Interswitch ResponseMessage" + queryTransactionResponse.getStatus();
			
			responseModel.setResponseCode(ResponseConstants.WEBSERVICE_FAILED_RESPONSE_CODE);
			responseModel.setResponseMessage(responseModel.getResponseMessage().isEmpty()
										? message
										: responseModel.getResponseMessage());
			
		} else {
			//set response object appropriately
			responseModel.setResponseCode(ResponseConstants.SUCCEESS_CODE);
			responseModel.setResponseMessage(ResponseConstants.SUCCEESS_MESSAGE);
		}

		String response = CommonMethods.ObjectToJsonString(responseModel);
		
		// log request and responses
		CommonMethods.logContent("*** ExternalFundsTransfer Request ***" + body + "\r\n\r\n"
				+ "*** ExternalFundsTransfer Request(Interswitch) *** \r\n\r\n"
				+ "*** ExternalFundsTransfer Response(Interswitch) ***" + BPAdviceRespStr + "\r\n\r\n"
				+ "*** QueryTransaction Response ***" + queryTransRespStr + "\r\n\r\n"
				+ "*** ExternalFundsTransfer Response ***" + response);

		System.out.println("\n**** End BillsPayment (finish) ****");

		return response;
	}

	
	public static void main(String[] args) {
		String body = "{\r\n" + 
				"	\"initiatingApp\": \"MAPP\",\r\n" + 
				"	\"paymentCode\": \"90501\",\r\n" + 
				"	\"customerId\": \"0000000001\",\r\n" + 
				"	\"fromAccountNumber\": \"001204000008\",\r\n" + 
				"	\"billerCategoryID\": \"1\",\r\n" + 
				"	\"billerDescription\": \"Pay your utility bills here\",\r\n" + 
				"	\"transactionAmount\": \"10000\",\r\n" + 
				"	\"transactionDescription\": \"payment for bills\",\r\n" + 
				"	\"cardNumber\": \"\",	\r\n" + 
				"	\"chargeAmount\": \"10\",\r\n" + 
				"	\"taxAmount\": \"2.5\"\r\n" + 
				"}";
		System.out.println(new BillsPaymentService().BillsPayment(body));
	}
	
}
