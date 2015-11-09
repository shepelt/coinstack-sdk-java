package io.blocko.coinstack.model;

import java.util.Date;

public class Stamp {
	private String txId;

	private int outputIndex;

	private int confirmations;

	private Date timestamp;

	public Stamp(String txId, int outputIndex, int confirmations, Date timestamp) {
		super();
		this.txId = txId;
		this.outputIndex = outputIndex;
		this.confirmations = confirmations;
		this.timestamp = timestamp;
	}

	public int getConfirmations() {
		return confirmations;
	}

	public int getOutputIndex() {
		return outputIndex;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public String getTxId() {
		return txId;
	}
}
