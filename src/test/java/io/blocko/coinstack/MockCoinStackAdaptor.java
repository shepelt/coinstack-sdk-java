/**
 * 
 */
package io.blocko.coinstack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.blocko.coinstack.backendadaptor.AbstractCoinStackAdaptor;
import io.blocko.coinstack.model.Block;
import io.blocko.coinstack.model.Input;
import io.blocko.coinstack.model.Output;
import io.blocko.coinstack.model.Subscription;
import io.blocko.coinstack.model.Transaction;

/**
 * @author nepho
 *
 */
public class MockCoinStackAdaptor extends AbstractCoinStackAdaptor {
	Map<String, Block> blockDB = new HashMap<String, Block>();
	{
		blockDB.put(
				"000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f",
				new Block(
						new Date(1231006505000L),
						"000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f",
						new String[] { "00000000839a8e6886ab5951d76f411475428afc90947ee320161bbf18eb6048" },
						0,
						null,
						new String[] { "4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b" }));
		blockDB.put(
				"00000000839a8e6886ab5951d76f411475428afc90947ee320161bbf18eb6048",
				new Block(
						new Date(1231469665000L),
						"00000000839a8e6886ab5951d76f411475428afc90947ee320161bbf18eb6048",
						new String[] { "000000006a625f06636b8bb6ac7b960a8d03705d1ace08b1a19da3fdcc99ddbd" },
						1,
						"000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f",
						new String[] { "0e3e2357e806b6cdb1f70b54c3a3a17b6714ee1f0e68bebb44a74b1efd512098" }));
	}
	Map<String, Transaction> transactionDB = new HashMap<String, Transaction>();
	{
		transactionDB
				.put("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b",
						new Transaction(
								"4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b",
								new String[] { "000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f" },
								new Date(1231006505000L),
								true,
								new Input[] {},
								new Output[] { new Output(
										null,
										0,
										"1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
										false,
										5000000000l,
										"4104678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5fac") }));

		transactionDB
				.put("001f5eba608a84ba97dd7ac1b21b822b74c91ffbd75c42c7b88abe178f632b31",
						new Transaction(
								"001f5eba608a84ba97dd7ac1b21b822b74c91ffbd75c42c7b88abe178f632b31",
								new String[] { "00000000000000000fad06ca404d52a779d452000057a8342b064618d05a4450" },
								new Date(1425014682000L),
								false,
								new Input[] { new Input(
										1,
										"1Dn86V7bJ7Knv716jj811aXHikyHFD1HQ1",
										"f693cadeacdbb2d980155fbafc82f00c607f2a1fb185cd27b054064b43d00f16",
										7998950000L) },
								new Output[] {
										new Output(
												null,
												0,
												"15Zf4AybWDV6QRcaJ4ErowVxhpdG89Qjni",
												false, 600000000L,
												"76a914320d9492f6b348e003a1ba30afca95eb8d0609e588ac"),
										new Output(
												null,
												0,
												"1Dn86V7bJ7Knv716jj811aXHikyHFD1HQ1",
												false, 7398940000L,
												"76a9148c2a2661cb4afd3ae0c1ea7b1beb5d34e769dbbc88ac") }));
	}
	Map<String, List<String>> addressHistoryDB = new HashMap<String, List<String>>();
	{
		List<String> list1 = new ArrayList<String>();
		list1.add("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b");
		list1.add("3387418aaddb4927209c5032f515aa442a6587d6e54677f08a03b8fa7789e688");
		addressHistoryDB.put("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa", list1);

		List<String> list2 = new ArrayList<String>();
		list2.add("f762df2ff3340171ce913bd3fc2534ee4f15166ffff46a85019eeb52efb8da9b");
		addressHistoryDB.put("1changeFu9bT4Bzbo8qQTcHS7pRfLcX1D", list2);

		List<String> list3 = new ArrayList<String>();
		list3.add("f762df2ff3340171ce913bd3fc2534ee4f15166ffff46a85019eeb52efb8da9b");
		addressHistoryDB.put("1z7Xp8ayc1HDnUhKiSsRz7ZVorxrRFUg6", list3);
	}
	Map<String, Long> addressBalanceDB = new HashMap<String, Long>();
	{
		addressBalanceDB.put("1z7Xp8ayc1HDnUhKiSsRz7ZVorxrRFUg6", new Long(
				4580000000L));
	}
	Map<String, List<Output>> unspentOutputDB = new HashMap<String, List<Output>>();
	{
		List<Output> outputList = new ArrayList<Output>();
		outputList
				.add(new Output(
						"9bdab8ef52eb9e01856af4ff6f16154fee3425fcd33b91ce710134f32fdf62f7",
						0, "1z7Xp8ayc1HDnUhKiSsRz7ZVorxrRFUg6", false,
						4580000000L,
						"76a9140acd296e1ba0b5153623c3c55f2d5b45b1a25ce988ac"));
		unspentOutputDB.put("1z7Xp8ayc1HDnUhKiSsRz7ZVorxrRFUg6", outputList);
	}

	public int getBestHeight() throws IOException {
		return 345229;
	}

	public String getBestBlockHash() throws IOException {
		return "00000000000000000326307b927806f617277c8650b70e66d78eab8323423a33";
	}

	public Block getBlock(String blockId) throws IOException {
		return blockDB.get(blockId);
	}

	public Transaction getTransaction(String transactionId) throws IOException {
		return transactionDB.get(transactionId);
	}

	public String[] getTransactions(String address) throws IOException {
		return addressHistoryDB.get(address).toArray(new String[0]);
	}

	public long getBalance(String address) throws IOException {
		return addressBalanceDB.get(address);
	}

	public Output[] getUnspentOutputs(String address) throws IOException {
		return unspentOutputDB.get(address).toArray(new Output[0]);
	}

	@Override
	public void init() {
	}

	@Override
	public void fini() {
	}

	@Override
	public void sendTransaction(String rawTransaction) throws IOException {
		// do nothing
	}

	@Override
	public Subscription[] listSubscriptions() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteSubscription(String id) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public String addSubscription(Subscription newSubscription)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isMainnet() {
		return true;
	}

	@Override
	public String stampDocument(String hash) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
