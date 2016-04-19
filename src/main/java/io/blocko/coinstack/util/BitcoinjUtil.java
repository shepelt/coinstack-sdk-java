package io.blocko.coinstack.util;

import java.util.Arrays;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.wallet.WalletTransaction;

import io.blocko.coinstack.CoinStackClient;
import io.blocko.coinstack.model.Output;

public class BitcoinjUtil {
	public static void injectOutputs(Wallet wallet, Output[] outputs, boolean isMainNet) {
		// sort outputs with txid and output index
		Arrays.sort(outputs, CoinStackClient.outputComparator);
		CoinStackClient.TemporaryTransaction tx = null;
		for (Output output : outputs) {
			// Sha256Hash outputHash = Sha256Hash.wrap(
			Sha256Hash outputHash = new Sha256Hash(CoinStackClient.convertEndianness(output.getTransactionId()));
			if (tx == null || !tx.getHash().equals(outputHash)) {
				tx = new CoinStackClient.TemporaryTransaction(isMainNet ? MainNetParams.get() : RegTestParams.get(), outputHash);
				tx.getConfidence().setConfidenceType(TransactionConfidence.ConfidenceType.BUILDING);
				wallet.addWalletTransaction(new WalletTransaction(WalletTransaction.Pool.UNSPENT, tx));
			}
	
			// fill hole between indexes with dummies
			while (tx.getOutputs().size() < output.getIndex()) {
				tx.addOutput(new TransactionOutput(isMainNet ? MainNetParams.get() : RegTestParams.get(), tx,
						Coin.NEGATIVE_SATOSHI, new byte[] {}));
			}
	
			tx.addOutput(new TransactionOutput(isMainNet ? MainNetParams.get() : RegTestParams.get(), tx,
					Coin.valueOf(output.getValue()), org.bitcoinj.core.Utils.HEX.decode(output.getScript())));
		}
	
	}

}
