package io.blocko.coinstack;

import java.util.ArrayList;
import java.util.List;

import io.blocko.coinstack.exception.MalformedInputException;
import io.blocko.coinstack.model.Output;

/**
 * @author shepelt
 *
 */
public class TransactionBuilder {
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

	public void allowDustyOutput(boolean allowDustyOutput) {
		this.allowDustyOutput = allowDustyOutput;
	}

	public boolean allowsDustyOutput() {
		return allowDustyOutput;
	}

	protected byte[] getData() {
		return data;
	}

	protected long getFee() {
		return fee;
	}

	protected Output[] getOutputs() {
		return outputs.toArray(new Output[outputs.size()]);
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
			throw new MalformedInputException("Invalid data", "payload length over 80 bytes");
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
			throw new MalformedInputException("Invalid fee", "Fee amount below dust threshold");
		}
		this.fee = fee;
	}

	public boolean shuffleOutputs() {
		return shuffleOutputs;
	}

	public void shuffleOutputs(boolean shuffleOutputs) {
		this.shuffleOutputs = shuffleOutputs;
	}

}
