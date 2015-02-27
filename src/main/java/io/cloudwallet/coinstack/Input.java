package io.cloudwallet.coinstack;

public class Input {

	public String outputAddress;
	public int outputIndex;
	public String outputTransactionId;
	public long value;

	public Input(int index, String outputAddress, String outputTransactionId,
			long value) {
		this.outputIndex = index;
		this.outputAddress = outputAddress;
		this.outputTransactionId = outputTransactionId;
		this.value = value;
	}

	public String getOutputAddress() {
		return outputAddress;
	}

	public int getOutputIndex() {
		return outputIndex;
	}

	public String getOutputTransactionId() {
		return outputTransactionId;
	}

	public long getValue() {
		return value;
	}

}
