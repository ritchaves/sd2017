package org.komparator.mediator.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Domain Root. */
public class Mediator {

	// Members ---------------------------------------------------------------

	private AtomicInteger cartIdCounter = new AtomicInteger(0);
	
	private List<Cart> cartList = new ArrayList<Cart>();
	
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

	// product ---------------------------------------------------------------

	public void reset() {
		cartList.clear();
		cartIdCounter.set(0);
	}

	public Boolean cartExists(Cart c){
		return cartList.contains(c);
	}
	
	public Cart getCart(String cId){
		for (Cart c: cartList){
			if (c.getcartID() == cId)
					return c;
		}
		return null;
	}
	
	public List<Cart> getCartList(){
		return cartList;
	}
	
	
	public Cart addNewCart(){
		Cart c = new Cart(this.cartIdCounter.incrementAndGet());
		return c;
	}
	

}