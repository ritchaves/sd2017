package org.komparator.mediator.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Purchase entity. Immutable i.e. once an object is created it cannot be
 * changed.
 */
public class Purchase {
	/** Purchase identifier. */
	private String purchaseId;
	
	private int finalPrice;

	private Date timestamp = new Date();
	
	private String result;
	
	private List<Item> purchased = new ArrayList<Item>();
	
	private List<Item> dropped = new ArrayList<Item>();;

	/** Create a new purchase. */
	public Purchase(String pid, int unitPrice, List<Item> purch, List<Item> drop, String res) {
		this.purchaseId = pid;
		this.finalPrice = unitPrice;
		purchased = purch;
		setDroppedItemList(drop);
		result = res;
	}
	
	public Purchase(String pid, String res, List<Item> purchasedItems, List<Item> droppedItems) {
		this.purchaseId = pid;
		purchased = purchasedItems;
		setDroppedItemList(droppedItems);
		result = res;
		
		this.finalPrice = generateFinalPrice();
		
	}

	public String getPurchaseId() {
		return purchaseId;
	}

	public int generateFinalPrice(){
		int p = 0;
		for (Item i: purchased){
			p = i.getPrice()*i.getQuantity();
			finalPrice = finalPrice + p;
		}
		return finalPrice;	
	}

	public int getFinalPrice() {
		return finalPrice;
	}
	
	public void setFinalPrice(int i) {
		finalPrice = i;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public List<Item> getDroppedItemList() {
		return dropped;
	}

	public void setDroppedItemList(List<Item> dropped) {
		this.dropped = dropped;
	}
	
	public List<Item> getPurchasedItemList() {
		return purchased;
	}

	public void setPurchasedItemList(List<Item> p) {
		this.purchased = p;
	}

	
}
