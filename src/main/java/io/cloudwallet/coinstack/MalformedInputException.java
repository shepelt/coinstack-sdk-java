package io.cloudwallet.coinstack;

public class MalformedInputException extends IllegalArgumentException {
	public MalformedInputException(String message) {
		super(message);
	}

	private static final long serialVersionUID = -3451824520529407046L;
}
