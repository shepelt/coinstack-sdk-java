/**
 * 
 */
package io.cloudwallet.coinstack;

import static org.junit.Assert.*;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.NetworkParameters;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author shepelt
 *
 */
public class BitcoinJTest {

	@Test
	public void test() throws Exception {
		Address foo;
		try {
			foo = new Address(
					NetworkParameters.fromID(NetworkParameters.ID_MAINNET),
					"1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa");
		} catch (Exception e) {
			// should be no exception
			Assert.fail();
		}
		
		try {
			foo = new Address(
					NetworkParameters.fromID(NetworkParameters.ID_MAINNET),
					"1A1zP1eP5QGefi2DMPTfssTL5SLmv7sisfN"); // malformed address
			// should be exception
			Assert.fail();
		} catch (Exception e) {
		}

	}
}
