package org.komparator.mediator.domain;

import java.util.ArrayList;
import java.util.List;


/** Domain Root. */
public class Cart {


//My attributes
	private List<Item> products = new ArrayList<Item>();
	
	private String cartID; //Is SH1 SH2 etc
		
    
	public Cart(int id) {
		
		
		String idString = Integer.toString(id);
		
		StringBuilder builder = new StringBuilder();
		builder.append("SC").append(idString);
		cartID = builder.toString();

			
	}
	
	public String getcartID(){
        return this.cartID;
	}
	
	public List<Item> getProducts(){
        return this.products;
	}

	
	public void addProduct(Item prod){
		
        if (! this.products.contains(prod)){
            this.products.add(prod);
        }    
        else{
        	
        	int aux = products.indexOf(prod);
        	
            int newQuantity = this.products.get(aux).getQuantity() + prod.getQuantity();
            
            this.products.get(aux).setQuantity(newQuantity);
        }
	}
	
	public boolean isCartEmpty(){
        return this.products.isEmpty();
	}
	
	public int cartLength(){
        return this.products.size();
	}
	
	

}
