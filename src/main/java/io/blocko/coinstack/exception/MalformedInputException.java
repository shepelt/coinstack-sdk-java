package io.blocko.coinstack.exception;

public class MalformedInputException extends CoinStackException {
	private static final long serialVersionUID = 4902924194986941114L;

	public MalformedInputException(String message, String detailedMessage) {
		super("io.coinstack#MalformedInput", 3000, 400, message, false, detailedMessage);
	}
}

