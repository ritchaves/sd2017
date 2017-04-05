package org.komparator.mediator.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import javax.jws.WebService;

import org.komparator.supplier.domain.*;
import org.komparator.supplier.ws.BadProductId;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.PurchaseView;
import org.komparator.supplier.ws.SupplierPortType;

// TODO annotate to bind with WSDL

@WebService(
		endpointInterface = "org.komparator.mediator.ws.MediatorPortType", 
		wsdlLocation = "mediator.1_0.wsdl", 
		name = "MediatorWebService", 
		portName = "MediatorPort", 
		targetNamespace = "http://ws.mediator.komparator.org/", 
		serviceName = "MediatorService"
)


// TODO implement port type interface
public class MediatorPortImpl implements MediatorPortType{

	// end point manager
	private MediatorEndpointManager endpointManager;

	public MediatorPortImpl(MediatorEndpointManager endpointManager) {
		this.endpointManager = endpointManager;
	}

	@Override
	public void clear() {
		//Supplier.getInstance().reset();
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<ItemView> getItems(String productId) throws InvalidItemId_Exception {
		// check product id
		if (productId == null)
			InvalidItemId_Exception("Product identifier cannot be null!");
		productId = productId.trim();
		if (productId.length() == 0)
			InvalidItemId_Exception("Product identifier cannot be empty or whitespace!");
		
		Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
		boolean hasSpecialChar = pattern.matcher(productId).find();
		
		if (hasSpecialChar)
			InvalidItemId_Exception("Product identifier must be alphanumeric!");

		// retrieve product
		Supplier supplier = Supplier.getInstance();
		Product p = supplier.getProduct(productId);
		if (p != null) {
			ItemView pv = newItemView(p);
			// product found!
			return pv;
		}
		// product not found
		return null;

	}

	@Override
	public List<CartView> listCarts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ItemView> searchItems(String descText) throws InvalidText_Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ShoppingResultView buyCart(String cartId, String creditCardNr)
			throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addToCart(String cartId, ItemIdView itemId, int itemQty) throws InvalidCartId_Exception,
			InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String ping(String name) {
		if (name == null || name.trim().length() == 0)
			name = "friend";

		String wsName = "Supplier";

		StringBuilder builder = new StringBuilder();
		builder.append("Hello ").append(name);
		builder.append(" from ").append(wsName);
		return builder.toString();
	}

	@Override
	public List<ShoppingResultView> shopHistory() {
		// TODO Auto-generated method stub
		return null;
	}

	// Main operations -------------------------------------------------------

    // TODO
	
    
	// Auxiliary operations --------------------------------------------------	
	

	@Override
	public List<ItemView> listItems() {
		Supplier supplier = Supplier.getInstance();
		List<ItemView> items = new ArrayList<ItemView>();
		for (String pid : supplier.getProductsIDs()) {
			Product p = supplier.getProduct(pid);
			ItemView pv = newItemView(p);
			items.add(pv);
			Collections.sort(items, new Comparator<ItemView>() {
				@Override
		        public int compare(ItemView item1, ItemView item2) {
		            return item2.compareTo(item1);
		        }
		    });
		}
		return null;
	}

	// View helpers ----------------------------------------------------------

	private ItemView newItemView(Product product) {
		ItemView view = new ItemView();
		view.setId(product.getId());
		view.setDesc(product.getDescription());
		view.setPrice(product.getPrice());
		return view;
	}

	private itemIdView newItemIdView(Purchase purchase, SupplierClient supplier) {
		itemIdView view = new itemIdView();
		view.setId(purchase.getPurchaseId());
		view.setSupplierId(supplier.getWsURL());
		return view;
	}

    
	// Exception helpers -----------------------------------------------------

	private void InvalidItemId_Exception(final String message) throws InvalidItemId_Exception {
		InvalidItemId faultInfo = new InvalidItemId();
		faultInfo.message = message;
		throw new InvalidItemId_Exception(message, faultInfo);
	}

	private void InvalidCartId_Exception(final String message) throws InvalidCartId_Exception {
		InvalidCartId faultInfo = new InvalidCartId();
		faultInfo.message = message;
		throw new InvalidCartId_Exception(message, faultInfo);
	}
	
	private void InvalidQuantity_Exception(final String message) throws InvalidQuantity_Exception {
		InvalidQuantity faultInfo = new InvalidQuantity();
		faultInfo.message = message;
		throw new InvalidQuantity_Exception(message, faultInfo);
	}
	
	private void NotEnoughItems_Exception(final String message) throws NotEnoughItems_Exception {
		NotEnoughItems faultInfo = new NotEnoughItems();
		faultInfo.message = message;
		throw new NotEnoughItems_Exception(message, faultInfo);
	}

	private void EmptyCart_Exception(final String message) throws EmptyCart_Exception {
		EmptyCart faultInfo = new EmptyCart();
		faultInfo.message = message;
		throw new EmptyCart_Exception(message, faultInfo);
	}

	private void InvalidText_Exception(final String message) throws InvalidText_Exception {
		InvalidText faultInfo = new InvalidText();
		faultInfo.message = message;
		throw new InvalidText_Exception(message, faultInfo);
	}
	
	private void InvalidCreditCard_Exception(final String message) throws InvalidCreditCard_Exception {
		InvalidCreditCard faultInfo = new InvalidCreditCard();
		faultInfo.message = message;
		throw new InvalidCreditCard_Exception(message, faultInfo);
	}
	
	
}
