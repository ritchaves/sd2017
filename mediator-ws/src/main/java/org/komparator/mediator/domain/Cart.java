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
public class Cart {


//My attributes
	private List<Item> products; = new ArrayList<Items>();
	
	private String cartID; //Is SH1 SH2 etc
		
    
	public Cart(int id) {
		
		
		String idString = Integer.toString(id);
		
		StringBuilder builder = new StringBuilder();
		builder.append("SH").append(idString);
		cartID = builder.toString();
			
	}
	
	public getcartID(){
        return this.cartID;
	}
	
	public getProducts(){
        return this.products;
	}

	
	public addProduct(Item prod){
        if (! this.products.contains(prod))
            this.produtcs.add(prod);
            
        else{
            int newQuantity = this.products.contains(prod).getQuantity() + prod.getQuantity();
            this.products.contains(prod).setQuantity(newQuantity);
        }
	}
	
	public isCartEmpty(){
        this.products.isEmpty();
	}
	
	public cartLength(){
        this.products.size();
	}
	
	

}
