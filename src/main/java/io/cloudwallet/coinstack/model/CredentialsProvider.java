package io.cloudwallet.coinstack.model;

import io.cloudwallet.coinstack.exception.InvalidKeyException;

public abstract class CredentialsProvider {

	public abstract String getAccessKey() throws InvalidKeyException;

	public abstract String getSecretKey() throws InvalidKeyException;
}
