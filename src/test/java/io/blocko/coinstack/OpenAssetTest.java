package io.blocko.coinstack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.wallet.WalletTransaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.blocko.coinstack.CoinStackClient;
import io.blocko.coinstack.Endpoint;
import io.blocko.coinstack.backendadaptor.CoreBackEndAdaptor;
import io.blocko.coinstack.exception.CoinStackException;
import io.blocko.coinstack.exception.MalformedInputException;
import io.blocko.coinstack.model.Block;
import io.blocko.coinstack.model.BlockchainStatus;
import io.blocko.coinstack.model.CredentialsProvider;
import io.blocko.coinstack.model.Output;
import io.blocko.coinstack.model.Transaction;
import io.blocko.coinstack.openassets.ColoringEngine;
import io.blocko.coinstack.openassets.util.Leb128;

/**
 * @author Nathan Lee
 *
 */
public class OpenAssetTest {
	protected CoinStackClient coinStackClient;
	protected ColoringEngine coloringEngine;

	/**
	 * @throws java.lang.Exception
	 */
//	@Before
//	public void setUp() throws Exception {
//		coinStackClient = new CoinStackClient(new MockCoinStackAdaptor());
//	}
	@Before
	public void setUp() throws Exception {
		coinStackClient = new CoinStackClient(new CoreBackEndAdaptor(new CredentialsProvider() {

			@Override
			public String getAccessKey() {
				return "e84ddc87dbb93d577907d524748e39";
			}

			@Override
			public String getSecretKey() {
				return "843a557f883ecec603aab5377d5c2a";
			}

		}, Endpoint.MAINNET));
		coloringEngine = new ColoringEngine(coinStackClient);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		coinStackClient.close();
	}

	@Test
	public void testIssueAsset() throws Exception {
		String privateKeyWIF = "KyJwJ3Na9fsgvoW2v4rVGRJ7Cnb2pG4yyQQvrGWvkpuovvMRE9Kb";
		
		long assetAmount = 666;
		String to = "akE2cSu1JuzpXNABPXSrwkWtgL4fiTNq1xz";
		//long amount = Math.convertToSatoshi("0.0001");
		long fee = Math.convertToSatoshi("0.0002");
		String rawTx = coloringEngine.issueAsset(privateKeyWIF, assetAmount, to, fee);
		//String rawTx = coinStackClient.createRawTransaction(privateKeyWIF, to, amount, fee);
		assertNotNull(rawTx);
		System.out.println(rawTx);
		assertNotNull(TransactionUtil.getTransactionHash(rawTx));
		System.out.println(TransactionUtil.getTransactionHash(rawTx));
	//	coinStackClient.sendTransaction(rawTx);
	}
	
	@Test
	public void testTransferAsset() throws Exception {
		String privateKeyWIF = "KztgqWTCKS6dxuUnKcJnyiHtUqJ78k91P8Rn9oNrFLhgnRh3wiiE";
		String assetID = "AKJFoih7ioqPXAHgnDzJvHE8x2FMcFerfv";
		long assetAmount = 30;
		String to = "akFNUeHPC59mrBw3E57bRjgKTdUZeMxeLur";
		//long amount = Math.convertToSatoshi("0.0001");
		long fee = Math.convertToSatoshi("0.0002");
		String rawTx = coloringEngine.transferAsset(privateKeyWIF, assetID, assetAmount, to, fee);
		//String rawTx = coinStackClient.createRawTransaction(privateKeyWIF, to, amount, fee);
		assertNotNull(rawTx);
		System.out.println(rawTx);
		assertNotNull(TransactionUtil.getTransactionHash(rawTx));
	//	coinStackClient.sendTransaction(rawTx);
	}
	
	@Test
	public void testUnspentOutput() throws Exception {
		Output[] outputs2 = coinStackClient.getUnspentOutputs("1DNztBKqX3BSLmWtCCsu2h5hHGJ6vkuVMt");
		String assetId = outputs2[0].getMetaData().getAsset_id();
		System.out.println("assetId : " + assetId);
	}
	
	@Test
	public void testLeb128() throws Exception {
		long assetAmount = 1000;
		byte [] res = Leb128.writeUnsignedLeb128((int)assetAmount);
		long resLong = Leb128.readUnsignedLeb128(res);
		System.out.println(resLong);
		System.out.println("assetAmountLEB128 : " +org.bitcoinj.core.Utils.HEX.encode(res));
		byte [] res2 = org.bitcoinj.core.Utils.HEX.decode("ce02");
		int resint = Leb128.readUnsignedLeb128(res2);
		System.out.println("resint : " + resint);
	}
	
}