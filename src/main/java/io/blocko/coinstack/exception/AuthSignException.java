package io.blocko.coinstack.exception;

public class AuthSignException extends CoinStackException {
	private static final long serialVersionUID = 4902924194986941114L;

	public AuthSignException(String message, String detailedMessage) {
		super("io.coinstack#AuthSign", 3000, 400, message, false, detailedMessage);
	}
}
