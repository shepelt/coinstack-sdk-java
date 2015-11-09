package io.blocko.coinstack.backendadaptor;

import java.io.IOException;

import io.blocko.coinstack.exception.CoinStackException;
import io.blocko.coinstack.model.Block;
import io.blocko.coinstack.model.Output;
import io.blocko.coinstack.model.Stamp;
import io.blocko.coinstack.model.Subscription;
import io.blocko.coinstack.model.Transaction;

public abstract class AbstractCoinStackAdaptor {

	public abstract void init();

	public abstract void fini();
	
	public abstract boolean isMainnet();

	public abstract int getBestHeight() throws IOException, CoinStackException;

	public abstract String getBestBlockHash() throws IOException, CoinStackException;

	public abstract Block getBlock(String blockId) throws IOException, CoinStackException;

	public abstract Transaction getTransaction(String transactionId)
			throws IOException, CoinStackException;

	public abstract String[] getTransactions(String address) throws IOException, CoinStackException;

	public abstract long getBalance(String address) throws IOException, CoinStackException;

	public abstract Output[] getUnspentOutputs(String address) throws IOException, CoinStackException;

	public abstract void sendTransaction(String rawTransaction) throws IOException, CoinStackException;

	public abstract Subscription[] listSubscriptions() throws IOException, CoinStackException;

	public abstract void deleteSubscription(String id) throws IOException, CoinStackException;

	public abstract String addSubscription(Subscription newSubscription) throws IOException, CoinStackException;

	public abstract String stampDocument(String hash) throws IOException, CoinStackException;

	public abstract  Stamp getStamp(String stampId) throws IOException, CoinStackException;

}