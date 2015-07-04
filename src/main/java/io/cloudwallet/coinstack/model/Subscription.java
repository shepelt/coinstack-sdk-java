package io.cloudwallet.coinstack.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Subscription {

	private String address;
	private String id;
	private String actionId;

	public Subscription(String address, String actionId) {
		this.address = address;
		this.actionId = actionId;
	}
	
	public Subscription(String id, String address, String actionId) {
		this.id = id;
		this.address = address;
		this.actionId = actionId;
	}

	public String getActionId() {
		return actionId;
	}

	public String getAddress() {
		return address;
	}

	public String getId() {
		return id;
	}

	public String toJsonString() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("action", this.getActionId());
		json.put("address", this.getAddress());
		return json.toString();
	}
}
