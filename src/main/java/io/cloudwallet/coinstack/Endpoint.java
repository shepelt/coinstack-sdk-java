package io.cloudwallet.coinstack;

import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;

public enum Endpoint {
	MAINNET() {
		@Override
		protected String endpoint() {
			return "https://mainnet.cloudwallet.io";
		}

		@Override
		protected boolean mainnet() {
			return true;
		}

	},
	TESTNET() {
		@Override
		protected String endpoint() {
			return "https://testnet.cloudwallet.io";
		}

		@Override
		protected boolean mainnet() {
			return false;
		}
	};
	private PublicKey key;

	protected PublicKey getPublicKey() {
		return this.key;
	}

	protected void setPublicKey(PublicKey key) {
		this.key = key;
	}

	protected abstract String endpoint();

	protected abstract boolean mainnet();

	private static boolean initialized = false;

	public static void init() throws IOException {
		if (initialized) {
			return;
		}
		initialized = true;
		InputStream in = CoinStackClient.class.getClassLoader()
				.getResourceAsStream("keys/public_key.der");
		if (null == in) {
			throw new IOException("loading certificate failed - not found");
		}
		try {
			PublicKey key = CertificatePinningManager.getPublicKey(in);
			MAINNET.setPublicKey(key);
			TESTNET.setPublicKey(key);
		} catch (Exception e) {
			throw new IOException("initializing certificate failed", e);
		}
	}
}