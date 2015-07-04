package io.cloudwallet.coinstack;

import java.util.ArrayList;
import java.util.List;

import io.cloudwallet.coinstack.exception.MalformedInputException;
import io.cloudwallet.coinstack.model.Output;

/**
 * @author shepelt
 *
 */
public class TransactionBuilder {
	private long fee;
	private List<Output> outputs;
	private byte[] data;

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
	 * Set transfer fee for transaction
	 * 
	 * @param fee
	 *            amount of fee to pay to miners (minimum 0.0001 BTC = 10000
	 *            satoshi)
	 */
	public void setFee(long fee) {
		if (fee < 10000) {
			throw new MalformedInputException("fee below minimum fee threshold");
		}
		this.fee = fee;
	}

	protected Output[] getOutputs() {
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
	 * 
	 */
	public void setData(byte[] data) {
		if (data.length > 80) {
			throw new MalformedInputException("payload length over 80 bytes");
		}

		if (null != this.data) {
			throw new MalformedInputException(
					"multiple data payload not allowed");
		}

		this.data = data;
	}

	protected byte[] getData() {
		return data;
	}

}
