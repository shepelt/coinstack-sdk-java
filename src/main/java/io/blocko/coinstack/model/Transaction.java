/**
 * 
 */
package io.blocko.coinstack.model;

import java.util.Date;

/**
 * @author nepho
 *
 */
public class Transaction {
	private String[] blockIds;
	private String id;
	private Date confirmationTime;
	private Input[] inputs;
	private boolean isCoinbase;
	private Output[] outputs;

	public Transaction(String id, String[] blockIds, Date confirmationTime,
			boolean isCoinbase, Input[] inputs, Output[] outputs) {
		this.id = id;
		this.blockIds = blockIds;
		this.confirmationTime = confirmationTime;
		this.isCoinbase = isCoinbase;
		this.inputs = inputs;
		this.outputs = outputs;
	}

	public String[] getBlockIds() {
		return blockIds;
	}

	public String getId() {
		return id;
	}

	public Input[] getInputs() {
		return inputs;
	}

	public Output[] getOutputs() {
		return outputs;
	}

	public boolean isCoinbase() {
		return isCoinbase;
	}

	public Date getConfirmationTime() {
		return confirmationTime;
	}
}
