package io.blocko.coinstack.openassets;

import java.io.UnsupportedEncodingException;

import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.VersionedChecksummedBytes;
import org.bitcoinj.params.MainNetParams;

import io.blocko.coinstack.ECKey;
import io.blocko.coinstack.exception.MalformedInputException;

public class Address {

	private static final class VersionedChecksummedBytesExtension extends VersionedChecksummedBytes {
		private static final long serialVersionUID = 1989886938171152676L;

		private VersionedChecksummedBytesExtension(int version, byte[] bytes) {
			super(version, bytes);
		}
	}

	private final static byte openAssetsNamespace = 19;
	private final static byte bitCoinNamespace = 0;
	private final static byte versionByte = 23;

	public static String deriveAssetAddressFromPrivateKey(String privateKey)
			throws MalformedInputException, AddressFormatException {
		String addressString = ECKey.deriveAddress(privateKey);

		org.bitcoinj.core.Address address = new org.bitcoinj.core.Address(MainNetParams.get(), addressString);
		byte[] forAssetsAddress = new byte[21];
		System.arraycopy(address.getHash160(), 0, forAssetsAddress, 1, 20);
		forAssetsAddress[0] = (byte) address.getVersion();

		String receiveAssetsAddress = new VersionedChecksummedBytesExtension(openAssetsNamespace, forAssetsAddress)
				.toString();

		return receiveAssetsAddress;
	}

	public static String deriveAssetAddressFromBitcoinAddress(String bitcoinAddress)
			throws MalformedInputException, AddressFormatException {
		org.bitcoinj.core.Address address = new org.bitcoinj.core.Address(MainNetParams.get(), bitcoinAddress);
		byte[] forAssetsAddress = new byte[21];
		System.arraycopy(address.getHash160(), 0, forAssetsAddress, 1, 20);
		forAssetsAddress[0] = (byte) address.getVersion();

		String receiveAssetsAddress = new VersionedChecksummedBytesExtension(openAssetsNamespace, forAssetsAddress)
				.toString();

		return receiveAssetsAddress;
	}

	public static String deriveBitcoinAddressFromAssetAddress(String assetAddress)
			throws MalformedInputException, AddressFormatException, UnsupportedEncodingException {
		byte[] decodedAddress = Base58.decode(assetAddress);
		byte[] forBitcoinAddress = new byte[20];
		System.arraycopy(decodedAddress, 2, forBitcoinAddress, 0, 20);

		String bitCoinAddress = new VersionedChecksummedBytesExtension(bitCoinNamespace, forBitcoinAddress).toString();

		return bitCoinAddress;
	}

	public static String createAssetID(byte[] inputScript) {
		byte[] hashResult = Utils.sha256hash160(inputScript);
		return new VersionedChecksummedBytesExtension(versionByte, hashResult).toString();

	}
}
