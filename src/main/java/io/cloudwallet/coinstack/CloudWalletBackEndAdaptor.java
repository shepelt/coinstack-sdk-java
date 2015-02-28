/**
 * 
 */
package io.cloudwallet.coinstack;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author nepho
 *
 */
public class CloudWalletBackEndAdaptor extends MockCoinStackAdaptor {
	private HttpClient httpClient;

	@Override
	public void init() {
		httpClient = new DefaultHttpClient();
	}

	@Override
	public void fini() {

	}

	@Override
	public int getBestHeight() throws IOException {
		HttpGet httpGet = new HttpGet(
				"http://search.cloudwallet.io/api/bestheight");
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
		HttpGet httpGet = new HttpGet(
				"http://search.cloudwallet.io/api/besthash");
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
		return null;
	}

	private static DateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS");
	static {
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	@Override
	public Transaction getTransaction(String transactionId) throws IOException {
		HttpGet httpGet = new HttpGet(
				"http://search.cloudwallet.io/api/transactions/"
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
		HttpGet httpGet = new HttpGet("http://search.cloudwallet.io/api/"
				+ address + "/history");
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
		HttpGet httpGet = new HttpGet("http://search.cloudwallet.io/api/"
				+ address + "/balance");
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
		HttpGet httpGet = new HttpGet("http://search.cloudwallet.io/api/"
				+ address + "/unspentoutputs");
		HttpResponse res = httpClient.execute(httpGet);
		String resJsonString = EntityUtils.toString(res.getEntity());
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
}
