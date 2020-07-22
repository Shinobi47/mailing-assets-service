package com.benayed.mailing.assets.exception;

public class TechnicalException extends RuntimeException{	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7972625860266124459L;

	public TechnicalException() {
		super();
	}
	
	public TechnicalException(String message) {
		super(message);
	}
	
	public TechnicalException(Exception e) {
		super(e);
	}

}
