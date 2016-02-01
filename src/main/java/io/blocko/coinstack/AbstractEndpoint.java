package io.blocko.coinstack;

import java.security.PublicKey;

public interface AbstractEndpoint {
	String endpoint();

	boolean mainnet();

	PublicKey getPublicKey();
};