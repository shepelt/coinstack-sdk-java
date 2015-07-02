package io.cloudwallet.coinstack;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;

public class DataTransactionOutput extends TransactionOutput {
	private static final long serialVersionUID = 6242964890109164964L;

	public DataTransactionOutput(NetworkParameters params, Transaction parent,
			Coin value, byte[] scriptBytes) {
		super(params, parent, value, scriptBytes);
	}

	@Override
	public Coin getMinNonDustValue() {
		return Coin.ZERO;
	}

}
