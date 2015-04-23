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
public class CoinStackClientWithCloudWalletBackEndTest extends
		CoinStackClientTest {

	@Override
	public void setUp() throws Exception {
		coinStackClient = new CoinStackClient(new CloudWalletBackEndAdaptor(
				new CredentialsProvider() {

					@Override
					String getAccessKey() {
						return "eb90dbf0-e98c-11e4-b571-0800200c9a66";
					}

					@Override
					String getSecretKey() {
						return "f8bd5b50-e98c-11e4-b571-0800200c9a66";
					}

				}, Endpoint.MAINNET), true);
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
			// coinStackClient.sendTransaction(rawTx);
		} catch (Exception e) {
			System.out.println(e);
			Assert.fail("sending tx failed");
		}

		Transaction tx = CoinStackClient
				.parseRawTransaction("010000000124398225cf3d515a7ef7e816c37cbfd1cae9e01b401b90192c4dd479d23e7eab000000006b483045022100a8b331d506e265e79feb535a51dd5fbcd2724f0f6a1482cbea38d772ceae4e8c02206d7ee4bf2af3f8289310a09cc9760df6ae4768e24238cc13aee1739b837900ea012102ce3b0c53a06262e2a64e0639f2901447c2288ab437b5317fe05848e92a2ba25fffffffff0216120100000000001976a91415aad25727498a360e92eeb96db26f55fb38edcb88ac10270000000000001976a914abf0db3809c8ae1697f067a5c92171fd6ca3aaa988ac00000000");
		assertNotNull(tx);
		assertNotNull(tx.getOutputs()[0].getAddress());

	}

	@Test
	public void testSSLParameters() throws Exception {
		CoinStackClient client = new CoinStackClient(
				new EnvironmentVariableCredentialsProvider(), Endpoint.MAINNET,
				new String[] { "TLSv1" },
				new String[] { "TLS_DHE_RSA_WITH_AES_128_CBC_SHA" });
		client.getBlockchainStatus();
	}

	@Test
	public void testSubscribe() throws Exception {
		// list previous subscriptions
		Subscription[] subscriptions = coinStackClient.listSubscriptions();
		System.out.println("listing subscriptions");
		for (Subscription subscription : subscriptions) {
			System.out.println(subscription.getId());
			coinStackClient.deleteSubscription(subscription.getId());
		}
		System.out.println("registering a new subscription");

		Subscription newSubscription = new WebHookSubscription(
				"1Gg95o3E89tmrLyUyZfq2xTLhetjNqy168",
				"http://requestb.in/o87t0qo8");
		String subscriptionId = coinStackClient
				.addSubscription(newSubscription);
		assertNotNull(subscriptionId);

		System.out.println("listing subscriptions");
		subscriptions = coinStackClient.listSubscriptions();
		assertEquals(1, subscriptions.length); // there should be only one
												// subscription
		assertEquals(subscriptionId, subscriptions[0].getId());
	}
}
