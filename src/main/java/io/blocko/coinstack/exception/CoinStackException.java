package io.blocko.coinstack.exception;

public class CoinStackException extends Exception {

	/**
	 * Exception class that represents an error response returned by CoinStack.
	 * This exception indicates that the request was correctly transmitted, 
	 * but was not processed for some reason.
	 * 
	 * CoinStackException provides information about the error and why it occurred.
	 */
	private static final long serialVersionUID = 7158042607133607984L;

	private String errorType;
	private int errorCode;
	private int statusCode;
	private String message;
	private boolean retry;
	private String cause;
	
	public CoinStackException(String errorType, int errorCode, int statusCode, String message, boolean retry,
			String cause) {
		super();
		this.errorType = errorType;
		this.errorCode = errorCode;
		this.statusCode = statusCode;
		this.message = message;
		this.retry = retry;
		this.cause = cause;
	}
	/**
	 * Returns CoinStack error code represented by this exception.
	 */
	public int getErrorCode() {
		return errorCode;
	}
	/**
	 * Returns CoinStack error type represented by this exception.
	 */
	public String getErrorType() {
		return errorType;
	}

	@Override
	public String getMessage() {
		return message;
	}
	/**
	 * Returns detailed message behind this exception.
	 */
	public String getDetailedMessage() {
		return cause;
	}
	/**
	 * Returns HTTP status code represented by this exception.
	 */
	public int getStatusCode() {
		return statusCode;
	}
	/**
	 * Whether client should retry the request
	 */
	public boolean shouldRetry() {
		return retry;
	}
}
