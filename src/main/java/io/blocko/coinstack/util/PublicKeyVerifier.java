package io.blocko.coinstack.util;

import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.http.conn.ssl.X509HostnameVerifier;

import io.blocko.coinstack.AbstractEndpoint;

public class PublicKeyVerifier implements X509HostnameVerifier {
	private AbstractEndpoint endpoint;

	public PublicKeyVerifier(AbstractEndpoint endpoint) {
		this.endpoint = endpoint;
	}
	@Override
	public boolean verify(String host, SSLSession arg1) {
		return false;
	}

	@Override
	public void verify(String host, String[] cns, String[] subjectAlts)
			throws SSLException {

	}

	@Override
	public void verify(String host, X509Certificate cert) throws SSLException {
	}

	@Override
	public void verify(String host, SSLSocket ssl) throws IOException {
		// if endpoint key null, skip cert pinning
		if (endpoint.getPublicKey() == null) {
			return;
		}
		
		Certificate[] certificates = ssl.getSession().getPeerCertificates();
		X509Certificate cert = (X509Certificate) certificates[0]; // get first certificate
		if (endpoint.getPublicKey().equals(cert.getPublicKey())) {
			// do nothing since match
		} else {
			// raise io exception
			throw new IOException("certificate public key failed to match");
		}
	}
}
