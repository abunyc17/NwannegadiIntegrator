package com.neptunesoftware.reuseableClasses;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailService {

	private String host;
	private String port;
	private String username;
	private String password;
	
	public MailService() {
	}
	
	public MailService(String host, String port, String username, String password) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;		
	}
	
	public MailService useDefault() {
		// this uses the Neptune Software group mail to send
		this.host = "secure.emailsrvr.com";
		this.port = "465";
		this.username = ""; // Neptune mail address
		this.password = ""; // password to Neptune mail address
		
		return this;
	}
	
	public MailService host(String host) {
		this.host = host;
		return this;
	}
	
	public MailService port(String port) {
		this.port = port;
		return this;
	}
	
	public MailService username(String username) {
		this.username = username;
		return this;
	}
	
	public MailService password(String password) {
		this.password = password;
		return this;
	}
    
	
	
	public static void main(String[] args) {
		String mailSubject = "Mobile App channel Licence Expiration";
		String mailContent = "Dear Amju Unique MFB,\r\n\r\n" + 
				"Please note that your licence for AmjuMobile app will expire in 15 day(s) time.\r\n\r\n" + 
				"Thanks for your usual Patronage\r\n\r\n" + 
				"Regards";
		
		MailService mailService = new MailService()
				.host("secure.emailsrvr.com") //("smtp-mail.outlook.com")
				.port("465")
				.username("wisdomessien@neptunesoftwaregroup.com")
				.password("NewPa55w0rd");
				
		mailService.sendMail("no-reply@neptunesoftwaregroup.com", "olawumioladimeji@neptunesoftwaregroup.com", mailSubject, mailContent);
	}
	
	
	public void sendMail(String senderMailAddress, String recipientMailAddress, String mailSubject, String mailContent) {

	    Properties prop = new Properties();
//		prop.put("mail.smtp.host", "smtp.gmail.com");
//	    prop.put("mail.smtp.port", "465");
//	    prop.put("mail.smtp.auth", "true");
//	    prop.put("mail.smtp.ssl.enable", true);
	    
		prop.put("mail.smtp.host", host);
	    prop.put("mail.smtp.port", port);
	    prop.put("mail.smtp.auth", true);
	    prop.put("mail.smtp.ssl.enable", true);
	    
	    Session session = Session.getInstance(prop,
	            new javax.mail.Authenticator() {
	                protected PasswordAuthentication getPasswordAuthentication() {
	                    return new PasswordAuthentication(username, password);
	                }
	            });

	    try {

	        Message message = new MimeMessage(session);
	        message.setFrom(new InternetAddress(senderMailAddress));
	        message.setRecipients(
	                Message.RecipientType.TO,
	                InternetAddress.parse(recipientMailAddress)
	        );
	        message.setSubject(mailSubject);
	        message.setText(mailContent);

	        Transport.send(message);

	        System.out.println("Mail sent!");

	    } catch (MessagingException e) {
	        e.printStackTrace();
	    }
	}
    
}
