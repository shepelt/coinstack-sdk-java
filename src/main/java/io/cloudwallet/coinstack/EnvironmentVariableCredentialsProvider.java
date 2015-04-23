package io.cloudwallet.coinstack;

import java.util.Map;

public class EnvironmentVariableCredentialsProvider extends CredentialsProvider {

	@Override
	String getAccessKey() {
		Map<String, String> env = System.getenv();
		return env.get("COINSTACK_ACCESS_KEY_ID");
	}

	@Override
	String getSecretKey() {
		Map<String, String> env = System.getenv();
		return env.get("COINSTACK_SECRET_ACCESS_KEY");
	}

}
