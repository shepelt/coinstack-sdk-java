package io.blocko.coinstack;

import java.security.SignatureException;

import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;

import io.blocko.coinstack.exception.MalformedInputException;

public class ECDSA {
	public static String signMessage(String privateKeyWIF, String messageText) throws MalformedInputException {
		return signMessage(privateKeyWIF, messageText, true);
	}

	public static String signMessage(String privateKeyWIF, String messageText, boolean isMainNet) throws MalformedInputException {
		ECKey eckey = null;
		String signature = null;

		try {
			if (isMainNet) {
				eckey = new DumpedPrivateKey(MainNetParams.get(), privateKeyWIF).getKey();
			} else {
				eckey = new DumpedPrivateKey(RegTestParams.get(), privateKeyWIF).getKey();
			}
		} catch (AddressFormatException e) {
			throw new MalformedInputException("Invalid private key", "Parsing private key failed");
		}

		signature = eckey.signMessage(messageText);
		return signature;
	}

	public static boolean verifyMessageSignature(String address, String messageText, String signature) {
		return verifyMessageSignature(address, messageText, signature, true);
	}

	public static boolean verifyMessageSignature(String address, String messageText, String signature,
			boolean isMainNet) {
		ECKey originalKey = null;
		String derivedAddress = null;
		try {
			originalKey = ECKey.signedMessageToKey(messageText, signature);
		} catch (SignatureException e) {
			return false;
		}

		if (isMainNet) {
			derivedAddress = originalKey.toAddress(MainNetParams.get()).toString();
		} else {
			derivedAddress = originalKey.toAddress(RegTestParams.get()).toString();
		}

		if (address.equals(derivedAddress))
			return true;
		else
			return false;
	}
}
