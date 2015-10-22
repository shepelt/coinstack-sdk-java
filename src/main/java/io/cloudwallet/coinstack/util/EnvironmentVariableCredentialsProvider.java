package io.cloudwallet.coinstack.util;

import io.cloudwallet.coinstack.exception.InvalidKeyException;
import io.cloudwallet.coinstack.model.CredentialsProvider;

import java.util.Map;

public class EnvironmentVariableCredentialsProvider extends CredentialsProvider {

	@Override
	public String getAccessKey() throws InvalidKeyException {
		Map<String, String> env = System.getenv();
		String access_key = env.get("COINSTACK_ACCESS_KEY_ID");
		if( access_key == null ) throw new InvalidKeyException("Invalid or No API Key is set");
		return access_key;
	}

	@Override
	public String getSecretKey() throws InvalidKeyException {
		Map<String, String> env = System.getenv();
		String secret_key = env.get("COINSTACK_SECRET_ACCESS_KEY");
		if( secret_key == null ) throw new InvalidKeyException("Invalid or No API Key is set");
		return secret_key;
	}

}
