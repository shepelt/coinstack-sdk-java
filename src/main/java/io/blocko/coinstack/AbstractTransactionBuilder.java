package io.blocko.coinstack;

import java.io.IOException;

import io.blocko.coinstack.exception.CoinStackException;

public abstract class AbstractTransactionBuilder {

	public abstract String buildTransaction(CoinStackClient client, String privateKeyWIF) throws IOException, CoinStackException;

}