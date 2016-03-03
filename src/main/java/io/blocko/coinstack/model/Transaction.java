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
	private int[] blockHeights;

	public Transaction(String id, String[] blockIds, Date confirmationTime,
			boolean isCoinbase, Input[] inputs, Output[] outputs) {
		this.id = id;
		this.blockIds = blockIds;
		this.confirmationTime = confirmationTime;
		this.isCoinbase = isCoinbase;
		this.inputs = inputs;
		this.outputs = outputs;
	}

	public int[] getBlockHeights() {
		return blockHeights;
	}

	public String[] getBlockIds() {
		return blockIds;
	}

	public Date getConfirmationTime() {
		return confirmationTime;
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

	public void setBlockHeights(int[] blockHeights) {
		this.blockHeights = blockHeights;
	}
}
