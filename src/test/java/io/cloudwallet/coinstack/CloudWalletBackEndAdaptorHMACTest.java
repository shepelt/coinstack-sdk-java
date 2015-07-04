package io.cloudwallet.coinstack;

import static org.junit.Assert.assertEquals;

import org.apache.http.client.methods.HttpGet;
import org.junit.Test;

public class CloudWalletBackEndAdaptorHMACTest {
	@Test
	public void testSignString() throws Exception {
		String hash = HMAC.signString("testpayload", "testsecret");
		System.out.println(hash);
		assertEquals("lJJdXnaVZm7GUuwL+KRzLiCRfbT7kBYPtxjCEHQhAxY=", hash);
	}

	@Test
	public void testHMAC() throws Exception {
		HttpGet httpGet = new HttpGet("http://localhost:3000/");
		String timesamp = HMAC.generateTimestamp();
		String apiKey = "eb90dbf0-e98c-11e4-b571-0800200c9a66";
		String secret = "f8bd5b50-e98c-11e4-b571-0800200c9a66";
		HMAC.signRequest(httpGet, apiKey, secret, timesamp);

		assertEquals(1, httpGet.getHeaders("Authorization").length);
	}
}
