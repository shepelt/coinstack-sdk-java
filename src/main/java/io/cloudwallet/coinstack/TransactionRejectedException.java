package io.cloudwallet.coinstack;

public class TransactionRejectedException extends Exception {
	private static final long serialVersionUID = -1236150541157342218L;

	public TransactionRejectedException(String message) {
		super(message);
	}

	public TransactionRejectedException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
