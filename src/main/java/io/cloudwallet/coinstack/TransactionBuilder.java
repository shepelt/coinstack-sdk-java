package io.cloudwallet.coinstack;

import java.util.ArrayList;
import java.util.List;

import io.cloudwallet.coinstack.exception.MalformedInputException;
import io.cloudwallet.coinstack.model.Output;

public class TransactionBuilder {
	private long fee;
	private List<Output> outputs;
	private byte[] data;
	
	public TransactionBuilder() {
		this.outputs = new ArrayList<Output>();
		this.data = null;
	}

	public void addOutput(String to, long amount) {	
		outputs.add(new Output("", 0, to, false, amount, null));
	}

	public void setFee(long fee) {
		this.fee = fee;
	}

	public Output[] getOutputs() {
		return outputs.toArray(new Output[outputs.size()]);
	}

	public long getFee() {
		return fee;
	}

	public void setData(byte[] data) {
		if (data.length > 80) {
			throw new MalformedInputException("payload length over 80 bytes");
		}
		
		if (null != this.data) {
			throw new MalformedInputException("multiple data payload not allowed");
		}
		
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}

}
