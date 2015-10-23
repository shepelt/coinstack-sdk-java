package io.cloudwallet.coinstack;

import java.security.SecureRandom;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;

import io.cloudwallet.coinstack.exception.MalformedInputException;

public class ECKey {
	private static SecureRandom secureRandom = new SecureRandom();

	/**
	 * Randomly generate a new private key
	 * 
	 * @return a new private key in Wallet Import Format
	 */
	public static String createNewPrivateKey() {
		return createNewPrivateKey(true);
	}

	public static String createNewPrivateKey(boolean isMainNet) {
		org.bitcoinj.core.ECKey ecKey = new org.bitcoinj.core.ECKey(secureRandom);
		if (isMainNet) {
			return ecKey.getPrivateKeyEncoded(MainNetParams.get()).toString();
		} else {
			return ecKey.getPrivateKeyEncoded(RegTestParams.get()).toString();
		}
	}
	
	/**
	 * Validate a given address
	 * 
	 * @param address
	 *            in string format
	 * @return whether given address is a valid bitcoin address
	 */
	public static boolean validateAddress(String address) {
		return validateAddress(address, true);
	}

	/**
	 * Validate a given address
	 * 
	 * @param address
	 *            in string format
	 * @return whether given address is a valid bitcoin address
	 */
	public static boolean validateAddress(String address, boolean isMainNet) {
		try {
			if (isMainNet) {
				new Address(MainNetParams.get(), address);
			} else {
				new Address(RegTestParams.get(), address);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Get address associated with given private key
	 * 
	 * @param privateKeyWIF
	 *            private key in Wallet Import Format
	 * @return the address associated with the private key given
	 * @throws MalformedInputException
	 *             in case the private key is in incorrect format
	 */
	public static String deriveAddress(String privateKeyWIF) throws MalformedInputException {
		return deriveAddress(privateKeyWIF, true);
	}

	public static String deriveAddress(String privateKeyWIF, boolean isMainNet) throws MalformedInputException {
		org.bitcoinj.core.ECKey signingKey;
		NetworkParameters network = isMainNet ? MainNetParams.get() : RegTestParams.get();
		try {
			signingKey = new DumpedPrivateKey(network, privateKeyWIF).getKey();
		} catch (AddressFormatException e) {
			throw new MalformedInputException("Parsing private key failed");
		}
		return signingKey.toAddress(network).toString();
	}

	public static byte[] derivePubKey(String privateKeyWIF) throws MalformedInputException {
		return derivePubKey(privateKeyWIF, true);
	}

	public static byte[] derivePubKey(String privateKeyWIF, boolean isMainNet) throws MalformedInputException {
		org.bitcoinj.core.ECKey signingKey;
		NetworkParameters network = isMainNet ? MainNetParams.get() : RegTestParams.get();
		try {
			signingKey = new DumpedPrivateKey(network, privateKeyWIF).getKey();
		} catch (AddressFormatException e) {
			throw new MalformedInputException("Parsing private key failed");
		}
		return signingKey.getPubKey();
	}



}
