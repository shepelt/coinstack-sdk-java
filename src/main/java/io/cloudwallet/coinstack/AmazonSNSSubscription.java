package io.cloudwallet.coinstack;

import org.json.JSONException;
import org.json.JSONObject;

public class AmazonSNSSubscription extends Subscription {

	private String region;
	private String topic;

	public AmazonSNSSubscription(String id, String address, String region,
			String topic) {
		super(id, address);
		this.region = region;
		this.topic = topic;
	}

	public String getRegion() {
		return region;
	}

	public String getTopic() {
		return topic;
	}

	@Override
	public String toJsonString() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("type", 2);
		json.put("address", this.getAddress());
		json.put("region", this.getRegion());
		json.put("topic", this.getTopic());
		return json.toString();
	}

}
