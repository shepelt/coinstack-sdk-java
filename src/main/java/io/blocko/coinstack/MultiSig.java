package io.blocko.coinstack;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

import io.blocko.coinstack.exception.MalformedInputException;

public class MultiSig {
	public static String createRedeemScript(int threshold, List<byte[]> pubkeys) {
		List<ECKey> eckeys = new ArrayList<ECKey>();
		for (int i = 0; i < pubkeys.size(); i++) {
			eckeys.add(ECKey.fromPublicOnly(pubkeys.get(i)));
		}
		Script sc = ScriptBuilder.createRedeemScript(threshold, eckeys);

		return new String(Hex.encodeHex(sc.getProgram()));
	}

	protected static String createAddressFromRedeemScript(Script redeemScript, boolean isMainNet) {
		Script sc = ScriptBuilder.createP2SHOutputScript(redeemScript);
		Address address = Address.fromP2SHScript(isMainNet ? MainNetParams.get() : RegTestParams.get(), sc);
		return address.toString();
	}

	public static String createAddressFromRedeemScript(String redeemScript, boolean isMainNet) {
		Script redeem = null;
		String from = null;
		try {
			redeem = new Script(Hex.decodeHex(redeemScript.toCharArray()));
		} catch (ScriptException e) {
			throw new MalformedInputException("Malfored redeem script");
		} catch (DecoderException e) {
			throw new MalformedInputException("Malfored redeem script");
		}
		from = createAddressFromRedeemScript(redeem, isMainNet);
		return from.toString();
	}

}
