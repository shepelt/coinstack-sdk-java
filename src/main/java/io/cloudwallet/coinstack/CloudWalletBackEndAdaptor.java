/**
 * 
 */
package io.cloudwallet.coinstack;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
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
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author nepho
 *
 */
public class CloudWalletBackEndAdaptor extends AbstractCoinStackAdaptor {

	private static final String[] defaultProtocols = new String[] { "TLSv1" };
	private static final String[] defaultCipherSuites = new String[] { "TLS_DHE_RSA_WITH_AES_128_CBC_SHA" };
	private String endpointURL;
	private String monitorEndpointURL;
	private String broadcastEndpointURL;
	private HttpClient httpClient;
	private Endpoint endpoint;
	private String[] protocols;
	private String[] cipherSuites;
	private String accessKey = "";
	private String secretKey = "";
	private CredentialsProvider credentialProvider;

	public CloudWalletBackEndAdaptor(CredentialsProvider provider,
			Endpoint endpoint) {
		this(provider, endpoint, defaultProtocols, defaultCipherSuites);
	}

	public CloudWalletBackEndAdaptor(CredentialsProvider provider,
			Endpoint endpoint, String[] protocols, String[] cipherSuites) {
		super();
		this.credentialProvider = provider;
		this.endpoint = endpoint;
		if (endpoint != null) {
			this.endpointURL = endpoint.endpoint();
			this.monitorEndpointURL = endpoint.monitorEndpoint();
			this.broadcastEndpointURL = endpoint.broadcastEndpoint();
		} else {
			this.endpointURL = "https://mainnet.cloudwallet.io";
			this.monitorEndpointURL = "https://mainnetmonitor.cloudwallet.io";
			this.broadcastEndpointURL = "http://search.cloudwallet.io:9090/sendtx";
		}

		this.protocols = protocols;
		this.cipherSuites = cipherSuites;
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
	public void fini() {

	}

	@Override
	public int getBestHeight() throws IOException {
		HttpGet httpGet = new HttpGet(this.endpointURL + "/api/bestheight");
		HttpResponse res = httpClient.execute(httpGet);
		String resJsonString = EntityUtils.toString(res.getEntity());
		JSONObject resJson;
		try {
			resJson = new JSONObject(resJsonString);
			return resJson.getInt("height");
		} catch (JSONException e) {
			throw new IOException("Parsing response failed", e);
		}
	}

	@Override
	public String getBestBlockHash() throws IOException {
		HttpGet httpGet = new HttpGet(this.endpointURL + "/api/besthash");
		HttpResponse res = httpClient.execute(httpGet);
		String resJsonString = EntityUtils.toString(res.getEntity());
		JSONObject resJson;
		try {
			resJson = new JSONObject(resJsonString);
			return resJson.getString("hash");
		} catch (JSONException e) {
			throw new IOException("Parsing response failed", e);
		}
	}

	@Override
	public Block getBlock(String blockId) throws IOException {
		HttpGet httpGet = new HttpGet(this.endpointURL + "/api/blocks/"
				+ blockId);
		HttpResponse res = httpClient.execute(httpGet);
		String resJsonString = EntityUtils.toString(res.getEntity());
		JSONObject resJson;
		try {
			resJson = new JSONObject(resJsonString);
			String[] txIds;
			JSONArray childJsons = resJson.getJSONArray("transaction_list");
			txIds = new String[childJsons.length()];
			for (int i = 0; i < childJsons.length(); i++) {
				txIds[i] = childJsons.getJSONObject(i).getString("_id");
			}
			String parentId;
			if (resJson.isNull("parent")) {
				parentId = null;
			} else {
				parentId = resJson.getString("parent");
			}
			return new Block(dateFormat.parse(resJson
					.getString("confirmation_time")), resJson.getString("_id"),
					new String[] { resJson.getString("child") },
					resJson.getInt("height"), parentId, txIds);
		} catch (JSONException e) {
			throw new IOException("Parsing response failed", e);
		} catch (ParseException e) {
			throw new IOException("Parsing response failed", e);
		}
	}

	private static DateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS");
	static {
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	@Override
	public Transaction getTransaction(String transactionId) throws IOException {
		HttpGet httpGet = new HttpGet(this.endpointURL + "/api/transactions/"
				+ transactionId);
		HttpResponse res = httpClient.execute(httpGet);
		String resJsonString = EntityUtils.toString(res.getEntity());
		JSONObject resJson;

		try {
			String[] blockIds;
			Input[] inputs;
			Output[] outputs;

			resJson = new JSONObject(resJsonString);

			JSONArray transactionBlockIds = resJson.getJSONArray("blockhash");
			blockIds = new String[transactionBlockIds.length()];
			for (int i = 0; i < transactionBlockIds.length(); i++) {
				blockIds[i] = transactionBlockIds.getJSONObject(i).getString(
						"blockhash");

			}

			JSONArray transactionInputs = resJson.getJSONArray("inputs");
			inputs = new Input[transactionInputs.length()];
			for (int i = 0; i < transactionInputs.length(); i++) {
				inputs[i] = new Input(transactionInputs.getJSONObject(i)
						.getInt("index"), transactionInputs.getJSONObject(i)
						.getString("address"), transactionInputs.getJSONObject(
						i).getString("hash"), transactionInputs
						.getJSONObject(i).getLong("value"));

			}

			JSONArray transactionOutputs = resJson.getJSONArray("outputs");
			outputs = new Output[transactionOutputs.length()];
			for (int i = 0; i < transactionOutputs.length(); i++) {
				JSONObject transactionOutput = transactionOutputs
						.getJSONObject(i);
				outputs[i] = new Output(transactionId,
						transactionOutput.getInt("index"),
						transactionOutput.getString("address"),
						transactionOutput.getBoolean("used"),
						transactionOutput.getLong("value"),
						transactionOutput.getString("script"));

			}

			return new Transaction(resJson.getString("_id"), blockIds,
					dateFormat.parse(resJson.getString("timestamp")),
					resJson.getBoolean("coinbase"), inputs, outputs);
		} catch (JSONException e) {
			throw new IOException("Parsing response failed", e);
		} catch (ParseException e) {
			throw new IOException("Parsing response failed", e);
		}
	}

	@Override
	public String[] getTransactions(String address) throws IOException {
		HttpGet httpGet = new HttpGet(this.endpointURL + "/api/" + address
				+ "/history");
		HttpResponse res = httpClient.execute(httpGet);
		String resJsonString = EntityUtils.toString(res.getEntity());
		JSONArray resJson;

		List<String> transactions = new LinkedList<String>();
		try {
			resJson = new JSONArray(resJsonString);
			for (int i = 0; i < resJson.length(); i++) {
				JSONObject output = resJson.getJSONObject(i);
				transactions.add(output.getString("_id"));
			}
		} catch (JSONException e) {
			throw new IOException("Parsing response failed", e);
		}
		return transactions.toArray(new String[0]);
	}

	@Override
	public long getBalance(String address) throws IOException {
		HttpGet httpGet = new HttpGet(this.endpointURL + "/api/" + address
				+ "/balance");
		HttpResponse res = httpClient.execute(httpGet);
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
	public Output[] getUnspentOutputs(String address) throws IOException {
		HttpGet httpGet = new HttpGet(this.endpointURL + "/api/" + address
				+ "/unspentoutputs");
		HttpResponse res = httpClient.execute(httpGet);
		String resJsonString = EntityUtils.toString(res.getEntity());
		EntityUtils.consume(res.getEntity());
		JSONArray resJson;

		List<Output> outputs = new LinkedList<Output>();
		try {
			resJson = new JSONArray(resJsonString);
			for (int i = 0; i < resJson.length(); i++) {
				JSONObject output = resJson.getJSONObject(i);
				outputs.add(new Output(output.getString("hash"), output
						.getInt("index"), address, false, output
						.getLong("amount"), output.getString("script")));
			}
		} catch (JSONException e) {
			throw new IOException("Parsing response failed", e);
		}
		return outputs.toArray(new Output[0]);
	}

	@Override
	public void sendTransaction(String rawTransaction) throws IOException,
			TransactionRejectedException {
		// send tx
		try {
			HttpPost httpPost = new HttpPost(broadcastEndpointURL);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("tx", rawTransaction));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse res = httpClient.execute(httpPost);
			StatusLine statusLine = res.getStatusLine();
			int status = statusLine.getStatusCode();

			if (status == 409) {
				throw new TransactionRejectedException(
						"Transaction already present in blockchain");
			} else if (status == 200) {
				// sending tx successful
				return;
			} else {
				String errorMessage = EntityUtils.toString(res.getEntity());
				EntityUtils.consume(res.getEntity());
				throw new TransactionRejectedException(
						"Transaction not accepted", new Throwable(errorMessage));
			}
		} catch (IOException e) {
			throw new IOException("Broadcasting transaction failed", e);
		}
	}


	private static void setAuth(HttpRequestBase http,
			CredentialsProvider credentialProvider) {
		http.setHeader("APIKey", credentialProvider.getAccessKey());
		http.setHeader("SecretKey", credentialProvider.getSecretKey());
	}
	
	@Override
	Subscription[] listSubscriptions() throws IOException {
		HttpGet httpGet = new HttpGet(this.monitorEndpointURL + "/subscriptions");
		setAuth(httpGet, credentialProvider);
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
	void deleteSubscription(String id) throws IOException {
		HttpDelete httpDelete = new HttpDelete(this.monitorEndpointURL  + "/subscriptions/" + id);
		setAuth(httpDelete, credentialProvider);
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

	@Override
	String addSubscription(Subscription newSubscription) throws IOException {
		// send tx
		try {
			HttpPost httpPost = new HttpPost(this.monitorEndpointURL + "/subscriptions");
			setAuth(httpPost, credentialProvider);
			httpPost.setEntity(new StringEntity(newSubscription.toJsonString(),
					Charset.forName("UTF-8")));
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
}