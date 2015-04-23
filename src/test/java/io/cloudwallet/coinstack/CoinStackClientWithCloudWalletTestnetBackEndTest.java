package io.cloudwallet.coinstack;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

public class CoinStackClientWithCloudWalletTestnetBackEndTest extends
		CoinStackClientWithTestnetTest {
	@Override
	public void setUp() throws Exception {
		coinStackClient = new CoinStackClient(
				new CloudWalletBackEndAdaptor(
						new EnvironmentVariableCredentialsProvider(),
						Endpoint.TESTNET), false);
	}

	@Test
	public void testCreateTransaction() throws Exception {
		String privateKeyWIF = "91iHcsiUkB5Gae5DkLzviUYpfguk9uXZwjB9KvXdtyeDJ7NWpoU";
		String to = "mz9R2o5kVaSG1bExrWakrkXYe1kUFBEF2J";
		long amount = CoinStackClient.convertToSatoshi("0.0001");
		long fee = CoinStackClient.convertToSatoshi("0.0001");
		String rawTx = coinStackClient.createRawTransaction(privateKeyWIF, to,
				amount, fee);
		assertNotNull(rawTx);
		System.out.println(rawTx);
		assertNotNull(CoinStackClient.getTransactionHash(rawTx, false));
		System.out.println(CoinStackClient.getTransactionHash(rawTx, false));

		// try sending raw tx
		try {
			// coinStackClient.sendTransaction(rawTx);
		} catch (Exception e) {
			System.out.println(e);
			Assert.fail("sending tx failed");
		}

		Transaction tx = CoinStackClient
				.parseRawTransaction(
						"010000000124398225cf3d515a7ef7e816c37cbfd1cae9e01b401b90192c4dd479d23e7eab000000006b483045022100a8b331d506e265e79feb535a51dd5fbcd2724f0f6a1482cbea38d772ceae4e8c02206d7ee4bf2af3f8289310a09cc9760df6ae4768e24238cc13aee1739b837900ea012102ce3b0c53a06262e2a64e0639f2901447c2288ab437b5317fe05848e92a2ba25fffffffff0216120100000000001976a91415aad25727498a360e92eeb96db26f55fb38edcb88ac10270000000000001976a914abf0db3809c8ae1697f067a5c92171fd6ca3aaa988ac00000000",
						false);
		assertNotNull(tx);
		assertNotNull(tx.getOutputs()[0].getAddress());

	}
}
