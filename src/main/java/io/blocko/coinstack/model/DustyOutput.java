package io.blocko.coinstack.model;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;

public class DustyOutput extends TransactionOutput {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7550390476117227363L;

	public DustyOutput(NetworkParameters params, Transaction parent, Coin value, Address to) {
		super(params, parent, value, to);
	}

	@Override
	public Coin getMinNonDustValue() {
		return Coin.ZERO;
	}

}
