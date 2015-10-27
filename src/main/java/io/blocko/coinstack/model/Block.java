/**
 * 
 */
package io.blocko.coinstack.model;

import java.util.Date;

/**
 * @author nepho
 *
 */
public class Block {
	public Block(Date blockConfirmationTime, String blockId, String[] childIds,
			int height, String parentId, String[] transactionIds) {
		this.blockConfirmationTime = blockConfirmationTime;
		this.blockId = blockId;
		this.childIds = childIds;
		this.height = height;
		this.parentId = parentId;
		this.transactionIds = transactionIds;
	}

	private Date blockConfirmationTime;
	private String blockId;
	private String[] childIds;
	private int height;
	private String parentId;
	private String[] transactionIds;

	public Date getBlockConfirmationTime() {
		return blockConfirmationTime;
	}

	public String getBlockId() {
		return blockId;
	}

	public String[] getChildIds() {
		return childIds;
	}

	public int getHeight() {
		return height;
	}

	public String getParentId() {
		return parentId;
	}

	public String[] getTransactionIds() {
		return transactionIds;
	}

	public void setBlockConfirmationTime(Date blockConfirmationTime) {
		this.blockConfirmationTime = blockConfirmationTime;
	}

	public void setBlockId(String blockId) {
		this.blockId = blockId;
	}

	public void setChildIds(String[] childIds) {
		this.childIds = childIds;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public void setTransactionIds(String[] transactionIds) {
		this.transactionIds = transactionIds;
	}

}
