package io.cloudwallet.coinstack;


public class CoinStackClientWithCloudWalletTestBackEndTest extends
		CoinStackClientWithCloudWalletBackEndTest {

	@Override
	public void setUp() throws Exception {
		coinStackClient = new CoinStackClient(new CloudWalletBackEndAdaptor(
				"http://dev.cloudwallet.io:9000"));
	}
}
