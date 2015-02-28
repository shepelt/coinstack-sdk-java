/**
 * 
 */
package io.cloudwallet.coinstack;

import java.io.IOException;
import java.util.List;

/**
 * @author nepho
 *
 */
public class CoinStackClient {
	private MockCoinStackAdaptor coinStackAdaptor;

	public CoinStackClient(MockCoinStackAdaptor coinStackAdaptor) {
		coinStackAdaptor.init();
		this.coinStackAdaptor = coinStackAdaptor;
	}

	public BlockchainStatus getBlockchainStatus() throws IOException {
		return new BlockchainStatus(coinStackAdaptor.getBestHeight(),
				coinStackAdaptor.getBestBlockHash());
	}

	public Block getBlock(String blockId) throws IOException {
		return coinStackAdaptor.getBlock(blockId);
	}

	public Transaction getTransaction(String transactionId) throws IOException {
		return coinStackAdaptor.getTransaction(transactionId);
	}

	public String[] getTransactions(String address) throws IOException {
		return coinStackAdaptor.getTransactions(address);
	}

	public long getBalance(String address) throws IOException {
		return coinStackAdaptor.getBalance(address);
	}

	public Output[] getUnspentOutputs(String address) throws IOException {
		return coinStackAdaptor.getUnspentOutputs(address);
	}

	public void close() {
		coinStackAdaptor.fini();
	}
}
