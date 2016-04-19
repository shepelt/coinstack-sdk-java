package io.blocko.coinstack.openassets.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Util {
	public static void uint16ToByteArrayLE(long val, byte[] out, int offset) {
		out[offset + 1] = (byte) (0xFF & (val >> 0));
		out[offset + 0] = (byte) (0xFF & (val >> 8));
	}

	public static byte[] byteConcat(byte[] a, byte[] b) {
		byte[] result = new byte[a.length + b.length];
		// copy a to result
		System.arraycopy(a, 0, result, 0, a.length);
		// copy b to result
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}

	public static byte[] littleEndian(byte[] payload) {
		ByteBuffer buffer = ByteBuffer.wrap(payload);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		return buffer.array();
	}

	public static void byteConcat2(byte[] a, byte[] b) {
		byte[] result = new byte[a.length + b.length];
		// copy a to result
		System.arraycopy(a, 0, result, 0, a.length);
		// copy b to result
		System.arraycopy(b, 0, result, a.length, b.length);
		a = result;
	}
}
