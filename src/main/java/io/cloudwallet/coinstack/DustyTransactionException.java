package io.cloudwallet.coinstack;

public class DustyTransactionException extends Exception {
	private static final long serialVersionUID = -337783809277051530L;

	public DustyTransactionException(String message) {
		super(message);
	}

}
