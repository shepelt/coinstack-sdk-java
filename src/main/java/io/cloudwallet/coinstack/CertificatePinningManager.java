package io.cloudwallet.coinstack;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.io.IOUtils;

public class CertificatePinningManager {
	public static PublicKey getPublicKey(InputStream is) throws Exception {
		byte[] keyBytes = IOUtils.toByteArray(is);
		X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(spec);
	}
}
