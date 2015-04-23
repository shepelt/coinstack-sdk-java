package io.cloudwallet.coinstack;

public class CoinStackClientWithCloudWalletBackEndWithEnvCredentialsTest extends
		CoinStackClientWithCloudWalletBackEndTest {

	@Override
	public void setUp() throws Exception {
		super.setUp();
		coinStackClient = new CoinStackClient();
	}

}
