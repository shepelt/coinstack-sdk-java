/**
 * Copyright (c) CloudWallet Inc. and/or its affiliates. All rights reserved.
 */
package io.cloudwallet.coinstack;

import io.cloudwallet.coinstack.backendadaptor.AbstractCoinStackAdaptor;
import io.cloudwallet.coinstack.backendadaptor.CoreBackEndAdaptor;
import io.cloudwallet.coinstack.exception.DustyTransactionException;
import io.cloudwallet.coinstack.exception.InsufficientFundException;
import io.cloudwallet.coinstack.exception.MalformedInputException;
import io.cloudwallet.coinstack.exception.TransactionRejectedException;
import io.cloudwallet.coinstack.model.Block;
import io.cloudwallet.coinstack.model.BlockchainStatus;
import io.cloudwallet.coinstack.model.CredentialsProvider;
import io.cloudwallet.coinstack.model.DataTransactionOutput;
import io.cloudwallet.coinstack.model.Input;
import io.cloudwallet.coinstack.model.Output;
import io.cloudwallet.coinstack.model.Subscription;
import io.cloudwallet.coinstack.model.Transaction;
import io.cloudwallet.coinstack.util.EnvironmentVariableCredentialsProvider;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.ECKey.ECDSASignature;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.SignatureException;
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
//import org.bitcoinj.core.Context;
import org.bitcoinj.core.DumpedPrivateKey;
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
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptChunk;
import org.bitcoinj.script.ScriptOpCodes;
import org.bitcoinj.wallet.WalletTransaction;

import com.google.common.primitives.UnsignedBytes;

/**
 * A client class for accessing Bitcoin Blockchain through CoinStack. Provides
 * functionality for creating and managing Bitcoin addresses and transactions.
 */
public class CoinStackClient {
	private AbstractCoinStackAdaptor coinStackAdaptor;
	private static SecureRandom secureRandom = new SecureRandom();
	private NetworkParameters network;

	/**
	 * Creates a CoinStack client instance that connects to mainnet endpoint
	 * Defaults to using credentials from env variables COINSTACK_ACCESS_KEY_ID
	 * and COINSTACK_SECRET_ACCESS_KEY
	 */
	public CoinStackClient() {
		this.coinStackAdaptor = new CoreBackEndAdaptor(
				new EnvironmentVariableCredentialsProvider(), Endpoint.MAINNET);
		coinStackAdaptor.init();

		this.network = MainNetParams.get();
	};

	/**
	 * Creates a CoinStack client instance with endpoint specified
	 * 
	 * @param endpoint
	 *            endpoint to connect to (available: EndPoint.MAINNET,
	 *            EndPoint.TESTNET)
	 */
	public CoinStackClient(CredentialsProvider provider, Endpoint endpoint) {
		this.coinStackAdaptor = new CoreBackEndAdaptor(provider, endpoint);
		coinStackAdaptor.init();

		this.network = endpoint.mainnet() ? MainNetParams.get()
				: TestNet3Params.get();
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

		this.network = endpoint.mainnet() ? MainNetParams.get()
				: TestNet3Params.get();
	}

	protected CoinStackClient(AbstractCoinStackAdaptor coinStackAdaptor) {
		this.coinStackAdaptor = coinStackAdaptor;
		coinStackAdaptor.init();
		network = coinStackAdaptor.isMainnet() ? MainNetParams.get()
				: TestNet3Params.get();
	}

	/**
	 * CoinStack client needs to be closed after usage to prevent resource leaks
	 */
	public void close() {
		coinStackAdaptor.fini();
	}

	/**
	 * Fetch current Blockchain status
	 * 
	 * @return current status of Blockchain in BlockchainStatus object
	 * @throws IOException
	 *             in case of network failure
	 */
	public BlockchainStatus getBlockchainStatus() throws IOException {
		Endpoint.init();
		return new BlockchainStatus(coinStackAdaptor.getBestHeight(),
				coinStackAdaptor.getBestBlockHash());
	}

	/**
	 * Fetch block information
	 * 
	 * @param blockId
	 *            block ID (block hash) in string format
	 * @return block information and related transaction
	 * @throws IOException
	 *             in case of network failure
	 */
	public Block getBlock(String blockId) throws IOException {
		Endpoint.init();
		return coinStackAdaptor.getBlock(blockId);
	}

	/**
	 * Fetch transaction information
	 * 
	 * @param transactionId
	 *            transaction ID (transaction hash) in string format
	 * @return transaction information, inputs, and outputs
	 * @throws IOException
	 *             in case of network failure
	 */
	public Transaction getTransaction(String transactionId) throws IOException {
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
	 */
	public String[] getTransactions(String address) throws IOException {
		Endpoint.init();
		return coinStackAdaptor.getTransactions(address);
	}

	/**
	 * Fetch current balance of given address
	 * 
	 * @param address
	 *            to view balance of in string format
	 * @return balance in satoshi format (e.g. 0.0001 BTC = 10000 satoshi)
	 * @throws IOException
	 *             in case of network failure
	 */
	public long getBalance(String address) throws IOException {
		Endpoint.init();
		return coinStackAdaptor.getBalance(address);
	}

	/**
	 * Fetch unspent outputs of given address
	 * 
	 * @param address
	 *            to view unspent received transaction outputs of
	 * @return list of unspent transaction outputs
	 * @throws IOException
	 *             in case of network failure
	 */
	public Output[] getUnspentOutputs(String address) throws IOException {
		Endpoint.init();
		return coinStackAdaptor.getUnspentOutputs(address);
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
			throws IOException, InsufficientFundException,
			DustyTransactionException {
		Endpoint.init();
		// check sanity test for parameters
		Address destinationAddressParsed;
		try {
			destinationAddressParsed = new Address(network, destinationAddress);
		} catch (AddressFormatException e) {
			throw new MalformedInputException("Malformed destination address");
		}

		// derive address from private key
		final ECKey signingKey;
		try {
			signingKey = new DumpedPrivateKey(network, privateKeyWIF).getKey();
		} catch (AddressFormatException e) {
			throw new MalformedInputException("Parsing private key failed");
		}
		Address fromAddress = signingKey.toAddress(network);
		String from = fromAddress.toString();

		// get unspentout from address
		Output[] outputs = this.getUnspentOutputs(from);

		Wallet tempWallet = new Wallet(network);
		tempWallet.allowSpendingUnconfirmedTransactions();
		tempWallet.importKey(signingKey);
		injectOutputs(tempWallet, outputs);
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
			throw new DustyTransactionException(
					"Send amount below dust threshold");
		}
		byte[] rawTx = tx.bitcoinSerialize();
		// // convert to string encoded hex and return
		return Utils.HEX.encode(rawTx);
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
			byte[] payload) throws IOException, InsufficientFundException,
			DustyTransactionException {
		Endpoint.init();
		// check sanity test for parameters
		if (payload.length > 80) {
			throw new MalformedInputException("payload length over 80 bytes");
		}
		// derive address from private key
		final ECKey signingKey;
		try {
			signingKey = new DumpedPrivateKey(network, privateKeyWIF).getKey();
		} catch (AddressFormatException e) {
			throw new MalformedInputException("Parsing private key failed");
		}
		Address fromAddress = signingKey.toAddress(network);
		String from = fromAddress.toString();

		// get unspentout from address
		Output[] outputs = this.getUnspentOutputs(from);

		Wallet tempWallet = new Wallet(network);
		tempWallet.allowSpendingUnconfirmedTransactions();
		tempWallet.importKey(signingKey);
		injectOutputs(tempWallet, outputs);

		org.bitcoinj.core.Transaction txTemplate = new org.bitcoinj.core.Transaction(
				network);
		Script script = new ScriptBuilder().op(ScriptOpCodes.OP_RETURN)
				.data(payload).build();
		TransactionOutput output = new DataTransactionOutput(this.network,
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
			throw new DustyTransactionException(
					"Send amount below dust threshold");
		}
		byte[] rawTx = tx.bitcoinSerialize();
		// // convert to string encoded hex and return
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
	public String createSignedTransaction(TransactionBuilder builder,
			String privateKeyWIF) throws IOException,
			InsufficientFundException, DustyTransactionException {
		Endpoint.init();
		// check sanity test for parameters
		org.bitcoinj.core.Transaction txTemplate = new org.bitcoinj.core.Transaction(
				network);
		for (Output output : builder.getOutputs()) {
			Address destinationAddressParsed;
			try {
				destinationAddressParsed = new Address(network,
						output.getAddress());
			} catch (AddressFormatException e) {
				throw new MalformedInputException(
						"Malformed destination address");
			}
			txTemplate.addOutput(Coin.valueOf(output.getValue()),
					destinationAddressParsed);
		}

		// add OP_RETURN
		if (null != builder.getData()) {
			Script script = new ScriptBuilder().op(ScriptOpCodes.OP_RETURN)
					.data(builder.getData()).build();
			TransactionOutput output = new DataTransactionOutput(this.network,
					txTemplate, Coin.ZERO, script.getProgram());
			txTemplate.addOutput(output);
		}

		// derive address from private key
		final ECKey signingKey;
		try {
			signingKey = new DumpedPrivateKey(network, privateKeyWIF).getKey();
		} catch (AddressFormatException e) {
			throw new MalformedInputException("Parsing private key failed");
		}
		Address fromAddress = signingKey.toAddress(network);
		String from = fromAddress.toString();

		// get unspentout from address
		Output[] outputs = this.getUnspentOutputs(from);
		Wallet tempWallet = new Wallet(network);
		tempWallet.allowSpendingUnconfirmedTransactions();
		tempWallet.importKey(signingKey);
		injectOutputs(tempWallet, outputs);

		SendRequest request = SendRequest.forTx(txTemplate);
		request.changeAddress = fromAddress;
		request.fee = Coin.valueOf(builder.getFee());
		request.feePerKb = Coin.ZERO;

		org.bitcoinj.core.Transaction tx;
		try {
			tx = tempWallet.sendCoinsOffline(request);
		} catch (InsufficientMoneyException e) {
			throw new InsufficientFundException("Insufficient fund");
		} catch (DustySendRequested e) {
			throw new DustyTransactionException(
					"Send amount below dust threshold");
		}
		byte[] rawTx = tx.bitcoinSerialize();
		// // convert to string encoded hex and return
		return Utils.HEX.encode(rawTx);
	}

	/**
	 * Calculate transaction hash from raw transaction
	 * 
	 * @param rawTransaction
	 *            transaction in hex-encoded string format
	 * @return the hash (transaction ID) of given raw transaction
	 */
	public static String getTransactionHash(String rawTransaction) {
		return getTransactionHash(rawTransaction, true);
	}

	public static String getTransactionHash(String rawTransaction,
			boolean isMainNet) {
		return new org.bitcoinj.core.Transaction(
				isMainNet ? MainNetParams.get() : TestNet3Params.get(),
				org.bitcoinj.core.Utils.HEX.decode(rawTransaction))
				.getHashAsString();
	}

	/**
	 * Construct a transaction object from raw transaction
	 * 
	 * @param rawTransaction
	 *            transaction in hex-encoded string format
	 * @return a transaction object representing the given raw transaction
	 */
	public static Transaction parseRawTransaction(String rawTransaction) {
		return parseRawTransaction(rawTransaction, true);
	}

	public static Transaction parseRawTransaction(String rawTransaction,
			boolean isMainNet) {
		org.bitcoinj.core.Transaction tx = new org.bitcoinj.core.Transaction(
				isMainNet ? MainNetParams.get() : TestNet3Params.get(),
				org.bitcoinj.core.Utils.HEX.decode(rawTransaction));
		tx.getInputs();
		tx.getOutputs();
		Input[] inputs = new Input[tx.getInputs().size()];
		for (int i = 0; i < tx.getInputs().size(); i++) {
			inputs[i] = new Input(i, null, tx.getInput(i)
					.getParentTransaction().getHashAsString(), 0l);
		}

		Output[] outputs = new Output[tx.getOutputs().size()];
		for (int i = 0; i < tx.getOutputs().size(); i++) {
			outputs[i] = new Output(tx.getHashAsString(), i, tx.getOutput(i)
					.getScriptPubKey().getToAddress(MainNetParams.get())
					.toString(), false, tx.getOutput(i).getValue().value,
					Utils.HEX.encode(tx.getOutput(i).getScriptBytes()));
		}

		Transaction parsedTx = new Transaction(tx.getHashAsString(),
				new String[] {}, tx.getUpdateTime(), false, inputs, outputs);
		return parsedTx;
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
			TransactionRejectedException {
		Endpoint.init();
		coinStackAdaptor.sendTransaction(rawTransaction);
	}

	/**
	 * Convert human-readable bitcoin string (e.g. 0.0001 BTC) to satoshi unit
	 * 
	 * @param bitcoinAmount
	 *            in human-friendly, string format (e.g. 0.0001 BTC)
	 * @return bitcoin amount in satoshi
	 */
	public static long convertToSatoshi(String bitcoinAmount) {
		return Coin.parseCoin(bitcoinAmount).value;
	}

	protected static String convertEndianness(String original) {
		StringBuilder result = new StringBuilder();
		for (int i = original.length() - 2; i >= 0; i -= 2) {
			result.append(original.substring(i, i + 2));
		}
		return result.toString();
	}

	private static Comparator<Output> outputComparator = new Comparator<Output>() {
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
    private class SIGNATURE_COMPARATOR2 implements Comparator<TransactionSignature> {
    	private Comparator<byte[]> comparator = UnsignedBytes.lexicographicalComparator();
    	Sha256Hash sighash;
    	Script redeem;
    	public SIGNATURE_COMPARATOR2(Sha256Hash sighash, Script redeem) {
    		this.sighash = sighash;
    		this.redeem = redeem;
    	}
        @Override
        public int compare(TransactionSignature k1, TransactionSignature k2) {
        	ECKey ec1 = getECKeyFromSignature(k1.toCanonicalised(), sighash, redeem);
        	ECKey ec2 = getECKeyFromSignature(k2.toCanonicalised(), sighash, redeem);
            return comparator.compare(ec1.getPubKey(), ec2.getPubKey());
        }
    }
	private static class TemporaryTransaction extends
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

	protected static void injectOutputs(Wallet wallet, Output[] outputs) {
		// sort outputs with txid and output index
		Arrays.sort(outputs, outputComparator);
		TemporaryTransaction tx = null;
		for (Output output : outputs) {
			//Sha256Hash outputHash = Sha256Hash.wrap(
			Sha256Hash outputHash =  new Sha256Hash(
					CoinStackClient.convertEndianness(output.getTransactionId()));
			if (tx == null || !tx.getHash().equals(outputHash)) {
				tx = new TemporaryTransaction(MainNetParams.get(), outputHash);
				tx.getConfidence().setConfidenceType(
						TransactionConfidence.ConfidenceType.BUILDING);
				wallet.addWalletTransaction(new WalletTransaction(
						WalletTransaction.Pool.UNSPENT, tx));
			}

			// fill hole between indexes with dummies
			while (tx.getOutputs().size() < output.getIndex()) {
				tx.addOutput(new TransactionOutput(MainNetParams.get(), tx,
						Coin.NEGATIVE_SATOSHI, new byte[] {}));
			}

			tx.addOutput(new TransactionOutput(MainNetParams.get(), tx, Coin
					.valueOf(output.getValue()), org.bitcoinj.core.Utils.HEX
					.decode(output.getScript())));
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
	public static String deriveAddress(String privateKeyWIF)
			throws MalformedInputException {
		return deriveAddress(privateKeyWIF, true);
	}

	public static String deriveAddress(String privateKeyWIF, boolean isMainNet)
			throws MalformedInputException {
		ECKey signingKey;
		NetworkParameters network = isMainNet ? MainNetParams.get()
				: TestNet3Params.get();
		try {
			signingKey = new DumpedPrivateKey(network, privateKeyWIF).getKey();
		} catch (AddressFormatException e) {
			throw new MalformedInputException("Parsing private key failed");
		}
		return signingKey.toAddress(network).toString();
	}

	/**
	 * Randomly generate a new private key
	 * 
	 * @return a new private key in Wallet Import Format
	 */
	public static String createNewPrivateKey() {
		return createNewPrivateKey(true);
	}

	public static String createNewPrivateKey(boolean isMainNet) {
		ECKey ecKey = new ECKey(secureRandom);
		if (isMainNet) {
			return ecKey.getPrivateKeyEncoded(MainNetParams.get()).toString();
		} else {
			return ecKey.getPrivateKeyEncoded(TestNet3Params.get()).toString();
		}
	}
	
	public static String signMessage(String privateKeyWIF, String messageText, boolean isMainNet) {
		ECKey eckey = null;
		String signature = null;
		try {
			if (isMainNet) {
				eckey = new DumpedPrivateKey(MainNetParams.get(), privateKeyWIF).getKey();
			} else {
				eckey = new DumpedPrivateKey(TestNet3Params.get(), privateKeyWIF).getKey();
			}
			
			signature = eckey.signMessage(messageText);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return signature;
	}
	
	public static boolean verifyMessageSignature(String address, String messageText, String signature,boolean isMainNet) {
		ECKey originalKey = null;
		String derivedAddress = null;
		try {
			originalKey = ECKey.signedMessageToKey(messageText, signature);
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (isMainNet) {
			derivedAddress = originalKey.toAddress(MainNetParams.get()).toString();
		} else {
			derivedAddress = originalKey.toAddress(TestNet3Params.get()).toString();
		}
		
		if( address.equals(derivedAddress))
			return true;
		else
			return false;
		
	}
	
	public static String hashSha256(String message) {
		Sha256Hash hash = Sha256Hash.create(message.getBytes());
		return Utils.HEX.encode(hash.getBytes());
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
				new Address(TestNet3Params.get(), address);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Fetch list of subscriptions active
	 * 
	 * @return
	 * @throws IOException
	 */
	public Subscription[] listSubscriptions() throws IOException {
		Endpoint.init();
		return coinStackAdaptor.listSubscriptions();
	}

	/**
	 * Delete subscription with given id
	 * 
	 * @param id
	 * @throws IOException
	 */
	public void deleteSubscription(String id) throws IOException {
		Endpoint.init();
		coinStackAdaptor.deleteSubscription(id);
	}

	/**
	 * Add a subscription with given condition
	 * 
	 * @param newSubscription
	 * @return
	 * @throws IOException
	 */
	public String addSubscription(Subscription newSubscription)
			throws IOException {
		Endpoint.init();
		return coinStackAdaptor.addSubscription(newSubscription);
	}
	
	public String createRedeemScript(int threshold, List<byte[]> pubkeys) {
		List<ECKey> eckeys = new ArrayList<ECKey>();
		for(int i = 0 ; i < pubkeys.size(); i++) {
			eckeys.add(ECKey.fromPublicOnly(pubkeys.get(i)));			
		}
		Script sc = ScriptBuilder.createRedeemScript(threshold, eckeys);
		
		return new String(Hex.encodeHex(sc.getProgram()));
	}
	
	@SuppressWarnings("unused")
	private Script createRedeemScriptToScript(int threshold, List<byte[]> pubkeys) {
		List<ECKey> eckeys = new ArrayList<ECKey>();
		for(int i = 0 ; i < pubkeys.size(); i++) {
			eckeys.add(ECKey.fromPublicOnly(pubkeys.get(i)));			
		}
		Script sc = ScriptBuilder.createRedeemScript(threshold, eckeys);
		return sc;
	}
	
	public String createAddressFromRedeemScript(Script redeemScript) throws DecoderException {
		Script sc = ScriptBuilder.createP2SHOutputScript(redeemScript);
		Address address = Address.fromP2SHScript(MainNetParams.get(), sc);
		return address.toString();
	}
	
	public String createMultiSigTransaction(TransactionBuilder builder,
			List<String> privateKeys, String redeemScript) throws IOException,
			InsufficientFundException, DustyTransactionException, AddressFormatException {
		Endpoint.init();

		org.bitcoinj.core.Transaction txTemplate = new org.bitcoinj.core.Transaction(
				network);

		//Transaction output added
		long totalSpendValue = 0;
		for (Output output : builder.getOutputs()) {
			Address destinationAddressParsed;
			try {
				destinationAddressParsed = new Address(network,
						output.getAddress());
			} catch (AddressFormatException e) {
				throw new MalformedInputException(
						"Malformed destination address");
			}
			txTemplate.addOutput(Coin.valueOf(output.getValue()),
					destinationAddressParsed);
			totalSpendValue += output.getValue(); 
		}

		// add OP_RETURN if there is any
		if (null != builder.getData()) {
			Script script = new ScriptBuilder().op(ScriptOpCodes.OP_RETURN)
					.data(builder.getData()).build();
			TransactionOutput output = new DataTransactionOutput(this.network,
					txTemplate, Coin.ZERO, script.getProgram());
			txTemplate.addOutput(output);
		}
		
		// derive address from reddemScript
		String from = null;
		Script redeem = null;
		try {
			redeem = new Script(Hex.decodeHex(redeemScript.toCharArray()));
			from = createAddressFromRedeemScript(redeem);
		} catch (DecoderException e1) {
			e1.printStackTrace();
		}

		// get unspentout from address
		// Transaction input added
		Output[] outputs = this.getUnspentOutputs(from);
		injectOutputs(txTemplate, outputs, privateKeys, redeem, totalSpendValue + builder.getFee(),  from);
		
		byte[] rawTx = txTemplate.bitcoinSerialize();
		// // convert to string encoded hex and return
		return Utils.HEX.encode(rawTx);
	}
	
	protected  long injectOutputs(org.bitcoinj.core.Transaction transaction, Output[] outputs, 
			List<String> privateKeys,  Script redeemScript, long restFee, String from) throws IOException, AddressFormatException {
		long totalValue = 0;
		Arrays.sort(outputs, outputComparator);
		org.bitcoinj.core.Transaction tx = null;
//		Context context = new Context(network);
		for (Output output : outputs) {
			Sha256Hash outputHash = new Sha256Hash(
					CoinStackClient.convertEndianness(output.getTransactionId()));

			if (tx == null || !tx.getHash().equals(outputHash)) {
				tx = new TemporaryTransaction(MainNetParams.get(), outputHash);
				tx.getConfidence().setConfidenceType(
						TransactionConfidence.ConfidenceType.BUILDING);
			}

			transaction.getConfidence().setConfidenceType(
					TransactionConfidence.ConfidenceType.BUILDING);
			
			// fill hole between indexes with dummies
			while (tx.getOutputs().size() < output.getIndex()) {
				tx.addOutput(new TransactionOutput(MainNetParams.get(), tx,
						Coin.NEGATIVE_SATOSHI, new byte[] {}));
			}
			
			tx.addOutput(new TransactionOutput(MainNetParams.get(), tx, Coin
					.valueOf(output.getValue()), org.bitcoinj.core.Utils.HEX
					.decode(output.getScript())));

			transaction.addInput(tx.getOutput(tx.getOutputs().size()-1)); 
			totalValue += output.getValue();
		}
		long rest = totalValue - restFee;
		if(rest < 0) {
			throw  new InsufficientFundException("Insufficient fund");
		}
		
		if(rest > 0) {
			try {
				transaction.addOutput(Coin.valueOf(rest),
					new Address(network, from));
			} catch (AddressFormatException e) {
		
				e.printStackTrace();
			}
		}
		List<TransactionSignature> signatures = new ArrayList<TransactionSignature>();
		List<ECKey> eckeys = new ArrayList<ECKey>();
		for(int i =0  ; i < privateKeys.size() ; i++) {
			ECKey eckey = null;
			try {
				eckey = new DumpedPrivateKey(network, privateKeys.get(i)).getKey();
				eckeys.add(eckey);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//ECKey [] ecKeyArrays = eckeys.toArray(new ECKey [0]);
		//Arrays.sort(ecKeyArrays, ECKeyComparator);
		List<ECKey> pubkeys = new ArrayList<ECKey>(eckeys);
		Collections.sort(pubkeys, ECKey.PUBKEY_COMPARATOR);
		for(int j = 0 ; j < outputs.length ; j++) {
			Sha256Hash sighash = transaction.hashForSignature(j, redeemScript,SigHash.ALL, false);
			for(int i =0  ; i < privateKeys.size() ; i++) {

				ECKey.ECDSASignature mySignature = pubkeys.get(i).sign(sighash);
				TransactionSignature signature = new TransactionSignature(mySignature, SigHash.ALL , false);
				signatures.add(signature);
			}
		Script inputScript = ScriptBuilder.createP2SHMultiSigInputScript(signatures, redeemScript);

		transaction.getInput(j).setScriptSig(inputScript);
		signatures.clear();
		}
		return totalValue;
	}

	
	protected  long injectOutputs(org.bitcoinj.core.Transaction transaction, Output[] outputs, 
			String privateKey,  Script redeemScript, long restFee, String from) throws IOException, AddressFormatException {
		long totalValue = 0;
		Arrays.sort(outputs, outputComparator);
		org.bitcoinj.core.Transaction tx = null;
//		Context context = new Context(network);
		for (Output output : outputs) {
			Sha256Hash outputHash = new Sha256Hash(
					CoinStackClient.convertEndianness(output.getTransactionId()));

			if (tx == null || !tx.getHash().equals(outputHash)) {
				tx = new TemporaryTransaction(MainNetParams.get(), outputHash);
				tx.getConfidence().setConfidenceType(
						TransactionConfidence.ConfidenceType.BUILDING);
			}

			transaction.getConfidence().setConfidenceType(
					TransactionConfidence.ConfidenceType.BUILDING);
			
			// fill hole between indexes with dummies
			while (tx.getOutputs().size() < output.getIndex()) {
				tx.addOutput(new TransactionOutput(MainNetParams.get(), tx,
						Coin.NEGATIVE_SATOSHI, new byte[] {}));
			}
			
			tx.addOutput(new TransactionOutput(MainNetParams.get(), tx, Coin
					.valueOf(output.getValue()), org.bitcoinj.core.Utils.HEX
					.decode(output.getScript())));

			transaction.addInput(tx.getOutput(tx.getOutputs().size()-1)); 
			totalValue += output.getValue(); 
		}
		long rest = totalValue - restFee;
		if(rest < 0) {
			throw  new InsufficientFundException("Insufficient fund");
		}
		
		if(rest > 0) {
			try {
				transaction.addOutput(Coin.valueOf(rest),
					new Address(network, from));
			} catch (AddressFormatException e) {
		
				e.printStackTrace();
			}
		}
		List<TransactionSignature> signatures = new ArrayList<TransactionSignature>();
		ECKey eckey = new DumpedPrivateKey(network, privateKey).getKey();
		for(int i = 0 ; i < outputs.length ; i ++) {
			Sha256Hash sighash = transaction.hashForSignature(i, redeemScript,SigHash.ALL, false);
			ECKey.ECDSASignature mySignature = eckey.sign(sighash);
			TransactionSignature signature = new TransactionSignature(mySignature, SigHash.ALL , false);
			signatures.add(signature);
			Script inputScript = ScriptBuilder.createP2SHMultiSigInputScript(signatures, redeemScript);
			transaction.getInput(i).setScriptSig(inputScript);
			signatures.clear();
		}

		return totalValue;
	}
	
	
	public String signMultiSigTransaction(String transactionSerialized, 
			String myPrivateKey, String redeemScript) throws  IOException {

		org.bitcoinj.core.Transaction transaction = new org.bitcoinj.core.Transaction(network, 
				org.bitcoinj.core.Utils.HEX.decode(transactionSerialized));
		    
		Script redeem = null;
		ECKey eckey = null;
		try {
			redeem = new Script(Hex.decodeHex(redeemScript.toCharArray()));
			eckey = new DumpedPrivateKey(network, myPrivateKey).getKey();
		} catch (DecoderException e1) {
			e1.printStackTrace();
		} catch (AddressFormatException e) {
			e.printStackTrace();
		}
		
		for(int i = 0 ; i < transaction.getInputs().size() ; i ++) {
			List<TransactionSignature> tsList = getSignatures(transaction, i, redeem);
			Sha256Hash sighash = transaction.hashForSignature(i, redeem,SigHash.ALL, false);
			ECKey.ECDSASignature mySignature = eckey.sign(sighash);
			TransactionSignature signature = new TransactionSignature(mySignature, SigHash.ALL , false);
			tsList.add(signature);
			SIGNATURE_COMPARATOR2 comparator = new SIGNATURE_COMPARATOR2(sighash, redeem);
			Collections.sort(tsList, comparator);
			Script inputScript = ScriptBuilder.createP2SHMultiSigInputScript(tsList, redeem);
			transaction.getInput(i).setScriptSig(inputScript);
			tsList.clear();
		}

		byte[] rawTx = transaction.bitcoinSerialize();

		return Utils.HEX.encode(rawTx);
	}
	
	private List<TransactionSignature> getSignatures(org.bitcoinj.core.Transaction transaction, int index, Script redeem) {
		List<ScriptChunk> sc = transaction.getInput(index).getScriptSig().getChunks();
		List<TransactionSignature> tsList = new ArrayList<TransactionSignature>();
		for(int i = 1 ; i < sc.size()-1; i++) {

			TransactionSignature ts = TransactionSignature.decodeFromBitcoin(sc.get(i).data, false);
			tsList.add(ts);
		}	
		return tsList;
	}
	
	private static ECKey getECKeyFromSignature(ECDSASignature ec,Sha256Hash sighash, Script redeem) {
		ECKey thisEc = null;
		for(int i = 0 ; i < 4 ; i++) {
			thisEc = ECKey.recoverFromSignature(i, ec, sighash, false);
			
			if(thisEc != null ) {
				if(isPubkey(thisEc, redeem)) return thisEc;

			}
		}
		return thisEc;
	}
	
	private static boolean isPubkey(ECKey pubkey, Script redeem) {
		List<ECKey> ecs = getECKeyFromRedeemScript(redeem);
		for(int i = 0 ; i < ecs.size() ; i++) {
			if(new String(Hex.encodeHex(ecs.get(i).getPubKey()))
					.equals(new String(Hex.encodeHex(pubkey.getPubKey()))))
				return true;
		}
		return false;
		
	}
	
	private static List<ECKey> getECKeyFromRedeemScript(Script redeemScript) {
		ArrayList<ECKey> result = new ArrayList<ECKey>();
		List<ScriptChunk> chunks = redeemScript.getChunks();
        int numKeys = chunks.size() - 3;
        for (int i = 0 ; i < numKeys ; i++) 
            result.add(ECKey.fromPublicOnly(chunks.get(1 + i).data));
        return result;
	}
	
	public String createMultiSigTransactionWithPartialSign(TransactionBuilder builder,
			String privateKeys, String redeemScript) throws IOException,
			InsufficientFundException, DustyTransactionException, AddressFormatException {
		Endpoint.init();
		org.bitcoinj.core.Transaction txTemplate = new org.bitcoinj.core.Transaction(network);

		//Transaction output added
		long totalSpendValue = 0;
		for (Output output : builder.getOutputs()) {
			Address destinationAddressParsed;
			try {
				destinationAddressParsed = new Address(network, output.getAddress());
			} catch (AddressFormatException e) {
				throw new MalformedInputException(
						"Malformed destination address");
			}
			txTemplate.addOutput(Coin.valueOf(output.getValue()), destinationAddressParsed);
			totalSpendValue += output.getValue(); 
		}

		// add OP_RETURN if there is any
		if (null != builder.getData()) {
			Script script = new ScriptBuilder().op(ScriptOpCodes.OP_RETURN)
					.data(builder.getData()).build();
			TransactionOutput output = new DataTransactionOutput(this.network,
					txTemplate, Coin.ZERO, script.getProgram());
			txTemplate.addOutput(output);
		}
		
		// derive address from reddemScript
		String from = null;
		Script redeem = null;
		try {
			redeem = new Script(Hex.decodeHex(redeemScript.toCharArray()));
			from = createAddressFromRedeemScript(redeem);
		} catch (DecoderException e1) {
			e1.printStackTrace();
		}
		
		// get unspentout from address
		// Transaction input added
		Output[] outputs = this.getUnspentOutputs(from);
		injectOutputs(txTemplate, outputs, privateKeys, redeem, totalSpendValue + builder.getFee(), from);
		
		byte[] rawTx = txTemplate.bitcoinSerialize();
		return Utils.HEX.encode(rawTx);
	}
}
