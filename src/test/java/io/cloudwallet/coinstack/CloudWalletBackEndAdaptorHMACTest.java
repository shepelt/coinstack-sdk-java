package io.cloudwallet.coinstack;

import static org.junit.Assert.assertEquals;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class CloudWalletBackEndAdaptorHMACTest {
	private static String signString(String payload, String secret)
			throws InvalidKeyException, NoSuchAlgorithmException {
		Mac sha256Mac = Mac.getInstance("HmacSHA256");
		SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(),
				"HmacSHA256");
		sha256Mac.init(secretKey);

		String hash = Base64.encodeBase64String(sha256Mac.doFinal(payload
				.getBytes()));
		return hash;
	}

	@Test
	public void testSignString() throws Exception {
		String hash = signString("testpayload", "testsecret");
		System.out.println(hash);
		assertEquals("lJJdXnaVZm7GUuwL+KRzLiCRfbT7kBYPtxjCEHQhAxY=", hash);
	}

	private static final String CONTENT_TYPE = "Content-Type";
	private static final String CONTENT_MD5 = "Content-MD5";

	/**
	 * @param request
	 * @param accessKey
	 * @param secret
	 * @param timestamp
	 *            string representing timestamp in RFC3339 (e.g
	 *            2002-10-02T10:00:00-05:00)
	 * @throws Exception
	 */
	public static void SignRequest(HttpRequestBase request, String accessKey,
			String secret, String timestamp) throws Exception {
		// examine required heaers (Content-MD5 and Content-Type)
		String contentMD5 = "";
		String contentType = "";
		for (Header header : request.getHeaders(CONTENT_MD5)) {
			contentMD5 = header.getValue();
		}
		for (Header header : request.getHeaders(CONTENT_TYPE)) {
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

		String hash = signString(authString.toString(), secret);
		String authHeader = String.format(
				"APIKey=%s,Signature=%s,Timestamp=%s", accessKey, hash,
				timestamp);
		// generate Auth header and set
		request.addHeader("Authorization", authHeader);
	}

	@Test
	public void testHMAC() throws Exception {
		HttpClientBuilder builder = HttpClientBuilder.create();
		CloseableHttpClient httpClient = builder.build();
		HttpGet httpGet = new HttpGet("http://localhost:3000/");
		String timesamp = org.joda.time.DateTime.now().toString();
		String apiKey = "eb90dbf0-e98c-11e4-b571-0800200c9a66";
		String secret = "f8bd5b50-e98c-11e4-b571-0800200c9a66";
		SignRequest(httpGet, apiKey, secret, timesamp);
		HttpResponse res = httpClient.execute(httpGet);

		if (res.getStatusLine().getStatusCode() == 200) {

			String resJsonString = EntityUtils.toString(res.getEntity());
			System.out.println(resJsonString);
		} else {
			System.out.println(res.getStatusLine().toString());
		}

	}

}
