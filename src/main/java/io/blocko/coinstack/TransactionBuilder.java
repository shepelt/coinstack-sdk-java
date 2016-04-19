package io.blocko.coinstack;

import io.blocko.coinstack.exception.CoinStackException;
import io.blocko.coinstack.exception.InsufficientFundException;
import io.blocko.coinstack.exception.MalformedInputException;
import io.blocko.coinstack.model.DataTransactionOutput;
import io.blocko.coinstack.model.DustyOutput;
import io.blocko.coinstack.model.Output;
import io.blocko.coinstack.util.BitcoinjUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.core.Wallet.DustySendRequested;
import org.bitcoinj.core.Wallet.SendRequest;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;

/**
 * @author shepelt
 *
 */
public class TransactionBuilder extends AbstractTransactionBuilder {
	private long fee;
	private List<Output> outputs;
	private byte[] data;
	private boolean allowDustyOutput = false;
	private boolean shuffleOutputs = true;

	/**
	 * A helper class for creating a transaction template to be signed
	 */
	public TransactionBuilder() {
		this.outputs = new ArrayList<Output>();
		this.data = null;
	}

	/**
	 * Add a standard output to transaction
	 * 
	 * @param to
	 *            address to send fund
	 * @param amount
	 *            amount of fund to transfer
	 */
	public void addOutput(String destinationAddress, long amount) {
		outputs.add(new Output("", 0, destinationAddress, false, amount, null));
	}

	/**
	 * If output valus is less than 546 satoshis, it is dusty output If
	 * allowDustyOutput is true, this small value is accepted by client
	 */
	public void allowDustyOutput(boolean allowDustyOutput) {
		this.allowDustyOutput = allowDustyOutput;
	}

	public boolean allowsDustyOutput() {
		return allowDustyOutput;
	}

	public Output[] getOutputs() {
		return outputs.toArray(new Output[outputs.size()]);
	}

	protected long getFee() {
		return fee;
	}

	/**
	 * Set data transfered using OP_RETURN for transaction
	 * 
	 * @param data
	 *            80 bytes data payload to be attached to transaction
	 * @throws MalformedInputException
	 * 
	 */
	public void setData(byte[] data) throws MalformedInputException {
		if (data.length > 80) {
			throw new MalformedInputException("Invalid data",
					"payload length over 80 bytes");
		}

		if (null != this.data) {
			throw new MalformedInputException("Invalid data",
					"multiple data payload not allowed");
		}

		this.data = data;
	}

	/**
	 * Set transfer fee for transaction
	 * 
	 * @param fee
	 *            amount of fee to pay to miners (minimum 0.0001 BTC = 10000
	 *            satoshi)
	 * @throws MalformedInputException
	 */
	public void setFee(long fee) throws MalformedInputException {
		if (fee < 10000) {
			throw new MalformedInputException("Invalid fee",
					"Fee amount below dust threshold");
		}
		this.fee = fee;
	}

	/**
	 * return shuffleOutputs boolean
	 */
	public boolean shuffleOutputs() {
		return shuffleOutputs;
	}

	/**
	 * Set option whether to shuffle order of outputs or not
	 * 
	 * @param shuffleOutputs
	 *            true is enabling shuffle, false is disable
	 * 
	 */
	public void shuffleOutputs(boolean shuffleOutputs) {
		this.shuffleOutputs = shuffleOutputs;
	}

	public byte[] getData() {
		return data;
	}

	private NetworkParameters getNetwork(CoinStackClient client) {
		return client.isMainNet() ? MainNetParams.get() : RegTestParams.get();
	}

	@Override
	public String buildTransaction(CoinStackClient client, String privateKeyWIF)
			throws IOException, CoinStackException {
		// check sanity test for parameters
		org.bitcoinj.core.Transaction txTemplate = new org.bitcoinj.core.Transaction(
				this.getNetwork(client));
		for (Output output : this.getOutputs()) {
			Address destinationAddressParsed;
			try {
				destinationAddressParsed = new Address(this.getNetwork(client),
						output.getAddress());
			} catch (AddressFormatException e) {
				throw new MalformedInputException("Invalid output",
						"Malformed address");
			}

			if (this.allowsDustyOutput()) {
				txTemplate.addOutput(new DustyOutput(this.getNetwork(client),
						txTemplate, Coin.valueOf(output.getValue()),
						destinationAddressParsed));
			} else {
				txTemplate.addOutput(Coin.valueOf(output.getValue()),
						destinationAddressParsed);
			}
		}

		// add OP_RETURN
		if (null != this.getData()) {
			Script script = new ScriptBuilder().op(ScriptOpCodes.OP_RETURN)
					.data(this.getData()).build();
			TransactionOutput output = new DataTransactionOutput(
					this.getNetwork(client), txTemplate, Coin.ZERO,
					script.getProgram());
			txTemplate.addOutput(output);
		}

		// derive address from private key
		final ECKey signingKey;
		try {
			signingKey = new DumpedPrivateKey(this.getNetwork(client),
					privateKeyWIF).getKey();
		} catch (AddressFormatException e) {
			throw new MalformedInputException("Invalid input",
					"Parsing private key failed");
		}
		Address fromAddress = signingKey.toAddress(this.getNetwork(client));
		String from = fromAddress.toString();

		// get unspentout from address
		Output[] outputs = client.getUnspentOutputs(from);
		Wallet tempWallet = new Wallet(this.getNetwork(client));
		tempWallet.allowSpendingUnconfirmedTransactions();
		tempWallet.importKey(signingKey);
		BitcoinjUtil.injectOutputs(tempWallet, outputs, client.isMainNet());

		SendRequest request = SendRequest.forTx(txTemplate);
		request.changeAddress = fromAddress;
		request.fee = Coin.valueOf(this.getFee());
		request.feePerKb = Coin.ZERO;

		org.bitcoinj.core.Transaction tx;
		try {
			tx = tempWallet.sendCoinsOffline(request);
		} catch (InsufficientMoneyException e) {
			throw new InsufficientFundException("Insufficient fund");
		} catch (DustySendRequested e) {
			throw new MalformedInputException("Invalid output",
					"Send amount below dust threshold");
		}
		byte[] rawTx = tx.bitcoinSerialize();
		// // convert to string encoded hex and return
		return Utils.HEX.encode(rawTx);
	}
}
