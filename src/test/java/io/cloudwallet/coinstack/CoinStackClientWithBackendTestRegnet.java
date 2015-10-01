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

import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.TransactionSignature;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author nepho
 *
 */
public class CoinStackClientWithBackendTestRegnet extends CoinStackClientTestRegnet {

	@Override
	public void setUp() throws Exception {
		coinStackClient = new CoinStackClient(new CoreBackEndAdaptor(
				new CredentialsProvider() {

					@Override
					public String getAccessKey() {
						return "ff4a73aaaf6155ff9908d695e08006";
					}

					@Override
					public String getSecretKey() {
						return "4167377810d03e3bc196c90f0ed9e5";
					}

				}, Endpoint.TESTNET));
	}

	@Test
	public void testCreateTransaction() throws Exception {
		String privateKeyWIF = "cVCT2gx8CxxD1mJDu1iubsMd7Jkg7cLSLUR9wH7m61dk3N22xyH8";
		String to = "muP55RU19xr2MbHk98KYjktdidteXkTSkw";
		long amount = CoinStackClient.convertToSatoshi("0.0005");
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
		String privateKeyWIF = "cVCT2gx8CxxD1mJDu1iubsMd7Jkg7cLSLUR9wH7m61dk3N22xyH8";
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
			coinStackClient.sendTransaction(rawTx);
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

//	@Test
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
		String privateKeyWIF = "cVCT2gx8CxxD1mJDu1iubsMd7Jkg7cLSLUR9wH7m61dk3N22xyH8";
		String to = "2N8Z56makemLtPvbMyir64fiFEHpWEKwsE2";
	
		//String to = "3L5qhqsAqzdzzTziDMrUonAFxZMiA3HsqL";
		long amount = CoinStackClient.convertToSatoshi("0.0030");
		long fee = CoinStackClient.convertToSatoshi("0.0001");
		TransactionBuilder builder = new TransactionBuilder();
		builder.addOutput(to, amount);
		builder.setFee(fee);
		
		String signedTx = coinStackClient.createSignedTransaction(builder, privateKeyWIF);
		System.out.println(signedTx);
		assertNotNull(signedTx);
		coinStackClient.sendTransaction(signedTx);
		TransactionBuilder dataTx = new TransactionBuilder();
		//dataTx.addOutput(to, amount);
		dataTx.setData("hello world".getBytes());
		dataTx.setFee(fee);
		
		String signedDataTx = coinStackClient.createSignedTransaction(dataTx, privateKeyWIF);
		System.out.println(signedDataTx);
		assertNotNull(signedDataTx);

	}
	
	@Test
	public void testMultiSigTransaction() throws Exception {
		String privateKey1 = "cVCT2gx8CxxD1mJDu1iubsMd7Jkg7cLSLUR9wH7m61dk3N22xyH8";
		String privateKey2 = "cVt6VWDXeRpeGuBo767ZAqiVw1yoEKTB5uytAoRNGZwebVZcYiyx";
		String privateKey3 = "cNC1ibmJ2KibaCnj65oAS323mKD7qqMHd3fsALbcaC8ADMeywEjy";
		List<byte[]> pubkeys = new ArrayList<byte[]>();
		pubkeys.add(CoinStackClient.derivePubKey(privateKey1, false));
		pubkeys.add(CoinStackClient.derivePubKey(privateKey2, false));
		pubkeys.add(CoinStackClient.derivePubKey(privateKey3, false));
		
		String redeemScript = coinStackClient.createRedeemScript(2, pubkeys);
		System.out.println("re : " + redeemScript);
		List<String> prikeys = new ArrayList<String>();
		prikeys.add(privateKey1);
		prikeys.add(privateKey3);
		//prikeys.add(privateKey3);
		//String to = "1Ce8WxgwjarzLtV6zkUGgdwmAe5yjHoPXX";
		String to = "ms3jPiSK4XpHQP2JNrpthBeVCSuzxrtXZ1";
		//1Gg95o3E89tmrLyUyZfq2xTLhetjNqy168
		long amount = CoinStackClient.convertToSatoshi("0.0005");
		long fee = CoinStackClient.convertToSatoshi("0.0001");
		TransactionBuilder builder = new TransactionBuilder();
		builder.addOutput(to, amount);
		builder.setFee(fee);
		
		String signedTx = coinStackClient.createMultiSigTransaction(builder, prikeys, redeemScript);
		System.out.println(signedTx);
		assertNotNull(signedTx);
		coinStackClient.sendTransaction(signedTx);
	}
	
	@Test
	public void testPartialSignTransaction() throws Exception {
		String privateKey1 = "cVCT2gx8CxxD1mJDu1iubsMd7Jkg7cLSLUR9wH7m61dk3N22xyH8";
		String privateKey2 = "cVt6VWDXeRpeGuBo767ZAqiVw1yoEKTB5uytAoRNGZwebVZcYiyx";
		String privateKey3 = "cNC1ibmJ2KibaCnj65oAS323mKD7qqMHd3fsALbcaC8ADMeywEjy";
		List<byte[]> pubkeys = new ArrayList<byte[]>();
		pubkeys.add(CoinStackClient.derivePubKey(privateKey1, false));
		pubkeys.add(CoinStackClient.derivePubKey(privateKey2, false));
		pubkeys.add(CoinStackClient.derivePubKey(privateKey3, false));
		System.out.println(Utils.HEX.encode(CoinStackClient.derivePubKey(privateKey1, false)));
		System.out.println(Utils.HEX.encode(CoinStackClient.derivePubKey(privateKey2, false)));
		System.out.println(Utils.HEX.encode(CoinStackClient.derivePubKey(privateKey3, false)));
				
		String redeemScript = coinStackClient.createRedeemScript(2, pubkeys);
		String to = "ms3jPiSK4XpHQP2JNrpthBeVCSuzxrtXZ1";
		
		long amount = CoinStackClient.convertToSatoshi("0.0003");
		long fee = CoinStackClient.convertToSatoshi("0.0001");
		
		TransactionBuilder builder = new TransactionBuilder();
		builder.addOutput(to, amount);
		builder.setFee(fee);
		
		String signedTx= coinStackClient.createMultiSigTransactionWithPartialSign(builder, privateKey3, redeemScript);
		System.out.println(signedTx);
		//String partial = "0100000001a389a0739611ac73c2cf7348e27eba43fbcf8a54708320440ed0efed4d455ca701000000fd14010047304402205de2dbce96c8bc402e592e754343627ecbafb20cc49a5890f5197b2fe917a3610220623ae7678d851705bb8e2675a964e544895203ea58dbe23c6676d1c1c6a80faa014cc9524104162a5b6239e12d3d52f2c880555934525dbb014dae7165380f77dcbf58b121b8033f59a1f7a4dcea589fc4405ac756542dfa393d53f7a559038f59b8d1084de541046a8fca1041f6ecf55aaa4e431b6c4ee72b51492330e777f2967697eb633e277eabf5d6e2ab3132b218a2d03b013ac90a80a4a2b5a27d1fa2a78cccad64d43b6f4104e850211b270fe7c97335411fcb774f6c7af0a8dd2e3360ba577e0c2979c51a375f5c256e2c8701d1b9777c15b7fc8b42af435977fe338e4a4e19683c884ad0fd53aeffffffff0110270000000000001976a9149a258fac5c9f2b79de327e7622b0c1e5783508cb88ac00000000";
		
		signedTx = coinStackClient.signMultiSigTransaction(signedTx, privateKey2, redeemScript);
		System.out.println(signedTx);

		assertNotNull(signedTx);
		coinStackClient.sendTransaction(signedTx);
	}
}
