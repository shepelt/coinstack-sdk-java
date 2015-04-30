/**
 * 
 */
package io.cloudwallet.coinstack;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author nepho
 *
 */
public class CoinStackClientWithCoreBackendTest extends CoinStackClientTest {

	@Override
	public void setUp() throws Exception {
		coinStackClient = new CoinStackClient(new CoreBackEndAdaptor(
				new CredentialsProvider() {

					@Override
					String getAccessKey() {
						return "eb90dbf0-e98c-11e4-b571-0800200c9a66";
					}

					@Override
					String getSecretKey() {
						return "f8bd5b50-e98c-11e4-b571-0800200c9a66";
					}

				}, Endpoint.COINSTACK_CORE_MAINNET));
	}
}
