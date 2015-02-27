/**
 * 
 */
package io.cloudwallet.coinstack;

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

	public static Transaction getTransaction(MockCoinStackAdaptor mockCoinStackAdaptor,
			String transactionId) {
		return mockCoinStackAdaptor.getTransaction(transactionId);
	}
}
