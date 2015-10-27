package io.blocko.coinstack.model;

import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;
import org.bitcoinj.script.ScriptOpCodes;

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

	public byte[] getData() {
		byte[] scriptBytes = null;
		try {
			scriptBytes = Hex.decodeHex(script.toCharArray());
		} catch (DecoderException e) {
			return null;
		}
		Script script = new Script(scriptBytes);
		List<ScriptChunk> chunks = script.getChunks();
		
		if (chunks.size() < 2) {
			return null;
		}
		if (!chunks.get(0).equalsOpCode(ScriptOpCodes.OP_RETURN)) {
			return null;
		}
		return chunks.get(1).data;
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
