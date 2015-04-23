package io.cloudwallet.coinstack;

import org.json.JSONException;
import org.json.JSONObject;

public class AmazonSNSSubscription extends Subscription {

	private String region;
	private String topic;

	protected AmazonSNSSubscription(String id, String address, String region,
			String topic) {
		super(id, address);
		this.region = region;
		this.topic = topic;
	}
	
	/**
	 * Creates a subscription that posts a message to Amazon SNS when triggered 
	 * 
	 * @param address Blockchain address
	 * @param region AWS region
	 * @param topic ARN for SNS topic
	 */
	public AmazonSNSSubscription(String address, String region,
			String topic) {
		super(address);
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
	protected String toJsonString() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("type", 2);
		json.put("address", this.getAddress());
		json.put("region", this.getRegion());
		json.put("topic", this.getTopic());
		return json.toString();
	}

}
