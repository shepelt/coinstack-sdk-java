package io.blocko.coinstack.exception;

public class InvalidResponseException extends CoinStackException {
	private static final long serialVersionUID = 4902924194986941114L;

	public InvalidResponseException(String message, String detailedMessage) {
		super("io.coinstack#InvalidResponse", 4000, 500, message, false, detailedMessage);
	}
}
