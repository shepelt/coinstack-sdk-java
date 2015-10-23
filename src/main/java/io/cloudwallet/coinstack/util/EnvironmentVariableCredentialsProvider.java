package io.cloudwallet.coinstack.util;

import java.util.Map;

import io.cloudwallet.coinstack.model.CredentialsProvider;

public class EnvironmentVariableCredentialsProvider extends CredentialsProvider {

	@Override
	public String getAccessKey() {
		Map<String, String> env = System.getenv();
		String access_key = env.get("COINSTACK_ACCESS_KEY_ID");
		return access_key;
	}

	@Override
	public String getSecretKey() {
		Map<String, String> env = System.getenv();
		String secret_key = env.get("COINSTACK_SECRET_ACCESS_KEY");
		return secret_key;
	}

}
