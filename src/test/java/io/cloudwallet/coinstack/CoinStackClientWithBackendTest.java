/**
 * 
 */
package io.cloudwallet.coinstack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.cloudwallet.coinstack.backendadaptor.CoreBackEndAdaptor;
import io.cloudwallet.coinstack.model.CredentialsProvider;
import io.cloudwallet.coinstack.model.Subscription;
import io.cloudwallet.coinstack.model.Transaction;

/**
 * 
 * @author nepho
 *
 */
public class CoinStackClientWithBackendTest extends CoinStackClientTest {

	@Override
	public void setUp() throws Exception {

		coinStackClient = new CoinStackClient(new CoreBackEndAdaptor(new CredentialsProvider() {

			@Override
			public String getAccessKey() {
				return "e84ddc87dbb93d577907d524748e39";
			}

			@Override
			public String getSecretKey() {
				return "843a557f883ecec603aab5377d5c2a";
			}

		}, Endpoint.MAINNET));
		// coinStackClient = new CoinStackClient();
	}

	@Test
	public void testCreateTransaction() throws Exception {
		String privateKeyWIF = "Kwg7NfVRrnrDUehdE9hn3qEZ51Tfk7rdr6rmyoHvjhRhoZE1KVkd";
		String to = "1Gg95o3E89tmrLyUyZfq2xTLhetjNqy168";
		long amount = CoinStackClient.convertToSatoshi("0.0001");
		long fee = CoinStackClient.convertToSatoshi("0.0001");
		String rawTx = coinStackClient.createRawTransaction(privateKeyWIF, to, amount, fee);
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

		boolean exceptionRaised = false;
		try {
			coinStackClient.sendTransaction(
					"010000000124398225cf3d515a7ef7e816c37cbfd1cae9e01b401b90192c4dd479d23e7eab000000006b483045022100a8b331d506e265e79feb535a51dd5fbcd2724f0f6a1482cbea38d772ceae4e8c02206d7ee4bf2af3f8289310a09cc9760df6ae4768e24238cc13aee1739b837900ea012102ce3b0c53a06262e2a64e0639f2901447c2288ab437b5317fe05848e92a2ba25fffffffff0216120100000000001976a91415aad25727498a360e92eeb96db26f55fb38edcb88ac10270000000000001976a914abf0db3809c8ae1697f067a5c92171fd6ca3aaa988ac00000000");
		} catch (Exception e) {
			System.out.println(e);
			exceptionRaised = true;
		}

		if (!exceptionRaised) {
			Assert.fail("exception not raised");
		}

		Transaction tx = CoinStackClient.parseRawTransaction(
				"010000000124398225cf3d515a7ef7e816c37cbfd1cae9e01b401b90192c4dd479d23e7eab000000006b483045022100a8b331d506e265e79feb535a51dd5fbcd2724f0f6a1482cbea38d772ceae4e8c02206d7ee4bf2af3f8289310a09cc9760df6ae4768e24238cc13aee1739b837900ea012102ce3b0c53a06262e2a64e0639f2901447c2288ab437b5317fe05848e92a2ba25fffffffff0216120100000000001976a91415aad25727498a360e92eeb96db26f55fb38edcb88ac10270000000000001976a914abf0db3809c8ae1697f067a5c92171fd6ca3aaa988ac00000000");
		assertNotNull(tx);
		assertNotNull(tx.getOutputs()[0].getAddress());

	}

	@Test
	public void testDataTransaction() throws Exception {
		String privateKeyWIF = "Kwg7NfVRrnrDUehdE9hn3qEZ51Tfk7rdr6rmyoHvjhRhoZE1KVkd";
		// String to = "1Gg95o3E89tmrLyUyZfq2xTLhetjNqy168";
		// long amount = CoinStackClient.convertToSatoshi("0.0001");
		long fee = CoinStackClient.convertToSatoshi("0.0001");
		String rawTx = coinStackClient.createDataTransaction(privateKeyWIF, fee, "test data".getBytes());
		assertNotNull(rawTx);
		System.out.println(rawTx);
		assertNotNull(CoinStackClient.getTransactionHash(rawTx));
		System.out.println(CoinStackClient.getTransactionHash(rawTx));

		// try sending raw tx
		try {
			// coinStackClient.sendTransaction(rawTx);
		} catch (Exception e) {
			System.out.println(e);
			System.out.println(e.getCause());
			Assert.fail("sending tx failed");
		}
		//
		// Transaction tx = CoinStackClient
		// .parseRawTransaction("010000000124398225cf3d515a7ef7e816c37cbfd1cae9e01b401b90192c4dd479d23e7eab000000006b483045022100a8b331d506e265e79feb535a51dd5fbcd2724f0f6a1482cbea38d772ceae4e8c02206d7ee4bf2af3f8289310a09cc9760df6ae4768e24238cc13aee1739b837900ea012102ce3b0c53a06262e2a64e0639f2901447c2288ab437b5317fe05848e92a2ba25fffffffff0216120100000000001976a91415aad25727498a360e92eeb96db26f55fb38edcb88ac10270000000000001976a914abf0db3809c8ae1697f067a5c92171fd6ca3aaa988ac00000000");
		// assertNotNull(tx);
		// assertNotNull(tx.getOutputs()[0].getAddress());

	}

	@Test
	public void testSSLParameters() throws Exception {

		CoinStackClient client = new CoinStackClient(new CoreBackEndAdaptor(new CredentialsProvider() {

			@Override
			public String getAccessKey() {
				return "eb90dbf0-e98c-11e4-b571-0800200c9a66";
			}

			@Override
			public String getSecretKey() {
				return "f8bd5b50-e98c-11e4-b571-0800200c9a66";
			}

		}, Endpoint.MAINNET, new String[] { "TLSv1" }, new String[] { "TLS_DHE_RSA_WITH_AES_128_CBC_SHA" }));
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

		Subscription newSubscription = new Subscription("1Gg95o3E89tmrLyUyZfq2xTLhetjNqy168", "3ee8dd5a6e");
		String subscriptionId = coinStackClient.addSubscription(newSubscription);
		assertNotNull(subscriptionId);

		System.out.println("listing subscriptions");
		subscriptions = coinStackClient.listSubscriptions();
		assertEquals(1, subscriptions.length); // there should be only one
												// subscription
		assertEquals(subscriptionId, subscriptions[0].getId());
	}

	@Test
	public void testBuildTransaction() throws Exception {
		String privateKeyWIF = "5J82YdoYrtE3YGxjFW9Rr3R21qtDH9gFwkphHtnMpijcHs2PH7M";
		String to = "357UeWvhR2xK9hUEdVnrxf1Kkbf6B1wLGT";

		// String to = "3L5qhqsAqzdzzTziDMrUonAFxZMiA3HsqL";
		long amount = CoinStackClient.convertToSatoshi("0.0002");
		long fee = CoinStackClient.convertToSatoshi("0.0001");
		TransactionBuilder builder = new TransactionBuilder();
		builder.addOutput(to, amount);
		builder.setFee(fee);

		String signedTx = coinStackClient.createSignedTransaction(builder, privateKeyWIF);
		System.out.println(signedTx);
		assertNotNull(signedTx);

		TransactionBuilder dataTx = new TransactionBuilder();
		dataTx.setData("hello world".getBytes());
		dataTx.setFee(fee);

		String signedDataTx = coinStackClient.createSignedTransaction(dataTx, privateKeyWIF);
		System.out.println(signedDataTx);
		assertNotNull(signedDataTx);
		coinStackClient.sendTransaction(signedTx);
	}

	@Test
	public void testMultiSigTransaction() throws Exception {
		String privateKey1 = "5Jh46BhaJJeiW8C9tTz6ockGEgYnLYfrJmGnwYdcDpBbAvWvCbv";
		// String privateKey2 =
		// "5KF55BbKeZZqmAmpQAovn7KoBRjVdW4UN9uPGZoK1y9RrkPhnhA";
		String privateKey3 = "5JSad8KB82c3XW69e1hz8g1YFFts4GTdjHuWHkh4d4A8MZWw12N";
		String redeemScript = "52410468806910b7a3589f40c09092d3a45c64f1ef950e23d4b5aa92ad4c3de7804ed95f0f50aca9ae928fb6e00223fad667693bf3e2b716dd6c9d474ad79f5b7a107e410494bae4aa9a4c2ca6103899098ca0867f62ca24af02fee2d6473a698d92fbc8c449aa2e236c0684ebb9e0fbb23d847d4624fd8ca4a1fdc940df432c6e312e18e84104cbc882d221f567005ea61aa45d5414f25371472f6ab5973e13a39a9edc26359b6980aa4f6f34cea62e82bbe13adc7fde9fc26bba2be2e7c5f8011a68bea39bae53ae";
		List<String> prikeys = new ArrayList<String>();
		prikeys.add(privateKey1);
		prikeys.add(privateKey3);
		// prikeys.add(privateKey3);
		// String to = "1Ce8WxgwjarzLtV6zkUGgdwmAe5yjHoPXX";
		String to = "1F444Loh6KzUQ8u8mAsz5upBtQ356vN95s";
		// 1Gg95o3E89tmrLyUyZfq2xTLhetjNqy168
		long amount = CoinStackClient.convertToSatoshi("0.0039");
		long fee = CoinStackClient.convertToSatoshi("0.0001");
		TransactionBuilder builder = new TransactionBuilder();
		builder.addOutput(to, amount);
		builder.setFee(fee);

		String signedTx = coinStackClient.createMultiSigTransaction(builder, prikeys, redeemScript);
		System.out.println(signedTx);
		assertNotNull(signedTx);
		// coinStackClient.sendTransaction(signedTx);
	}

	@Test
	public void testPartialSignTransaction() throws Exception {
		String privateKey3 = "5JiqywVBWDphZbR2UWnUtj3yTX52LmGhnBy8gGED7GDdxzPuRaZ";
		String privateKey2 = "5J4ZadEdMs3zaqTutP1eQoKnCGKSYrUgkPwBZrk3hNmFiz7B6Ke";
		String privateKey1 = "5JKhaPecauUSKKZTJ2R8zhNZqFxLSDu3Q5dPU3ijSqkf2WGVekn";
		String redeemScript = "524104162a5b6239e12d3d52f2c880555934525dbb014dae7165380f77dcbf58b121b8033f59a1f7a4dcea589fc4405ac756542dfa393d53f7a559038f59b8d1084de541046a8fca1041f6ecf55aaa4e431b6c4ee72b51492330e777f2967697eb633e277eabf5d6e2ab3132b218a2d03b013ac90a80a4a2b5a27d1fa2a78cccad64d43b6f4104e850211b270fe7c97335411fcb774f6c7af0a8dd2e3360ba577e0c2979c51a375f5c256e2c8701d1b9777c15b7fc8b42af435977fe338e4a4e19683c884ad0fd53ae";
		String to = "1F444Loh6KzUQ8u8mAsz5upBtQ356vN95s";
		long amount = CoinStackClient.convertToSatoshi("0.0001");
		long fee = CoinStackClient.convertToSatoshi("0.0001");

		TransactionBuilder builder = new TransactionBuilder();
		builder.addOutput(to, amount);
		builder.setFee(fee);

		String signedTx = coinStackClient.createMultiSigTransactionWithPartialSign(builder, privateKey3, redeemScript);
		System.out.println(signedTx);
		// String partial =
		// "0100000001a389a0739611ac73c2cf7348e27eba43fbcf8a54708320440ed0efed4d455ca701000000fd14010047304402205de2dbce96c8bc402e592e754343627ecbafb20cc49a5890f5197b2fe917a3610220623ae7678d851705bb8e2675a964e544895203ea58dbe23c6676d1c1c6a80faa014cc9524104162a5b6239e12d3d52f2c880555934525dbb014dae7165380f77dcbf58b121b8033f59a1f7a4dcea589fc4405ac756542dfa393d53f7a559038f59b8d1084de541046a8fca1041f6ecf55aaa4e431b6c4ee72b51492330e777f2967697eb633e277eabf5d6e2ab3132b218a2d03b013ac90a80a4a2b5a27d1fa2a78cccad64d43b6f4104e850211b270fe7c97335411fcb774f6c7af0a8dd2e3360ba577e0c2979c51a375f5c256e2c8701d1b9777c15b7fc8b42af435977fe338e4a4e19683c884ad0fd53aeffffffff0110270000000000001976a9149a258fac5c9f2b79de327e7622b0c1e5783508cb88ac00000000";

		signedTx = coinStackClient.signMultiSigTransaction(signedTx, privateKey2, redeemScript);
		System.out.println(signedTx);

		assertNotNull(signedTx);
	}

	@Test
	public void testDocumentStamping() throws Exception {
		String message = "Hello, world";
		String stampid = coinStackClient.stampDocument(message.getBytes());
		System.out.println(stampid);
		assertNotNull(stampid);
	}
}
