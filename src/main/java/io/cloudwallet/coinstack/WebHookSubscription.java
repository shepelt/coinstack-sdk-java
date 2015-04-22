package io.cloudwallet.coinstack;

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
}
