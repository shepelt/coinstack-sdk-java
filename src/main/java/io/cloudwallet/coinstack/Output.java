package io.cloudwallet.coinstack;

public class Output {
	private String address;
	private boolean isSpent;
	private long value;
	private String script;

	public Output(String address, boolean isSpent, long value, String script) {
		super();
		this.address = address;
		this.isSpent = isSpent;
		this.value = value;
		this.script = script;
	}

	public String getAddress() {
		return address;
	}

	public boolean isSpent() {
		return isSpent;
	}

	public long getValue() {
		return value;
	}

	public String getScript() {
		return script;
	}

}
