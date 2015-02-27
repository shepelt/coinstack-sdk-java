/**
 * 
 */
package io.cloudwallet.coinstack;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author nepho
 *
 */
public class CoinStackClientTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetBlockchainStatus() {
		MockCoinStackAdaptor mockCoinStackAdaptor = new MockCoinStackAdaptor();

		BlockchainStatus blockchainStatus = CoinStackClient
				.getBlockchainStatus(mockCoinStackAdaptor);
		assertTrue(blockchainStatus.getBestHeight() > 0);
		assertNotNull(blockchainStatus.getBestBlockHash());
		assertTrue(blockchainStatus.getBestBlockHash().length() > 0);
	}

	@Test
	public void testGetBlock() {
		MockCoinStackAdaptor mockCoinStackAdaptor = new MockCoinStackAdaptor();

		// genesis block test
		Block block = CoinStackClient
				.getBlock(mockCoinStackAdaptor,
						"000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f");
		assertNotNull(block);
		assertEquals(0, block.getHeight());
		assertEquals(1, block.getTransactionIds().length);
		assertEquals(
				"4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b",
				block.getTransactionIds()[0]);
		assertEquals(1231006505000L, block.getBlockConfirmationTime().getTime());
		assertTrue(block.getParentId() == null);
		assertNotNull(block.getChildIds());
		assertEquals(1, block.getChildIds().length);
		assertEquals(
				"00000000839a8e6886ab5951d76f411475428afc90947ee320161bbf18eb6048",
				block.getChildIds()[0]);

		// other block test
		block = CoinStackClient
				.getBlock(mockCoinStackAdaptor,
						"00000000839a8e6886ab5951d76f411475428afc90947ee320161bbf18eb6048");
		assertNotNull(block);
		assertEquals(1, block.getHeight());
		assertEquals(1, block.getTransactionIds().length);
		assertEquals(
				"0e3e2357e806b6cdb1f70b54c3a3a17b6714ee1f0e68bebb44a74b1efd512098",
				block.getTransactionIds()[0]);
		assertEquals(1231469665000L, block.getBlockConfirmationTime().getTime());
		assertTrue(block
				.getParentId()
				.equals("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f"));
		assertNotNull(block.getChildIds());
		assertEquals(1, block.getChildIds().length);
		assertEquals(
				"000000006a625f06636b8bb6ac7b960a8d03705d1ace08b1a19da3fdcc99ddbd",
				block.getChildIds()[0]);
	}

	@Test
	public void testTransaction() {
		MockCoinStackAdaptor mockCoinStackAdaptor = new MockCoinStackAdaptor();
		Transaction transaction = CoinStackClient
				.getTransaction(mockCoinStackAdaptor,
						"4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b");
		assertNotNull(transaction);
		assertEquals("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b", transaction.getId());
		assertEquals(1, transaction.getBlockIds().length);
		assertEquals("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f", transaction.getBlockIds()[0]);
		assertEquals(1231006505000L, transaction.getConfirmationTime().getTime());
		assertTrue(transaction.isCoinbase());
		assertEquals(0, transaction.getInputs().length);
		assertEquals(1, transaction.getOutputs().length);
		assertEquals("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa", transaction.getOutputs()[0].getAddress());
		assertFalse(transaction.getOutputs()[0].isSpent());
		assertEquals(5000000000l, transaction.getOutputs()[0].getValue());
		assertEquals("4104678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5fac", transaction.getOutputs()[0].getScript());
	}

	@Test
	public void testTimestamp() {
		Timestamp timestamp = new Timestamp(1231006505000L);
		System.out.println(timestamp.toString());

		Date date = new Date(1231006505000L);
		System.out.println(date.getTime());

		assertEquals(1231006505000L, date.getTime());
	}

}
