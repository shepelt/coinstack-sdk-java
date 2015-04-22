package io.cloudwallet.coinstack;

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

}
