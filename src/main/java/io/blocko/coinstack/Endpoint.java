package io.blocko.coinstack;

import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;

import io.blocko.coinstack.util.CertificatePinningManager;

public enum Endpoint implements AbstractEndpoint {
	MAINNET() {
		@Override
		public String endpoint() {
			return "https://mainnet.cloudwallet.io";
		}

		@Override
		public boolean mainnet() {
			return true;
		}

	},
	TESTNET() {
		@Override
		public String endpoint() {
			return "https://regtestnet.cloudwallet.io";
		}

		@Override
		public boolean mainnet() {
			return false;
		}
	};
	private PublicKey key;

	public PublicKey getPublicKey() {
		return this.key;
	}

	protected void setPublicKey(PublicKey key) {
		this.key = key;
	}

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