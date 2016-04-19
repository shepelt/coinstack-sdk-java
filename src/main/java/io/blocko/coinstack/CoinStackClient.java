/**
 * Copyright (c) Blocko Inc. and/or its affiliates. All rights reserved.
 */
package io.blocko.coinstack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.ECKey.ECDSASignature;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction.SigHash;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.core.Wallet.DustySendRequested;
import org.bitcoinj.core.Wallet.SendRequest;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptChunk;
import org.bitcoinj.script.ScriptOpCodes;

import com.google.common.primitives.UnsignedBytes;

import io.blocko.coinstack.backendadaptor.AbstractCoinStackAdaptor;
import io.blocko.coinstack.backendadaptor.CoreBackEndAdaptor;
import io.blocko.coinstack.exception.CoinStackException;
import io.blocko.coinstack.exception.InsufficientFundException;
import io.blocko.coinstack.exception.MalformedInputException;
import io.blocko.coinstack.model.Block;
import io.blocko.coinstack.model.BlockchainStatus;
import io.blocko.coinstack.model.CredentialsProvider;
import io.blocko.coinstack.model.DataTransactionOutput;
import io.blocko.coinstack.model.DustyOutput;
import io.blocko.coinstack.model.Output;
import io.blocko.coinstack.model.Stamp;
import io.blocko.coinstack.model.Subscription;
import io.blocko.coinstack.model.Transaction;
import io.blocko.coinstack.openassets.model.AssetOutput;
import io.blocko.coinstack.util.BitcoinjUtil;
import io.blocko.coinstack.util.EnvironmentVariableCredentialsProvider;

/**
 * A client class for accessing Bitcoin Blockchain through CoinStack. Provides
 * functionality for creating and managing Bitcoin addresses and transactions.
 */
public class CoinStackClient {
	private class SIGNATURE_COMPARATOR2 implements
			Comparator<TransactionSignature> {
		private Comparator<byte[]> comparator = UnsignedBytes
				.lexicographicalComparator();
		Sha256Hash sighash;
		Script redeem;

		public SIGNATURE_COMPARATOR2(Sha256Hash sighash, Script redeem) {
			this.sighash = sighash;
			this.redeem = redeem;
		}

		@Override
		public int compare(TransactionSignature k1, TransactionSignature k2) {
			ECKey ec1 = getECKeyFromSignature(k1.toCanonicalised(), sighash,
					redeem);
			ECKey ec2 = getECKeyFromSignature(k2.toCanonicalised(), sighash,
					redeem);
			return comparator.compare(ec1.getPubKey(), ec2.getPubKey());
		}
	}

	public static class TemporaryTransaction extends
			org.bitcoinj.core.Transaction {
		private static final long serialVersionUID = -6832934294927540476L;
		private final Sha256Hash hash;

		public TemporaryTransaction(final NetworkParameters params,
				final Sha256Hash hash) {
			super(params);
			this.hash = hash;
		}

		@Override
		public Sha256Hash getHash() {
			return hash;
		}
	}

	public static Comparator<Output> outputComparator = new Comparator<Output>() {
		public int compare(Output output1, Output output2) {
			int idCompare = output1.getTransactionId().compareTo(
					output2.getTransactionId());
			if (idCompare > 0) {
				return 1;
			} else if (idCompare < 0) {
				return -1;
			} else {
				return output1.getIndex() - output2.getIndex();
			}
		}
	};

	public static String convertEndianness(String original) {
		StringBuilder result = new StringBuilder();
		for (int i = original.length() - 2; i >= 0; i -= 2) {
			result.append(original.substring(i, i + 2));
		}
		return result.toString();
	};

	private AbstractCoinStackAdaptor coinStackAdaptor;

	private NetworkParameters network;

	private boolean isMainNet;

	/**
	 * Creates a CoinStack client instance that connects to mainnet endpoint
	 * Defaults to using credentials from env variables COINSTACK_ACCESS_KEY_ID
	 * and COINSTACK_SECRET_ACCESS_KEY
	 */
	public CoinStackClient() {
		try {
			this.coinStackAdaptor = new CoreBackEndAdaptor(
					new EnvironmentVariableCredentialsProvider(),
					Endpoint.MAINNET);
			coinStackAdaptor.init();
		} catch (Exception e) {
			e.getMessage();
		}
		this.network = MainNetParams.get();
	}

	public CoinStackClient(AbstractCoinStackAdaptor coinStackAdaptor) {
		this.coinStackAdaptor = coinStackAdaptor;
		coinStackAdaptor.init();
		this.network = coinStackAdaptor.isMainnet() ? MainNetParams.get()
				: RegTestParams.get();
		this.isMainNet = coinStackAdaptor.isMainnet();
	}

	/**
	 * Creates a CoinStack client instance with endpoint specified
	 * 
	 * @param endpoint
	 *            endpoint to connect to (available: EndPoint.MAINNET,
	 *            EndPoint.TESTNET)
	 */
	public CoinStackClient(CredentialsProvider provider,
			AbstractEndpoint endpoint) {
		this.coinStackAdaptor = new CoreBackEndAdaptor(provider, endpoint);
		coinStackAdaptor.init();

		this.network = endpoint.mainnet() ? MainNetParams.get() : RegTestParams
				.get();
		this.isMainNet = endpoint.mainnet();
	}

	/**
	 * Creates a CoinStack client instance with endpoint specified and SSL
	 * parameters
	 * 
	 * @param endpoint
	 *            endpoint to connect to (available: EndPoint.MAINNET,
	 *            EndPoint.TESTNET)
	 * @param sslProtocols
	 *            ssl protocols to enable for HTTPs connection (default:
	 *            TLSv1.2, TLSv1)
	 * @param sslCipherSuites
	 *            ssl cipher suites to enable for HTTPs connection (default:
	 *            TLS_DHE_RSA_WITH_AES_128_CBC_SHA)
	 */
	public CoinStackClient(CredentialsProvider provider, Endpoint endpoint,
			String[] sslProtocols, String[] sslCipherSuites) {
		this.coinStackAdaptor = new CoreBackEndAdaptor(provider, endpoint,
				sslProtocols, sslCipherSuites);
		coinStackAdaptor.init();

		this.network = endpoint.mainnet() ? MainNetParams.get() : RegTestParams
				.get();
		this.isMainNet = endpoint.mainnet();
	}

	/**
	 * Add a subscription with given condition
	 * 
	 * @param newSubscription
	 * @return
	 * @throws IOException
	 * @throws CoinStackException
	 */
	public String addSubscription(Subscription newSubscription)
			throws IOException, CoinStackException {
		Endpoint.init();
		return coinStackAdaptor.addSubscription(newSubscription);
	}

	/**
	 * CoinStack client needs to be closed after usage to prevent resource leaks
	 */
	public void close() {
		coinStackAdaptor.fini();
	}

	/**
	 * Create MultiSig transaction. MultiSig Transaction is a transaction of
	 * which the condition of redemption requires multi private keys.
	 * 
	 * @param builder
	 *            TransactionBuilder object that Coinstack provides Basic
	 *            information to build Multisig transaction
	 * @param privateKeys
	 *            A list of private keys which are required to meet redeem
	 *            script
	 * 
	 * @param redeemScript
	 *            Redeemscript which is initially generated by using multiple
	 *            private keys to fund Multisig transaction and handed over to
	 *            receivers.
	 * @return transaction signed and ready to be broadcasted in hex-encoded
	 *         string
	 * @throws IOException
	 *             in case of network failure
	 * @throws InsufficientFundException
	 *             in case there is not sufficient fund in private key provided
	 * @throws DustyTransactionException
	 *             in case the amount to transfer is too small to be handled by
	 *             blockchain
	 */
	public String createMultiSigTransaction(TransactionBuilder builder,
			List<String> privateKeys, String redeemScript) throws IOException,
			CoinStackException {
		Endpoint.init();

		org.bitcoinj.core.Transaction txTemplate = new org.bitcoinj.core.Transaction(
				getNetwork());

		// Transaction output added
		long totalSpendValue = 0;
		for (Output output : builder.getOutputs()) {
			Address destinationAddressParsed;
			try {
				destinationAddressParsed = new Address(getNetwork(),
						output.getAddress());
			} catch (AddressFormatException e) {
				throw new MalformedInputException("Invalid output",
						"Malformed address");
			}
			txTemplate.addOutput(Coin.valueOf(output.getValue()),
					destinationAddressParsed);
			totalSpendValue += output.getValue();
		}

		// add OP_RETURN if there is any
		if (null != builder.getData()) {
			Script script = new ScriptBuilder().op(ScriptOpCodes.OP_RETURN)
					.data(builder.getData()).build();
			TransactionOutput output = new DataTransactionOutput(
					this.getNetwork(), txTemplate, Coin.ZERO,
					script.getProgram());
			txTemplate.addOutput(output);
		}

		// derive address from reddemScript
		String from = null;
		Script redeem = null;
		try {
			redeem = new Script(Hex.decodeHex(redeemScript.toCharArray()));
			from = MultiSig.createAddressFromRedeemScript(redeem, isMainNet());
		} catch (DecoderException e1) {
			e1.printStackTrace();
		}
		// get unspentout from address
		// Transaction input added
		Output[] outputs = this.getUnspentOutputs(from);
		try {
			injectOutputs(txTemplate, outputs, privateKeys, redeem,
					totalSpendValue + builder.getFee(), from);
		} catch (AddressFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte[] rawTx = txTemplate.bitcoinSerialize();
		// // convert to string encoded hex and return
		return Utils.HEX.encode(rawTx);
	}

	/**
	 * Create MultiSig transaction with partial sign. This method is used to
	 * make a incomplete transaction by using a private key. And this incomplete
	 * transaction will delivered to other signers who have another private key.
	 * 
	 * @param builder
	 *            TransactionBuilder object that Coinstack provides Basic
	 *            information to build Multisig transaction
	 * @param privateKey
	 *            One of multiple private keys which are required to meet redeem
	 *            script
	 * 
	 * @param redeemScript
	 *            Redeem script which is initially generated by using multiple
	 *            private keys
	 * @return incomplete transaction signed and note ready to be broadcasted in
	 *         hex-encoded string
	 * @throws IOException
	 *             in case of network failure
	 * @throws InsufficientFundException
	 *             in case there is not sufficient fund in private key provided
	 * @throws DustyTransactionException
	 *             in case the amount to transfer is too small to be handled by
	 *             blockchain
	 */
	public String createMultiSigTransactionWithPartialSign(
			TransactionBuilder builder, String privateKey, String redeemScript)
			throws IOException, CoinStackException {
		Endpoint.init();
		org.bitcoinj.core.Transaction txTemplate = new org.bitcoinj.core.Transaction(
				getNetwork());

		// Transaction output added
		long totalSpendValue = 0;
		for (Output output : builder.getOutputs()) {
			Address destinationAddressParsed;
			try {
				destinationAddressParsed = new Address(getNetwork(),
						output.getAddress());
			} catch (AddressFormatException e) {
				throw new MalformedInputException("Invalid output",
						"Malformed address");
			}
			txTemplate.addOutput(Coin.valueOf(output.getValue()),
					destinationAddressParsed);
			totalSpendValue += output.getValue();
		}

		// add OP_RETURN if there is any
		if (null != builder.getData()) {
			Script script = new ScriptBuilder().op(ScriptOpCodes.OP_RETURN)
					.data(builder.getData()).build();
			TransactionOutput output = new DataTransactionOutput(
					this.getNetwork(), txTemplate, Coin.ZERO,
					script.getProgram());
			txTemplate.addOutput(output);
		}

		// derive address from reddemScript
		String from = null;
		Script redeem = null;
		try {
			redeem = new Script(Hex.decodeHex(redeemScript.toCharArray()));
			from = MultiSig.createAddressFromRedeemScript(redeem, isMainNet());
		} catch (DecoderException e1) {
			throw new IOException("Failed to parse redeem scripts");
		}

		// get unspentout from address
		// Transaction input added
		Output[] outputs = this.getUnspentOutputs(from);
		try {
			injectOutputs(txTemplate, outputs, privateKey, redeem,
					totalSpendValue + builder.getFee(), from);
		} catch (AddressFormatException e) {
			throw new MalformedInputException("Invalid output",
					"Malformed output address");
		}

		byte[] rawTx = txTemplate.bitcoinSerialize();
		return Utils.HEX.encode(rawTx);
	}

	/**
	 * Construct a transaction and sign it using private key
	 * 
	 * @param builder
	 *            Transaction builder to create transaction with
	 * @param privateKeyWIF
	 *            private key in Wallet Import Format to sign transaction with
	 * @return transaction signed and ready to be broadcasted in hex-encoded
	 *         string
	 * @throws IOException
	 *             in case of network failure
	 * @throws InsufficientFundException
	 *             in case there is not sufficient fund in private key provided
	 * @throws DustyTransactionException
	 *             in case the amount to transfer is too small to be handled by
	 *             blockchain
	 */
	public String createSignedTransaction(AbstractTransactionBuilder builder,
			String privateKeyWIF) throws IOException, CoinStackException {
		Endpoint.init();
		return builder.buildTransaction(this, privateKeyWIF);
	}

	/**
	 * Delete subscription with given id
	 * 
	 * @param id
	 * @throws IOException
	 * @throws CoinStackException
	 */
	public void deleteSubscription(String id) throws IOException,
			CoinStackException {
		Endpoint.init();
		coinStackAdaptor.deleteSubscription(id);
	}

	/**
	 * Fetch current balance of given address
	 * 
	 * @param address
	 *            to view balance of in string format
	 * @return balance in satoshi format (e.g. 0.0001 BTC = 10000 satoshi)
	 * @throws IOException
	 *             in case of network failure
	 * @throws CoinStackException
	 */
	public long getBalance(String address) throws IOException,
			CoinStackException {
		Endpoint.init();
		return coinStackAdaptor.getBalance(address);
	}

	/**
	 * Fetch block information
	 * 
	 * @param blockId
	 *            block ID (block hash) in string format
	 * @return block information and related transaction
	 * @throws IOException
	 *             in case of network failure
	 * @throws CoinStackException
	 */
	public Block getBlock(String blockId) throws IOException,
			CoinStackException {
		Endpoint.init();
		return coinStackAdaptor.getBlock(blockId);
	}

	/**
	 * Fetch current Blockchain status
	 * 
	 * @return current status of Blockchain in BlockchainStatus object
	 * @throws IOException
	 *             in case of network failure
	 * @throws CoinStackException
	 */
	public BlockchainStatus getBlockchainStatus() throws IOException,
			CoinStackException {
		Endpoint.init();
		return new BlockchainStatus(coinStackAdaptor.getBestHeight(),
				coinStackAdaptor.getBestBlockHash());
	}

	private List<ECKey> getECKeyFromRedeemScript(Script redeemScript) {
		ArrayList<ECKey> result = new ArrayList<ECKey>();
		List<ScriptChunk> chunks = redeemScript.getChunks();
		int numKeys = chunks.size() - 3;
		for (int i = 0; i < numKeys; i++)
			result.add(ECKey.fromPublicOnly(chunks.get(1 + i).data));
		return result;
	}

	private ECKey getECKeyFromSignature(ECDSASignature ec, Sha256Hash sighash,
			Script redeem) {
		ECKey thisEc = null;
		for (int i = 0; i < 4; i++) {
			thisEc = ECKey.recoverFromSignature(i, ec, sighash, !isMainNet());
			if (thisEc != null) {
				if (isPubkey(thisEc, redeem))
					return thisEc;

			}
		}
		return thisEc;
	}

	private List<TransactionSignature> getSignatures(
			org.bitcoinj.core.Transaction transaction, int index, Script redeem) {
		List<ScriptChunk> sc = transaction.getInput(index).getScriptSig()
				.getChunks();
		List<TransactionSignature> tsList = new ArrayList<TransactionSignature>();
		for (int i = 1; i < sc.size() - 1; i++) {

			TransactionSignature ts = TransactionSignature.decodeFromBitcoin(
					sc.get(i).data, false);
			tsList.add(ts);
		}
		return tsList;
	}

	/**
	 * Fetch transaction information
	 * 
	 * @param transactionId
	 *            transaction ID (transaction hash) in string format
	 * @return transaction information, inputs, and outputs
	 * @throws IOException
	 *             in case of network failure
	 * @throws CoinStackException
	 */
	public Transaction getTransaction(String transactionId) throws IOException,
			CoinStackException {
		Endpoint.init();
		return coinStackAdaptor.getTransaction(transactionId);
	}

	/**
	 * Get transaction history associated with given address
	 * 
	 * @param address
	 *            to view history of in string format
	 * @return list of transaction IDs related to given address
	 * @throws IOException
	 *             in case of network failure
	 * @throws CoinStackException
	 */
	public String[] getTransactions(String address) throws IOException,
			CoinStackException {
		Endpoint.init();
		return coinStackAdaptor.getTransactions(address);
	}

	/**
	 * Fetch unspent outputs of given address
	 * 
	 * @param address
	 *            to view unspent received transaction outputs of
	 * @return list of unspent transaction outputs
	 * @throws IOException
	 *             in case of network failure
	 * @throws CoinStackException
	 */
	public Output[] getUnspentOutputs(String address) throws IOException,
			CoinStackException {
		Endpoint.init();
		return coinStackAdaptor.getUnspentOutputs(address);
	}

	public AssetOutput[] getUnspentAssetOutputs(String address)
			throws IOException, CoinStackException {
		Endpoint.init();
		return coinStackAdaptor.getUnspentAssetOutputs(address);
	}

	protected long injectOutputs(org.bitcoinj.core.Transaction transaction,
			Output[] outputs, List<String> privateKeys, Script redeemScript,
			long restFee, String from) throws IOException,
			AddressFormatException, InsufficientFundException,
			MalformedInputException {
		long totalValue = 0;
		Arrays.sort(outputs, outputComparator);
		org.bitcoinj.core.Transaction tx = null;
		// Context context = new Context(network);
		for (Output output : outputs) {
			Sha256Hash outputHash = new Sha256Hash(
					CoinStackClient.convertEndianness(output.getTransactionId()));

			if (tx == null || !tx.getHash().equals(outputHash)) {
				tx = new TemporaryTransaction(isMainNet() ? MainNetParams.get()
						: RegTestParams.get(), outputHash);
				tx.getConfidence().setConfidenceType(
						TransactionConfidence.ConfidenceType.BUILDING);
			}

			transaction.getConfidence().setConfidenceType(
					TransactionConfidence.ConfidenceType.BUILDING);

			// fill hole between indexes with dummies
			while (tx.getOutputs().size() < output.getIndex()) {
				tx.addOutput(new TransactionOutput(isMainNet() ? MainNetParams
						.get() : RegTestParams.get(), tx,
						Coin.NEGATIVE_SATOSHI, new byte[] {}));
			}

			tx.addOutput(new TransactionOutput(isMainNet() ? MainNetParams
					.get() : RegTestParams.get(), tx, Coin.valueOf(output
					.getValue()), org.bitcoinj.core.Utils.HEX.decode(output
					.getScript())));

			transaction.addInput(tx.getOutput(tx.getOutputs().size() - 1));
			totalValue += output.getValue();
		}
		long rest = totalValue - restFee;
		if (rest < 0) {
			throw new InsufficientFundException("Insufficient fund");
		}

		if (rest > 0) {
			try {
				transaction.addOutput(Coin.valueOf(rest), new Address(
						getNetwork(), from));
			} catch (AddressFormatException e) {
				throw new MalformedInputException("Invalid output",
						"Malformed output address");
			}
		}
		List<TransactionSignature> signatures = new ArrayList<TransactionSignature>();
		List<ECKey> eckeys = new ArrayList<ECKey>();
		for (int i = 0; i < privateKeys.size(); i++) {
			ECKey eckey = null;
			try {
				eckey = new DumpedPrivateKey(getNetwork(), privateKeys.get(i))
						.getKey();
				eckeys.add(eckey);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// ECKey [] ecKeyArrays = eckeys.toArray(new ECKey [0]);
		// Arrays.sort(ecKeyArrays, ECKeyComparator);
		List<ECKey> pubkeys = new ArrayList<ECKey>(eckeys);
		Collections.sort(pubkeys, ECKey.PUBKEY_COMPARATOR);
		for (int j = 0; j < outputs.length; j++) {
			Sha256Hash sighash = transaction.hashForSignature(j, redeemScript,
					SigHash.ALL, false);
			for (int i = 0; i < privateKeys.size(); i++) {

				ECKey.ECDSASignature mySignature = pubkeys.get(i).sign(sighash);
				TransactionSignature signature = new TransactionSignature(
						mySignature, SigHash.ALL, false);
				signatures.add(signature);
			}
			Script inputScript = ScriptBuilder.createP2SHMultiSigInputScript(
					signatures, redeemScript);

			transaction.getInput(j).setScriptSig(inputScript);
			signatures.clear();
		}
		return totalValue;
	}

	protected long injectOutputs(org.bitcoinj.core.Transaction transaction,
			Output[] outputs, String privateKey, Script redeemScript,
			long restFee, String from) throws IOException,
			AddressFormatException, InsufficientFundException {
		long totalValue = 0;
		Arrays.sort(outputs, outputComparator);
		org.bitcoinj.core.Transaction tx = null;
		// Context context = new Context(network);
		for (Output output : outputs) {
			Sha256Hash outputHash = new Sha256Hash(
					CoinStackClient.convertEndianness(output.getTransactionId()));

			if (tx == null || !tx.getHash().equals(outputHash)) {
				tx = new TemporaryTransaction(isMainNet() ? MainNetParams.get()
						: RegTestParams.get(), outputHash);
				tx.getConfidence().setConfidenceType(
						TransactionConfidence.ConfidenceType.BUILDING);
			}

			transaction.getConfidence().setConfidenceType(
					TransactionConfidence.ConfidenceType.BUILDING);

			// fill hole between indexes with dummies
			while (tx.getOutputs().size() < output.getIndex()) {
				tx.addOutput(new TransactionOutput(isMainNet() ? MainNetParams
						.get() : RegTestParams.get(), tx,
						Coin.NEGATIVE_SATOSHI, new byte[] {}));
			}

			tx.addOutput(new TransactionOutput(isMainNet() ? MainNetParams
					.get() : RegTestParams.get(), tx, Coin.valueOf(output
					.getValue()), org.bitcoinj.core.Utils.HEX.decode(output
					.getScript())));

			transaction.addInput(tx.getOutput(tx.getOutputs().size() - 1));
			totalValue += output.getValue();
		}
		long rest = totalValue - restFee;
		if (rest < 0) {
			throw new InsufficientFundException("Insufficient fund");
		}

		if (rest > 0) {
			transaction.addOutput(Coin.valueOf(rest), new Address(getNetwork(),
					from));
		}
		List<TransactionSignature> signatures = new ArrayList<TransactionSignature>();
		ECKey eckey = new DumpedPrivateKey(getNetwork(), privateKey).getKey();
		for (int i = 0; i < outputs.length; i++) {
			Sha256Hash sighash = transaction.hashForSignature(i, redeemScript,
					SigHash.ALL, false);
			ECKey.ECDSASignature mySignature = eckey.sign(sighash);
			TransactionSignature signature = new TransactionSignature(
					mySignature, SigHash.ALL, false);
			signatures.add(signature);
			Script inputScript = ScriptBuilder.createP2SHMultiSigInputScript(
					signatures, redeemScript);
			transaction.getInput(i).setScriptSig(inputScript);
			signatures.clear();
		}

		return totalValue;
	}

	private boolean isPubkey(ECKey pubkey, Script redeem) {
		List<ECKey> ecs = getECKeyFromRedeemScript(redeem);

		for (int i = 0; i < ecs.size(); i++) {
			if (new String(Hex.encodeHex(ecs.get(i).getPubKey()))
					.equals(new String(Hex.encodeHex(pubkey.getPubKey()))))
				return true;
		}
		return false;

	}

	/**
	 * Fetch list of subscriptions active
	 * 
	 * @return
	 * @throws IOException
	 * @throws CoinStackException
	 */
	public Subscription[] listSubscriptions() throws IOException,
			CoinStackException {
		Endpoint.init();
		return coinStackAdaptor.listSubscriptions();
	}

	/**
	 * Broadcast signed raw transaction to blockchain
	 * 
	 * @param rawTransaction
	 *            a signed transaction in hex-encoded string format ready to be
	 *            broadcasted
	 * @throws IOException
	 *             in case of network failure
	 * @throws TransactionRejectedException
	 *             in case the transaction provided was rejected by blockchain
	 *             network
	 */
	public void sendTransaction(String rawTransaction) throws IOException,
			CoinStackException {
		Endpoint.init();
		coinStackAdaptor.sendTransaction(rawTransaction);
	}

	/**
	 * Add signature to incomplete transaction which is generated by
	 * createMultiSigTransactionWithPartialSign This method is to finalize
	 * incomplete Multisig transaction with last signature required to meet a
	 * redeem script
	 * 
	 * @param transactionSerialized
	 *            Incomplete transaction which is the result of
	 *            createMultiSigTransactionWithPartialSign
	 * @param myPrivateKey
	 * 
	 * @param redeemScript
	 *            Redeem script which is initially generated by using multiple
	 *            private keys
	 * @return transaction signed and ready to be broadcasted in hex-encoded
	 *         string
	 * @throws IOException
	 *             in case of network failure
	 * @throws InsufficientFundException
	 *             in case there is not sufficient fund in private key provided
	 * @throws DustyTransactionException
	 *             in case the amount to transfer is too small to be handled by
	 *             blockchain
	 */
	public String signMultiSigTransaction(String transactionSerialized,
			String myPrivateKey, String redeemScript) throws IOException,
			CoinStackException {

		org.bitcoinj.core.Transaction transaction = new org.bitcoinj.core.Transaction(
				getNetwork(),
				org.bitcoinj.core.Utils.HEX.decode(transactionSerialized));

		Script redeem = null;
		ECKey eckey = null;
		try {
			redeem = new Script(Hex.decodeHex(redeemScript.toCharArray()));
		} catch (DecoderException e) {
			throw new MalformedInputException("Invalid redeem script",
					"failed to decode redeem script");
		}
		try {
			eckey = new DumpedPrivateKey(getNetwork(), myPrivateKey).getKey();
		} catch (AddressFormatException e) {
			throw new MalformedInputException("Invalid private key",
					"Parsing private key failed");
		}

		for (int i = 0; i < transaction.getInputs().size(); i++) {
			List<TransactionSignature> tsList = getSignatures(transaction, i,
					redeem);
			Sha256Hash sighash = transaction.hashForSignature(i, redeem,
					SigHash.ALL, false);
			ECKey.ECDSASignature mySignature = eckey.sign(sighash);
			TransactionSignature signature = new TransactionSignature(
					mySignature, SigHash.ALL, false);
			tsList.add(signature);
			SIGNATURE_COMPARATOR2 comparator = new SIGNATURE_COMPARATOR2(
					sighash, redeem);
			Collections.sort(tsList, comparator);
			Script inputScript = ScriptBuilder.createP2SHMultiSigInputScript(
					tsList, redeem);
			transaction.getInput(i).setScriptSig(inputScript);
			tsList.clear();
		}

		byte[] rawTx = transaction.bitcoinSerialize();

		return Utils.HEX.encode(rawTx);
	}

	/**
	 * Add signature to incomplete transaction which is generated by
	 * createMultiSigTransactionWithPartialSign This method is to finalize
	 * incomplete Multisig transaction with last signature required to meet a
	 * redeem script
	 * 
	 * @param documentHash
	 *            a String to be registered on the Blockchain the value should
	 *            be hex
	 * @return result of stamping is blockchain transaction ID and vout, offset
	 *         value of outputs
	 * 
	 * @throws IOException
	 *             in case of network failure
	 * @throws MalformedInputException
	 *             hash value should be hex value. If it is not, this exception
	 *             is populated
	 */
	public String stampDocument(String documentHash) throws IOException,
			CoinStackException {
		Endpoint.init();
		byte[] hash = null;
		try {
			hash = Hex.decodeHex(documentHash.toCharArray());
		} catch (DecoderException e) {
			throw new MalformedInputException("Invalid input",
					"Invaild hash format - needs to be hex encoded");
		}
		if (hash.length != 32) {
			throw new MalformedInputException("Invalid input",
					"Invaild hash format - needs to be hex encoded");
		}

		return coinStackAdaptor.stampDocument(documentHash);
	}

	/**
	 * Construct a transaction for storing raw data and sign it using private
	 * key
	 * 
	 * @param privateKeyWIF
	 *            private key in Wallet Import Format to sign transaction with
	 * @param payload
	 *            80 byte data to embeded in transaction
	 * @param fee
	 *            amount of fee to pay to miners (minimum 0.0001 BTC = 10000
	 *            satoshi)
	 * @return transaction signed and ready to be broadcasted in hex-encoded
	 *         string
	 * @throws IOException
	 *             in case of network failure
	 * @throws InsufficientFundException
	 *             in case there is not sufficient fund in private key provided
	 * @throws DustyTransactionException
	 *             in case the amount to transfer is too small to be handled by
	 *             blockchain
	 */
	@Deprecated
	public String createDataTransaction(String privateKeyWIF, long fee,
			byte[] payload) throws IOException, CoinStackException {
		Endpoint.init();
		// check sanity test for parameters
		if (payload.length > 80) {
			throw new MalformedInputException("Invalid data",
					"payload length over 80 bytes");
		}
		// derive address from private key
		final ECKey signingKey;
		try {
			signingKey = new DumpedPrivateKey(getNetwork(), privateKeyWIF)
					.getKey();
		} catch (AddressFormatException e) {
			throw new MalformedInputException("Invalid private key",
					"Malformed private key");
		}
		Address fromAddress = signingKey.toAddress(getNetwork());
		String from = fromAddress.toString();

		// get unspentout from address
		Output[] outputs = this.getUnspentOutputs(from);

		Wallet tempWallet = new Wallet(getNetwork());
		tempWallet.allowSpendingUnconfirmedTransactions();
		tempWallet.importKey(signingKey);
		BitcoinjUtil.injectOutputs(tempWallet, outputs, isMainNet());

		org.bitcoinj.core.Transaction txTemplate = new org.bitcoinj.core.Transaction(
				getNetwork());
		Script script = new ScriptBuilder().op(ScriptOpCodes.OP_RETURN)
				.data(payload).build();
		TransactionOutput output = new DataTransactionOutput(this.getNetwork(),
				txTemplate, Coin.ZERO, script.getProgram());
		txTemplate.addOutput(output);

		SendRequest request = SendRequest.forTx(txTemplate);
		request.changeAddress = fromAddress;
		request.fee = Coin.valueOf(fee);
		request.feePerKb = Coin.ZERO;

		org.bitcoinj.core.Transaction tx;
		try {
			tx = tempWallet.sendCoinsOffline(request);
		} catch (InsufficientMoneyException e) {
			throw new InsufficientFundException("Insufficient fund");
		} catch (DustySendRequested e) {
			throw new MalformedInputException("Invalid output",
					"Send amount below dust threshold");
		}
		byte[] rawTx = tx.bitcoinSerialize();
		// // convert to string encoded hex and return
		return Utils.HEX.encode(rawTx);
	}

	/**
	 * Construct a transaction for sending bitcoin and sign it using private key
	 * 
	 * @param privateKeyWIF
	 *            private key in Wallet Import Format to sign transactino with
	 * @param destinationAddress
	 *            address to send fund
	 * @param amount
	 *            amount of fund to transfer
	 * @param fee
	 *            amount of fee to pay to miners (minimum 0.0001 BTC = 10000
	 *            satoshi)
	 * @return transaction signed and ready to be broadcasted in hex-encoded
	 *         string
	 * @throws IOException
	 *             in case of network failure
	 * @throws InsufficientFundException
	 *             in case there is not sufficient fund in private key provided
	 * @throws DustyTransactionException
	 *             in case the amount to transfer is too small to be handled by
	 *             blockchain
	 */
	@Deprecated
	public String createRawTransaction(String privateKeyWIF,
			String destinationAddress, long amount, long fee)
			throws IOException, CoinStackException {
		Endpoint.init();
		// check sanity test for parameters
		Address destinationAddressParsed;
		try {
			destinationAddressParsed = new Address(getNetwork(),
					destinationAddress);
		} catch (AddressFormatException e) {
			throw new MalformedInputException("Invalid destination address",
					"Malformed address");
		}

		// derive address from private key
		final ECKey signingKey;
		try {
			signingKey = new DumpedPrivateKey(getNetwork(), privateKeyWIF)
					.getKey();
		} catch (AddressFormatException e) {
			throw new MalformedInputException("Invalid private key",
					"Malformed private key");
		}
		Address fromAddress = signingKey.toAddress(getNetwork());
		String from = fromAddress.toString();

		// get unspentout from address
		Output[] outputs = this.getUnspentOutputs(from);

		Wallet tempWallet = new Wallet(getNetwork());
		tempWallet.allowSpendingUnconfirmedTransactions();
		tempWallet.importKey(signingKey);
		BitcoinjUtil.injectOutputs(tempWallet, outputs, isMainNet());
		SendRequest request = SendRequest.to(destinationAddressParsed,
				Coin.valueOf(amount));
		request.changeAddress = fromAddress;
		request.fee = Coin.valueOf(fee);
		request.feePerKb = Coin.ZERO;

		org.bitcoinj.core.Transaction tx;
		try {
			tx = tempWallet.sendCoinsOffline(request);
		} catch (InsufficientMoneyException e) {
			throw new InsufficientFundException("Insufficient fund");
		} catch (DustySendRequested e) {
			throw new MalformedInputException("Invalid output",
					"Send amount below dust threshold");
		}
		byte[] rawTx = tx.bitcoinSerialize();
		// // convert to string encoded hex and return
		return Utils.HEX.encode(rawTx);
	}

	/**
	 * Inquiry the information of Stamping Data and time to stamp, number of
	 * confirmations are provided
	 * 
	 * @param stampId
	 *            stampId is the result of stampDocument method which is
	 *            consisted of txID and vout
	 * 
	 * @return Stamp object containing transaction id, vout, number of
	 *         confirmations, and date
	 */
	public Stamp getStamp(String stampId) throws IOException,
			CoinStackException {
		Endpoint.init();
		return coinStackAdaptor.getStamp(stampId);
	}

	private NetworkParameters getNetwork() {
		return network;
	}

	public boolean isMainNet() {
		return isMainNet;
	}
}
