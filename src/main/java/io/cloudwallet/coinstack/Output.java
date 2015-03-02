package io.cloudwallet.coinstack;

public class Output implements Comparable<Output> {
	private String transactionId;
	private int index;
	private String address;
	private boolean isSpent;
	private long value;
	private String script;

	public Output(String transactionId, int index, String address,
			boolean isSpent, long value, String script) {
		this.transactionId = transactionId;
		this.index = index;
		this.address = address;
		this.isSpent = isSpent;
		this.value = value;
		this.script = script;
	}

	public String getAddress() {
		return address;
	}

	public int getIndex() {
		return index;
	}

	public String getScript() {
		return script;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public long getValue() {
		return value;
	}

	public boolean isSpent() {
		return isSpent;
	}

	public int compareTo(Output compareOutput) {
		if (this.getValue() > compareOutput.getValue()) {
			return 1;
		} else if (this.getValue() < compareOutput.getValue()) {
			return -1;
		} else {
			return 0;
		}
	}
}
