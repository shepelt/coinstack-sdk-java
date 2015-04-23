package io.cloudwallet.coinstack;

import org.json.JSONException;
import org.json.JSONObject;

public class WebHookSubscription extends Subscription {

	private String url;

	protected WebHookSubscription(String id, String address, String url) {
		super(id, address);
		this.url = url;
	}

	/**
	 * Creates a subscription that posts a message to given HTTP endpoint when triggered
	 * 
	 * @param address Blockchain address
	 * @param url HTTP endpoint to send message to
	 */
	public WebHookSubscription(String address, String url) {
		super(address);
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	@Override
	protected String toJsonString() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("type", 1);
		json.put("address", this.getAddress());
		json.put("url", this.getUrl());
		return json.toString();
	}
}
