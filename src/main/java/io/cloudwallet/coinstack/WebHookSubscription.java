package io.cloudwallet.coinstack;

import org.json.JSONException;
import org.json.JSONObject;

public class WebHookSubscription extends Subscription {

	private String url;

	public WebHookSubscription(String id, String address, String url) {
		super(id, address);
		this.url = url;
	}

	public WebHookSubscription(String address, String url) {
		super(address);
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	@Override
	public String toJsonString() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("type", 1);
		json.put("address", this.getAddress());
		json.put("url", this.getUrl());
		return json.toString();
	}
}
