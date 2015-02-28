/**
 * 
 */
package io.cloudwallet.coinstack;

import java.util.List;

/**
 * @author nepho
 *
 */
public class CoinStackClient {
	/**
	 * @param mockCoinStackAdaptor
	 * @return
	 * 
	 */
	public static BlockchainStatus getBlockchainStatus(
			MockCoinStackAdaptor mockCoinStackAdaptor) {
		return new BlockchainStatus(mockCoinStackAdaptor.getBestHeight(),
				mockCoinStackAdaptor.getBestBlockHash());
	}

	public static Block getBlock(MockCoinStackAdaptor mockCoinStackAdaptor,
			String blockId) {
		return mockCoinStackAdaptor.getBlock(blockId);
	}

	public static Transaction getTransaction(
			MockCoinStackAdaptor mockCoinStackAdaptor, String transactionId) {
		return mockCoinStackAdaptor.getTransaction(transactionId);
	}

	public static String[] getTransactions(
			MockCoinStackAdaptor mockCoinStackAdaptor, String address) {
		return mockCoinStackAdaptor.getTransactions(address);
	}

	public static long getBalance(
			MockCoinStackAdaptor mockCoinStackAdaptor, String address) {
		return mockCoinStackAdaptor.getBalance(address);
	}
}
