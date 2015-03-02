/**
 * 
 */
package io.cloudwallet.coinstack;

import static org.junit.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author nepho
 *
 */
public class CoinStackClientWithCloudWalletBackEndTest extends
		CoinStackClientTest {

	@Override
	public void setUp() throws Exception {
		coinStackClient = new CoinStackClient(new CloudWalletBackEndAdaptor(
				"http://search.cloudwallet.io"));
	}
	

	@Test
	public void testCreateTransaction() throws Exception {
		String privateKeyWIF = "Kwg7NfVRrnrDUehdE9hn3qEZ51Tfk7rdr6rmyoHvjhRhoZE1KVkd";
		String to = "1Gg95o3E89tmrLyUyZfq2xTLhetjNqy168";
		long amount = CoinStackClient.convertToSatoshi("0.0001");
		long fee = CoinStackClient.convertToSatoshi("0.0001");
		String rawTx = coinStackClient.createRawTransaction(privateKeyWIF, to,
				amount, fee);
		assertNotNull(rawTx);
		System.out.println(rawTx);
		assertNotNull(CoinStackClient.getTransactionHash(rawTx));
		System.out.println(CoinStackClient.getTransactionHash(rawTx));
		
		// try sending raw tx
		try {
			coinStackClient.sendTransaction(rawTx);
		} catch (Exception e) {
			System.out.println(e);
			Assert.fail("sending tx failed");
		}
	}
}
