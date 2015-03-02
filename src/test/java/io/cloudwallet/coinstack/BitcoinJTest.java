package io.cloudwallet.coinstack;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.core.Transaction;
import org.junit.Assert;
import org.junit.Test;

import com.subgraph.orchid.encoders.Hex;

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
					MainNetParams.get(),
					"1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa");
		} catch (Exception e) {
			// should be no exception
			Assert.fail();
		}

		try {
			foo = new Address(
					MainNetParams.get(),
					"1A1zP1eP5QGefi2DMPTfssTL5SLmv7sisfN"); // malformed address
			// should be exception
			Assert.fail();
		} catch (Exception e) {
		}

		SecureRandom secureRandom = new SecureRandom();
		ECKey ecKey = new ECKey(secureRandom);
		assertNotNull(ecKey.getPrivKeyBytes());
		String privateKey = ecKey.getPrivateKeyEncoded(MainNetParams.get())
				.toString(); // WIF format
		assertNotNull(privateKey);
//		System.out.println(privateKey);
//		System.out.println(ecKey.toAddress(MainNetParams.get()).toString()); // address
		
		
		// creating and signing and serializng raw tx
		ECKey signingKey = new DumpedPrivateKey(MainNetParams.get(), "Kwg7NfVRrnrDUehdE9hn3qEZ51Tfk7rdr6rmyoHvjhRhoZE1KVkd").getKey();
		TransactionOutPoint prevOutPoint = new TransactionOutPoint(MainNetParams.get(), 1, new Sha256Hash("fdf0e02ee773812de491bdab815e6704ea6da21ba4de1249576e224f41ff80d6"));
		Script prevOutScript = new Script(org.bitcoinj.core.Utils.HEX.decode("76a91415aad25727498a360e92eeb96db26f55fb38edcb88ac"));
		Transaction tx = new Transaction(MainNetParams.get());
		tx.addOutput(Coin.parseCoin("0.0001"), new Address(MainNetParams.get(), "12yZpvc6Udd7QAetLfRBy8NmJjMLG7XD1N"));
		
		tx.addSignedInput(prevOutPoint, prevOutScript, signingKey);
		tx.verify();
		byte[] rawTx = tx.bitcoinSerialize();
//		System.out.println(org.bitcoinj.core.Utils.HEX.encode(rawTx));
		
		// testing satoshi-coin conversion
		String stringCoin1 = "0.0001";
		Coin coin = Coin.parseCoin(stringCoin1); // convert fractional value to Coin object
		long longCoin = coin.value;
		Coin coin2 = Coin.valueOf(longCoin); // convert long value to Coin object
		assertEquals(coin.value, coin2.value);
		String stringCoin2 = coin2.toPlainString();
		assertEquals(stringCoin1, stringCoin2);
	}
}
