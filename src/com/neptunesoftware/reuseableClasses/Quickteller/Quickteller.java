package com.neptunesoftware.reuseableClasses.Quickteller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import com.neptunesoftware.reuseableClasses.CommonMethods;
import com.neptunesoftware.reuseableClasses.CypherCrypt;
import com.neptunesoftware.reuseableClasses.ResponseConstants;
import com.neptunesoftware.reuseableClasses.ResponseModel;
import com.neptunesoftware.reuseableClasses.Quickteller.FundTransfer.AccountReceivable;
import com.neptunesoftware.reuseableClasses.Quickteller.FundTransfer.Beneficiary;
import com.neptunesoftware.reuseableClasses.Quickteller.FundTransfer.FundTransferRequest;
import com.neptunesoftware.reuseableClasses.Quickteller.FundTransfer.Initiation;
import com.neptunesoftware.reuseableClasses.Quickteller.FundTransfer.Sender;
import com.neptunesoftware.reuseableClasses.Quickteller.FundTransfer.Termination;
import com.neptunesoftware.reuseableClasses.Quickteller.GetBankCode.BankResponse;
import com.neptunesoftware.reuseableClasses.Quickteller.NameEnquiry.NameEnquiryResponse;
import com.neptunesoftware.reuseableClasses.Quickteller.SendBillsPaymentAdvice.BillPaymentAdviceRequest;
import com.neptunesoftware.reuseableClasses.Quickteller.data.QuicktellerConfig;
import com.neptunesoftware.reuseableClasses.Quickteller.data.QuicktellerCredential;
import com.neptunesoftware.reuseableClasses.Rubikon.config.Errors;
import com.neptunesoftware.reuseableClasses.WebserviceCall.HttpResponse;
import com.neptunesoftware.reuseableClasses.WebserviceCall.WebserviceCall;


public class Quickteller {

	private QuicktellerConstants quicktellerConstants;
	private WebserviceCall webserviceCall;
	
	
	public Quickteller() {
		QuicktellerCredential quicktellerCredential = readQuicktellerConfig();
		
		if(!quicktellerCredential.getBaseUrl().isEmpty())
			this.quicktellerConstants = new QuicktellerConstants()
											.baseUrl(quicktellerCredential.getBaseUrl())
											.clientId(quicktellerCredential.getClientId())
											.clientSecret(quicktellerCredential.getClientSecret())
											.transferCodePrefix(quicktellerCredential.getTransferCodePrefix())
											.terminalId(quicktellerCredential.getTerminalId())
											.initiatingEntityCode(quicktellerCredential.getInitiatingEntityCode())
											.signatureMethod(quicktellerCredential.getSignatureMethod());
		else
			this.quicktellerConstants = new QuicktellerConstants();
		
		this.webserviceCall = new WebserviceCall(quicktellerConstants.getBaseUrl());
	}
	
	public Quickteller(String baseUrl, String clientId, String clientSecret, String initiatingEntityCode,
    		String transferCodePrefix, String terminalId, String signatureMethod) {
		
		this.quicktellerConstants = new QuicktellerConstants(baseUrl, clientId, clientSecret, initiatingEntityCode,
	    													transferCodePrefix, terminalId, signatureMethod);
		this.webserviceCall = new WebserviceCall(quicktellerConstants.getBaseUrl());
	}
	
    public Quickteller baseUrl(String baseUrl) {
    	this.quicktellerConstants = quicktellerConstants.baseUrl(baseUrl);
    	return this;
    }
    
    public Quickteller clientId(String clientId) {
    	this.quicktellerConstants = quicktellerConstants.clientId(clientId);
    	return this;
    }
    
    public Quickteller clientSecret(String clientSecret) {
    	this.quicktellerConstants = quicktellerConstants.clientSecret(clientSecret);
    	return this;
    }
        
    public Quickteller initiatingEntityCode(String initiatingEntityCode) {
    	this.quicktellerConstants = quicktellerConstants.initiatingEntityCode(initiatingEntityCode);
    	return this;
    }
    
    public Quickteller transferCodePrefix(String transferCodePrefix) {
    	this.quicktellerConstants = quicktellerConstants.transferCodePrefix(transferCodePrefix);
    	return this;
    }
    
    public Quickteller terminalId(String terminalId) {
    	this.quicktellerConstants = quicktellerConstants.terminalId(terminalId);
    	return this;
    }
    
    public Quickteller signatureMethod(String signatureMethod) {
    	this.quicktellerConstants = quicktellerConstants.signatureMethod(signatureMethod);
    	return this;
    }
    
	
    public String getClientId() {
    	return this.quicktellerConstants.getBaseUrl();
    }
	
	public static void main(String[] args) {
		
//		Quickteller quickteller = new Quickteller().baseUrl("hello").clientId("hi").clientSecret("there");
//		
//		System.out.println("ClientId: " + quickteller.getClientId());
		
//		Quickteller quickteller = new Quickteller();
//		//System.out.println("getBanks: \n\r" + quickteller.getBankCodes());
//		System.out.println("name: \n\r" + quickteller.nameEnquiry("063", "0034819411"));
		
//		System.out.println(CommonMethods.objectToXml(readQuicktellerConfig()));
		
	}
	
	
	
	
	
	
	
	
	//******* GET methods *********
	
	public String getBankCodes(){
		
		HashMap<String, String> extraHeaders = new HashMap<String, String>();
		extraHeaders = commonHeaders(QuicktellerConstants.GET_BANK_CODE_URL, QuicktellerConstants.GET);
		
		HttpResponse httpResponse = webserviceCall.getMethod(QuicktellerConstants.GET_BANK_CODE_URL, extraHeaders);

		String resp = removeLocalBank(httpResponse.getResponseBody());
		return resp;
	}

	public String getBillers(){
		
		HashMap<String, String> extraHeaders = new HashMap<String, String>();
		extraHeaders = commonHeaders(QuicktellerConstants.GET_BILLER_URL, QuicktellerConstants.GET);

		HttpResponse httpResponse = webserviceCall.getMethod(QuicktellerConstants.GET_BILLER_URL, extraHeaders);

		String resp = httpResponse.getResponseBody();
		return resp;
	}

	public String getBillerCategories() {

		HashMap<String, String> extraHeaders = new HashMap<String, String>();
		extraHeaders = commonHeaders(QuicktellerConstants.GET_BILLER_CATEGORIES_URL, QuicktellerConstants.GET);

		HttpResponse httpResponse = webserviceCall.getMethod(QuicktellerConstants.GET_BILLER_CATEGORIES_URL, extraHeaders);

		String resp = httpResponse.getResponseBody();
		return resp;
	}

	public String getBillersByCategory(String id) {
		
		String URL = QuicktellerConstants.GET_BILLER_BY_CATEGORY_URL_PREFIX + id + QuicktellerConstants.GET_BILLER_BY_CATEGORY_URL_SUFFIX;

		HashMap<String, String> extraHeaders = new HashMap<String, String>();
		extraHeaders = commonHeaders(URL, QuicktellerConstants.GET);

		HttpResponse httpResponse = webserviceCall.getMethod(URL, extraHeaders);

		String resp = httpResponse.getResponseBody();
		return resp;
	}

	public String getBillersPaymentItems(String billerId) {
		
		String URL = QuicktellerConstants.GET_BILLER_PAYMENT_ITEMS_URL_PREFIX + billerId
				+ QuicktellerConstants.GET_BILLER_PAYMENT_ITEMS_URL_SUFFIX;

		HashMap<String, String> extraHeaders = new HashMap<String, String>();
		extraHeaders = commonHeaders(URL, QuicktellerConstants.GET);

		HttpResponse httpResponse = webserviceCall.getMethod(URL, extraHeaders);

		String resp = httpResponse.getResponseBody();
		return resp;
	}

	public String queryTransaction(String requestreference) {
		// WebserviceCall webserviceCall = new WebserviceCall();

		String URL = QuicktellerConstants.QUERY_TRANSACTION_URL + requestreference;

		HashMap<String, String> extraHeaders = new HashMap<String, String>();
		extraHeaders = commonHeaders(URL, QuicktellerConstants.GET);

		HttpResponse httpResponse = new HttpResponse();
		try {
			httpResponse = webserviceCall.getMethod(URL, extraHeaders);
		} catch (Exception e) {}

		String resp = httpResponse.getResponseBody() == null ? "" : httpResponse.getResponseBody();
		return resp;
	}
	
	public String nameEnquiry(String bankCode, String accountNo) {
		
		HashMap<String, String> extraHeaders = new HashMap<String, String>();
		extraHeaders = commonHeaders(QuicktellerConstants.NAME_ENQUIRY_URL, QuicktellerConstants.GET);
		extraHeaders.put("bankCode", bankCode);
		extraHeaders.put("accountId", accountNo);

		HttpResponse httpResponse = webserviceCall.getMethod(QuicktellerConstants.NAME_ENQUIRY_URL, extraHeaders);

		String resp = customizeResponse(httpResponse.getResponseBody());
		return resp;
	}

	
	
	
	//******* POST methods *********	
	
	public String fundTransfer(String beneficiaryAcctNumber, String beneficiaryName, String amountInKobo, 
			String beneficiaryBankCode, String senderName) {
		
		FundTransferRequest fundTransferRequest = createFundTransferRequest(beneficiaryAcctNumber, 
				beneficiaryName, amountInKobo, beneficiaryBankCode, senderName);
		
		String fundTransferRequestStr = CommonMethods.ObjectToJsonString(fundTransferRequest);

		HttpResponse httpResponse = fundTransferService(fundTransferRequestStr);
		
		if (httpResponse == null || httpResponse.getStatusCode() == -1)
			return "";
		
		return httpResponse.getResponseBody();
		
	}
	
	public String sendBillPaymentAdvice(String paymentCode, String customerId, String customerMobile, 
			String customerEmail, String amount, AtomicReference<String> requestReference) {
		
		BillPaymentAdviceRequest billPaymentAdviceRequest = createBillPaymentAdviceRequest(paymentCode, customerId, 
				customerMobile, customerEmail, amount);
		
		// used as a reference variable so the value of requestReference can be accessed outside this class
		requestReference.set(billPaymentAdviceRequest.requestReference);
		
		String billPaymentAdviceRequestStr = CommonMethods.ObjectToJsonString(billPaymentAdviceRequest);
		
		HttpResponse httpResponse = sendBillPaymentAdviceInterswitch(billPaymentAdviceRequestStr);
		
		if (httpResponse == null || httpResponse.getStatusCode() == -1)
			return "";
		
		return httpResponse.getResponseBody();
		
	}
	
	public String customerValidation(String body) {
		// WebserviceCall webserviceCall = new WebserviceCall();

		HashMap<String, String> extraHeaders = new HashMap<String, String>();
		extraHeaders = commonHeaders(QuicktellerConstants.CUSTOMER_VALIDATION_URL, QuicktellerConstants.POST);

		HttpResponse httpResponse = webserviceCall.postMethod(QuicktellerConstants.CUSTOMER_VALIDATION_URL, body, extraHeaders);

		String resp = httpResponse.getResponseBody();
		return resp;
	}
	
	
	
	
	
	//****** Used by getBankCodes *******
	private String removeLocalBank(String responseBody){
		
		// if response is empty, return
		if (responseBody.isEmpty() && responseBody.equals(null))
			return CommonMethods.ObjectToJsonString(new BankResponse(ResponseConstants.WEBSERVICE_UNAVAILABLE_CODE, ResponseConstants.WEBSERVICE_UNAVAILABLE_MESSAGE));
		
		//if response is an error instance, return
		Errors errorResp = (Errors) CommonMethods.JSONStringToObject(responseBody, Errors.class);
		if(!errorResp.error.getCode().isEmpty()) {
			return customizeErrorResponse(errorResp);
		}
				
		BankResponse bankResponse = new BankResponse();
		
		try {
			bankResponse = (BankResponse) CommonMethods.JSONStringToObject(responseBody, BankResponse.class);
						
		} catch (Exception e) {
			String message = ResponseConstants.EXCEPTION_MESSAGE + "- " + e.getMessage();
			return CommonMethods.ObjectToJsonString(new BankResponse(ResponseConstants.EXCEPTION_CODE, message));
		}
		
		//remove from the list of banks Amju's cbn code
		bankResponse.getBanks().removeIf(bank -> bank.getCbnCode().equals("306"));
		bankResponse.setResponseCode(ResponseConstants.SUCCEESS_CODE);
		bankResponse.setResponseMessage(ResponseConstants.SUCCEESS_MESSAGE);
		
		return CommonMethods.ObjectToJsonString(bankResponse);
	}
	
	//****** Used by nameEnquiry *******
	private String customizeResponse(String responseBody){
		
		// if response is empty, return
		if (responseBody.isEmpty() && responseBody.equals(null))
			return CommonMethods.ObjectToJsonString(new NameEnquiryResponse(ResponseConstants.WEBSERVICE_UNAVAILABLE_CODE, ResponseConstants.WEBSERVICE_UNAVAILABLE_MESSAGE));
		
		//if response is an error instance, return
		Errors errorResp = (Errors) CommonMethods.JSONStringToObject(responseBody, Errors.class);
		if(!errorResp.error.getCode().isEmpty()) {
			return customizeErrorResponse(errorResp);
		}
		
		NameEnquiryResponse nameEnquiryResponse = new NameEnquiryResponse();
		
		try {
			nameEnquiryResponse = (NameEnquiryResponse) CommonMethods.JSONStringToObject(responseBody, NameEnquiryResponse.class);
		} catch (Exception e) {
			String message = ResponseConstants.EXCEPTION_MESSAGE + "- " + e.getMessage();
			return CommonMethods.ObjectToJsonString(new NameEnquiryResponse(ResponseConstants.EXCEPTION_CODE, message));
		}
		
		nameEnquiryResponse.setResponseCode(ResponseConstants.SUCCEESS_CODE);
		nameEnquiryResponse.setResponseMessage(ResponseConstants.SUCCEESS_MESSAGE);
		
		return CommonMethods.ObjectToJsonString(nameEnquiryResponse);
	}
	
	private String customizeErrorResponse(Errors errorResp){	
		ResponseModel responseModel = new ResponseModel();
		responseModel.setResponseCode(ResponseConstants.WEBSERVICE_FAILED_RESPONSE_CODE);
		responseModel.setResponseMessage(CommonMethods.ObjectToJsonString(errorResp.error));
		
		return CommonMethods.ObjectToJsonString(responseModel);
	}
	
	
	
	
	//*** used by fundTransfer ****
	private FundTransferRequest createFundTransferRequest(String beneficiaryAcctNumber,
			String beneficiaryName, String amount, String beneficiaryBankCode, String senderName) {
		FundTransferRequest fundTransfer = null;

		try {
		
			fundTransfer = new FundTransferRequest();

			Beneficiary beneficiary = new Beneficiary("", "", beneficiaryName, beneficiaryName);
			Initiation initiation = new Initiation(amount, QuicktellerConstants.CURRENCY_CODE_NUMBER,
					QuicktellerConstants.INITIATING_PAYMENT_METHOD_CODE, QuicktellerConstants.CHANNEL_LOCATION);
			Sender sender = new Sender("", "", senderName, senderName);

			AccountReceivable accountReceivable = new AccountReceivable(beneficiaryAcctNumber,
					QuicktellerConstants.ACCOUNT_TYPE_DEFAULT);
			Termination termination = new Termination(amount, beneficiaryBankCode, QuicktellerConstants.CURRENCY_CODE_NUMBER,
					QuicktellerConstants.TERMINATING_PAYMENT_METHOD_CODE, QuicktellerConstants.COUNTRY_CODE);
			termination.setAccountReceivable(accountReceivable);

			// set the MAC value for the request object
			String macCipher = "" + initiation.getAmount() + initiation.getCurrencyCode()
					+ initiation.getPaymentMethodCode() + termination.getTerminationAmount()
					+ termination.getTerminationCurrencyCode() + termination.getTerminationPaymentMethodCode()
					+ termination.getTerminationCountryCode();
			String MAC = QuicktellerConstants.SHA512(macCipher);

			fundTransfer.mac = MAC;
			fundTransfer.beneficiary = beneficiary;
			fundTransfer.initiatingEntityCode = quicktellerConstants.getInitiatingEntityCode();
			fundTransfer.initiation = initiation;
			fundTransfer.sender = sender;
			fundTransfer.termination = termination;
			fundTransfer.transferCode = quicktellerConstants.getTransferCodePrefix() + QuicktellerConstants.timeStamp();

			return fundTransfer;
			

		} catch (Exception e) {
			System.out.println("Service Endpoint Unavailable.");
			return fundTransfer;
		}
	}
	
	private HttpResponse fundTransferService(String body) {
		
		HttpResponse httpResponse = new HttpResponse();
		try {
			FundTransferRequest fundTransferReq = (FundTransferRequest) CommonMethods.JSONStringToObject(body, FundTransferRequest.class);

			// set the MAC value for the request object
			fundTransferReq.mac = generateMAC(fundTransferReq);

			// create a json string from the request object
			String request = CommonMethods.ObjectToJSONOrXMLstring(body, fundTransferReq);

			HashMap<String, String> extraHeaders = new HashMap<String, String>();
			extraHeaders = commonHeaders(QuicktellerConstants.FUNDS_TRANSFER_URL, QuicktellerConstants.POST);

			httpResponse = webserviceCall.postMethod(QuicktellerConstants.FUNDS_TRANSFER_URL, request, extraHeaders);

			// String resp = httpResponse.getResponseBody();
			return httpResponse;

		} catch (Exception ex) {
			return null;
		}

	}
	
	private String generateMAC(FundTransferRequest fundTransfer){
		try {
			// collect the iniatiation object
			Initiation initiation = new Initiation();
			initiation = fundTransfer.initiation;

			// collect the termination object
			Termination termination = new Termination();
			termination = fundTransfer.termination;

			// compute the MAC cipher
			String macCipher = "" + initiation.getAmount() + initiation.getCurrencyCode()
					+ initiation.getPaymentMethodCode() + termination.getTerminationAmount()
					+ termination.getTerminationCurrencyCode() + termination.getTerminationPaymentMethodCode()
					+ termination.getTerminationCountryCode();

			// encode MAC cipher
			return QuicktellerConstants.SHA512(macCipher);

		} catch (Exception ex) {
			return "";
		}
	}
		
	
	//*** used by sendBillPaymentAdvice ****
	public BillPaymentAdviceRequest createBillPaymentAdviceRequest(String paymentCode,
			String customerId, String customerMobile, String customerEmail, String amount) {
		
		BillPaymentAdviceRequest BPA = null;

		try {
			BPA = new BillPaymentAdviceRequest();

			BPA.terminalId = quicktellerConstants.getTerminalId();
			BPA.paymentCode = paymentCode;
			BPA.customerId = customerId;
			BPA.customerMobile = customerMobile;
			BPA.customerEmail = customerEmail;
			BPA.amount = amount;
			BPA.requestReference = quicktellerConstants.getTransferCodePrefix() + QuicktellerConstants.timeStamp().substring(2);

			return BPA;

		} catch (Exception e) {
			System.out.println("Service Endpoint Unavailable.");
			return BPA;
		}
	}
	
	public HttpResponse sendBillPaymentAdviceInterswitch(String request) {
		//This method is used by BillspAyment and AirtimeRecharge
		
		HashMap<String, String> extraHeaders = new HashMap<String, String>();
		extraHeaders = commonHeaders(QuicktellerConstants.SEND_BILL_PAYMENT_ADVICE_URL, QuicktellerConstants.POST);

		HttpResponse httpResponse = webserviceCall.postMethod(QuicktellerConstants.SEND_BILL_PAYMENT_ADVICE_URL, request, extraHeaders);

		//String resp = httpResponse.getResponseBody();
		return httpResponse;
	}

	
	//*** used by all GET and POST methods
	private HashMap<String, String> commonHeaders(String path, String httpMethod){
		
		String url = quicktellerConstants.getBaseUrl() + path;
		
		String encodedResourceUrl = "";
		try {
			encodedResourceUrl = URLEncoder.encode(url, "ISO-8859-1"); // new String(url.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {}
		
		String timestamp = QuicktellerConstants.timeStamp();
		String nonce = QuicktellerConstants.nonce();
		
		String signatureCipher = httpMethod + "&" + encodedResourceUrl + "&" + timestamp + "&" + nonce + "&" 
								+ quicktellerConstants.getClientId() + "&" + quicktellerConstants.getClientSecret();
		
		String signature = QuicktellerConstants.signature(signatureCipher, quicktellerConstants.getSignatureMethod());
		
		
		HashMap<String, String> commonHeaders = new HashMap<String, String>();
		
		commonHeaders.put("TerminalId", quicktellerConstants.getTerminalId());
				
		commonHeaders.put("Content-Type", QuicktellerConstants.CONTENT_TYPE);
		
		commonHeaders.put("Authorization", quicktellerConstants.getAuthorization());
		
		commonHeaders.put("Timestamp", timestamp);

		commonHeaders.put("Nonce", nonce);

		commonHeaders.put("SignatureMethod", quicktellerConstants.getSignatureMethod());

		commonHeaders.put("Signature", signature);
		
		return commonHeaders;
	}
	
	
	//*** used by constructor
	public static QuicktellerCredential readQuicktellerConfig() {

		// used to know whether there's a default set
		boolean hasDefaultValue = false;
		
		QuicktellerCredential quicktellerCredential = new QuicktellerCredential();
		QuicktellerConfig quicktellerConfig = readConfig();
		
		if (quicktellerConfig.getResponseCode().equals(ResponseConstants.SUCCEESS_CODE)) {
			for (QuicktellerCredential quicktellerCred : quicktellerConfig.getQuicktellerCredential()) {
				if (quicktellerCred.getDefaultValue().trim().equalsIgnoreCase("true")
					|| quicktellerCred.getDefaultValue().trim().equalsIgnoreCase("yes")) {

					hasDefaultValue = true; // yes there is a default set
					
					quicktellerCredential = quicktellerCred;
					break;
				}
			}
			
			// in the absence of a default value, the first configuration is used
			if(!hasDefaultValue) {
				for (QuicktellerCredential quicktellerCred : quicktellerConfig.getQuicktellerCredential()) {					
					quicktellerCredential = quicktellerCred;
					
					System.out.println("\nNo default value was set in QuicktellerInfo.xml. First configuration is used instead \n");
					break;
				}
			}
		}
		
		return quicktellerCredential;
	}
	
	private static QuicktellerConfig readConfig() {
		
		QuicktellerConfig quicktellerConfig = new QuicktellerConfig();
		
		try {
		String content = CommonMethods.getInfo("QuicktellerInfo.xml", Quickteller.class);
		
		quicktellerConfig = CommonMethods.xmlStringToObject(content, QuicktellerConfig.class);
		quicktellerConfig = decryptContent(quicktellerConfig);
		
		quicktellerConfig.setResponseCode(ResponseConstants.SUCCEESS_CODE);
				
		} catch(Exception e) {
			System.out.println("Cannot read QuicktellerInfo.xml");
			quicktellerConfig.setResponseCode(ResponseConstants.FILE_ERROR_CODE);
		}
		
		return quicktellerConfig;
	}
	
	private static QuicktellerConfig decryptContent(QuicktellerConfig quicktellerConfig) {
		QuicktellerConfig quicktellerConfigDup = quicktellerConfig;
		try {
			if (!quicktellerConfig.getQuicktellerCredential().isEmpty())
				for (QuicktellerCredential quicktellerCredential : quicktellerConfig.getQuicktellerCredential()) {
					String baseUrl = CypherCrypt.deCypher(quicktellerCredential.getBaseUrl().trim()) == null
							|| CypherCrypt.deCypher(quicktellerCredential.getBaseUrl().trim()).equals("")
									? quicktellerCredential.getBaseUrl().trim()
									: CypherCrypt.deCypher(quicktellerCredential.getBaseUrl().trim());

					String clientId = CypherCrypt.deCypher(quicktellerCredential.getClientId().trim()) == null
							|| CypherCrypt.deCypher(quicktellerCredential.getClientId().trim()).equals("")
									? quicktellerCredential.getClientId().trim()
									: CypherCrypt.deCypher(quicktellerCredential.getClientId().trim());

					String clientSecret = CypherCrypt.deCypher(quicktellerCredential.getClientSecret().trim()) == null
							|| CypherCrypt.deCypher(quicktellerCredential.getClientSecret().trim()).equals("")
									? quicktellerCredential.getClientSecret().trim()
									: CypherCrypt.deCypher(quicktellerCredential.getClientSecret().trim());

					String initiatingEntityCode = CypherCrypt
							.deCypher(quicktellerCredential.getInitiatingEntityCode().trim()) == null
							|| CypherCrypt.deCypher(quicktellerCredential.getInitiatingEntityCode().trim()).equals("")
									? quicktellerCredential.getInitiatingEntityCode().trim()
									: CypherCrypt.deCypher(quicktellerCredential.getInitiatingEntityCode().trim());

					String signatureMethod = CypherCrypt
							.deCypher(quicktellerCredential.getSignatureMethod().trim()) == null
							|| CypherCrypt.deCypher(quicktellerCredential.getSignatureMethod().trim()).equals("")
									? quicktellerCredential.getSignatureMethod().trim()
									: CypherCrypt.deCypher(quicktellerCredential.getSignatureMethod().trim());

					String terminalId = CypherCrypt.deCypher(quicktellerCredential.getTerminalId().trim()) == null
							|| CypherCrypt.deCypher(quicktellerCredential.getTerminalId().trim()).equals("")
									? quicktellerCredential.getTerminalId().trim()
									: CypherCrypt.deCypher(quicktellerCredential.getTerminalId().trim());

					String transferCodePrefix = CypherCrypt
							.deCypher(quicktellerCredential.getTransferCodePrefix().trim()) == null
							|| CypherCrypt.deCypher(quicktellerCredential.getTransferCodePrefix().trim()).equals("")
									? quicktellerCredential.getTransferCodePrefix().trim()
									: CypherCrypt.deCypher(quicktellerCredential.getTransferCodePrefix().trim());

					quicktellerCredential.setBaseUrl(baseUrl);
					quicktellerCredential.setClientId(clientId);
					quicktellerCredential.setClientSecret(clientSecret);
					quicktellerCredential.setInitiatingEntityCode(initiatingEntityCode);
					quicktellerCredential.setSignatureMethod(signatureMethod);
					quicktellerCredential.setTerminalId(terminalId);
					quicktellerCredential.setTransferCodePrefix(transferCodePrefix);

				}
			
			return quicktellerConfig;
			
		} catch (Exception e) {
			System.out.println("QuicktellerCredential: \n" + CommonMethods.objectToXml(quicktellerConfig));
			System.out.println("Cannot decrypt content");
			
			return quicktellerConfigDup;
		}
		
	}
	

	

	
	
}
