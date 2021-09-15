package com.neptunesoftware.reuseableClasses.Rubikon.config;

import java.util.List;

import com.neptunesoftware.reuseableClasses.CommonMethods;
import com.neptunesoftware.reuseableClasses.CypherCrypt;
import com.neptunesoftware.reuseableClasses.ResponseConstants;

public class RubikonUtil {

	public static RubikonCredential readRubikonConfig() {
		
		// used to know whether there's a default set
		boolean hasDefaultValue = false;
		
		RubikonCredential rubikonCredential = new RubikonCredential();
		Rubikon rubikon = readConfig();
		
		if (rubikon.getResponseCode().equals(ResponseConstants.SUCCEESS_CODE)) {
			for (RubikonCredential rubikonCred : rubikon.getRubikonCredential()) {
				if (rubikonCred.getDefaultValue().trim().equalsIgnoreCase("true")
					|| rubikonCred.getDefaultValue().trim().equalsIgnoreCase("yes")) {

					hasDefaultValue = true; // yes there is a default set
					
					rubikonCredential = rubikonCred;
					break;
				}
			}
			
			// in the absence of a default value, the first configuration is used
			if(!hasDefaultValue) {
				for (RubikonCredential rubikonCred : rubikon.getRubikonCredential()) {					
					rubikonCredential = rubikonCred;
					
					System.out.println("\nNo default value was set in RubikonInfo.xml. First configuration is used instead \n");
					break;
				}
			}
		}
		
		return rubikonCredential;
		
	}
	
	
	private static Rubikon readConfig() {
		
		Rubikon rubikon = new Rubikon();
		
		try {
		String content = CommonMethods.getInfo("RubikonInfo.xml", RubikonUtil.class);
				
		rubikon = CommonMethods.xmlStringToObject(content, Rubikon.class);
		rubikon = decryptContent(rubikon);
		
		rubikon.setResponseCode(ResponseConstants.SUCCEESS_CODE);
				
		} catch(Exception e) {
			System.out.println("Cannot read RubikonInfo.xml");
			rubikon.setResponseCode(ResponseConstants.FILE_ERROR_CODE);
		}
		
		return rubikon;
	}
	
	private static Rubikon decryptContent(final Rubikon rubikon) {
		
		try {
			if (!rubikon.getRubikonCredential().isEmpty())
				for (RubikonCredential rubikonCredential : rubikon.getRubikonCredential()) {
					String ipAddress = CypherCrypt.deCypher(rubikonCredential.getIpAddress().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getIpAddress().trim()).equals("")
									? rubikonCredential.getIpAddress().trim()
									: CypherCrypt.deCypher(rubikonCredential.getIpAddress().trim());

					String portNumber = CypherCrypt.deCypher(rubikonCredential.getPortNumber().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getPortNumber().trim()).equals("")
									? rubikonCredential.getPortNumber().trim()
									: CypherCrypt.deCypher(rubikonCredential.getPortNumber().trim());

					String channelId = CypherCrypt.deCypher(rubikonCredential.getChannelId().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getChannelId().trim()).equals("")
									? rubikonCredential.getChannelId().trim()
									: CypherCrypt.deCypher(rubikonCredential.getChannelId().trim());

					String channelCode = CypherCrypt.deCypher(rubikonCredential.getChannelCode().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getChannelCode().trim()).equals("")
									? rubikonCredential.getChannelCode().trim()
									: CypherCrypt.deCypher(rubikonCredential.getChannelCode().trim());

					String transactionFee = CypherCrypt.deCypher(rubikonCredential.getTransactionFee().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getTransactionFee().trim()).equals("")
									? rubikonCredential.getTransactionFee().trim()
									: CypherCrypt.deCypher(rubikonCredential.getTransactionFee().trim());

					String chargeCode = CypherCrypt.deCypher(rubikonCredential.getChargeCode().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getChargeCode().trim()).equals("")
									? rubikonCredential.getChargeCode().trim()
									: CypherCrypt.deCypher(rubikonCredential.getChargeCode().trim());

					String taxCode = CypherCrypt.deCypher(rubikonCredential.getTaxCode().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getTaxCode().trim()).equals("")
									? rubikonCredential.getTaxCode().trim()
									: CypherCrypt.deCypher(rubikonCredential.getTaxCode().trim());

					String currencyCode = CypherCrypt.deCypher(rubikonCredential.getCurrencyCode().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getCurrencyCode().trim()).equals("")
									? rubikonCredential.getCurrencyCode().trim()
									: CypherCrypt.deCypher(rubikonCredential.getCurrencyCode().trim());

					String currencyId = CypherCrypt.deCypher(rubikonCredential.getCurrencyId().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getCurrencyId().trim()).equals("")
									? rubikonCredential.getCurrencyId().trim()
									: CypherCrypt.deCypher(rubikonCredential.getCurrencyId().trim());

					String fundTransferCredit = CypherCrypt
							.deCypher(rubikonCredential.getFundTransferCredit().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getFundTransferCredit().trim()).equals("")
									? rubikonCredential.getFundTransferCredit().trim()
									: CypherCrypt.deCypher(rubikonCredential.getFundTransferCredit().trim());

					String fundTransferDebit = CypherCrypt
							.deCypher(rubikonCredential.getFundTransferDebit().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getFundTransferDebit().trim()).equals("")
									? rubikonCredential.getFundTransferDebit().trim()
									: CypherCrypt.deCypher(rubikonCredential.getFundTransferDebit().trim());

					String billsPaymentCredit = CypherCrypt
							.deCypher(rubikonCredential.getBillsPaymentCredit().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getBillsPaymentCredit().trim()).equals("")
									? rubikonCredential.getBillsPaymentCredit().trim()
									: CypherCrypt.deCypher(rubikonCredential.getBillsPaymentCredit().trim());

					String billsPaymentDebit = CypherCrypt
							.deCypher(rubikonCredential.getBillsPaymentDebit().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getBillsPaymentDebit().trim()).equals("")
									? rubikonCredential.getBillsPaymentDebit().trim()
									: CypherCrypt.deCypher(rubikonCredential.getBillsPaymentDebit().trim());

					String glAccountNumber = CypherCrypt.deCypher(rubikonCredential.getGlAccountNumber().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getGlAccountNumber().trim()).equals("")
									? rubikonCredential.getGlAccountNumber().trim()
									: CypherCrypt.deCypher(rubikonCredential.getGlAccountNumber().trim());

					String mobileRechargeCredit = CypherCrypt
							.deCypher(rubikonCredential.getMobileRechargeCredit().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getMobileRechargeCredit().trim()).equals("")
									? rubikonCredential.getMobileRechargeCredit().trim()
									: CypherCrypt.deCypher(rubikonCredential.getMobileRechargeCredit().trim());

					String mobileRechargeDebit = CypherCrypt
							.deCypher(rubikonCredential.getMobileRechargeDebit().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getMobileRechargeDebit().trim()).equals("")
									? rubikonCredential.getMobileRechargeDebit().trim()
									: CypherCrypt.deCypher(rubikonCredential.getMobileRechargeDebit().trim());

					String authenticationUsername = CypherCrypt
							.deCypher(rubikonCredential.getAuthenticatedUsername().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getAuthenticatedUsername().trim()).equals("")
									? rubikonCredential.getAuthenticatedUsername().trim()
									: CypherCrypt.deCypher(rubikonCredential.getAuthenticatedUsername().trim());

					String authenticationPassword = CypherCrypt
							.deCypher(rubikonCredential.getAuthenticatedPassword().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getAuthenticatedPassword().trim()).equals("")
									? rubikonCredential.getAuthenticatedPassword().trim()
									: CypherCrypt.deCypher(rubikonCredential.getAuthenticatedPassword().trim());

					String transferLimitInternal = CypherCrypt
							.deCypher(rubikonCredential.getTransferLimitInternal().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getTransferLimitInternal().trim()).equals("")
									? rubikonCredential.getTransferLimitInternal().trim()
									: CypherCrypt.deCypher(rubikonCredential.getTransferLimitInternal().trim());

					String transferLimitExternal = CypherCrypt
							.deCypher(rubikonCredential.getTransferLimitExternal().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getTransferLimitExternal().trim()).equals("")
									? rubikonCredential.getTransferLimitExternal().trim()
									: CypherCrypt.deCypher(rubikonCredential.getTransferLimitExternal().trim());

					String applicationUsername = CypherCrypt
							.deCypher(rubikonCredential.getApplicationUsername().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getApplicationUsername().trim()).equals("")
									? rubikonCredential.getApplicationUsername().trim()
									: CypherCrypt.deCypher(rubikonCredential.getApplicationUsername().trim());

					String chargeCodeBillsPayment = CypherCrypt
							.deCypher(rubikonCredential.getChargeCodeBillsPayment().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getChargeCodeBillsPayment().trim()).equals("")
									? rubikonCredential.getChargeCodeBillsPayment().trim()
									: CypherCrypt.deCypher(rubikonCredential.getChargeCodeBillsPayment().trim());

					String authenticateService = CypherCrypt
							.deCypher(rubikonCredential.getAuthenticateService().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getAuthenticateService().trim()).equals("")
									? rubikonCredential.getAuthenticateService().trim()
									: CypherCrypt.deCypher(rubikonCredential.getAppVersion().trim());

					String appVersion = CypherCrypt.deCypher(rubikonCredential.getAppVersion().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getAppVersion().trim()).equals("")
									? rubikonCredential.getAppVersion().trim()
									: CypherCrypt.deCypher(rubikonCredential.getAppVersion().trim());

					String licence = CypherCrypt.deCypher(rubikonCredential.getLincenceInfo().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getLincenceInfo().trim()).equals("")
									? rubikonCredential.getLincenceInfo().trim()
									: CypherCrypt.deCypher(rubikonCredential.getLincenceInfo().trim());

					String alertPeriodInDays = CypherCrypt
							.deCypher(rubikonCredential.getAlertPeriodInDays().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getAlertPeriodInDays().trim()).equals("")
									? rubikonCredential.getAlertPeriodInDays().trim()
									: CypherCrypt.deCypher(rubikonCredential.getAlertPeriodInDays().trim());

					String gracePeriodInDays = CypherCrypt
							.deCypher(rubikonCredential.getGracePeriodInDays().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getGracePeriodInDays().trim()).equals("")
									? rubikonCredential.getGracePeriodInDays().trim()
									: CypherCrypt.deCypher(rubikonCredential.getGracePeriodInDays().trim());

					String apiKey = CypherCrypt.deCypher(rubikonCredential.getApiKey().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getApiKey().trim()).equals("")
									? rubikonCredential.getApiKey().trim()
									: CypherCrypt.deCypher(rubikonCredential.getApiKey().trim());

					String authenticationId = CypherCrypt
							.deCypher(rubikonCredential.getAuthenticationId().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getAuthenticationId().trim()).equals("")
									? rubikonCredential.getAuthenticationId().trim()
									: CypherCrypt.deCypher(rubikonCredential.getAuthenticationId().trim());

					String applicationId = CypherCrypt.deCypher(rubikonCredential.getApplicationId().trim()) == null
							|| CypherCrypt.deCypher(rubikonCredential.getApplicationId().trim()).equals("")
									? rubikonCredential.getApplicationId().trim()
									: CypherCrypt.deCypher(rubikonCredential.getApplicationId().trim());

					// collect all mail recipients
					List<String> mailRecipients = rubikonCredential.getMailRecipient().getEmailAddress();

					// go through each mail recipients
					int index = 0;
					for (String mailAddress : mailRecipients) {
						// and decrypt value passed if it was encrypted
						String email = CypherCrypt.deCypher(mailAddress.trim()) == null
								|| CypherCrypt.deCypher(mailAddress.trim()).equals("") ? mailAddress.trim()
										: CypherCrypt.deCypher(mailAddress.trim());

						// set the values back to the object
						mailRecipients.set(index++, email);
					}

					rubikonCredential.setIpAddress(ipAddress);
					rubikonCredential.setPortNumber(portNumber);
					rubikonCredential.setChannelId(channelId);
					rubikonCredential.setChannelCode(channelCode);
					rubikonCredential.setTransactionFee(transactionFee);
					rubikonCredential.setChargeCode(chargeCode);
					rubikonCredential.setTaxCode(taxCode);
					rubikonCredential.setCurrencyCode(currencyCode);
					rubikonCredential.setCurrencyId(currencyId);
					rubikonCredential.setFundTransferCredit(fundTransferCredit);
					rubikonCredential.setFundTransferDebit(fundTransferDebit);
					rubikonCredential.setBillsPaymentCredit(billsPaymentCredit);
					rubikonCredential.setBillsPaymentDebit(billsPaymentDebit);
					rubikonCredential.setGlAccountNumber(glAccountNumber);
					rubikonCredential.setMobileRechargeCredit(mobileRechargeCredit);
					rubikonCredential.setMobileRechargeDebit(mobileRechargeDebit);
					rubikonCredential.setAuthenticatedUsername(authenticationUsername);
					rubikonCredential.setAuthenticatedPassword(authenticationPassword);
					rubikonCredential.setTransferLimitInternal(transferLimitInternal);
					rubikonCredential.setTransferLimitExternal(transferLimitExternal);
					rubikonCredential.setApplicationUsername(applicationUsername);
					rubikonCredential.setChargeCodeBillsPayment(chargeCodeBillsPayment);
					rubikonCredential.setAuthenticateService(authenticateService);
					rubikonCredential.setAppVersion(appVersion);
					rubikonCredential.setLincenceInfo(licence);
					rubikonCredential.setAlertPeriodInDays(alertPeriodInDays);
					rubikonCredential.setGracePeriodInDays(gracePeriodInDays);
					rubikonCredential.getMailRecipient().setEmailAddress(mailRecipients);
					rubikonCredential.setApiKey(apiKey);
					rubikonCredential.setAuthenticationId(authenticationId);
					rubikonCredential.setApplicationId(applicationId);

				}
			
			return rubikon;
			
		} catch (Exception e) {
			System.out.println("RubikonInfo: \n" + CommonMethods.objectToXml(rubikon));
			System.out.println("Cannot decrypt content");
			
			return rubikon;
		}		
		
	}
	
	
	public static void main(String[] args) {
		
		System.out.println("RubikonConfig: \n\r" + CommonMethods.ObjectToJsonString(readRubikonConfig()));
		
		System.out.println(readRubikonConfig().getMailRecipient().getEmailAddress());
	}
	
}
