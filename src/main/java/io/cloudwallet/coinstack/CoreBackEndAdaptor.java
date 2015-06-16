package io.cloudwallet.coinstack;

import io.cloudwallet.coinstack.HMAC.HMACSigningException;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CoreBackEndAdaptor extends AbstractCoinStackAdaptor {
	private static final String[] defaultProtocols = new String[] { "TLSv1" };
	private static final String[] defaultCipherSuites = new String[] { "TLS_DHE_RSA_WITH_AES_128_CBC_SHA" };
	private HttpClient httpClient;
	private Endpoint endpoint;
	private String[] protocols;

	private String[] cipherSuites;

	private CredentialsProvider credentialProvider;

	public CoreBackEndAdaptor(CredentialsProvider credentialsProvider,
			Endpoint endpoint) {
		this(credentialsProvider, endpoint, defaultProtocols,
				defaultCipherSuites);
	}

	public CoreBackEndAdaptor(CredentialsProvider provider, Endpoint endpoint,
			String[] protocols, String[] cipherSuites) {
		super();
		this.credentialProvider = provider;
		this.endpoint = endpoint;
		this.protocols = protocols;
		this.cipherSuites = cipherSuites;
	}

	@Override
	String addSubscription(Subscription newSubscription) throws IOException {
		try {
			HttpPost httpPost = new HttpPost(this.endpoint.monitorEndpoint()
					+ "/subscriptions");
			byte[] payload = newSubscription.toJsonString().getBytes("UTF8");
			httpPost.setEntity(new ByteArrayEntity(payload));
			signPostRequest(httpPost, payload);
			HttpResponse res = httpClient.execute(httpPost);
			if (res.getStatusLine().getStatusCode() == 401) {
				throw new IOException("Failed to authorize request");
			}
			StatusLine statusLine = res.getStatusLine();
			int status = statusLine.getStatusCode();

			if (status == 200) {
				// read result to extract id
				String resJsonString = EntityUtils.toString(res.getEntity());
				EntityUtils.consume(res.getEntity());
				JSONObject resJson;
				try {
					resJson = new JSONObject(resJsonString);
					return resJson.getString("id");
				} catch (JSONException e) {
					throw new IOException("Parsing response failed", e);
				}
			} else {
				res.getEntity().getContent().close();
				throw new IOException("failed to add subscription");
			}
		} catch (IOException e) {
			throw new IOException("Failed to add subscription", e);
		} catch (JSONException e) {
			throw new IOException("Failed to marhsall subscription", e);
		}
	}

	@Override
	void deleteSubscription(String id) throws IOException {
		HttpDelete httpDelete = new HttpDelete(this.endpoint.monitorEndpoint()
				+ "/subscriptions/" + id);
		signRequest(httpDelete);
		HttpResponse res = httpClient.execute(httpDelete);
		if (res.getStatusLine().getStatusCode() == 401) {
			throw new IOException("Failed to authorize request");
		}
		StatusLine statusLine = res.getStatusLine();
		int status = statusLine.getStatusCode();
		EntityUtils.consume(res.getEntity());
		if (status == 200) {
			return;
		} else {
			throw new IOException("failed to delete given subscription");
		}
	}

	private void checkResponse(HttpResponse res) throws IOException {
		int statusCode = res.getStatusLine().getStatusCode();
		switch (statusCode) {
		case 200:
			// success and continue
			break;
		case 401:
			throw new IOException("Authorization failed");
		default:
			throw new IOException("Request failed - " + statusCode);
		}
	}

	@Override
	void fini() {

	}

	@Override
	long getBalance(String address) throws IOException {
		HttpGet httpGet = new HttpGet(this.endpoint.endpoint() + "/addresses/"
				+ address + "/balance");
		signRequest(httpGet);
		HttpResponse res = httpClient.execute(httpGet);
		checkResponse(res);
		String resJsonString = EntityUtils.toString(res.getEntity());
		JSONObject resJson;
		try {
			resJson = new JSONObject(resJsonString);
			return resJson.getLong("balance");
		} catch (JSONException e) {
			throw new IOException("Parsing response failed", e);
		}
	}

	@Override
	String getBestBlockHash() throws IOException {
		HttpGet httpGet = new HttpGet(this.endpoint.endpoint() + "/blockchain");
		signRequest(httpGet);
		HttpResponse res = httpClient.execute(httpGet);
		checkResponse(res);

		String resJsonString = EntityUtils.toString(res.getEntity());
		JSONObject resJson;
		try {
			resJson = new JSONObject(resJsonString);
			return resJson.getString("best_block_hash");
		} catch (JSONException e) {
			throw new IOException("Parsing response failed", e);
		}
	}

	@Override
	int getBestHeight() throws IOException {
		HttpGet httpGet = new HttpGet(this.endpoint.endpoint() + "/blockchain");
		signRequest(httpGet);
		HttpResponse res = httpClient.execute(httpGet);
		checkResponse(res);

		String resJsonString = EntityUtils.toString(res.getEntity());
		JSONObject resJson;
		try {
			resJson = new JSONObject(resJsonString);
			return resJson.getInt("best_height");
		} catch (JSONException e) {
			throw new IOException("Parsing response failed", e);
		}
	}

	@Override
	Block getBlock(String blockId) throws IOException {
		HttpGet httpGet = new HttpGet(this.endpoint.endpoint() + "/blocks/"
				+ blockId);
		signRequest(httpGet);
		HttpResponse res = httpClient.execute(httpGet);
		checkResponse(res);

		String resJsonString = EntityUtils.toString(res.getEntity());

		JSONObject resJson;
		try {
			resJson = new JSONObject(resJsonString);
			String[] txIds;
			JSONArray childJsons = resJson.getJSONArray("transaction_list");
			txIds = new String[childJsons.length()];
			for (int i = 0; i < childJsons.length(); i++) {
				txIds[i] = childJsons.getString(i);
			}
			String parentId;
			if (resJson.isNull("parent")) {
				parentId = null;
			} else {
				parentId = resJson.getString("parent");
			}
			return new Block(DateTime.parse(
					resJson.getString("confirmation_time")).toDate(),
					resJson.getString("block_hash"), new String[] { resJson
							.getJSONArray("children").getString(0) },
					resJson.getInt("height"), parentId, txIds);
		} catch (JSONException e) {
			throw new IOException("Parsing response failed", e);
		}
	}

	@Override
	Transaction getTransaction(String transactionId) throws IOException {

		HttpGet httpGet = new HttpGet(this.endpoint.endpoint()
				+ "/transactions/" + transactionId);
		signRequest(httpGet);
		HttpResponse res = httpClient.execute(httpGet);
		checkResponse(res);

		String resJsonString = EntityUtils.toString(res.getEntity());

		JSONObject resJson;

		try {
			String[] blockIds;
			Input[] inputs;
			Output[] outputs;

			resJson = new JSONObject(resJsonString);

			JSONArray transactionBlockId = resJson.getJSONArray("block_hash");
			blockIds = new String[transactionBlockId.length()];
			for (int i = 0; i < transactionBlockId.length(); i++) {
				blockIds[i] = transactionBlockId.getJSONObject(i).getString(
						"block_hash");
			}

			JSONArray transactionInputs = resJson.getJSONArray("inputs");
			inputs = new Input[transactionInputs.length()];
			for (int i = 0; i < transactionInputs.length(); i++) {
				inputs[i] = new Input(transactionInputs.getJSONObject(i)
						.getInt("output_index"), transactionInputs
						.getJSONObject(i).getJSONArray("address").getString(0),
						transactionInputs.getJSONObject(i).getString(
								"transaction_hash"), transactionInputs
								.getJSONObject(i).getLong("value"));

			}

			JSONArray transactionOutputs = resJson.getJSONArray("outputs");
			outputs = new Output[transactionOutputs.length()];
			for (int i = 0; i < transactionOutputs.length(); i++) {
				JSONObject transactionOutput = transactionOutputs
						.getJSONObject(i);
				outputs[i] = new Output(transactionId, i, transactionOutput
						.getJSONArray("address").getString(0),
						transactionOutput.getBoolean("used"),
						transactionOutput.getLong("value"),
						transactionOutput.getString("script"));

			}

			return new Transaction(resJson.getString("transaction_hash"),
					blockIds, DateTime.parse(resJson.getString("time"))
							.toDate(), resJson.getBoolean("coinbase"), inputs,
					outputs);
		} catch (JSONException e) {
			throw new IOException("Parsing response failed", e);
		}
	}

	@Override
	String[] getTransactions(String address) throws IOException {
		HttpGet httpGet = new HttpGet(this.endpoint.endpoint() + "/addresses/"
				+ address + "/history");
		signRequest(httpGet);
		HttpResponse res = httpClient.execute(httpGet);
		checkResponse(res);

		String resJsonString = EntityUtils.toString(res.getEntity());
		JSONArray resJson;

		List<String> transactions = new LinkedList<String>();
		try {
			resJson = new JSONArray(resJsonString);
			for (int i = 0; i < resJson.length(); i++) {
				transactions.add(resJson.getString(i));
			}
		} catch (JSONException e) {
			throw new IOException("Parsing response failed", e);
		}
		return transactions.toArray(new String[0]);
	}

	@Override
	Output[] getUnspentOutputs(String address) throws IOException {
		HttpGet httpGet = new HttpGet(this.endpoint.endpoint() + "/addresses/"
				+ address + "/unspentoutputs");
		signRequest(httpGet);
		HttpResponse res = httpClient.execute(httpGet);
		checkResponse(res);
		String resJsonString = EntityUtils.toString(res.getEntity());
		EntityUtils.consume(res.getEntity());
		JSONArray resJson;

		List<Output> outputs = new LinkedList<Output>();
		try {
			resJson = new JSONArray(resJsonString);
			for (int i = 0; i < resJson.length(); i++) {
				JSONObject output = resJson.getJSONObject(i);
				outputs.add(new Output(output.getString("transaction_hash"),
						output.getInt("index"), address, false, output
								.getLong("value"), output.getString("script")));
			}
		} catch (JSONException e) {
			throw new IOException("Parsing response failed", e);
		}
		return outputs.toArray(new Output[0]);
	}

	@Override
	public void init() {
		// initialize public key verifier
		PublicKeyVerifier hostnameVerifier = new PublicKeyVerifier(
				this.endpoint);
		SSLContext context = SSLContexts.createDefault();
		SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(
				context, protocols, cipherSuites, hostnameVerifier);
		ConnectionSocketFactory plainConnectionSocketFactory = new PlainConnectionSocketFactory();
		Registry<ConnectionSocketFactory> registry = RegistryBuilder
				.<ConnectionSocketFactory> create()
				.register("https", sslConnectionFactory)
				.register("http", plainConnectionSocketFactory).build();
		HttpClientConnectionManager connManager = new BasicHttpClientConnectionManager(
				registry);
		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setConnectionManager(connManager);
		httpClient = builder.build();
	}

	@Override
	boolean isMainnet() {
		return this.endpoint.mainnet();
	}

	@Override
	Subscription[] listSubscriptions() throws IOException {
		HttpGet httpGet = new HttpGet(this.endpoint.monitorEndpoint()
				+ "/subscriptions");
		signRequest(httpGet);
		HttpResponse res = httpClient.execute(httpGet);

		if (res.getStatusLine().getStatusCode() == 401) {
			throw new IOException("Failed to authorize request");
		}

		String resJsonString = EntityUtils.toString(res.getEntity());
		EntityUtils.consume(res.getEntity());
		JSONArray resJson;

		List<Subscription> subscriptions = new LinkedList<Subscription>();
		try {
			resJson = new JSONArray(resJsonString);
			for (int i = 0; i < resJson.length(); i++) {
				JSONObject subscription = resJson.getJSONObject(i);
				if (subscription.getInt("Type") == 1) {
					// webhook subscription
					subscriptions.add(new WebHookSubscription(subscription
							.getString("Id"),
							subscription.getString("Address"), subscription
									.getString("Url")));
				} else if (subscription.getInt("Type") == 2) {
					// SNS subscription
					subscriptions.add(new AmazonSNSSubscription(subscription
							.getString("Id"),
							subscription.getString("Address"), subscription
									.getString("Region"), subscription
									.getString("Topic")));
				}
			}
		} catch (JSONException e) {
			throw new IOException("Parsing response failed", e);
		}
		return subscriptions.toArray(new Subscription[0]);
	}

	@Override
	void sendTransaction(String rawTransaction) throws IOException,
			TransactionRejectedException {
		// TODO Auto-generated method stub

	}

	private void signPostRequest(HttpPost req, byte[] content) throws IOException {
		try {
			String md5 = calculateMD5(content);
			req.addHeader(HMAC.CONTENT_MD5, md5);
			HMAC.signRequest(req, this.credentialProvider.getAccessKey(),
					this.credentialProvider.getSecretKey(),
					HMAC.generateTimestamp());
		} catch (HMACSigningException e) {
			throw new IOException("Failed to sign request", e);
		} catch (NoSuchAlgorithmException e) {
			throw new IOException("Failed to sign request", e);
		}
	}
	private String calculateMD5(byte[] contentToEncode) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("MD5");
		digest.update(contentToEncode);
		String result = new String(Hex.encodeHex(digest.digest()));
		return result;
	}
	
	private void signRequest(HttpRequestBase req) throws IOException {
		try {
			HMAC.signRequest(req, this.credentialProvider.getAccessKey(),
					this.credentialProvider.getSecretKey(),
					HMAC.generateTimestamp());
		} catch (HMACSigningException e) {
			throw new IOException("Failed to sign request", e);
		}
	}

}
