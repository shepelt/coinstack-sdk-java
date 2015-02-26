/**
 * 
 */
package io.cloudwallet.coinstack;

/**
 * @author nepho
 *
 */
public class BlockchainStatus {
	private int bestHeight;
	private String bestBlockHash;

	public BlockchainStatus(int bestHeight, String bestBlockHash) {
		this.bestHeight = bestHeight;
		this.bestBlockHash = bestBlockHash;
	}

	public String getBestBlockHash() {
		return bestBlockHash;
	}

	public int getBestHeight() {
		return bestHeight;
	}

}
