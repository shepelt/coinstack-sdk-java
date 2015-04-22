/**
 * 
 */
package io.cloudwallet.coinstack;

import static org.junit.Assert.*;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.Test;

/**
 * @author nepho
 *
 */
public class HttpClientTest {
	// @Test
	// public void test() throws Exception {
	// // when creating client
	// HttpClientBuilder builder = HttpClientBuilder.create();
	// SSLContext context = SSLContexts.createDefault();
	//
	// String[] protocols = new String[] { "TLSv1.2", "TLSv1" };
	// String[] ciphersuites = new String[] { "TLS_DHE_RSA_WITH_AES_128_CBC_SHA"
	// };
	//
	// SSLConnectionSocketFactory sslConnectionFactory = new
	// SSLConnectionSocketFactory(
	// context, protocols, ciphersuites,
	// SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	// Registry<ConnectionSocketFactory> registry = RegistryBuilder
	// .<ConnectionSocketFactory> create()
	// .register("https", sslConnectionFactory).build();
	// HttpClientConnectionManager connManager = new
	// BasicHttpClientConnectionManager(
	// registry);
	// builder.setConnectionManager(connManager);
	// HttpClient httpClient = builder.build();
	// HttpGet httpGet = new HttpGet(
	// "https://search.cloudwallet.io/api/1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa/balance");
	// HttpResponse res = httpClient.execute(httpGet);
	//
	// try {
	// System.out.println(res.getStatusLine());
	// String resJsonString = EntityUtils.toString(res.getEntity());
	// System.out.println(resJsonString);
	// JSONObject resJson = new JSONObject(resJsonString);
	// long longValue = resJson.getLong("balance");
	// System.out.println(longValue);
	// } finally {
	//
	// }
	// }

	@Test
	public void test2() throws Exception {
		InputStream in = this.getClass().getClassLoader()
				.getResourceAsStream("keys/public_key.der");
		final PublicKey key = getPublicKey(in);

		X509HostnameVerifier hostnameVerifier = new X509HostnameVerifier() {
			@Override
			public boolean verify(String host, SSLSession arg1) {
				return false;
			}

			@Override
			public void verify(String host, String[] cns, String[] subjectAlts)
					throws SSLException {

			}

			@Override
			public void verify(String host, X509Certificate cert)
					throws SSLException {
			}

			@Override
			public void verify(String host, SSLSocket ssl) throws IOException {
				Certificate[] certificates = ssl.getSession()
						.getPeerCertificates();
				X509Certificate cert = (X509Certificate) certificates[0];
				System.out.println(cert.getPublicKey().toString());
				System.out.println(Hex.encodeHexString(cert.getPublicKey()
						.getEncoded()));
				if (key.equals(cert.getPublicKey())) {
					System.out.println("match!");
				} else {
					System.out.println("no match!");
				}
			}
		};

		// when creating client
		HttpClientBuilder builder = HttpClientBuilder.create();
		SSLContext context = SSLContexts.createDefault();

		String[] protocols = new String[] { "TLSv1.2", "TLSv1" };
		String[] ciphersuites = new String[] { "TLS_DHE_RSA_WITH_AES_128_CBC_SHA" };

//		SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(
//				context, protocols, ciphersuites, hostnameVerifier);
		SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(
				context, hostnameVerifier);
		Registry<ConnectionSocketFactory> registry = RegistryBuilder
				.<ConnectionSocketFactory> create()
				.register("https", sslConnectionFactory).build();
		HttpClientConnectionManager connManager = new BasicHttpClientConnectionManager(
				registry);
		builder.setConnectionManager(connManager);
		HttpClient httpClient = builder.build();
		HttpGet httpGet = new HttpGet(
//				"https://testnet.cloudwallet.io/api/1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa/balance");
//	"https://blockchain.info");
				"https://search.cloudwallet.io/api/1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa/balance");
		HttpResponse res = httpClient.execute(httpGet);

		try {
//			System.out.println(res.getStatusLine());
//			String resJsonString = EntityUtils.toString(res.getEntity());
//			System.out.println(resJsonString);
//			JSONObject resJson = new JSONObject(resJsonString);
//			long longValue = resJson.getLong("balance");
//			System.out.println(longValue);
		} finally {

		}
	}

	public static PublicKey getPublicKey(InputStream is) throws Exception {
		byte[] keyBytes = IOUtils.toByteArray(is);
		X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(spec);
	}

	@Test
	public void test3() throws Exception {
		InputStream in = this.getClass().getClassLoader()
				.getResourceAsStream("keys/public_key.der");
		PublicKey key = getPublicKey(in);
		assertNotNull(key);
		System.out.println(key.toString());
	}

}
