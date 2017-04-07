package org.komparator.mediator.domain;

import java.util.ArrayList;
import java.util.List;


/** Domain Root. */
public class Cart {


//My attributes
	private List<Item> products = new ArrayList<Item>();
	
	private String cartID; //Is SH1 SH2 etc
	
	private boolean purchased;
		
    
	public Cart(int id) {
		
		
		String idString = Integer.toString(id);
		
		StringBuilder builder = new StringBuilder();
		builder.append("SC").append(idString);
		cartID = builder.toString();
		
		setPurchased(false);
	
	}
	
	public Cart(String id) {
		
		cartID = id;
		
		setPurchased(false);
	
	}
	
	public String getcartID(){
        return this.cartID;
	}
	
	public List<Item> getProducts(){
        return this.products;
	}
	
	public Item getProduct(String prod){
		for (Item i: products){
			if (i.getId().equals(prod))
				return i;
		}
		return null;
			
	}

	
	public void addProduct(Item prod){
		
		for(Item i :products){
        	if (i.getId().equals(prod.getId()) && i.getSupplierId().equals(prod.getSupplierId())){
        		int newQuantity = i.getQuantity() + prod.getQuantity();
        		i.setQuantity(newQuantity);
        		return;
        	}
        }
		
		products.add(prod);
	}
	
	public void addProduct(String prodId, String supId, int quant){
		Item i = new Item(prodId, null, quant, -1, supId);
		addProduct(i);
	}
	
	public boolean isCartEmpty(){
        return this.products.isEmpty();
	}
	
	public int cartLength(){
        return this.products.size();
	}

	public boolean wasPurchased() {
		return purchased;
	}

	public void setPurchased(boolean purchased) {
		this.purchased = purchased;
	}
	
	

}
