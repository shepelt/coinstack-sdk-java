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
	public static BlockchainStatus getBlockchainStatus(MockCoinStackAdaptor mockCoinStackAdaptor) {
		return new BlockchainStatus(mockCoinStackAdaptor.getBestHeight(), mockCoinStackAdaptor.getBestBlockHash());
	}
}
