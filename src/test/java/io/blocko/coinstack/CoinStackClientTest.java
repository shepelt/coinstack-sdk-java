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
import org.bitcoinj.wallet.WalletTransaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.blocko.coinstack.CoinStackClient;
import io.blocko.coinstack.model.Block;
import io.blocko.coinstack.model.BlockchainStatus;
import io.blocko.coinstack.model.Output;
import io.blocko.coinstack.model.Transaction;

/**
 * @author nepho
 *
 */
public class CoinStackClientTest {
	protected CoinStackClient coinStackClient;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		coinStackClient = new CoinStackClient(new MockCoinStackAdaptor());
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
		BlockchainStatus blockchainStatus = coinStackClient.getBlockchainStatus();
		assertTrue(blockchainStatus.getBestHeight() > 0);
		assertNotNull(blockchainStatus.getBestBlockHash());
		assertTrue(blockchainStatus.getBestBlockHash().length() > 0);
	}

	@Test
	public void testGetBlock() throws Exception {
		// genesis block test
		// Block block = coinStackClient
		// .getBlock("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f");
		// assertNotNull(block);
		// assertEquals(0, block.getHeight());
		// assertEquals(1, block.getTransactionIds().length);
		// assertEquals(
		// "4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b",
		// block.getTransactionIds()[0]);
		// assertEquals(1231006505000L,
		// block.getBlockConfirmationTime().getTime());
		// assertTrue(block.getParentId() == null);
		// assertNotNull(block.getChildIds());
		// assertEquals(1, block.getChildIds().length);
		// assertEquals(
		// "00000000839a8e6886ab5951d76f411475428afc90947ee320161bbf18eb6048",
		// block.getChildIds()[0]);

		// other block test
		Block block = coinStackClient.getBlock("00000000839a8e6886ab5951d76f411475428afc90947ee320161bbf18eb6048");
		assertNotNull(block);
		assertEquals(1, block.getHeight());
		assertEquals(1, block.getTransactionIds().length);
		assertEquals("0e3e2357e806b6cdb1f70b54c3a3a17b6714ee1f0e68bebb44a74b1efd512098", block.getTransactionIds()[0]);
		assertEquals(1231469665000L, block.getBlockConfirmationTime().getTime());
		assertTrue(block.getParentId().equals("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f"));
		assertNotNull(block.getChildIds());
		assertEquals(1, block.getChildIds().length);
		assertEquals("000000006a625f06636b8bb6ac7b960a8d03705d1ace08b1a19da3fdcc99ddbd", block.getChildIds()[0]);
	}

	@Test
	public void testTransaction() throws Exception {
		// Transaction transaction = coinStackClient
		// .getTransaction("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b");
		// assertNotNull(transaction);
		// assertEquals(
		// "4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b",
		// transaction.getId());
		// assertEquals(1, transaction.getBlockIds().length);
		// assertEquals(
		// "000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f",
		// transaction.getBlockIds()[0]);
		// assertEquals(1231006505000L, transaction.getConfirmationTime()
		// .getTime());
		// assertTrue(transaction.isCoinbase());
		// assertEquals(0, transaction.getInputs().length);
		// assertEquals(1, transaction.getOutputs().length);
		// assertEquals("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
		// transaction.getOutputs()[0].getAddress());
		// assertEquals(5000000000l, transaction.getOutputs()[0].getValue());
		// assertEquals(
		// "4104678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5fac",
		// transaction.getOutputs()[0].getScript());

		Transaction transaction = coinStackClient
				.getTransaction("001f5eba608a84ba97dd7ac1b21b822b74c91ffbd75c42c7b88abe178f632b31");
		assertNotNull(transaction);
		assertEquals("001f5eba608a84ba97dd7ac1b21b822b74c91ffbd75c42c7b88abe178f632b31", transaction.getId());
		assertEquals(1, transaction.getBlockIds().length);
		assertEquals("00000000000000000fad06ca404d52a779d452000057a8342b064618d05a4450", transaction.getBlockIds()[0]);
		assertEquals(1425014682000L, transaction.getConfirmationTime().getTime());
		assertFalse(transaction.isCoinbase());
		assertEquals(1, transaction.getInputs().length);
		assertNotNull(transaction.getInputs()[0].getOutputTransactionId());
		assertEquals("f693cadeacdbb2d980155fbafc82f00c607f2a1fb185cd27b054064b43d00f16",
				transaction.getInputs()[0].getOutputTransactionId());
		assertEquals(1, transaction.getInputs()[0].getOutputIndex());
		assertNotNull(transaction.getInputs()[0].getOutputAddress());
		assertEquals("1Dn86V7bJ7Knv716jj811aXHikyHFD1HQ1", transaction.getInputs()[0].getOutputAddress());
		assertEquals(7998950000L, transaction.getInputs()[0].getValue());

		assertEquals(2, transaction.getOutputs().length);
		assertEquals("15Zf4AybWDV6QRcaJ4ErowVxhpdG89Qjni", transaction.getOutputs()[0].getAddress());
		assertEquals("1Dn86V7bJ7Knv716jj811aXHikyHFD1HQ1", transaction.getOutputs()[1].getAddress());
		assertEquals(600000000L, transaction.getOutputs()[0].getValue());
		assertEquals(7398940000L, transaction.getOutputs()[1].getValue());
		assertEquals("76a914320d9492f6b348e003a1ba30afca95eb8d0609e588ac", transaction.getOutputs()[0].getScript());
		assertEquals("76a9148c2a2661cb4afd3ae0c1ea7b1beb5d34e769dbbc88ac", transaction.getOutputs()[1].getScript());
	}

	@Test
	public void testAddress() throws Exception {
		// Genesis Address
		String[] transactions = coinStackClient.getTransactions("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa");

		assertNotNull(transactions);
		assertTrue(transactions.length >= 1);

		transactions = coinStackClient.getTransactions("1changeFu9bT4Bzbo8qQTcHS7pRfLcX1D");
		assertNotNull(transactions);
		assertTrue(transactions.length >= 1);

		transactions = coinStackClient.getTransactions("1z7Xp8ayc1HDnUhKiSsRz7ZVorxrRFUg6");
		assertNotNull(transactions);
		assertTrue(transactions.length > 0);
		long balance = coinStackClient.getBalance("1z7Xp8ayc1HDnUhKiSsRz7ZVorxrRFUg6");
		// sended satoshi to the address which nobody has authority for testing
		assertTrue(4580000000L <= balance);

		// testing unspent outputs
		Output[] outputs = coinStackClient.getUnspentOutputs("1z7Xp8ayc1HDnUhKiSsRz7ZVorxrRFUg6");
		assertNotNull(outputs);
		assertTrue(1 <= outputs.length);
//		assertEquals("9bdab8ef52eb9e01856af4ff6f16154fee3425fcd33b91ce710134f32fdf62f7", outputs[0].getTransactionId());
//		assertEquals(0, outputs[0].getIndex());
//		assertEquals(4580000000L, outputs[0].getValue());
//		assertEquals("76a9140acd296e1ba0b5153623c3c55f2d5b45b1a25ce988ac", outputs[0].getScript());

		// test validating addesses
		assertTrue(ECKey.validateAddress("1changeFu9bT4Bzbo8qQTcHS7pRfLcX1D"));
		assertFalse(ECKey.validateAddress("1A1zP1eP5QGefi2DMPTfssTL5SLmv7sisfN"));

		// create new private key and address
		String newPrivateKeyWIF = ECKey.createNewPrivateKey();
		assertNotNull(newPrivateKeyWIF);
		String newAddress = ECKey.deriveAddress(newPrivateKeyWIF);
		assertNotNull(newAddress);
		assertTrue(ECKey.validateAddress(newAddress));
	}

	@Test
	public void testConstructTemporaryWallet() throws Exception {
		org.bitcoinj.core.ECKey signingKey = new DumpedPrivateKey(MainNetParams.get(),
				"Kwg7NfVRrnrDUehdE9hn3qEZ51Tfk7rdr6rmyoHvjhRhoZE1KVkd").getKey();

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
				new Output("cc29ca30675676d101f2a0044ff14d69c09f586eb87be4808bbffe4d80302e86", 0, "", false, 2000l,
						"ffff"),
				new Output("cc29ca30675676d101f2a0044ff14d69c09f586eb87be4808bbffe4d80302e86", 3, "", false, 1000l,
						"ffff"),
				new Output("b72d2f5065bb91b42cb9288e500421f53f53dc38f59e978bfbb350545312c865", 3, "", false, 3000l,
						"ffff"),
				new Output("45c353f908ff6ee2ce6c0a6256e7070c7c071def6f2b04ecf0992a1d266f800e", 0, "", false, 4000l,
						"ffff") };
		MockWallet wallet = new MockWallet(MainNetParams.get());
		CoinStackClient.injectOutputs(wallet, outputBatch1, true);

		assertEquals(3, wallet.getTxMap().size());

		Output[] outputBatch2 = {
				new Output("cc29ca30675676d101f2a0044ff14d69c09f586eb87be4808bbffe4d80302e86", 3, "", false, 1000l,
						"ffff"),
				new Output("b72d2f5065bb91b42cb9288e500421f53f53dc38f59e978bfbb350545312c865", 3, "", false, 3000l,
						"ffff"),
				new Output("cc29ca30675676d101f2a0044ff14d69c09f586eb87be4808bbffe4d80302e86", 0, "", false, 2000l,
						"ffff"),
				new Output("45c353f908ff6ee2ce6c0a6256e7070c7c071def6f2b04ecf0992a1d266f800e", 0, "", false, 4000l,
						"ffff") };
		wallet = new MockWallet(MainNetParams.get());
		CoinStackClient.injectOutputs(wallet, outputBatch2, true);

		assertEquals(3, wallet.getTxMap().size());
		int nonDummyOutputCount = 0;
		for (TransactionOutput output : wallet.getTxMap()
				.get(new Sha256Hash(CoinStackClient
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
		// System.out.println("publickey1.getBytes()" +
		// publickey1.getBytes().toString());
		// System.out.println("publickey1.()" + publickey1);
		pubkeys.add(Hex.decodeHex(publickey1.toCharArray()));
		pubkeys.add(Hex.decodeHex(publickey2.toCharArray()));
		pubkeys.add(Hex.decodeHex(publickey3.toCharArray()));

		String redeemScript = MultiSig.createRedeemScript(2, pubkeys);
		System.out.println("redeem : " + redeemScript);
	}

	@Test
	public void testSignMessage() {
		String privateKeyWIF = "Kwg7NfVRrnrDUehdE9hn3qEZ51Tfk7rdr6rmyoHvjhRhoZE1KVkd";
		// String message = "this is a test";
		// String signature = CoinStackClient.signMessage(privateKeyWIF,
		// message, true);
		// System.out.println("signature : " + signature);
		// String address = CoinStackClient.deriveAddress(privateKeyWIF);

		String signature = "H4uYYncnZ0wBSeZJ7BFsRVA76hwuOYAAJI5AfngS2Q+sjl7LDdLLCtwpVBCyq7J5+oS4jbvDP1o2fauqV163teQ=";
		String message = "Hello Bro";
		String address = "14hV8B8vQRDz6WPWkSe2FMimyr7qYJQBZ5";
		boolean res = ECDSA.verifyMessageSignature(address, message, signature, true);
		System.out.println("res : " + res);
		assertTrue(res);
	}
}