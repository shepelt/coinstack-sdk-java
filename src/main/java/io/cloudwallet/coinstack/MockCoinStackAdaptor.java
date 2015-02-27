/**
 * 
 */
package io.cloudwallet.coinstack;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author nepho
 *
 */
public class MockCoinStackAdaptor {
	private Map<String, Block> blockDB = new HashMap<String, Block>();
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
	private Map<String, Transaction> transactionDB = new HashMap<String, Transaction>();
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
										"1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
										false,
										5000000000l,
										"4104678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5fac") }));
	}

	public int getBestHeight() {
		return 345229;
	}

	public String getBestBlockHash() {
		return "00000000000000000326307b927806f617277c8650b70e66d78eab8323423a33";
	}

	public Block getBlock(String blockId) {
		return blockDB.get(blockId);
	}

	public Transaction getTransaction(String transactionId) {
		return transactionDB.get(transactionId);
	}

}
