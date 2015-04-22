/**
 * 
 */
package io.cloudwallet.coinstack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.wallet.WalletTransaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nepho
 *
 */
public class CoinStackClientWithTestnetTest {
	protected CoinStackClient coinStackClient;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		coinStackClient = new CoinStackClient(new MockTestnetCoinStackAdaptor(), false);
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
		Block block = coinStackClient
				.getBlock("000000000933ea01ad0ee984209779baaec3ced90fa3f408719526f8d77f4943");
		assertNotNull(block);
		assertEquals(0, block.getHeight());
		assertEquals(1, block.getTransactionIds().length);
		assertEquals(
				"4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b",
				block.getTransactionIds()[0]);
		assertEquals(1296688602000L, block.getBlockConfirmationTime().getTime());
		assertTrue(block.getParentId() == null);
		assertNotNull(block.getChildIds());
		assertEquals(1, block.getChildIds().length);
		assertEquals(
				"00000000b873e79784647a6c82962c70d228557d24a747ea4d1b8bbe878e1206",
				block.getChildIds()[0]);

		// other block test
		block = coinStackClient
				.getBlock("00000000b873e79784647a6c82962c70d228557d24a747ea4d1b8bbe878e1206");
		assertNotNull(block);
		assertEquals(1, block.getHeight());
		assertEquals(1, block.getTransactionIds().length);
		assertEquals(
				"f0315ffc38709d70ad5647e22048358dd3745f3ce3874223c80a7c92fab0c8ba",
				block.getTransactionIds()[0]);
		assertEquals(1296688928000L, block.getBlockConfirmationTime().getTime());
		assertTrue(block
				.getParentId()
				.equals("000000000933ea01ad0ee984209779baaec3ced90fa3f408719526f8d77f4943"));
		assertNotNull(block.getChildIds());
		assertEquals(1, block.getChildIds().length);
		assertEquals(
				"000000006c02c8ea6e4ff69651f7fcde348fb9d557a06e6957b65552002a7820",
				block.getChildIds()[0]);
	}

	@Test
	public void testTransaction() throws Exception {
		Transaction transaction = coinStackClient
				.getTransaction("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b");
		assertNotNull(transaction);
		assertEquals(
				"4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b",
				transaction.getId());
		assertEquals(1, transaction.getBlockIds().length);
		assertEquals(
				"000000000933ea01ad0ee984209779baaec3ced90fa3f408719526f8d77f4943",
				transaction.getBlockIds()[0]);
		assertEquals(1296688602000L, transaction.getConfirmationTime()
				.getTime());
		assertTrue(transaction.isCoinbase());
		assertEquals(0, transaction.getInputs().length);
		assertEquals(1, transaction.getOutputs().length);
		assertEquals("mpXwg4jMtRhuSpVq4xS3HFHmCmWp9NyGKt",
				transaction.getOutputs()[0].getAddress());
		assertEquals(5000000000l, transaction.getOutputs()[0].getValue());
		assertEquals(
				"4104678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5fac",
				transaction.getOutputs()[0].getScript());

		transaction = coinStackClient
				.getTransaction("083e1d738d782b388767fc6e6b77107718763281b31bd7cf6a699a3fd4a6958b");
		assertNotNull(transaction);
		assertEquals(
				"083e1d738d782b388767fc6e6b77107718763281b31bd7cf6a699a3fd4a6958b",
				transaction.getId());
		assertEquals(1, transaction.getBlockIds().length);
		assertEquals(
				"0000000015a0e164c15b871293fdc12c3983b2cd118ebb754d80fe023a43fc07",
				transaction.getBlockIds()[0]);
		assertEquals(1426152531000L, transaction.getConfirmationTime()
				.getTime());
		assertFalse(transaction.isCoinbase());
		assertEquals(2, transaction.getInputs().length);
		assertNotNull(transaction.getInputs()[0].getOutputTransactionId());
		assertEquals(
				"14a94b66578b5c9c2e904586b2b2dfc079e8ee26dac83e87851b52478b61a1ae",
				transaction.getInputs()[0].getOutputTransactionId());
		assertEquals(0, transaction.getInputs()[0].getOutputIndex());
		assertNotNull(transaction.getInputs()[0].getOutputAddress());
		assertEquals("moAHUTQ23CU2aAMD2M8e8MBHYvrWXdNjcM",
				transaction.getInputs()[0].getOutputAddress());
		assertEquals(1000000L, transaction.getInputs()[0].getValue());
		assertEquals(1, transaction.getOutputs().length);
		assertEquals("mz9R2o5kVaSG1bExrWakrkXYe1kUFBEF2J",
				transaction.getOutputs()[0].getAddress());
		assertEquals(50000000L, transaction.getOutputs()[0].getValue());
		assertEquals("76a914cc578ebe1f9613053d18e0f03e0fbaf0952caedb88ac",
				transaction.getOutputs()[0].getScript());
	}

	@Test
	public void testAddress() throws Exception {
		// Genesis Address
		String[] transactions = coinStackClient
				.getTransactions("mpXwg4jMtRhuSpVq4xS3HFHmCmWp9NyGKt");

		assertNotNull(transactions);
		assertTrue(transactions.length >= 1);

		transactions = coinStackClient
				.getTransactions("mfqge3DMrshybtbS3QZ58yd8SjkCF3iFMf");
		assertNotNull(transactions);
		assertTrue(transactions.length >= 1);

		transactions = coinStackClient
				.getTransactions("mz9R2o5kVaSG1bExrWakrkXYe1kUFBEF2J");
		assertNotNull(transactions);
		assertTrue(transactions.length == 1);
		long balance = coinStackClient
				.getBalance("mz9R2o5kVaSG1bExrWakrkXYe1kUFBEF2J");
		// Test 를 위해 그 누구도 권한이 없는 주소로 송금하였습니다. 혹시 잔고가 변경되면 연락해주세요.
		assertTrue(balance == 50000000L);

		// testing unspent outputs
		Output[] outputs = coinStackClient
				.getUnspentOutputs("mz9R2o5kVaSG1bExrWakrkXYe1kUFBEF2J");
		assertNotNull(outputs);
		assertEquals(1, outputs.length);
		assertEquals(
				"8b95a6d43f9a696acfd71bb3813276187710776b6efc6787382b788d731d3e08",
				outputs[0].getTransactionId());
		assertEquals(0, outputs[0].getIndex());
		assertEquals(50000000L, outputs[0].getValue());
		assertEquals("76a914cc578ebe1f9613053d18e0f03e0fbaf0952caedb88ac",
				outputs[0].getScript());

		// test validating addesses
		assertTrue(CoinStackClient
				.validateAddress("mfqge3DMrshybtbS3QZ58yd8SjkCF3iFMf", false));
		assertFalse(CoinStackClient
				.validateAddress("1A1zP1eP5QGefi2DMPTfssTL5SLmv7sisfN", false));

		// create new private key and address
		String newPrivateKeyWIF = CoinStackClient.createNewPrivateKey(false);
		assertNotNull(newPrivateKeyWIF);
		String newAddress = CoinStackClient.deriveAddress(newPrivateKeyWIF, false);
		assertNotNull(newAddress);
		assertTrue(CoinStackClient.validateAddress(newAddress, false));
	}

	@Test
	public void testConstructTemporaryWallet() throws Exception {
		ECKey signingKey = new DumpedPrivateKey(MainNetParams.get(),
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
		Logger logger = LoggerFactory.getLogger(Wallet.class);
		
		MockWallet wallet = new MockWallet(MainNetParams.get());
		CoinStackClient.injectOutputs(wallet, outputBatch1);

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
		CoinStackClient.injectOutputs(wallet, outputBatch1);

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
}