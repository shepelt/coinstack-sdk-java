/**
 * 
 */
package io.cloudwallet.coinstack;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.signers.LocalTransactionSigner;
import org.bitcoinj.signers.TransactionSigner.ProposedTransaction;
import org.bitcoinj.wallet.KeyBag;
import org.bitcoinj.wallet.RedeemData;

/**
 * @author nepho
 *
 */
public class CoinStackClient {
	private AbstractCoinStackAdaptor coinStackAdaptor;
	private static SecureRandom secureRandom = new SecureRandom();

	public CoinStackClient() {
		this.coinStackAdaptor = new CloudWalletBackEndAdaptor(
				"http://search.cloudwallet.io");
		coinStackAdaptor.init();
	}

	protected CoinStackClient(AbstractCoinStackAdaptor coinStackAdaptor) {
		this.coinStackAdaptor = coinStackAdaptor;
		coinStackAdaptor.init();
	}

	public void close() {
		coinStackAdaptor.fini();
	}

	public BlockchainStatus getBlockchainStatus() throws IOException {
		return new BlockchainStatus(coinStackAdaptor.getBestHeight(),
				coinStackAdaptor.getBestBlockHash());
	}

	public Block getBlock(String blockId) throws IOException {
		return coinStackAdaptor.getBlock(blockId);
	}

	public Transaction getTransaction(String transactionId) throws IOException {
		return coinStackAdaptor.getTransaction(transactionId);
	}

	public String[] getTransactions(String address) throws IOException {
		return coinStackAdaptor.getTransactions(address);
	}

	public long getBalance(String address) throws IOException {
		return coinStackAdaptor.getBalance(address);
	}

	public Output[] getUnspentOutputs(String address) throws IOException {
		return coinStackAdaptor.getUnspentOutputs(address);
	}

	public String createRawTransaction(String privateKeyWIF,
			String destinationAddress, long amount, long fee)
			throws IOException, IllegalArgumentException {
		// check sanity test for parameters

		// derive address from private key
		final ECKey signingKey;
		try {
			signingKey = new DumpedPrivateKey(MainNetParams.get(),
					privateKeyWIF).getKey();
		} catch (AddressFormatException e) {
			throw new IllegalArgumentException("Parsing private key failed", e);
		}
		Address fromAddress = signingKey.toAddress(MainNetParams.get());
		String from = fromAddress.toString();
		// get unspentout from address
		Output[] outputs = this.getUnspentOutputs(from);
		// create bigcoinj tx object
		org.bitcoinj.core.Transaction tx = new org.bitcoinj.core.Transaction(
				MainNetParams.get());
		// pick inputs
		Output[] pickedOutputs = pickOutputsNeeded(outputs, amount, fee);
		if (null == pickedOutputs) {
			throw new IllegalArgumentException(
					"Not enough balance for amount and fee");
		}
		// calculate change
		long changeAmount = calculateChange(pickedOutputs, amount, fee);
		Address destinationAddressConveretd;
		try {
			destinationAddressConveretd = new Address(MainNetParams.get(),
					destinationAddress);
		} catch (AddressFormatException e) {
			throw new IllegalArgumentException("Malformed destination address");
		}
		// add outputs
		// add destinationAddress output
		tx.addOutput(Coin.valueOf(amount), destinationAddressConveretd);
		// add change
		if (changeAmount > 0l) {
			tx.addOutput(Coin.valueOf(changeAmount), fromAddress);
		}
		// add inputs and sign them
		for (Output output : pickedOutputs) {
			Script prevOutScript = new Script(
					org.bitcoinj.core.Utils.HEX.decode(output.getScript()));
			tx.addInput(
					new Sha256Hash(convertEndianness(output.getTransactionId())),
					output.getIndex(), prevOutScript);
//			TransactionOutput prevOutput = new TransactionOutput(MainNetParams.get(), new org.bitcoinj.core.Transaction(MainNetParams.get()), Coin.valueOf(output.getValue()), prevOutScript);
//			tx.addInput(prevOutput);
			
//			TransactionOutPoint previousOutPoint = new TransactionOutPoint(
//					MainNetParams.get(), output.getIndex(), new Sha256Hash(
//							convertEndianness(output.getTransactionId())));
//			Script prevOutScript = new Script(
//					org.bitcoinj.core.Utils.HEX.decode(output.getScript()));
//						tx.addSignedInput(previousOutPoint, prevOutScript, signingKey);
//			tx.addInput(previousOutput );
			
		}
		// sign tx using txsigner
		CoinStackSigner signer = new CoinStackSigner();
		signer.signingKey = signingKey;
		KeyBag keyBag = new KeyBag() {
			
			public RedeemData findRedeemDataFromScriptHash(byte[] scriptHash) {
				return null;
			}
			
			public ECKey findKeyFromPubKey(byte[] pubkey) {
				// TODO Auto-generated method stub
				return null;
			}
			
			public ECKey findKeyFromPubHash(byte[] pubkeyHash) {
				// return private key only
				return signingKey;
			}
		};
		ProposedTransaction propTx = new ProposedTransaction(tx);
		signer.signInputs(propTx, keyBag);

		// serialize bitcoinj tx object
		byte[] rawTx = tx.bitcoinSerialize();
		// convert to string encoded hex and return
		return Utils.HEX.encode(rawTx);
	}

	public static String getTransactionHash(String rawTransaction) {
		return new org.bitcoinj.core.Transaction(MainNetParams.get(),
				org.bitcoinj.core.Utils.HEX.decode(rawTransaction))
				.getHashAsString();
	}

	public void sendTransaction(String rawTransaction) throws IOException {
		coinStackAdaptor.sendTransaction(rawTransaction);
	}

	protected static Output[] pickOutputsNeeded(Output[] outputs, long amount,
			long fee) {
		long sum = 0;
		long threshold = amount + fee;
		// sort outputs first in ascending order to use small outputs first
		Arrays.sort(outputs);
		List<Output> filteredOutputs = new LinkedList<Output>();
		for (Output output : outputs) {
			sum += output.getValue();
			filteredOutputs.add(output);
			if (sum >= threshold) {
				break;
			}
		}
		if (sum >= threshold) {
			return filteredOutputs.toArray(new Output[0]);
		} else {
			return null;
		}
	}

	protected static long calculateChange(Output[] outputs, long amount,
			long fee) {
		long outputSum = 0l;
		for (Output output : outputs) {
			outputSum += output.getValue();
		}
		return outputSum - amount - fee;
	}

	public static long convertToSatoshi(String bitcoinAmount) {
		return Coin.parseCoin(bitcoinAmount).value;
	}

	protected static String convertEndianness(String original) {
		StringBuilder result = new StringBuilder();
		for (int i = original.length() - 2; i >= 0; i -= 2) {
			result.append(original.substring(i, i + 2));
		}
		return result.toString();
	}

	public static String deriveAddress(String privateKeyWIF)
			throws IllegalArgumentException {
		ECKey signingKey;
		try {
			signingKey = new DumpedPrivateKey(MainNetParams.get(),
					privateKeyWIF).getKey();
		} catch (AddressFormatException e) {
			throw new IllegalArgumentException("Parsing private key failed", e);
		}
		return signingKey.toAddress(MainNetParams.get()).toString();
	}

	public static String createNewPrivateKey() {
		ECKey ecKey = new ECKey(secureRandom);
		return ecKey.getPrivateKeyEncoded(MainNetParams.get()).toString();
	}

	public static boolean validateAddress(String string) {
		try {
			new Address(MainNetParams.get(), string);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
