package io.cloudwallet.coinstack.backendadaptor;

import java.io.IOException;

import io.cloudwallet.coinstack.exception.TransactionRejectedException;
import io.cloudwallet.coinstack.model.Block;
import io.cloudwallet.coinstack.model.Output;
import io.cloudwallet.coinstack.model.Subscription;
import io.cloudwallet.coinstack.model.Transaction;

public abstract class AbstractCoinStackAdaptor {

	public abstract void init();

	public abstract void fini();
	
	public abstract boolean isMainnet();

	public abstract int getBestHeight() throws IOException;

	public abstract String getBestBlockHash() throws IOException;

	public abstract Block getBlock(String blockId) throws IOException;

	public abstract Transaction getTransaction(String transactionId)
			throws IOException;

	public abstract String[] getTransactions(String address) throws IOException;

	public abstract long getBalance(String address) throws IOException;

	public abstract Output[] getUnspentOutputs(String address) throws IOException;

	public abstract void sendTransaction(String rawTransaction) throws IOException, TransactionRejectedException;

	public abstract Subscription[] listSubscriptions() throws IOException;

	public abstract void deleteSubscription(String id) throws IOException;

	public abstract String addSubscription(Subscription newSubscription) throws IOException;

	public abstract String stampDocument(String hash) throws IOException;

}