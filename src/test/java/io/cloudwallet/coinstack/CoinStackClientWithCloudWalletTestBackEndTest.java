package io.cloudwallet.coinstack;

import static org.junit.Assert.*;

import org.junit.Test;

public class CoinStackClientWithCloudWalletTestBackEndTest extends
		CoinStackClientTest {

	@Override
	public void setUp() throws Exception {
		coinStackClient = new CoinStackClient(new CloudWalletBackEndAdaptor(
				"http://dev.cloudwallet.io:9000"));
	}
}
