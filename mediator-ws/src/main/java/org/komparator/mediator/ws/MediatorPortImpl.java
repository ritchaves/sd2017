package org.komparator.mediator.ws;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.jws.WebService;

import org.komparator.mediator.domain.*;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadText_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;


@WebService(
		endpointInterface = "org.komparator.mediator.ws.MediatorPortType", 
		wsdlLocation = "mediator.1_0.wsdl", 
		name = "MediatorWebService", 
		portName = "MediatorPort", 
		targetNamespace = "http://ws.mediator.komparator.org/", 
		serviceName = "MediatorService"
)

public class MediatorPortImpl implements MediatorPortType{

	// end point manager
	private MediatorEndpointManager endpointManager;
	
	
	public MediatorPortImpl(MediatorEndpointManager endpointManager) {
		this.endpointManager = endpointManager;
	}
	
	
	public List<String> myUddiList() {
		UDDINaming uddinn = endpointManager.getUddiNaming();
		List <String> availableSupplierswsURL = new ArrayList<String>();
		try {
			return availableSupplierswsURL = (List<String>) uddinn.list("A57_Supplier%");
		} catch (UDDINamingException e) {
			// TODO Excepcao!!!!!! ********************************************
		}
		return availableSupplierswsURL;
	}
	
	public Collection<UDDIRecord> myUddiRecordList() {		
		UDDINaming uddinn = endpointManager.getUddiNaming();
//		System.out.println(uddinn.getUDDIUrl());              testing my biche uddi
//		try {
//			System.out.println(uddinn.lookup("A57_Supplier%"));
//		} catch (UDDINamingException e1) {
//			e1.printStackTrace();
//		}
		Collection<UDDIRecord> availableSupplierswsURL = new ArrayList<UDDIRecord>();
		try { 
			availableSupplierswsURL = uddinn.listRecords("A57_Supplier%");
			//System.out.println(availableSupplierswsURL);
			return availableSupplierswsURL;
		} catch (UDDINamingException e) {
			// TODO Excepcao!!!!!! ********************************************
		}
		return availableSupplierswsURL;
	}

	// Main operations -------------------------------------------------------
	
	@Override
	public List<ItemView> getItems(String productId) throws InvalidItemId_Exception {
		if (productId == null)
			throwInvalidItemId("Product identifier cannot be null!");
		productId = productId.trim();
		if (productId.length() == 0)
			throwInvalidItemId("Product identifier cannot be empty or whitespace!");
		
		Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
		boolean hasSpecialChar = pattern.matcher(productId).find();
		
		if (hasSpecialChar)
			throwInvalidItemId("Product identifier must be alphanumeric!");
		
		
		Collection<UDDIRecord> SuppliersWsURL = myUddiRecordList();
		List<ItemView> pricesPerSupplier = new ArrayList<ItemView>();
		try {		
			for (UDDIRecord url : SuppliersWsURL) {
				SupplierClient S = null;
					S = new SupplierClient(url.getUrl());
					if (S.getProduct(productId) != null) {
					ItemIdView itId = newItemIdView(S.getProduct(productId), url.getOrgName());
					ItemView it = newItemView(S.getProduct(productId), itId);
					pricesPerSupplier.add(it);
			}
			Collections.sort(pricesPerSupplier, new Comparator<ItemView>() {
				@Override
				public int compare(ItemView i1, ItemView i2) {
					return new Integer(i1.getPrice()).compareTo(new Integer(i2.getPrice()));
				}
			});
			
			}
		 
			return pricesPerSupplier;
			
		} catch (SupplierClientException | BadProductId_Exception e) {
			System.err.println("Caught exception in" + e);
		}
		pricesPerSupplier = Collections.emptyList();
		return pricesPerSupplier;
	}

	@Override
	public List<ItemView> searchItems(String descText) throws InvalidText_Exception {
		if (descText == null)
			throwInvalidText("Search text cannot be null!");
		descText = descText.trim();
		if (descText.length() == 0)
			throwInvalidText("Seach text cannot be empty or whitespace!");
		
		Collection<UDDIRecord> SuppliersWsURL = myUddiRecordList();
		List<ItemView> save = new ArrayList<ItemView>();
		System.out.println(SuppliersWsURL);
		
		try {		
			for (UDDIRecord url : SuppliersWsURL) {
				SupplierClient S = null;
					S = new SupplierClient(url.getUrl());
					
					List<ProductView> existingProducts = null;
					existingProducts = S.searchProducts(descText);
					if(existingProducts != null)
						for(ProductView pv : existingProducts) {
							ItemIdView itID = newItemIdView(pv, url.getOrgName());
							ItemView it = newItemView(pv, itID);
							save.add(it);
						}
			}
			
			Map<String,ItemView> map = new HashMap<String,ItemView>();
			for (ItemView i : save) map.put(i.getItemId().getProductId(),i);
			Map<String, ItemView> treeMap = new TreeMap<String, ItemView>(map);
			List<ItemView> SortedList = new ArrayList<ItemView>(treeMap.values());
			Collections.sort(SortedList, new Comparator<ItemView>() {
				@Override
				public int compare(ItemView i1, ItemView i2) {
					return new Integer(i1.getPrice()).compareTo(new Integer(i2.getPrice()));
				}
			});
			
			
			return SortedList;
		} catch (SupplierClientException | BadText_Exception e) {			
			throwInvalidText("Search text cannot be null, whitespace or empty!");	// TODO Excepcao!!!!!! ********************************************
		}
		return null;
	}
					
	@Override
	//TODO INCOMPLETE!!! 
	//Issues with prices!! ask supplier??
	public void addToCart(String cartId, ItemIdView itemId, int itemQty) throws InvalidCartId_Exception,
			InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		//Collection<UDDIRecord> SuppliersWsURL = myUddiRecordList();

		
		Cart c = Mediator.getInstance().getCart(cartId);
		if (c == null)
			c = Mediator.getInstance().addNewCart();
		
		c.addProduct(itemId.getProductId(), itemId.getSupplierId(), itemQty);
		
		//TODO find itemId.getSupplierId() in UDDII
		//SupplierClient S = null;
		//S = new SupplierClient(url.getUrl());
		//if c.getProduct(itemId.getProductId()).getQuantity >= supplier - throw exception!
	}
	
	@Override
	//Estrutura que me pareceu correcta! TODO
	//TODO INCOMPLETE!!! 
	//Issues with prices!! ask supplier??
	public ShoppingResultView buyCart(String cartId, String creditCardNr)
			throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
		
		//Validação do cartão de credito CreditCard
		UDDINaming uddinn = endpointManager.getUddiNaming();
		
		ShoppingResultView view = new ShoppingResultView();
		
		try {
			String CCwsURL = uddinn.lookup("CreditCard");
			/*CreditCardClient ccc = new CreditCardClient(CCwsURL);
			if (ccc.validateNumber(creditCardNr)){
			
				Cart c = Mediator.getInstance().getCartList().getCart(cartId);
			
				for(Item i: c){
					//TODO find itemId.getSupplierId() in UDDII
					//SupplierClient S = null;
					//S = new SupplierClient(url.getUrl());
					//s.buyProduct(i)
					//Tratar Excepçoes
				}
				c.setPurchased(true);
				//view.setId(ID de compra);
				 * List<CartItemView> getDroppedItems
				 * List<CartItemView> getPurchasedItems
				
			}*/
		} catch (UDDINamingException e) {
			// TODO Excepcao!!!!!! ********************************************
		}
		
		
		return null;
	}

	    
	// Auxiliary operations --------------------------------------------------	
	
	@Override
	public String ping(String name) {
		UDDINaming uddinn = endpointManager.getUddiNaming();
		List<String> availableSupplierswsURL = null;
		try {
			availableSupplierswsURL = (List<String>) uddinn.list("A57_Supplier%");
		} catch (UDDINamingException e) {
			// TODO Excepcao!!!!!! ********************************************
		}
		
		for (String url : availableSupplierswsURL) {
			SupplierClient S = null;
			try {
				S = new SupplierClient(url);
				if (name == null || name.trim().length() == 0)
					name = "friend";
				String result = S.ping(name) + " from Mediator.";
				return result;
				
			} catch (SupplierClientException e) {
				// TODO Excepcao!!!!!! ********************************************
			}	
		}
		return null;
	}
	
	@Override
	public void clear() {
		Mediator.getInstance().reset();
		//get every Supplier! TODO!!
		List<String> availableSupplierswsURL = new ArrayList<String>();
		
		for (String url : availableSupplierswsURL) {
			SupplierClient S = null;
			try {
				S = new SupplierClient(url);
				S.clear();
				
			} catch (SupplierClientException e) {
				// TODO Excepcao!!!!!! ********************************************
			}	
		
		}
	}
	
	@Override
	public List<CartView> listCarts() {
		List<CartView> listView = new ArrayList<CartView>();
		CartView view = new CartView();
		for (Cart c: Mediator.getInstance().getCartList()){
			view.setCartId(c.getcartID());
			// TODO finish me!!!! ********************************* FALTA O CONTEUDO DO CARRINHO
			listView.add(view);
		}	
		return null;
	}
	
	
	@Override
	public List<ShoppingResultView> shopHistory() {
		
		List<ShoppingResultView> lSRV = new ArrayList<ShoppingResultView>();
		ShoppingResultView view = new ShoppingResultView();
		
		for (Cart c: Mediator.getInstance().getCartList()){
			if (c.wasPurchased()){
				//FIXME TODO ************************************************************ ???? nem sei se é isto!!
			}
		}
		// TODO Auto-generated method stub
		return null;
	}

	
	public List<ItemView> listItems() {
		
//		Supplier supplier = Supplier.getInstance();
//		List<ItemView> items = new ArrayList<ItemView>();
//		for (String pid : supplier.getProductsIDs()) {
//			Product p = supplier.getProduct(pid);
//			ItemView pv = newItemView(p);
//			items.add(pv);
//		}
		return null;
	}


	// View helpers ----------------------------------------------------------

	private ItemView newItemView(ProductView product, ItemIdView iid) {
		ItemView view = new ItemView();
		view.setItemId(iid); //Isto e o item c o id do product e o fornecedor
		view.setDesc(product.getDesc());
		view.setPrice(product.getPrice());
		return view;
	}

	private ItemIdView newItemIdView(ProductView product, String supplier) {
		ItemIdView view = new ItemIdView();
		view.setProductId(product.getId());
		view.setSupplierId(supplier);
		return view;
	}
	
	private CartItemView newCartItemView(ItemView item, Item product) {
		CartItemView view = new CartItemView();
		view.setItem(item);
		view.setQuantity(product.getQuantity());
		return view;
	}
	
	private CartView newCartView(Cart cart) {
		CartView view = new CartView();
		view.setCartId(cart.getcartID());
		return view;
	}

    
	// Exception helpers -----------------------------------------------------

	private void throwInvalidItemId(final String message) throws InvalidItemId_Exception {
		InvalidItemId faultInfo = new InvalidItemId();
		faultInfo.message = message;
		throw new InvalidItemId_Exception(message, faultInfo);
	}

	private void throwInvalidCartId(final String message) throws InvalidCartId_Exception {
		InvalidCartId faultInfo = new InvalidCartId();
		faultInfo.message = message;
		throw new InvalidCartId_Exception(message, faultInfo);
	}
	
	private void throwInvalidQuantity(final String message) throws InvalidQuantity_Exception {
		InvalidQuantity faultInfo = new InvalidQuantity();
		faultInfo.message = message;
		throw new InvalidQuantity_Exception(message, faultInfo);
	}
	
	private void throwNotEnoughItems(final String message) throws NotEnoughItems_Exception {
		NotEnoughItems faultInfo = new NotEnoughItems();
		faultInfo.message = message;
		throw new NotEnoughItems_Exception(message, faultInfo);
	}

	private void throwEmptyCart(final String message) throws EmptyCart_Exception {
		EmptyCart faultInfo = new EmptyCart();
		faultInfo.message = message;
		throw new EmptyCart_Exception(message, faultInfo);
	}

	private void throwInvalidText(final String message) throws InvalidText_Exception {
		InvalidText faultInfo = new InvalidText();
		faultInfo.message = message;
		throw new InvalidText_Exception(message, faultInfo);
	}
	
	private void throwInvalidCreditCard(final String message) throws InvalidCreditCard_Exception {
		InvalidCreditCard faultInfo = new InvalidCreditCard();
		faultInfo.message = message;
		throw new InvalidCreditCard_Exception(message, faultInfo);
	}
	
	private void throwBadProductId(final String message) throws BadProductId_Exception {
		//BadProductId faultInfo = new BadProductId();
		//faultInfo.message = message;
		//throw new BadProductId_Exception(message, faultInfo);
	}
	
	
}
