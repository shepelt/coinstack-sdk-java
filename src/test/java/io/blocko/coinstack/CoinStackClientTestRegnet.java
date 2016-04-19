/**
 * 
 */
package io.blocko.coinstack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.wallet.WalletTransaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.blocko.coinstack.CoinStackClient;
import io.blocko.coinstack.Endpoint;
import io.blocko.coinstack.backendadaptor.CoreBackEndAdaptor;
import io.blocko.coinstack.exception.MalformedInputException;
import io.blocko.coinstack.model.Block;
import io.blocko.coinstack.model.BlockchainStatus;
import io.blocko.coinstack.model.CredentialsProvider;
import io.blocko.coinstack.model.Output;
import io.blocko.coinstack.model.Transaction;
import io.blocko.coinstack.util.BitcoinjUtil;

/**
 * @author nepho
 *
 */
public class CoinStackClientTestRegnet {
	protected CoinStackClient coinStackClient;

	/**
	 * @throws java.lang.Exception
	 */
//	@Before
//	public void setUp() throws Exception {
//		coinStackClient = new CoinStackClient(new MockCoinStackAdaptor());
//	}
	@Before
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
		
		coinStackClient = new CoinStackClient(new CoreBackEndAdaptor(
				new CredentialsProvider() {

					@Override
					public String getAccessKey() {
						return "80d1b821dcb89f965452de4ebbf7ae";
					}

					@Override
					public String getSecretKey() {
						return "55e2b5ac1bb4a0486b477926efa8bc";
					}

				}, Endpoint.TESTNET));

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		coinStackClient.close();
	}

	@Test
	public void testGetBlockchainStatus() throws Exception {
		BlockchainStatus blockchainStatus = coinStackClient
				.getBlockchainStatus();
		assertTrue(blockchainStatus.getBestHeight() > 0);
		assertNotNull(blockchainStatus.getBestBlockHash());
		assertTrue(blockchainStatus.getBestBlockHash().length() > 0);
	}

	@Test
	public void testGetBlock() throws Exception {
		// genesis block test
//		Block block = coinStackClient
//				.getBlock("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f");
//		assertNotNull(block);
//		assertEquals(0, block.getHeight());
//		assertEquals(1, block.getTransactionIds().length);
//		assertEquals(
//				"4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b",
//				block.getTransactionIds()[0]);
//		assertEquals(1231006505000L, block.getBlockConfirmationTime().getTime());
//		assertTrue(block.getParentId() == null);
//		assertNotNull(block.getChildIds());
//		assertEquals(1, block.getChildIds().length);
//		assertEquals(
//				"00000000839a8e6886ab5951d76f411475428afc90947ee320161bbf18eb6048",
//				block.getChildIds()[0]);

		// other block test
		Block block = coinStackClient
				.getBlock("1f98c670dcb265fed8d468cdab2e3f58cda2b09710cca9d1faa9ce8dadcf2454");

		assertNotNull(block);
		assertEquals(1035, block.getHeight());
		assertEquals(1, block.getTransactionIds().length);

		assertEquals(
				"d02a4c13ca95088508430c94c8337f746b0c61f92c1a2f0a34c6f40e7ac88269",
				block.getTransactionIds()[0]);

		assertEquals(1443060063000L, block.getBlockConfirmationTime().getTime());

		assertTrue(block
				.getParentId()
				.equals("77c7bf4ee353b023d89c813b8cb2834fbfcd9958552bba79e53ae2a3d9362dcf"));

		assertNotNull(block.getChildIds());
		assertEquals(1, block.getChildIds().length);
		assertEquals(
				"4c0685007c5b072b6bea5627bec497f0786dfd53ef049452106cb7d2e1427b2c",
				block.getChildIds()[0]);
	}

	@Test
	public void testTransaction() throws Exception {

		Transaction transaction = coinStackClient
				.getTransaction("3b873586e0df639bfaa4ec475d615fda4c71ac4a699cbabe50debf44e97c92f5");
		assertNotNull(transaction);
		assertEquals(
				"3b873586e0df639bfaa4ec475d615fda4c71ac4a699cbabe50debf44e97c92f5",
				transaction.getId());
		assertEquals(1, transaction.getBlockIds().length);
		assertEquals(
				"7503fd16fc7baed9215ac54ba6a70ac713cbe4a988bc397bb665d9693a98628a",
				transaction.getBlockIds()[0]);

		assertEquals(1443084065000L, transaction.getConfirmationTime()
				.getTime());
		assertFalse(transaction.isCoinbase());
		assertEquals(1, transaction.getInputs().length);
		assertNotNull(transaction.getInputs()[0].getOutputTransactionId());
		assertEquals(
				"cdb69cd57ea19529c0f9638ae61dc3117db5776ca376e0d0e34a02dc31c90d07",
				transaction.getInputs()[0].getOutputTransactionId());

		assertEquals(0, transaction.getInputs()[0].getOutputIndex());
		assertNotNull(transaction.getInputs()[0].getOutputAddress());

		assertEquals("mhkCQhZyYAEoWyMTsgFTPhkd9o7tYm9PsH",
				transaction.getInputs()[0].getOutputAddress());

		assertEquals(499999970000L, transaction.getInputs()[0].getValue());
		assertEquals(2, transaction.getOutputs().length);

		assertEquals("muP55RU19xr2MbHk98KYjktdidteXkTSkw",
				transaction.getOutputs()[0].getAddress());
		assertEquals("mhkCQhZyYAEoWyMTsgFTPhkd9o7tYm9PsH",
				transaction.getOutputs()[1].getAddress());
		
		assertEquals(500000L, transaction.getOutputs()[0].getValue());
		assertEquals(499999460000L, transaction.getOutputs()[1].getValue());
		
		assertEquals("76a91498141844117bb755f75d329a2374d09809a9e91288ac",
				transaction.getOutputs()[0].getScript());
		assertEquals("76a914187170fb8176e3e8602cded5b8ce1dc8205bb5f388ac",
				transaction.getOutputs()[1].getScript());
	}

	@Test
	public void testAddress() throws Exception {
		// Genesis Address
	//	Transaction transact1 = coinStackClient.getTransaction("d02a4c13ca95088508430c94c8337f746b0c61f92c1a2f0a34c6f40e7ac88269");
	//	System.out.println(transact1.getInputs()[0].outputAddress);
		String[] transactions = coinStackClient
				.getTransactions("mhkCQhZyYAEoWyMTsgFTPhkd9o7tYm9PsH");
		
		assertNotNull(transactions);
		assertTrue(transactions.length >= 1);

		transactions = coinStackClient
				.getTransactions("mhkCQhZyYAEoWyMTsgFTPhkd9o7tYm9PsH");
		assertNotNull(transactions);
		assertTrue(transactions.length >= 1);

		transactions = coinStackClient
				.getTransactions("mhkCQhZyYAEoWyMTsgFTPhkd9o7tYm9PsH");
		assertNotNull(transactions);
		assertTrue(transactions.length > 0);
		long balance = coinStackClient
				.getBalance("mhkCQhZyYAEoWyMTsgFTPhkd9o7tYm9PsH");
		// sended satoshi to the address which nobody has authority for testing
//		assertTrue(4580000000L <= balance);

		// testing unspent outputs
		Output[] outputs = coinStackClient
				.getUnspentOutputs("muP55RU19xr2MbHk98KYjktdidteXkTSkw");
		assertNotNull(outputs);
//		assertTrue(1 <= outputs.length);
//		System.out.println("outputs[0].getTransactionId() : " + outputs[0].getTransactionId());
//		assertEquals(
//				"412d7d603abe811044d26da96d5b0b8a3526b329286032eee8b4d2f446990ac7",
//				outputs[0].getTransactionId());
//		assertEquals(1, outputs[0].getIndex());
//		assertEquals(60000L, outputs[0].getValue());
//		System.out.println("outputs[0].getScript() : " + outputs[0].getScript());
//		assertEquals("76a91498141844117bb755f75d329a2374d09809a9e91288ac",
//				outputs[0].getScript());

		// test validating addesses
		assertTrue(ECKey
				.validateAddress("mhkCQhZyYAEoWyMTsgFTPhkd9o7tYm9PsH", false));
		assertFalse(ECKey
				.validateAddress("1A1zP1eP5QGefi2DMPTfssTL5SLmv7sisfN", false));

		// create new private key and address
		String newPrivateKeyWIF = ECKey.createNewPrivateKey(false);
		assertNotNull(newPrivateKeyWIF);
		String newAddress = ECKey.deriveAddress(newPrivateKeyWIF, false);
		assertNotNull(newAddress);
		System.out.println("newPrivateKeyWIF : " + newPrivateKeyWIF);
		System.out.println("newAddress : " + newAddress);
		assertTrue(ECKey.validateAddress(newAddress, false));
	}

	@Test
	public void testConstructTemporaryWallet() throws Exception {
		org.bitcoinj.core.ECKey signingKey = new DumpedPrivateKey(MainNetParams.get(),
				"Kwg7NfVRrnrDUehdE9hn3qEZ51Tfk7rdr6rmyoHvjhRhoZE1KVkd")
				.getKey();

		Wallet tempWallet = new Wallet(MainNetParams.get());
		tempWallet.importKey(signingKey);

	}

	private static class MockWallet extends Wallet {
		private static final long serialVersionUID = 7820716411471123317L;
		private Map<Sha256Hash, WalletTransaction> txMap = new HashMap<Sha256Hash, WalletTransaction>();

		public Map<Sha256Hash, WalletTransaction> getTxMap() {
			return txMap;
		}

		public MockWallet(NetworkParameters params) {
			super(params);
		}

		@Override
		public void addWalletTransaction(WalletTransaction wtx) {
			txMap.put(wtx.getTransaction().getHash(), wtx);
		}
	}

	@Test
	public void testAligningOutputs() throws Exception {
		Output[] outputBatch1 = {
				new Output(
						"cc29ca30675676d101f2a0044ff14d69c09f586eb87be4808bbffe4d80302e86",
						0, "", false, 2000l, "ffff"),
				new Output(
						"cc29ca30675676d101f2a0044ff14d69c09f586eb87be4808bbffe4d80302e86",
						3, "", false, 1000l, "ffff"),
				new Output(
						"b72d2f5065bb91b42cb9288e500421f53f53dc38f59e978bfbb350545312c865",
						3, "", false, 3000l, "ffff"),
				new Output(
						"45c353f908ff6ee2ce6c0a6256e7070c7c071def6f2b04ecf0992a1d266f800e",
						0, "", false, 4000l, "ffff") };
		MockWallet wallet = new MockWallet(RegTestParams.get());
		BitcoinjUtil.injectOutputs(wallet, outputBatch1, false);

		assertEquals(3, wallet.getTxMap().size());

		Output[] outputBatch2 = {
				new Output(
						"cc29ca30675676d101f2a0044ff14d69c09f586eb87be4808bbffe4d80302e86",
						3, "", false, 1000l, "ffff"),
				new Output(
						"b72d2f5065bb91b42cb9288e500421f53f53dc38f59e978bfbb350545312c865",
						3, "", false, 3000l, "ffff"),
				new Output(
						"cc29ca30675676d101f2a0044ff14d69c09f586eb87be4808bbffe4d80302e86",
						0, "", false, 2000l, "ffff"),
				new Output(
						"45c353f908ff6ee2ce6c0a6256e7070c7c071def6f2b04ecf0992a1d266f800e",
						0, "", false, 4000l, "ffff") };
		wallet = new MockWallet(MainNetParams.get());
		BitcoinjUtil.injectOutputs(wallet, outputBatch2, false);

		assertEquals(3, wallet.getTxMap().size());
		int nonDummyOutputCount = 0;
		for (TransactionOutput output : wallet
				.getTxMap()
				.get(new Sha256Hash(
						CoinStackClient
								.convertEndianness("cc29ca30675676d101f2a0044ff14d69c09f586eb87be4808bbffe4d80302e86")))
				.getTransaction().getOutputs()) {
			if (!output.getValue().equals(Coin.NEGATIVE_SATOSHI)) {
				nonDummyOutputCount++;
			}

		}
		assertEquals(2, nonDummyOutputCount);

	}


	@Test
	public void testConvertEndinaness() {
		String original = "1234566780";
		String expected = "8067563412";
		assertEquals(expected, CoinStackClient.convertEndianness(original));
	}
	
	@Test
	public void testRedeemScript() throws UnsupportedEncodingException, DecoderException {
		String publickey1 = "04a93d29a957d3e0064f6fde7a6c296e1ab2643f877d98ca5f52bdb9df43d3f70dfc040ee212b9bae63b2cf266ca677d31d72db53d1e928851d131d1cb9d9bde25";
		String publickey2 = "04c30518fb1d5f0e96ba9f262f31260c8ce02e322494436e4ccb0981ba687a1a2626ab30911ddad22023bcbdac1329fc73e999ff2836c608e9c8e405f4e6c0b615";
		String publickey3 = "04486a7c8754177b488982dcb13bd37fa6005a439dce55101c35ba3c5f60928036920e72a59429f0425967b735a1c52c9d2018a5ef4f63b8a8473ded4a29d5bad0";
		List<byte[]> pubkeys = new ArrayList<byte[]>(3);
		//System.out.println("publickey1.getBytes()" + publickey1.getBytes().toString());
		//System.out.println("publickey1.()" + publickey1);
		pubkeys.add(Hex.decodeHex(publickey1.toCharArray()));
		pubkeys.add(Hex.decodeHex(publickey2.toCharArray()));
		pubkeys.add(Hex.decodeHex(publickey3.toCharArray()));
		
		String redeemScript = MultiSig.createRedeemScript(2, pubkeys);
		System.out.println("redeem : " + redeemScript);
	}
	
	@Test
	public void testSignMessage() throws Exception {
		String privateKeyWIF = "cVt6VWDXeRpeGuBo767ZAqiVw1yoEKTB5uytAoRNGZwebVZcYiyx";
		String message = "this is a test";
		String signature = ECDSA.signMessage(privateKeyWIF, message, false);
		System.out.println("signature : " + signature);
		String address = ECKey.deriveAddress(privateKeyWIF, false);
		
	//	 String signature = "H4uYYncnZ0wBSeZJ7BFsRVA76hwuOYAAJI5AfngS2Q+sjl7LDdLLCtwpVBCyq7J5+oS4jbvDP1o2fauqV163teQ=";
	//	5l06MIf2bbOI0Earrw5dwICYpIdgKDyJVDOYWYa4kH84O994bAk70ld1AU5KIINf0s4v1hJGJvpwjNcnm9P9GE8y0UMJTutg8izF7mJV6LeFvjesMRELK4Gdj7uyDHm0
	//    String message = "Hello Bro";
	//    String address = "14hV8B8vQRDz6WPWkSe2FMimyr7qYJQBZ5";
		//		1FRevgdPHacyhi7FUxHQFC2qY9jm2iqLJr
		boolean res = ECDSA.verifyMessageSignature(address, message, signature, false);
		System.out.println("res : " + res);
		assertTrue(res);
	}
}