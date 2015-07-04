package io.cloudwallet.coinstack.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpRequestBase;

public class HMAC {
	
	public static class HMACSigningException extends Exception {
		private static final long serialVersionUID = -553622637573122275L;

		public HMACSigningException(Throwable t) {
			super(t);
		}

	}

	private static final String HMAC_SHA256 = "HmacSHA256";

	public static final String CONTENT_TYPE = "Content-Type";

	public static final String CONTENT_MD5 = "Content-MD5";

	public static String generateTimestamp() {
		return org.joda.time.DateTime.now().toString();
	}
	
	/**
	 * @param request
	 * @param accessKey
	 * @param secret
	 * @param timestamp
	 *            string representing timestamp in RFC3339 (e.g
	 *            2002-10-02T10:00:00-05:00)
	 * @throws Exception
	 */
	public static void signRequest(HttpRequestBase request, String accessKey,
			String secret, String timestamp) throws HMACSigningException {
		// examine required heaers (Content-MD5 and Content-Type)
		String contentMD5 = "";
		String contentType = "";
		for (Header header : request.getHeaders(HMAC.CONTENT_MD5)) {
			contentMD5 = header.getValue();
		}
		for (Header header : request.getHeaders(HMAC.CONTENT_TYPE)) {
			contentType = header.getValue();
		}
	
		// set HMAC headers
		StringBuffer authString = new StringBuffer();
		authString = authString.append(request.getMethod()).append("\n"); // METHOD
		authString = authString.append(request.getURI().getAuthority()).append(
				"\n"); // HOST
		authString = authString.append(request.getURI().getPath()).append("\n"); // SUBURL
		authString = authString.append(timestamp).append("\n"); // timestamp
		authString = authString.append(contentMD5).append("\n"); // Content-MD5
		authString = authString.append(contentType).append("\n"); // Content-type
	
		String hash;
		try {
			hash = HMAC.signString(authString.toString(), secret);
		} catch (InvalidKeyException e) {
			throw new HMACSigningException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new HMACSigningException(e);
		}
		String authHeader = String.format(
				"APIKey=%s,Signature=%s,Timestamp=%s", accessKey, hash,
				timestamp);
		// generate Auth header and set
		request.addHeader("Authorization", authHeader);
	}
	
	public static String signString(String payload, String secret)
			throws InvalidKeyException, NoSuchAlgorithmException {
		Mac sha256Mac = Mac.getInstance(HMAC_SHA256);
		SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(),
				HMAC_SHA256);
		sha256Mac.init(secretKey);
	
		String hash = Base64.encodeBase64String(sha256Mac.doFinal(payload
				.getBytes()));
		return hash;
	}

}
