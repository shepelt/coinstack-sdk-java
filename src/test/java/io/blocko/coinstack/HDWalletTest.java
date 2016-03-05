package io.blocko.coinstack;

import static org.junit.Assert.*;

import java.security.SecureRandom;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.MainNetParams;
import org.junit.Test;

import io.blocko.coinstack.exception.MalformedInputException;

public class HDWalletTest {

	public static String bytesToHex(byte[] bytes) {
		char[] hexArray = "0123456789ABCDEF".toCharArray();
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	private byte[] toByteArray(String hexStr) {
		int len = hexStr.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(hexStr.charAt(i), 16) << 4)
					+ Character.digit(hexStr.charAt(i + 1), 16));
		}
		return data;
	}

	// @Test
	// public void testMasterKeyGeneration() throws Exception {
	// DeterministicKey deserialized = DeterministicKey.deserializeB58(null,
	// "xprv9s21ZrQH143K3JZbtwqwxDojpg9yFNGLEdWTkBMF2KrYZLxzdn9VsvQyUpW9MDDRuXxCrVxyRiuXtPv3cm3NWejzCSShTFP4o7EMXg68rev");
	// DeterministicKey child = deserialized.derive(0);
	// System.out.println(child.getPrivateKeyEncoded(MainNetParams.get()).toString());
	// String address =
	// ECKey.deriveAddress("KwSbGrRFUUKXukoDb91ibfsSEQ49ga781do42MYzrhHPSckiyQG8");
	// System.out.println(address);
	// }

	@Test
	public void testMasterKeyGenerationMultiple() throws Exception {
		byte[] seed = toByteArray("77265811D36191498DC636A29FAD664B07D238F54920572F0B660F3C07E45AAE");
		System.out.println(bytesToHex(seed));
		DeterministicKey master = HDKeyDerivation.createMasterPrivateKey(seed);
		DeterministicKey child = master.derive(0);
		System.out.println(child.getPrivateKeyEncoded(MainNetParams.get()).toString());
		String address = ECKey.deriveAddress("L4LxHCnYz5YYBAnb3rFChMAbjMhzzU6o4zJDvc2qEMEuq5LewMxm");
		System.out.println(address);

		byte[] pkBytes = Base58.decode(child.getPrivateKeyEncoded(MainNetParams.get()).toString());
		System.out.println(bytesToHex(pkBytes));
		String wif = Base58
				.encode(toByteArray("80D4967E41E2A258F748276ED8DEAD4F946D0650874BDE95A801B64CD6D727E8BF01478F0512"));

		System.out.println(wif);

		String rebuilt = rebuildChecksum("D4967E41E2A258F748276ED8DEAD4F946D0650874BDE95A801B64CD6D727E8BF");
		assertEquals("80D4967E41E2A258F748276ED8DEAD4F946D0650874BDE95A801B64CD6D727E8BF01478F0512", rebuilt);
		assertEquals("D4967E41E2A258F748276ED8DEAD4F946D0650874BDE95A801B64CD6D727E8BF", stripChecksum(rebuilt));
		
		System.out.println(stripChecksum(rebuilt));
	}

	private String stripChecksum(String withChecksum) {
		return withChecksum.substring(2, 66);
	}
	
	private String rebuildChecksum(String withoutChecksum) {
		byte[] pk = toByteArray(withoutChecksum);
		byte[] pkExtended = new byte[34];
		pkExtended[0] = (byte) 0x80;
		for (int i = 0; i < pk.length; i++) {
			pkExtended[i + 1] = pk[i];
		}
		pkExtended[33] = (byte) 0x01;
		byte[] intermediate = Sha256Hash.create(pkExtended).getBytes();
		byte[] checksum = Sha256Hash.create(intermediate).getBytes();
		byte[] result = new byte[38];
		result[0] = (byte) 0x80;
		for (int i = 0; i < pkExtended.length; i++) {
			result[i] = pkExtended[i];
		}
		for (int i = 0; i < 4; i++) {
			result[34 + i] = checksum[i];
		}
		return bytesToHex(result);
	}

}
