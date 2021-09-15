package com.neptunesoftware.ussd.FirstTimeUser.data;

import com.neptunesoftware.reuseableClasses.ResponseModel;

public class PinDataResponse extends ResponseModel{

	private String pin;

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}
	
}
