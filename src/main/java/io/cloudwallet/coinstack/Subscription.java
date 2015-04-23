package io.cloudwallet.coinstack;

import org.json.JSONException;

public abstract class Subscription {

	private String address;
	private String id;

	public Subscription(String address) {
		this.address = address;
	}
	
	public Subscription(String id, String address) {
		this.id = id;
		this.address = address;
	}

	public String getAddress() {
		return address;
	}

	public String getId() {
		return id;
	}

	abstract protected String toJsonString() throws JSONException;
}
