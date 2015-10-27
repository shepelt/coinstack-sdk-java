package io.blocko.coinstack;

import org.bitcoinj.core.Coin;

public class Math {
	/**
	 * Convert human-readable bitcoin string (e.g. 0.0001 BTC) to satoshi unit
	 * 
	 * @param bitcoinAmount
	 *            in human-friendly, string format (e.g. 0.0001 BTC)
	 * @return bitcoin amount in satoshi
	 */
	public static long convertToSatoshi(String bitcoinAmount) {
		return Coin.parseCoin(bitcoinAmount).value;
	}

}
