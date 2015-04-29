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
		protected String monitorEndpoint() {
			return "https://mainnetmonitor.cloudwallet.io";
		}

		@Override
		protected boolean mainnet() {
			return true;
		}

		@Override
		protected String broadcastEndpoint() {
			return "http://search.cloudwallet.io:9090/sendtx";
		}
	},
	TESTNET() {
		@Override
		protected String endpoint() {
			return "https://testnet.cloudwallet.io";
		}

		@Override
		protected String monitorEndpoint() {
			return "https://testnetmonitor.cloudwallet.io";
		}

		@Override
		protected boolean mainnet() {
			return false;
		}

		@Override
		protected String broadcastEndpoint() {
			return "";
		}
	},
	COINSTACK_CORE_MAINNET() {
		@Override
		protected String endpoint() {
			return "https://mainnet.cloudwallet.io";
		}

		@Override
		protected String monitorEndpoint() {
			return "https://mainnetmonitor.cloudwallet.io";
		}

		@Override
		protected boolean mainnet() {
			return true;
		}

		@Override
		protected String broadcastEndpoint() {
			return "http://search.cloudwallet.io:9090/sendtx";
		}
	},
	COINSTACK_CORE_TESTNET() {
		@Override
		protected String endpoint() {
			return "https://mainnet.cloudwallet.io";
		}

		@Override
		protected String monitorEndpoint() {
			return "https://mainnetmonitor.cloudwallet.io";
		}

		@Override
		protected boolean mainnet() {
			return true;
		}

		@Override
		protected String broadcastEndpoint() {
			return "http://search.cloudwallet.io:9090/sendtx";
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

	protected abstract String monitorEndpoint();

	protected abstract String broadcastEndpoint();

	protected abstract boolean mainnet();

	private static boolean initialized = false;

	public static void init() throws IOException {
		if (initialized) {
			return;
		}
		initialized = true;
		InputStream in = CoinStackClient.class.getClassLoader()
				.getResourceAsStream("keys/public_key.der");
		try {
			PublicKey key = CertificatePinningManager.getPublicKey(in);
			MAINNET.setPublicKey(key);
			TESTNET.setPublicKey(key);
		} catch (Exception e) {
			throw new IOException("initializing certificate failed", e);
		}
	}
}