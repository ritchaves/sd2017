package org.komparator.mediator.domain;

/**
 * Product entity. Only the product quantity is mutable so its get/set methods
 * are synchronized.
 */
public class Item {

	/** Product identifie
	r. */
	private String productId;
	/** Product description. */
	private String description;
	/** Available quantity of product. */
	private volatile int quantity;
	/** Price of product */
	private int price;
	
	/** supplier identifier. */
	private String supplierId;

	/** Create a new product */
	public Item(String pid, String description, int quantity, int price, String supplier) {
		this.productId = pid;
		this.description = description;
		this.quantity = quantity;
		this.price = price;
		this.supplierId = supplier;
	}

	public String getId() {
		return productId;
	}

	public String getDescription() {
		return description;
	}

	public int getPrice() {
		return price;
	}

	/** Synchronized locks object before returning quantity */
	public synchronized int getQuantity() {
		return quantity;
	}

	/** Synchronized locks object before setting quantity */
	public synchronized void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	public String getSupplierId() {
		return supplierId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Item [productId=").append(productId);
		builder.append(", description=").append(description);
		builder.append(", from supplier=").append(supplierId);
		builder.append(", quantity=").append(quantity);
		builder.append(", price=").append(price);
		builder.append("]");
		return builder.toString();
	}

}
