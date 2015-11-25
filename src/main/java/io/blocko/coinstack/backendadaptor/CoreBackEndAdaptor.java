package io.blocko.coinstack.backendadaptor;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.blocko.coinstack.Endpoint;
import io.blocko.coinstack.exception.AuthSignException;
import io.blocko.coinstack.exception.CoinStackException;
import io.blocko.coinstack.exception.InvalidResponseException;
import io.blocko.coinstack.exception.MalformedInputException;
import io.blocko.coinstack.model.Block;
import io.blocko.coinstack.model.CredentialsProvider;
import io.blocko.coinstack.model.Input;
import io.blocko.coinstack.model.Output;
import io.blocko.coinstack.model.Stamp;
import io.blocko.coinstack.model.Subscription;
import io.blocko.coinstack.model.Transaction;
import io.blocko.coinstack.util.HMAC;
import io.blocko.coinstack.util.HMAC.HMACSigningException;
import io.blocko.coinstack.util.PublicKeyVerifier;

public class CoreBackEndAdaptor extends AbstractCoinStackAdaptor {
	private static final String[] defaultProtocols = new String[] { "TLSv1" };
	private static final String[] defaultCipherSuites = new String[] { "TLS_DHE_RSA_WITH_AES_128_CBC_SHA" };
	private CloseableHttpClient httpClient;
	private Endpoint endpoint;
	private String[] protocols;

	private String[] cipherSuites;

	private CredentialsProvider credentialProvider;

	public CoreBackEndAdaptor(CredentialsProvider credentialsProvider, Endpoint endpoint) {
		this(credentialsProvider, endpoint, defaultProtocols, defaultCipherSuites);
	}

	public CoreBackEndAdaptor(CredentialsProvider provider, Endpoint endpoint, String[] protocols,
			String[] cipherSuites) {
		super();
		this.credentialProvider = provider;
		this.endpoint = endpoint;
		this.protocols = protocols;
		this.cipherSuites = cipherSuites;
	}

	private CoinStackException processError(String resJsonString, int status) throws InvalidResponseException {
		JSONObject resJson = null;
		try {
			resJson = new JSONObject(resJsonString);
		} catch (JSONException e) {
			throw new InvalidResponseException("Invalid server response", "failed to parse error information");
		}

		CoinStackException exception;
		try {
			String cause = "";
			if (resJson.has("error_cause")) {
				cause = resJson.getString("error_cause");
			}
			exception = new CoinStackException(resJson.getString("error_type"), resJson.getInt("error_code"), status,
					resJson.getString("error_message"), resJson.getBoolean("retry"), cause);
		} catch (JSONException e) {
			throw new InvalidResponseException("Invalid server response", "failed to parse error information");
		}
		return exception;
	}

	@Override
	public String addSubscription(Subscription newSubscription) throws IOException, CoinStackException {
		CloseableHttpResponse res = null;
		try {
			HttpPost httpPost = new HttpPost(this.endpoint.endpoint() + "/subscriptions");
			byte[] payload;
			try {
				payload = newSubscription.toJsonString().getBytes("UTF8");
			} catch (JSONException e) {
				throw new MalformedInputException("Invalid subscription", "failed to marshal subscription object");
			}
			httpPost.setEntity(new ByteArrayEntity(payload));
			signPostRequest(httpPost, payload);
			res = httpClient.execute(httpPost);

			int status = res.getStatusLine().getStatusCode();
			String resJsonString = EntityUtils.toString(res.getEntity());
			EntityUtils.consume(res.getEntity());

			if (status != 200) {
				throw processError(resJsonString, status);
			}
			// read result to extract id
			JSONObject resJson;
			try {
				resJson = new JSONObject(resJsonString);
				return resJson.getString("id");
			} catch (JSONException e) {
				throw new InvalidResponseException("Invalid add subscription response", "Parsing response failed");
			}
		} finally {
			if (null != res)
				res.close();
		}
	}

	@Override
	public void deleteSubscription(String id) throws IOException, CoinStackException {
		CloseableHttpResponse res = null;
		try {
			HttpDelete httpDelete = new HttpDelete(this.endpoint.endpoint() + "/subscriptions/" + id);
			signRequest(httpDelete);
			res = httpClient.execute(httpDelete);

			int status = res.getStatusLine().getStatusCode();
			String resJsonString = EntityUtils.toString(res.getEntity());
			EntityUtils.consume(res.getEntity());

			if (status != 200) {
				throw processError(resJsonString, status);
			}
			return;
		} finally {
			if (null != res)
				res.close();
		}
	}

	@Override
	public void fini() {

	}

	@Override
	public long getBalance(String address) throws IOException, CoinStackException {
		CloseableHttpResponse res = null;
		try {
			HttpGet httpGet = new HttpGet(this.endpoint.endpoint() + "/addresses/" + address + "/balance");
			signRequest(httpGet);
			res = httpClient.execute(httpGet);

			int status = res.getStatusLine().getStatusCode();
			String resJsonString = EntityUtils.toString(res.getEntity());
			EntityUtils.consume(res.getEntity());
			JSONObject resJson;

			if (status != 200) {
				throw processError(resJsonString, status);
			}
			try {
				resJson = new JSONObject(resJsonString);
				return resJson.getLong("balance");
			} catch (JSONException e) {
				throw new InvalidResponseException("Invalid balance response", "Parsing response failed");
			}
		} finally {
			if (null != res)
				res.close();
		}

	}

	@Override
	public String getBestBlockHash() throws IOException, CoinStackException {
		CloseableHttpResponse res = null;
		try {
			HttpGet httpGet = new HttpGet(this.endpoint.endpoint() + "/blockchain");
			signRequest(httpGet);
			res = httpClient.execute(httpGet);

			int status = res.getStatusLine().getStatusCode();
			String resJsonString = EntityUtils.toString(res.getEntity());
			EntityUtils.consume(res.getEntity());
			JSONObject resJson;

			if (status != 200) {
				throw processError(resJsonString, status);
			}
			try {
				resJson = new JSONObject(resJsonString);
				return resJson.getString("best_block_hash");
			} catch (JSONException e) {
				throw new InvalidResponseException("Invalid blockchain status response", "Parsing response failed");
			}
		} finally {
			if (null != res)
				res.close();
		}
	}

	@Override
	public int getBestHeight() throws IOException, CoinStackException {
		CloseableHttpResponse res = null;
		try {
			HttpGet httpGet = new HttpGet(this.endpoint.endpoint() + "/blockchain");
			signRequest(httpGet);
			res = httpClient.execute(httpGet);

			int status = res.getStatusLine().getStatusCode();
			String resJsonString = EntityUtils.toString(res.getEntity());
			EntityUtils.consume(res.getEntity());

			if (status != 200) {
				throw processError(resJsonString, status);
			}
			try {
				JSONObject resJson = new JSONObject(resJsonString);
				return resJson.getInt("best_height");
			} catch (JSONException e) {
				throw new InvalidResponseException("Invalid blockchain status response", "Parsing response failed");
			}
		} finally {
			if (null != res)
				res.close();
		}
	}

	@Override
	public Block getBlock(String blockId) throws IOException, CoinStackException {
		CloseableHttpResponse res = null;
		try {
			HttpGet httpGet = new HttpGet(this.endpoint.endpoint() + "/blocks/" + blockId);
			signRequest(httpGet);
			res = httpClient.execute(httpGet);

			int status = res.getStatusLine().getStatusCode();
			String resJsonString = EntityUtils.toString(res.getEntity());
			EntityUtils.consume(res.getEntity());

			if (status != 200) {
				throw processError(resJsonString, status);
			}

			try {
				JSONObject resJson = new JSONObject(resJsonString);
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
				return new Block(DateTime.parse(resJson.getString("confirmation_time")).toDate(),
						resJson.getString("block_hash"), new String[] { resJson.getJSONArray("children").getString(0) },
						resJson.getInt("height"), parentId, txIds);
			} catch (JSONException e) {
				throw new InvalidResponseException("Invalid server response", "failed to parse block information");
			}
		} finally {
			if (null != res)
				res.close();
		}
	}

	@Override
	public Transaction getTransaction(String transactionId) throws IOException, CoinStackException {
		CloseableHttpResponse res = null;
		try {
			HttpGet httpGet = new HttpGet(this.endpoint.endpoint() + "/transactions/" + transactionId);
			signRequest(httpGet);
			res = httpClient.execute(httpGet);

			int status = res.getStatusLine().getStatusCode();
			String resJsonString = EntityUtils.toString(res.getEntity());
			EntityUtils.consume(res.getEntity());

			if (status != 200) {
				throw processError(resJsonString, status);
			}

			try {
				String[] blockIds;
				Input[] inputs;
				Output[] outputs;

				JSONObject resJson = new JSONObject(resJsonString);

				JSONArray transactionBlockId = resJson.getJSONArray("block_hash");
				blockIds = new String[transactionBlockId.length()];
				for (int i = 0; i < transactionBlockId.length(); i++) {
					blockIds[i] = transactionBlockId.getJSONObject(i).getString("block_hash");
				}

				JSONArray transactionInputs = resJson.getJSONArray("inputs");
				inputs = new Input[transactionInputs.length()];
				for (int i = 0; i < transactionInputs.length(); i++) {
					inputs[i] = new Input(transactionInputs.getJSONObject(i).getInt("output_index"),
							transactionInputs.getJSONObject(i).getJSONArray("address").getString(0),
							transactionInputs.getJSONObject(i).getString("transaction_hash"),
							transactionInputs.getJSONObject(i).getLong("value"));

				}

				JSONArray transactionOutputs = resJson.getJSONArray("outputs");
				outputs = new Output[transactionOutputs.length()];
				for (int i = 0; i < transactionOutputs.length(); i++) {
					JSONObject transactionOutput = transactionOutputs.getJSONObject(i);
					if (transactionOutput.has("address")) {
						outputs[i] = new Output(transactionId, i,
								transactionOutput.getJSONArray("address").getString(0),
								transactionOutput.getBoolean("used"), transactionOutput.getLong("value"),
								transactionOutput.getString("script"));
					} else {
						outputs[i] = new Output(transactionId, i, null, transactionOutput.getBoolean("used"),
								transactionOutput.getLong("value"), transactionOutput.getString("script"));
					}

				}

				return new Transaction(resJson.getString("transaction_hash"), blockIds,
						DateTime.parse(resJson.getString("time")).toDate(), resJson.getBoolean("coinbase"), inputs,
						outputs);
			} catch (JSONException e) {
				throw new InvalidResponseException("Invalid transaction response", "Parsing response failed");
			}
		} finally {
			if (null != res)
				res.close();
		}
	}

	@Override
	public String[] getTransactions(String address) throws IOException, CoinStackException {
		CloseableHttpResponse res = null;
		try {
			HttpGet httpGet = new HttpGet(this.endpoint.endpoint() + "/addresses/" + address + "/history");
			signRequest(httpGet);
			res = httpClient.execute(httpGet);

			int status = res.getStatusLine().getStatusCode();
			String resJsonString = EntityUtils.toString(res.getEntity());
			EntityUtils.consume(res.getEntity());
			if (status != 200) {
				throw processError(resJsonString, status);
			}

			JSONArray resJson;
			List<String> transactions = new LinkedList<String>();
			try {
				resJson = new JSONArray(resJsonString);
				for (int i = 0; i < resJson.length(); i++) {
					transactions.add(resJson.getString(i));
				}
			} catch (JSONException e) {
				throw new InvalidResponseException("Invalid server response", "failed to parse address history");
			}
			return transactions.toArray(new String[0]);
		} finally {
			if (null != res)
				res.close();
		}
	}

	@Override
	public Output[] getUnspentOutputs(String address) throws IOException, CoinStackException {
		CloseableHttpResponse res = null;
		try {
			HttpGet httpGet = new HttpGet(this.endpoint.endpoint() + "/addresses/" + address + "/unspentoutputs");
			signRequest(httpGet);
			res = httpClient.execute(httpGet);

			int status = res.getStatusLine().getStatusCode();
			String resJsonString = EntityUtils.toString(res.getEntity());
			EntityUtils.consume(res.getEntity());

			if (status != 200) {
				throw processError(resJsonString, status);
			}

			List<Output> outputs = new LinkedList<Output>();
			try {
				JSONArray resJson = new JSONArray(resJsonString);
				for (int i = 0; i < resJson.length(); i++) {
					JSONObject output = resJson.getJSONObject(i);
					outputs.add(new Output(output.getString("transaction_hash"), output.getInt("index"), address, false,
							output.getLong("value"), output.getString("script")));
				}
			} catch (JSONException e) {
				throw new InvalidResponseException("Invalid server response",
						"failed to parse unspent output information");
			}
			return outputs.toArray(new Output[0]);
		} finally {
			if (null != res)
				res.close();
		}
	}

	@Override
	public void init() {
		// initialize public key verifier
		PublicKeyVerifier hostnameVerifier = new PublicKeyVerifier(this.endpoint);
		SSLContext context = SSLContexts.createDefault();
		SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(context, protocols,
				cipherSuites, hostnameVerifier);
		ConnectionSocketFactory plainConnectionSocketFactory = new PlainConnectionSocketFactory();
		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
				.register("https", sslConnectionFactory).register("http", plainConnectionSocketFactory).build();
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(registry);
		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setConnectionManager(connManager);
		httpClient = builder.build();
	}

	@Override
	public boolean isMainnet() {
		return this.endpoint.mainnet();
	}

	@Override
	public Subscription[] listSubscriptions() throws IOException, CoinStackException {
		CloseableHttpResponse res = null;
		try {
			HttpGet httpGet = new HttpGet(this.endpoint.endpoint() + "/subscriptions");
			signRequest(httpGet);
			res = httpClient.execute(httpGet);

			String resJsonString = EntityUtils.toString(res.getEntity());
			EntityUtils.consume(res.getEntity());

			List<Subscription> subscriptions = new LinkedList<Subscription>();
			try {
				JSONArray resJson = new JSONArray(resJsonString);
				for (int i = 0; i < resJson.length(); i++) {
					JSONObject subscription = resJson.getJSONObject(i);
					subscriptions.add(new Subscription(subscription.getString("id"), subscription.getString("address"),
							subscription.getString("action")));

				}
			} catch (JSONException e) {
				throw new InvalidResponseException("Invalid server response",
						"failed to parse subscription information");
			}
			return subscriptions.toArray(new Subscription[0]);
		} finally {
			if (null != res)
				res.close();
		}
	}

	@Override
	public void sendTransaction(String rawTransaction) throws IOException, CoinStackException {
		CloseableHttpResponse res = null;
		try {
			HttpPost httpPost = new HttpPost(this.endpoint.endpoint() + "/transactions");
			JSONObject txRequest = new JSONObject();
			try {
				txRequest.put("tx", rawTransaction);
			} catch (JSONException e) {
				throw new MalformedInputException("Invalid subscription", "failed to marshal subscription object");
			}
			byte[] payload = txRequest.toString().getBytes("UTF8");
			httpPost.setEntity(new ByteArrayEntity(payload));
			signPostRequest(httpPost, payload);
			res = httpClient.execute(httpPost);

			StatusLine statusLine = res.getStatusLine();
			int status = statusLine.getStatusCode();
			String resJsonString = EntityUtils.toString(res.getEntity());
			EntityUtils.consume(res.getEntity());

			if (status != 200) {
				throw processError(resJsonString, status);
			}
		} finally {
			if (null != res)
				res.close();
		}
	}

	private void signPostRequest(HttpPost req, byte[] content) throws CoinStackException {
		try {
			String md5 = calculateMD5(content);
			req.addHeader(HMAC.CONTENT_MD5, md5);
			HMAC.signRequest(req, this.credentialProvider.getAccessKey(), this.credentialProvider.getSecretKey(),
					HMAC.generateTimestamp());
		} catch (HMACSigningException e) {
			throw new AuthSignException("Failed to sign auth header", "failed to generate HMAC");
		} catch (NoSuchAlgorithmException e) {
			throw new AuthSignException("Failed to calculate MD5 header", "algorithm not found");
		}
	}

	private String calculateMD5(byte[] contentToEncode) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("MD5");
		digest.update(contentToEncode);
		String result = new String(Base64.encodeBase64(digest.digest()));
		return result;
	}

	private void signRequest(HttpRequestBase req) throws CoinStackException {
		try {
			HMAC.signRequest(req, this.credentialProvider.getAccessKey(), this.credentialProvider.getSecretKey(),
					HMAC.generateTimestamp());
		} catch (HMACSigningException e) {
			throw new AuthSignException("Failed to sign auth header", "failed to generate HMAC");
		}
	}

	@Override
	public String stampDocument(String hash) throws IOException, CoinStackException {
		CloseableHttpResponse res = null;
		try {
			HttpPost httpPost = new HttpPost(this.endpoint.endpoint() + "/stamps");
			JSONObject txRequest = new JSONObject();
			try {
				txRequest.put("hash", hash);
			} catch (JSONException e) {
				throw new MalformedInputException("Invalid stamp request", "failed to marshal stamp request");
			}
			byte[] payload = txRequest.toString().getBytes("UTF8");
			httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
			httpPost.setEntity(new ByteArrayEntity(payload));
			signPostRequest(httpPost, payload);

			res = httpClient.execute(httpPost);

			StatusLine statusLine = res.getStatusLine();
			int status = statusLine.getStatusCode();
			String resJsonString = EntityUtils.toString(res.getEntity());
			EntityUtils.consume(res.getEntity());

			if (status != 200) {
				throw processError(resJsonString, status);
			}

			// read result to extract id
			JSONObject resJson;
			try {
				resJson = new JSONObject(resJsonString);
				return resJson.getString("stampid");
			} catch (JSONException e) {
				throw new InvalidResponseException("Invalid server response", "failed to parse stamp respnose");
			}
		} finally {
			if (null != res)
				res.close();
		}
	}

	@Override
	public Stamp getStamp(String stampId) throws IOException, CoinStackException {
		CloseableHttpResponse res = null;
		try {
			HttpGet httpGet = new HttpGet(this.endpoint.endpoint() + "/stamps/" + stampId);
			signRequest(httpGet);
			res = httpClient.execute(httpGet);

			int status = res.getStatusLine().getStatusCode();
			String resJsonString = EntityUtils.toString(res.getEntity());
			EntityUtils.consume(res.getEntity());

			if (status != 200) {
				throw processError(resJsonString, status);
			}

			try {
				JSONObject resJson = new JSONObject(resJsonString);
				return new Stamp(resJson.getString("tx"), resJson.getInt("vout"), resJson.getInt("confirmations"),
						DateTime.parse(resJson.getString("timestamp")).toDate());
			} catch (JSONException e) {
				throw new InvalidResponseException("Invalid stamp response", "Parsing response failed");
			}
		} finally {
			if (null != res)
				res.close();
		}
	}

}
