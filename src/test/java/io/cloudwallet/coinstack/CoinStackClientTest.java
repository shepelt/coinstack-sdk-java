/**
 * 
 */
package io.cloudwallet.coinstack;

import static org.junit.Assert.*;

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

		BlockchainStatus blockchainStatus = CoinStackClient.getBlockchainStatus(mockCoinStackAdaptor);
		assertTrue(blockchainStatus.getBestHeight() > 0);
		assertNotNull(blockchainStatus.getBestBlockHash());
		assertTrue(blockchainStatus.getBestBlockHash().length() > 0);
	}

}
