/**
 * 
 */
package io.cloudwallet.coinstack;

import org.junit.After;
import org.junit.Before;

/**
 * @author nepho
 *
 */
public class CoinStackClientWithCloudWalletBackEndTest extends
		CoinStackClientTest {

	@Override
	public void setUp() throws Exception {
		coinStackClient = new CoinStackClient(new CloudWalletBackEndAdaptor());
	}
}
