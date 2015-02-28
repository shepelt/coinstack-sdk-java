/**
 * 
 */
package io.cloudwallet.coinstack;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.Test;

/**
 * @author nepho
 *
 */
public class HttpClientTest {
	@Test
	public void test() throws Exception {
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet("http://search.cloudwallet.io/api/1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa/balance");
		HttpResponse res = httpClient.execute(httpGet);

		try {
			System.out.println(res.getStatusLine());
			String resJsonString = EntityUtils.toString(res.getEntity());
			System.out.println(resJsonString);
			JSONObject resJson = new JSONObject(resJsonString);
			long longValue = resJson.getLong("balance");
			System.out.println(longValue);
		} finally {
			
		}
	}
}
