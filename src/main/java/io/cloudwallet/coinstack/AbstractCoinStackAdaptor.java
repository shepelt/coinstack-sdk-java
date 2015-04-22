package io.cloudwallet.coinstack;

import java.io.IOException;

public abstract class AbstractCoinStackAdaptor {

	abstract void init();

	abstract void fini();

	abstract int getBestHeight() throws IOException;

	abstract String getBestBlockHash() throws IOException;

	abstract Block getBlock(String blockId) throws IOException;

	abstract Transaction getTransaction(String transactionId)
			throws IOException;

	abstract String[] getTransactions(String address) throws IOException;

	abstract long getBalance(String address) throws IOException;

	abstract Output[] getUnspentOutputs(String address) throws IOException;

	abstract void sendTransaction(String rawTransaction) throws IOException, TransactionRejectedException;

	abstract Subscription[] listSubscriptions() throws IOException;

	abstract void deleteSubscription(String id) throws IOException;

	abstract String addSubscription(Subscription newSubscription) throws IOException;

}