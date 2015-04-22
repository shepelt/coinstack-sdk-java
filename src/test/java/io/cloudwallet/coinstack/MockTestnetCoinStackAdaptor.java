/**
 * 
 */
package io.cloudwallet.coinstack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nepho
 *
 */
public class MockTestnetCoinStackAdaptor extends AbstractCoinStackAdaptor {
	Map<String, Block> blockDB = new HashMap<String, Block>();
	{
		blockDB.put(
				"000000000933ea01ad0ee984209779baaec3ced90fa3f408719526f8d77f4943",
				new Block(
						new Date(1296688602000L),
						"000000000933ea01ad0ee984209779baaec3ced90fa3f408719526f8d77f4943",
						new String[] { "00000000b873e79784647a6c82962c70d228557d24a747ea4d1b8bbe878e1206" },
						0,
						null,
						new String[] { "4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b" }));
		blockDB.put(
				"00000000b873e79784647a6c82962c70d228557d24a747ea4d1b8bbe878e1206",
				new Block(
						new Date(1296688928000L),
						"00000000b873e79784647a6c82962c70d228557d24a747ea4d1b8bbe878e1206",
						new String[] { "000000006c02c8ea6e4ff69651f7fcde348fb9d557a06e6957b65552002a7820" },
						1,
						"000000000933ea01ad0ee984209779baaec3ced90fa3f408719526f8d77f4943",
						new String[] { "f0315ffc38709d70ad5647e22048358dd3745f3ce3874223c80a7c92fab0c8ba" }));
	}
	Map<String, Transaction> transactionDB = new HashMap<String, Transaction>();
	{
		transactionDB
				.put("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b",
						new Transaction(
								"4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b",
								new String[] { "000000000933ea01ad0ee984209779baaec3ced90fa3f408719526f8d77f4943" },
								new Date(1296688602000L),
								true,
								new Input[] {},
								new Output[] { new Output(
										null,
										0,
										"mpXwg4jMtRhuSpVq4xS3HFHmCmWp9NyGKt",
										false,
										5000000000L,
										"4104678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5fac") }));

		transactionDB
				.put("083e1d738d782b388767fc6e6b77107718763281b31bd7cf6a699a3fd4a6958b",
						new Transaction(
								"083e1d738d782b388767fc6e6b77107718763281b31bd7cf6a699a3fd4a6958b",
								new String[] { "0000000015a0e164c15b871293fdc12c3983b2cd118ebb754d80fe023a43fc07" },
								new Date(1426152531000L),
								false,
								new Input[] {
										new Input(
												0,
												"moAHUTQ23CU2aAMD2M8e8MBHYvrWXdNjcM",
												"14a94b66578b5c9c2e904586b2b2dfc079e8ee26dac83e87851b52478b61a1ae",
												1000000L),
										new Input(
												1,
												"mwxnGsu49hZTaBeYBrXnvN64mWSAdnLDVx",
												"9595c83d9ce049089303f5139ad094a39e15ab2539af365a93b67448e5352d08",
												49010000L) },
								new Output[] { new Output(null, 0,
										"mz9R2o5kVaSG1bExrWakrkXYe1kUFBEF2J",
										false, 50000000L,
										"76a914cc578ebe1f9613053d18e0f03e0fbaf0952caedb88ac") }));
	}
	Map<String, List<String>> addressHistoryDB = new HashMap<String, List<String>>();
	{
		List<String> list1 = new ArrayList<String>();
		list1.add("356e51f16967791f5a6ab38f70504cb3d9ca886cd5e189ee6d6dc78d7379af61");
		list1.add("9eeb7c618cc91d3daab1303367e1039ccd4b76d9a54ad015b028f3de07975ec5");
		list1.add("6d5fac9e71a45fd422e17e00e9fc1e29fb496ecd1f54ea018c3feb78a44e5a93");
		list1.add("6e680e445b90def5b5a6f026bdec69bf993a7c3e5c8469f7f68cf5fcf3cc12f7");
		list1.add("987e412948c08daf7f71c52ecce66ba13a6a8e74add66685559e51b1d7b9f965");
		list1.add("5d71ac8cbf2ccd400d2a49804447b9eb286862755b7c4f5132f31cf8dd7a5cb0");
		addressHistoryDB.put("mpXwg4jMtRhuSpVq4xS3HFHmCmWp9NyGKt", list1);

		List<String> list2 = new ArrayList<String>();
		list2.add("ea91e1ed3f4f1d920a06ccef5b1341e5f339319e2a0402e43bdc5e23b16ce7e1");
		addressHistoryDB.put("mfqge3DMrshybtbS3QZ58yd8SjkCF3iFMf", list2);

		List<String> list3 = new ArrayList<String>();
		list3.add("8b95a6d43f9a696acfd71bb3813276187710776b6efc6787382b788d731d3e08");
		addressHistoryDB.put("mz9R2o5kVaSG1bExrWakrkXYe1kUFBEF2J", list3);
	}
	Map<String, Long> addressBalanceDB = new HashMap<String, Long>();
	{
		addressBalanceDB.put("mz9R2o5kVaSG1bExrWakrkXYe1kUFBEF2J", new Long(
				50000000L));
	}
	Map<String, List<Output>> unspentOutputDB = new HashMap<String, List<Output>>();
	{
		List<Output> outputList = new ArrayList<Output>();
		outputList
				.add(new Output(
						"8b95a6d43f9a696acfd71bb3813276187710776b6efc6787382b788d731d3e08",
						0, "mpXwg4jMtRhuSpVq4xS3HFHmCmWp9NyGKt", false,
						50000000L,
						"76a914cc578ebe1f9613053d18e0f03e0fbaf0952caedb88ac"));
		unspentOutputDB.put("mz9R2o5kVaSG1bExrWakrkXYe1kUFBEF2J", outputList);
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
	void init() {
	}

	@Override
	void fini() {
	}

	@Override
	void sendTransaction(String rawTransaction) throws IOException {
		// do nothing
	}

	@Override
	Subscription[] listSubscriptions() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	void deleteSubscription(String id) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	String addSubscription(Subscription newSubscription) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
