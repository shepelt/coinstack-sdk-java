package io.blocko.coinstack.exception;

public class InsufficientFundException extends CoinStackException {
	public InsufficientFundException(String message) {
		super("io.coinstack#InsufficientFund", 3000, 400, message, false, "");
	}

	private static final long serialVersionUID = -2522287475243721135L;
}
