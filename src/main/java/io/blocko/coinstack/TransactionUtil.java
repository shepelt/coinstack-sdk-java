package io.blocko.coinstack;

import org.bitcoinj.core.Utils;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;

import io.blocko.coinstack.model.Input;
import io.blocko.coinstack.model.Output;
import io.blocko.coinstack.model.Transaction;

public class TransactionUtil {

	/**
	 * Construct a transaction object from raw transaction
	 * 
	 * @param rawTransaction
	 *            transaction in hex-encoded string format
	 * @return a transaction object representing the given raw transaction
	 */
	public static Transaction parseRawTransaction(String rawTransaction) {
		return TransactionUtil.parseRawTransaction(rawTransaction, true);
	}

	public static Transaction parseRawTransaction(String rawTransaction, boolean isMainNet) {
		org.bitcoinj.core.Transaction tx = new org.bitcoinj.core.Transaction(
				isMainNet ? MainNetParams.get() : RegTestParams.get(),
				org.bitcoinj.core.Utils.HEX.decode(rawTransaction));
		tx.getInputs();
		tx.getOutputs();
		Input[] inputs = new Input[tx.getInputs().size()];
		for (int i = 0; i < tx.getInputs().size(); i++) {
			inputs[i] = new Input(i, null, tx.getInput(i).getParentTransaction().getHashAsString(), 0l);
		}
	
		Output[] outputs = new Output[tx.getOutputs().size()];
		for (int i = 0; i < tx.getOutputs().size(); i++) {
			outputs[i] = new Output(tx.getHashAsString(), i,
					tx.getOutput(i).getScriptPubKey()
							.getToAddress(isMainNet ? MainNetParams.get() : RegTestParams.get()).toString(),
					false, tx.getOutput(i).getValue().value, Utils.HEX.encode(tx.getOutput(i).getScriptBytes()));
		}
	
		Transaction parsedTx = new Transaction(tx.getHashAsString(), new String[] {}, tx.getUpdateTime(), false, inputs,
				outputs);
		return parsedTx;
	}

	public static String getTransactionHash(String rawTransaction, boolean isMainNet) {
		return new org.bitcoinj.core.Transaction(isMainNet ? MainNetParams.get() : RegTestParams.get(),
				org.bitcoinj.core.Utils.HEX.decode(rawTransaction)).getHashAsString();
	}

	/**
	 * Calculate transaction hash from raw transaction
	 * 
	 * @param rawTransaction
	 *            transaction in hex-encoded string format
	 * @return the hash (transaction ID) of given raw transaction
	 */
	public static String getTransactionHash(String rawTransaction) {
		return getTransactionHash(rawTransaction, true);
	}

}
