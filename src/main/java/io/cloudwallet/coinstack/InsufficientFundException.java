package io.cloudwallet.coinstack;

public class InsufficientFundException extends IllegalArgumentException {
	public InsufficientFundException(String message) {
		super(message);
	}

	private static final long serialVersionUID = -2522287475243721135L;
}
