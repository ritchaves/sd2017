package org.komparator.mediator.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.komparator.mediator.domain.*;

/** Domain Root. */
public class Mediator {

	// Members ---------------------------------------------------------------

	private AtomicInteger cartIdCounter = new AtomicInteger(0);
	
	private List<Cart> cartList = new ArrayList<Cart>();
	
	private AtomicInteger purchaseIdCounter = new AtomicInteger(0);
	
	private Map<String, Purchase> purchases = new ConcurrentHashMap<>();
	
	private LocalDateTime aliveTS = null;
	
	// Singleton -------------------------------------------------------------

	/* Private constructor prevents instantiation from other classes */
	private Mediator() {
	}

	/**
	 * SingletonHolder is loaded on the first execution of
	 * Singleton.getInstance() or the first access to SingletonHolder.INSTANCE,
	 * not before.
	 */
	private static class SingletonHolder {
		private static final Mediator INSTANCE = new Mediator();
	}

	public static synchronized Mediator getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	//Returns the last timestamp saved
	public LocalDateTime getLastAlive() {
		return aliveTS;
	}
	
	public void setLastAlive() {
		aliveTS = LocalDateTime.now();
	}
	
		
		
	// product ---------------------------------------------------------------

	public void reset() {
		cartList.clear();
		cartIdCounter.set(0);
		purchases.clear();
		purchaseIdCounter.set(0);
	}

	public Boolean cartExists(Cart c){
		return cartList.contains(c);
	}
	
	public Cart getCart(String cId){
		for (Cart c: cartList){
			if (c.getcartID().equals(cId))
					return c;
		}
		return null;
	}
	
	public List<Cart> getCartList(){
		return cartList;
	}
	
	
	public Cart addNewCart(){
		Cart c = new Cart(this.cartIdCounter.incrementAndGet());
		cartList.add(c);
		return c;
	}
	
	public Cart addNewCart(String id){
		Cart c = new Cart(id);
		cartList.add(c);
		return c;
	}

	public void removeCart(Cart c) {
		this.cartList.remove(c);
		
	}
	
	
	//Purchase 
	
	public Purchase getPurchase(String purchaseId) {
		return purchases.get(purchaseId);
	}

	private String generatePurchaseId() {
		// relying on AtomicInteger to make sure assigned number is unique
		int purchaseId = purchaseIdCounter.incrementAndGet();
		return Integer.toString(purchaseId);
	}

	public List<String> getPurchasesIDs() {
		List<Purchase> purchasesList = new ArrayList<>(purchases.values());
		// using comparator to sort result list
		Collections.sort(purchasesList, new Comparator<Purchase>() {
			public int compare(Purchase p1, Purchase p2) {
				return p1.getTimestamp().compareTo(p2.getTimestamp());
			}
		});
		List<String> idsList = new ArrayList<String>();
		for (Purchase p : purchasesList) {
			idsList.add(p.getPurchaseId());
		}
		return idsList;
	}
	
	public String addPurchase(String result, List<Item> purchasedItems, List<Item> droppedItems){
		String purchaseId = generatePurchaseId();
		Purchase purchase = new Purchase(purchaseId, result, purchasedItems, droppedItems);
		purchases.put(purchaseId, purchase);
		return purchaseId;
	}
	
	public void addPurchase(String id, int totalPrice, String result, List<Item> purchasedItems, List<Item> droppedItems){
		Purchase purchase = new Purchase(id, totalPrice, result, purchasedItems, droppedItems);
		purchases.put(id, purchase);
	}

}