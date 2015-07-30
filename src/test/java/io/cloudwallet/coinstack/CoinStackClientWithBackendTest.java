/**
 * 
 */
package io.cloudwallet.coinstack;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.cloudwallet.coinstack.backendadaptor.CoreBackEndAdaptor;
import io.cloudwallet.coinstack.model.CredentialsProvider;
import io.cloudwallet.coinstack.model.Subscription;
import io.cloudwallet.coinstack.model.Transaction;

import org.bitcoinj.crypto.TransactionSignature;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author nepho
 *
 */
public class CoinStackClientWithBackendTest extends CoinStackClientTest {

	@Override
	public void setUp() throws Exception {
		coinStackClient = new CoinStackClient(new CoreBackEndAdaptor(
				new CredentialsProvider() {

					@Override
					public String getAccessKey() {
						return "70a7758ddeafd9154ae13473963acd";
					}

					@Override
					public String getSecretKey() {
						return "6b88dcf7a6b4e09adc8f1095e594f9";
					}

				}, Endpoint.MAINNET));
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

		boolean exceptionRaised = false;
		try {
			coinStackClient
					.sendTransaction("010000000124398225cf3d515a7ef7e816c37cbfd1cae9e01b401b90192c4dd479d23e7eab000000006b483045022100a8b331d506e265e79feb535a51dd5fbcd2724f0f6a1482cbea38d772ceae4e8c02206d7ee4bf2af3f8289310a09cc9760df6ae4768e24238cc13aee1739b837900ea012102ce3b0c53a06262e2a64e0639f2901447c2288ab437b5317fe05848e92a2ba25fffffffff0216120100000000001976a91415aad25727498a360e92eeb96db26f55fb38edcb88ac10270000000000001976a914abf0db3809c8ae1697f067a5c92171fd6ca3aaa988ac00000000");
		} catch (Exception e) {
			System.out.println(e);
			exceptionRaised = true;
		}

		if (!exceptionRaised) {
			Assert.fail("exception not raised");
		}

		Transaction tx = CoinStackClient
				.parseRawTransaction("010000000124398225cf3d515a7ef7e816c37cbfd1cae9e01b401b90192c4dd479d23e7eab000000006b483045022100a8b331d506e265e79feb535a51dd5fbcd2724f0f6a1482cbea38d772ceae4e8c02206d7ee4bf2af3f8289310a09cc9760df6ae4768e24238cc13aee1739b837900ea012102ce3b0c53a06262e2a64e0639f2901447c2288ab437b5317fe05848e92a2ba25fffffffff0216120100000000001976a91415aad25727498a360e92eeb96db26f55fb38edcb88ac10270000000000001976a914abf0db3809c8ae1697f067a5c92171fd6ca3aaa988ac00000000");
		assertNotNull(tx);
		assertNotNull(tx.getOutputs()[0].getAddress());

	}

	@Test
	public void testDataTransaction() throws Exception {
		String privateKeyWIF = "Kwg7NfVRrnrDUehdE9hn3qEZ51Tfk7rdr6rmyoHvjhRhoZE1KVkd";
		// String to = "1Gg95o3E89tmrLyUyZfq2xTLhetjNqy168";
		// long amount = CoinStackClient.convertToSatoshi("0.0001");
		long fee = CoinStackClient.convertToSatoshi("0.0001");
		String rawTx = coinStackClient.createDataTransaction(privateKeyWIF,
				fee, "test data".getBytes());
		assertNotNull(rawTx);
		System.out.println(rawTx);
		assertNotNull(CoinStackClient.getTransactionHash(rawTx));
		System.out.println(CoinStackClient.getTransactionHash(rawTx));

		// try sending raw tx
		try {
//			coinStackClient.sendTransaction(rawTx);
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

		CoinStackClient client = new CoinStackClient(new CoreBackEndAdaptor(
				new CredentialsProvider() {

					@Override
					public String getAccessKey() {
						return "eb90dbf0-e98c-11e4-b571-0800200c9a66";
					}

					@Override
					public String getSecretKey() {
						return "f8bd5b50-e98c-11e4-b571-0800200c9a66";
					}

				}, Endpoint.MAINNET, new String[] { "TLSv1" },
				new String[] { "TLS_DHE_RSA_WITH_AES_128_CBC_SHA" }));
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

		Subscription newSubscription = new Subscription(
				"1Gg95o3E89tmrLyUyZfq2xTLhetjNqy168", "1d4bce0584");
		String subscriptionId = coinStackClient
				.addSubscription(newSubscription);
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
		String to = "3F3LvS6FbeeHRuW7cKF9pdnx5xKad4zjXh";
		
		//String to = "3L5qhqsAqzdzzTziDMrUonAFxZMiA3HsqL";
		long amount = CoinStackClient.convertToSatoshi("0.0002");
		long fee = CoinStackClient.convertToSatoshi("0.0001");
		TransactionBuilder builder = new TransactionBuilder();
		builder.addOutput(to, amount);
		builder.setFee(fee);
		
		String signedTx = coinStackClient.createSignedTransaction(builder, privateKeyWIF);
		System.out.println(signedTx);
		assertNotNull(signedTx);
		
		TransactionBuilder dataTx = new TransactionBuilder();
		dataTx.addOutput(to, amount);
		dataTx.setData("hello world".getBytes());
		dataTx.setFee(fee);
		
		String signedDataTx = coinStackClient.createSignedTransaction(dataTx, privateKeyWIF);
		System.out.println(signedDataTx);
		assertNotNull(signedDataTx);
	//	coinStackClient.sendTransaction(signedTx);
	}
	
	@Test
	public void testMultiSigTransaction() throws Exception {
		String privateKey1 = "5KF55BbKeZZqmAmpQAovn7KoBRjVdW4UN9uPGZoK1y9RrkPhnhA";
		//String privateKey2 = "5KF55BbKeZZqmAmpQAovn7KoBRjVdW4UN9uPGZoK1y9RrkPhnhA";
		String privateKey3 = "5Jd7kKaKRNkqALDzyY1nQgPBd5JmmPr3CTBFhQ3fsjcuRLqjjTg";
		String redeemScript = "52410421955a8ec650aed2748344810b5ab057d4b87244759914ad40086fb526cd487868b70fae9652eff933b28fcabfac44f282800fd10b241d989453ad35dcb2191241045e55d7adf05bb2d9e771904f3b4b0116c0bca34c226930a4a7ac16dc7c1946f5c538a4d9882833af4378ae0e43465173757f86cd2d59ba2193624ea6aa1ef7064104a95f8b9cfb3fdb970d56cd093a54e557083a3cfc2ac0d7bfb260abb69544c706e7b382429b9cf6919fd25449b5bdf0c6fcf4bda5576f88b742366f05cc8068e553ae";
		List<String> prikeys = new ArrayList<String>();
		prikeys.add(privateKey1);
		prikeys.add(privateKey3);
		//prikeys.add(privateKey3);
		//String to = "1Ce8WxgwjarzLtV6zkUGgdwmAe5yjHoPXX";
		String to = "1F444Loh6KzUQ8u8mAsz5upBtQ356vN95s";
		//1Gg95o3E89tmrLyUyZfq2xTLhetjNqy168
		long amount = CoinStackClient.convertToSatoshi("0.0001");
		long fee = CoinStackClient.convertToSatoshi("0.0001");
		TransactionBuilder builder = new TransactionBuilder();
		builder.addOutput(to, amount);
		builder.setFee(fee);
		
		String signedTx = coinStackClient.createMultiSigTransaction(builder, prikeys, redeemScript);
		System.out.println(signedTx);
		assertNotNull(signedTx);
	//	coinStackClient.sendTransaction(signedTx);
	}
	
	@Test
	public void testPartialSignTransaction() throws Exception {
		String privateKey3 = "5KF55BbKeZZqmAmpQAovn7KoBRjVdW4UN9uPGZoK1y9RrkPhnhA";
		//String privateKey2 = "5Jd7kKaKRNkqALDzyY1nQgPBd5JmmPr3CTBFhQ3fsjcuRLqjjTg";
		String privateKey1 = "5HqJ1GoR3qAjvCzRhWk9KQSD54F6PJ4buwv98vrDhrWEUhMKM5g";
		String redeemScript = "52410421955a8ec650aed2748344810b5ab057d4b87244759914ad40086fb526cd487868b70fae9652eff933b28fcabfac44f282800fd10b241d989453ad35dcb2191241045e55d7adf05bb2d9e771904f3b4b0116c0bca34c226930a4a7ac16dc7c1946f5c538a4d9882833af4378ae0e43465173757f86cd2d59ba2193624ea6aa1ef7064104a95f8b9cfb3fdb970d56cd093a54e557083a3cfc2ac0d7bfb260abb69544c706e7b382429b9cf6919fd25449b5bdf0c6fcf4bda5576f88b742366f05cc8068e553ae";
		String to = "1F444Loh6KzUQ8u8mAsz5upBtQ356vN95s";
		long amount = CoinStackClient.convertToSatoshi("0.0001");
		long fee = CoinStackClient.convertToSatoshi("0.0001");
		
		TransactionBuilder builder = new TransactionBuilder();
		builder.addOutput(to, amount);
		builder.setFee(fee);
		
		StringBuilder signedTx = new StringBuilder();
		byte [] signatureList = coinStackClient.CreateMultiSigTransactionWithPartialSign(builder, privateKey1, redeemScript, signedTx);
		System.out.println(signedTx);
		System.out.println("sign 1 : " + signatureList);
		
		StringBuilder signedTx2 = new StringBuilder();
		signatureList= coinStackClient.addSignatureToMultisig(signedTx.toString(), signatureList, privateKey3, redeemScript, signedTx2);
		System.out.println(signedTx2);
		System.out.println("sign 2 : " + signatureList);

		assertNotNull(signedTx2);
	//	coinStackClient.sendTransaction(signedTx);
	}
}
