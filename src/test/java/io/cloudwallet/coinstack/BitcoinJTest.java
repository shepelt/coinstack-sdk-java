package io.cloudwallet.coinstack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.security.SecureRandom;

import org.bitcoin.protocols.payments.Protos.PaymentDetails;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.core.Wallet.SendRequest;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.wallet.WalletTransaction;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author shepelt
 *
 */
public class BitcoinJTest {
	private static class TemporaryTransaction extends Transaction {
		private final Sha256Hash hash;

		public TemporaryTransaction(final NetworkParameters params,
				final Sha256Hash hash) {
			super(params);
			this.hash = hash;
		}

		@Override
		public Sha256Hash getHash() {
			return hash;
		}
	}

	@Test
	public void test() throws Exception {
		Address foo;
		try {
			foo = new Address(MainNetParams.get(),
					"1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa");
		} catch (Exception e) {
			// should be no exception
			Assert.fail();
		}

		try {
			foo = new Address(MainNetParams.get(),
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
		// System.out.println(privateKey);
		// System.out.println(ecKey.toAddress(MainNetParams.get()).toString());
		// // address

		// creating and signing and serializng raw tx
		ECKey signingKey = new DumpedPrivateKey(MainNetParams.get(),
				"Kwg7NfVRrnrDUehdE9hn3qEZ51Tfk7rdr6rmyoHvjhRhoZE1KVkd")
				.getKey();
		// TransactionOutPoint prevOutPoint = new
		// TransactionOutPoint(MainNetParams.get(), 1, new
		// Sha256Hash("fdf0e02ee773812de491bdab815e6704ea6da21ba4de1249576e224f41ff80d6"));
		// Script prevOutScript = new
		// Script(org.bitcoinj.core.Utils.HEX.decode("76a91415aad25727498a360e92eeb96db26f55fb38edcb88ac"));
		// Transaction tx = new Transaction(MainNetParams.get());
		// tx.addOutput(Coin.parseCoin("0.0001"), new
		// Address(MainNetParams.get(), "12yZpvc6Udd7QAetLfRBy8NmJjMLG7XD1N"));
		//
		// tx.addSignedInput(prevOutPoint, prevOutScript, signingKey);
		// tx.verify();
		// byte[] rawTx = tx.bitcoinSerialize();
		// Transaction newTx = new Transaction(MainNetParams.get(), rawTx);
		// assertEquals(tx.getHashAsString(), newTx.getHashAsString());
		// System.out.println(newTx.getHashAsString());
		// System.out.println(org.bitcoinj.core.Utils.HEX.encode(rawTx));

		Wallet tempWallet = new Wallet(MainNetParams.get());
		tempWallet.importKey(signingKey);
		System.out.println(tempWallet.getChangeAddress());

		Transaction unspentTx = new TemporaryTransaction(
				MainNetParams.get(),
				new Sha256Hash(
						CoinStackClient
								.convertEndianness("1f2f57831087d639743148afa9556d6d3e501e603783ba274647e1c114bbce3c")));
		unspentTx.getConfidence().setConfidenceType(
				TransactionConfidence.ConfidenceType.BUILDING);
		tempWallet.addWalletTransaction(new WalletTransaction(
				WalletTransaction.Pool.UNSPENT, unspentTx));

		// add fake output before output
		int outputIndex = 1;
		while (unspentTx.getOutputs().size() < outputIndex) {
			unspentTx.addOutput(new TransactionOutput(MainNetParams.get(),
					unspentTx, Coin.NEGATIVE_SATOSHI, new byte[] {}));
		}
		unspentTx
				.addOutput(new TransactionOutput(
						MainNetParams.get(),
						unspentTx,
						Coin.valueOf(60000l),
						org.bitcoinj.core.Utils.HEX
								.decode("76a91415aad25727498a360e92eeb96db26f55fb38edcb88ac")));
		SendRequest request = SendRequest
				.to(new Address(MainNetParams.get(),
						"12yZpvc6Udd7QAetLfRBy8NmJjMLG7XD1N"), Coin
						.parseCoin("0.0001"));
		request.changeAddress = new Address(MainNetParams.get(),
				"12yZpvc6Udd7QAetLfRBy8NmJjMLG7XD1N");
		request.fee = Coin.parseCoin("0.0001");
		System.out.println(request.fee);
		System.out.println(request.toString());
		// create send request
		Transaction rawTx = tempWallet.sendCoinsOffline(request);
		assertNotNull(rawTx);
		System.out.println(org.bitcoinj.core.Utils.HEX.encode(rawTx
				.bitcoinSerialize()));

		// testing satoshi-coin conversion
		String stringCoin1 = "0.0001";
		Coin coin = Coin.parseCoin(stringCoin1); // convert fractional value to
													// Coin object
		long longCoin = coin.value;
		Coin coin2 = Coin.valueOf(longCoin); // convert long value to Coin
												// object
		assertEquals(coin.value, coin2.value);
		String stringCoin2 = coin2.toPlainString();
		assertEquals(stringCoin1, stringCoin2);
		Coin coin3 = Coin.valueOf(10000l);
		assertEquals(coin, coin3);
	}
}
