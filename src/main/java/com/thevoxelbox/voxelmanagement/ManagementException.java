package com.thevoxelbox.voxelmanagement;

public class ManagementException extends Exception {

	private final ExceptionType exceptionType;
	
	public ManagementException(ExceptionType exceptionType, String message) {
		super(message);
		this.exceptionType = exceptionType;
	}

	public ExceptionType getType(){
		return exceptionType;
	}
	
}
