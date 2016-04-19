package io.blocko.coinstack.openassets;

import java.io.IOException;

import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.VarInt;
import org.bitcoinj.core.Transaction.SigHash;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;

import io.blocko.coinstack.CoinStackClient;
import io.blocko.coinstack.Endpoint;
import io.blocko.coinstack.Math;
import io.blocko.coinstack.MockCoinStackAdaptor;
import io.blocko.coinstack.exception.CoinStackException;
import io.blocko.coinstack.exception.InsufficientFundException;
import io.blocko.coinstack.exception.MalformedInputException;
import io.blocko.coinstack.model.DataTransactionOutput;
import io.blocko.coinstack.model.Output;
import io.blocko.coinstack.openassets.model.AssetOutput;
import io.blocko.coinstack.openassets.model.OpenAssetOpCodes;
import io.blocko.coinstack.openassets.util.Leb128;
import io.blocko.coinstack.openassets.util.Util;

public class ColoringEngine {

	private NetworkParameters network = MainNetParams.get();
	private CoinStackClient coinStackClient;
	private static final long defaultAssetBTC = Math.convertToSatoshi("0.000006");

	public ColoringEngine(CoinStackClient coinStackClient) {
		this.coinStackClient = coinStackClient;
	}

	public String issueAsset(String privateKeyWIF, long assetAmount, String toAddress, long fee)
			throws IOException, CoinStackException {
		Endpoint.init();
	
		org.bitcoinj.core.Transaction txTemplate = new org.bitcoinj.core.Transaction(network);

		org.bitcoinj.core.Address destinationBitcoinAddress;
		String bitcoinAddress = null;
		try {
			bitcoinAddress = Address.deriveBitcoinAddressFromAssetAddress(toAddress);
		} catch (AddressFormatException e1) {
			e1.printStackTrace();
		}
		try {
			destinationBitcoinAddress = new org.bitcoinj.core.Address(network, bitcoinAddress);
		} catch (AddressFormatException e) {
			throw new MalformedInputException("Invalid output", "Malformed address");
		}
		txTemplate.addOutput(Coin.valueOf(defaultAssetBTC), destinationBitcoinAddress);

		final ECKey signingKey;
		try {
			signingKey = new DumpedPrivateKey(network, privateKeyWIF).getKey();
		} catch (AddressFormatException e) {
			throw new MalformedInputException("Invalid private key", "Malformed private key");
		}
		org.bitcoinj.core.Address fromAddress = signingKey.toAddress(network);
		String from = fromAddress.toString();

		Output[] outputs = coinStackClient.getUnspentOutputs(from);

		long totalValue = 0;

		org.bitcoinj.core.Transaction tx = null;

		for (Output output : outputs) {
			Sha256Hash outputHash = new Sha256Hash(CoinStackClient.convertEndianness(output.getTransactionId()));

			if (tx == null || !tx.getHash().equals(outputHash)) {
				tx = new TemporaryTransaction(MainNetParams.get(), outputHash);
				tx.getConfidence().setConfidenceType(TransactionConfidence.ConfidenceType.BUILDING);
			}

			txTemplate.getConfidence().setConfidenceType(TransactionConfidence.ConfidenceType.BUILDING);

			// fill hole between indexes with dummies
			while (tx.getOutputs().size() < output.getIndex()) {
				tx.addOutput(new TransactionOutput(MainNetParams.get(), tx, Coin.NEGATIVE_SATOSHI, new byte[] {}));
			}

			tx.addOutput(new TransactionOutput(MainNetParams.get(), tx, Coin.valueOf(output.getValue()),
					org.bitcoinj.core.Utils.HEX.decode(output.getScript())));

			txTemplate.addInput(tx.getOutput(tx.getOutputs().size() - 1));
			totalValue += output.getValue();
		}

		String calculatedAssetID = Address
				.createAssetID(org.bitcoinj.core.Utils.HEX.decode(outputs[0].getScript()));
		System.out.println("Calculated AssetID is " + calculatedAssetID);

		int[] amountList = { (int) assetAmount };
		String meta = "00";
		byte[] res = createMarkerOutput(amountList, meta);

		Script script = new ScriptBuilder().op(ScriptOpCodes.OP_RETURN).data(res).build();

		TransactionOutput outputOp = new DataTransactionOutput(this.network, txTemplate, Coin.ZERO,
				script.getProgram());
		txTemplate.addOutput(outputOp);

		long rest = totalValue - fee - defaultAssetBTC;
		if (rest < 0) {
			throw new InsufficientFundException("Insufficient fund");
		}

		if (rest > 0) {
			try {
				txTemplate.addOutput(Coin.valueOf(rest), new org.bitcoinj.core.Address(network, from));
			} catch (AddressFormatException e) {
				throw new MalformedInputException("Invalid output", "Malformed output address");
			}
		}

		for (int i = 0; i < txTemplate.getInputs().size(); i++) {
			Script sc = new Script(org.bitcoinj.core.Utils.HEX.decode(outputs[i].getScript()));
			Sha256Hash sighash = txTemplate.hashForSignature(i, sc, SigHash.ALL, false);
			ECKey.ECDSASignature mySignature = signingKey.sign(sighash);
			TransactionSignature signature = new TransactionSignature(mySignature, SigHash.ALL, false);
			Script scriptSig = ScriptBuilder.createInputScript(signature, signingKey);
			txTemplate.getInput(i).setScriptSig(scriptSig);
		}

		byte[] rawTx = txTemplate.bitcoinSerialize();
		// // convert to string encoded hex and return
		return Utils.HEX.encode(rawTx);
	}

	public String transferAsset(String privateKeyWIF, String assetID, long assetAmount, String toAddress, long fee)
			throws Exception {
		Endpoint.init();

		org.bitcoinj.core.Transaction txTemplate = new org.bitcoinj.core.Transaction(network);

		final ECKey signingKey;
		try {
			signingKey = new DumpedPrivateKey(network, privateKeyWIF).getKey();
		} catch (AddressFormatException e) {
			throw new MalformedInputException("Invalid private key", "Malformed private key");
		}
		org.bitcoinj.core.Address fromAddress = signingKey.toAddress(network);
		String from = fromAddress.toString();

		Output[] outputs = coinStackClient.getUnspentOutputs(from);
		long totalAssetAmount = 0;
		for (int i = 0; i < outputs.length; i++) {
			if (outputs[i].getMetaData() != null) {
				if (outputs[i].getMetaData().getAsset_id().equals(assetID)) {
					System.out.println(
							"outputs[i].getMetaData().getQuantity() : " + outputs[i].getMetaData().getQuantity());
					totalAssetAmount += outputs[i].getMetaData().getQuantity();
				}
			}
		}

		System.out.println("totalAssetAmount : " + totalAssetAmount);
		if (totalAssetAmount < assetAmount)
			throw new InsufficientFundException("Insufficient fund");

		long totalValue = 0;
		org.bitcoinj.core.Transaction tx = null;
		// Context context = new Context(network);
		for (Output output : outputs) {
			Sha256Hash outputHash = new Sha256Hash(CoinStackClient.convertEndianness(output.getTransactionId()));

			if (tx == null || !tx.getHash().equals(outputHash)) {
				tx = new TemporaryTransaction(MainNetParams.get(), outputHash);
				tx.getConfidence().setConfidenceType(TransactionConfidence.ConfidenceType.BUILDING);
			}

			txTemplate.getConfidence().setConfidenceType(TransactionConfidence.ConfidenceType.BUILDING);

			// fill hole between indexes with dummies
			while (tx.getOutputs().size() < output.getIndex()) {
				tx.addOutput(new TransactionOutput(MainNetParams.get(), tx, Coin.NEGATIVE_SATOSHI, new byte[] {}));
			}

			tx.addOutput(new TransactionOutput(MainNetParams.get(), tx, Coin.valueOf(output.getValue()),
					org.bitcoinj.core.Utils.HEX.decode(output.getScript())));

			txTemplate.addInput(tx.getOutput(tx.getOutputs().size() - 1));
			totalValue += output.getValue();
		}

		int[] amountList = { (int) assetAmount, (int) (totalAssetAmount - assetAmount) };
		String meta = "00";
		byte[] res = createMarkerOutput(amountList, meta);
		Script script = new ScriptBuilder().op(ScriptOpCodes.OP_RETURN).data(res).build();

		TransactionOutput outputOp = new DataTransactionOutput(this.network, txTemplate, Coin.ZERO,
				script.getProgram());
		txTemplate.addOutput(outputOp);

		org.bitcoinj.core.Address destinationBitcoinAddress;
		String bitcoinAddress = null;
		try {
			bitcoinAddress = Address.deriveBitcoinAddressFromAssetAddress(toAddress);
		} catch (AddressFormatException e1) {
			e1.printStackTrace();
		}

		try {
			destinationBitcoinAddress = new org.bitcoinj.core.Address(network, bitcoinAddress);
		} catch (AddressFormatException e) {
			throw new MalformedInputException("Invalid output", "Malformed address");
		}
		txTemplate.addOutput(Coin.valueOf(defaultAssetBTC), destinationBitcoinAddress);

		try {
			destinationBitcoinAddress = new org.bitcoinj.core.Address(network, from);
		} catch (AddressFormatException e) {
			throw new MalformedInputException("Invalid output", "Malformed address");
		}
		txTemplate.addOutput(Coin.valueOf(defaultAssetBTC), destinationBitcoinAddress);

		long rest = totalValue - fee - defaultAssetBTC - defaultAssetBTC;
		if (rest < 0) {
			throw new InsufficientFundException("Insufficient fund");
		}

		if (rest > 0) {
			try {
				txTemplate.addOutput(Coin.valueOf(rest), new org.bitcoinj.core.Address(network, from));
			} catch (AddressFormatException e) {
				throw new MalformedInputException("Invalid output", "Malformed output address");
			}
		}

		for (int i = 0; i < txTemplate.getInputs().size(); i++) {
			Script sc = new Script(org.bitcoinj.core.Utils.HEX.decode(outputs[i].getScript()));
			Sha256Hash sighash = txTemplate.hashForSignature(i, sc, SigHash.ALL, false);
			ECKey.ECDSASignature mySignature = signingKey.sign(sighash);
			TransactionSignature signature = new TransactionSignature(mySignature, SigHash.ALL, false);
			Script scriptSig = ScriptBuilder.createInputScript(signature, signingKey);
			txTemplate.getInput(i).setScriptSig(scriptSig);
		}

		byte[] rawTx = txTemplate.bitcoinSerialize();
		// // convert to string encoded hex and return
		return Utils.HEX.encode(rawTx);
	}

	private byte[] createMarkerOutput(int[] assetNumbers, String meta) {
		byte[] MARKER = new byte[2];
		byte[] OAVERSION = new byte[2];
		Util.uint16ToByteArrayLE(OpenAssetOpCodes.OP_MARKER, MARKER, 0);
		Util.uint16ToByteArrayLE(OpenAssetOpCodes.OP_OAVERSION, OAVERSION, 0);
		byte[] res = Util.byteConcat(MARKER, OAVERSION);
		byte[] encodedAssetCount = Util.littleEndian(new VarInt(assetNumbers.length).encode());
		res = Util.byteConcat(res, encodedAssetCount);

		for (int i = 0; i < assetNumbers.length; i++) {
			byte[] assetAmountLEB128 = Leb128.writeUnsignedLeb128(assetNumbers[i]);
			res = Util.byteConcat(res, assetAmountLEB128);
		}
		res = Util.byteConcat(res, org.bitcoinj.core.Utils.HEX.decode(meta));

		return res;
	}

	private static class TemporaryTransaction extends org.bitcoinj.core.Transaction {
		private static final long serialVersionUID = -6832934294927540476L;
		private final Sha256Hash hash;

		public TemporaryTransaction(final NetworkParameters params, final Sha256Hash hash) {
			super(params);
			this.hash = hash;
		}

		@Override
		public Sha256Hash getHash() {
			return hash;
		}
	}
}
