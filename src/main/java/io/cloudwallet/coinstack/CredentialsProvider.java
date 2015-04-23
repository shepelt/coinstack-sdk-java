package io.cloudwallet.coinstack;

public abstract class CredentialsProvider {

	abstract String getAccessKey();

	abstract String getSecretKey();
}
