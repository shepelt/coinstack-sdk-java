package io.cloudwallet.coinstack.util;

import io.cloudwallet.coinstack.model.CredentialsProvider;

import java.util.Map;

public class EnvironmentVariableCredentialsProvider extends CredentialsProvider {

	@Override
	public String getAccessKey() {
		Map<String, String> env = System.getenv();
		return env.get("COINSTACK_ACCESS_KEY_ID");
	}

	@Override
	public String getSecretKey() {
		Map<String, String> env = System.getenv();
		return env.get("COINSTACK_SECRET_ACCESS_KEY");
	}

}
