package org.komparator.mediator.ws;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.jws.HandlerChain;
import javax.jws.WebService;

import org.komparator.mediator.domain.*;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadQuantity_Exception;
import org.komparator.supplier.ws.BadText_Exception;
import org.komparator.supplier.ws.InsufficientQuantity_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;

import pt.ulisboa.tecnico.sdis.ws.cli.*;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;


@WebService(
		endpointInterface = "org.komparator.mediator.ws.MediatorPortType", 
		wsdlLocation = "mediator.2_0.wsdl",  //FIXME Isto faz sentido aqui? 2.0
		name = "MediatorWebService", 
		portName = "MediatorPort", 
		targetNamespace = "http://ws.mediator.komparator.org/", 
		serviceName = "MediatorService"
)
@HandlerChain(file = "/mediator-ws_handler-chain.xml")
public class MediatorPortImpl implements MediatorPortType{

	// end point manager
	private MediatorEndpointManager endpointManager;
	
	private String cccURL = "http://ws.sd.rnl.tecnico.ulisboa.pt:8080/cc";
	
	private List<ShoppingResultView> shopHistoryUp = new ArrayList<ShoppingResultView>();
	
	private List<CartView> listCartsUp = new ArrayList<CartView>();
	
	public MediatorPortImpl(MediatorEndpointManager endpointManager) {
		this.endpointManager = endpointManager;
	}
	
	//Being Alive - FIXME SEND HELP- fixed?
	@Override
	public void imAlive() {
		
		String med = endpointManager.getWSUrl();
		if(med.contains("8071")) {
			return;
		}
		else{
			Mediator.getInstance().setLastAlive();
		}
	}
	
	
	public List<String> myUddiList() {
		UDDINaming uddinn = endpointManager.getUddiNaming();
		List <String> availableSupplierswsURL = new ArrayList<String>();
		try {
			return availableSupplierswsURL = (List<String>) uddinn.list("A57_Supplier%");
		} catch (UDDINamingException e) {
			System.err.println("Caught exception from UDDINaming" + e);
		}
		return availableSupplierswsURL;
	}
	
	public Collection<UDDIRecord> myUddiRecordList() {		
		UDDINaming uddinn = endpointManager.getUddiNaming();
		Collection<UDDIRecord> availableSupplierswsURL = new ArrayList<UDDIRecord>();
		try { 
			availableSupplierswsURL = uddinn.listRecords("A57_Supplier%");
			return availableSupplierswsURL;
		} catch (UDDINamingException e) {
			System.err.println("Caught exception from UDDINaming" + e);
		}
		return availableSupplierswsURL;
	}

	// Main operations -------------------------------------------------------
	
	@Override
	public List<ItemView> getItems(String productId) throws InvalidItemId_Exception {
		
		//Check prod ID
		if (productId == null)
			throwInvalidItemId("Product identifier cannot be null!");
		productId = productId.trim();
		if (productId.length() == 0)
			throwInvalidItemId("Product identifier cannot be empty or whitespace!");
		
		Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
		boolean hasSpecialChar = pattern.matcher(productId).find();
		if (hasSpecialChar)
			throwInvalidItemId("Product identifier must be alphanumeric!");
		
		//Get list of sups
		Collection<UDDIRecord> SuppliersWsURL = myUddiRecordList();
		
		List<ItemView> pricesPerSupplier = new ArrayList<ItemView>();
		try {		
			for (UDDIRecord url : SuppliersWsURL) {
				SupplierClient S = null;
				S = new SupplierClient(url.getUrl());
				
				ProductView pv = S.getProduct(productId);
				if (pv != null) {
					ItemIdView itId = newItemIdView(pv, url.getOrgName());
					ItemView it = newItemView(pv, itId);
					
					pricesPerSupplier.add(it);
				}
			}
			
			//Mudei para o sort ser só depois de a lista estar toda preenchida.
			Collections.sort(pricesPerSupplier, new Comparator<ItemView>() {
				@Override
				public int compare(ItemView i1, ItemView i2) {
					return (new Integer(i1.getPrice())).compareTo(new Integer(i2.getPrice()));
				}
			});

			return pricesPerSupplier;
		} catch (SupplierClientException | BadProductId_Exception e) {
			System.err.println("Caught exception in" + e);
		}
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
			Comparator<ItemView> comparator = Comparator.comparing(ItemView -> ItemView.getItemId().getProductId());
		    comparator = comparator.thenComparing(Comparator.comparing(ItemView -> ItemView.getPrice()));
		    Stream<ItemView> saveStream = save.stream().sorted(comparator);
		    List<ItemView> sortedSave = saveStream.collect(Collectors.toList());
			return sortedSave;
		} catch (SupplierClientException | BadText_Exception e) {			
			System.err.println("Caught exception:" + e);
		}
		return null;
	}
					
	@Override
	public void addToCart(String cartId, ItemIdView itemId, int itemQty) throws InvalidCartId_Exception,
			InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		
		//Cart Check:
		if (cartId == null)
			throwInvalidCartId("Cart Identifier cannot be null!");
		cartId = cartId.trim();
		if (cartId.length() == 0)
			throwInvalidCartId("Cart identifier cannot be empty or whitespace!");
		
		Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
		boolean hasSpecialChar = pattern.matcher(cartId).find();
		if (hasSpecialChar)
			throwInvalidCartId("Cart identifier must be alphanumeric!");
		
		//Product Check
		if (itemId == null)
			throwInvalidItemId("Product identifier cannot be null!");
		String productId = itemId.getProductId();
		if (productId == null)
			throwInvalidItemId("Product identifier cannot be null!");
		String newProductId = productId.trim();
		if (newProductId.length() == 0)
			throwInvalidItemId("Product identifier cannot be empty or whitespace!");
		
		hasSpecialChar = pattern.matcher(newProductId).find();
		if (hasSpecialChar)
			throwInvalidItemId("Product identifier must be alphanumeric!");
		
		//Get supplier!!
		String supId = itemId.getSupplierId();
		if (supId == null)
			throwInvalidItemId("Supplier identifier cannot be null!");
		String trimSupId = supId.trim();
		if (trimSupId.length() == 0)
			throwInvalidItemId("Supplier identifier cannot be empty or whitespace!");
		
		//Quantity Check:
		if (itemQty <= 0)
			throwInvalidQuantity("Product quantity must be positive number!");
		
		
		UDDINaming uddinn = endpointManager.getUddiNaming();
		try {
			
			String url = uddinn.lookup(supId);
			SupplierClient S = null;
			S = new SupplierClient(url);
			
			try {
				
				Cart c = Mediator.getInstance().getCart(cartId);
				if (c == null)
					c = Mediator.getInstance().addNewCart(cartId);
				
				
				ProductView supProd = S.getProduct(productId);
				if(supProd == null)
					throwInvalidItemId("This product does not exist in this supplier!");
				
				int supQuantity = supProd.getQuantity();
				
				//Check final total quantity
				int totalQ;
				if (c.getProduct(productId) == null)
					totalQ = itemQty;
				else
					totalQ = c.getProduct(productId).getQuantity() + itemQty;
				if (totalQ > supQuantity){
					if (c.isCartEmpty())
						Mediator.getInstance().removeCart(c);
					throwNotEnoughItems("Supplier does not have that many items in stock!");
				}
				
				//Setup product for cart!
				int supPrice = supProd.getPrice();
				String desc = supProd.getDesc();
				Item item = new Item(productId, desc, itemQty, supPrice, supId);
				
				//Add to Cart
				c.addProduct(item);
				
				
			} catch (BadProductId_Exception e) {
				System.err.println("Caught exception:" + e);
				throwInvalidItemId("This supplier does not exist!");
			}
		} catch (UDDINamingException | SupplierClientException e) {
			System.err.println("Caught exception:" + e);
		}
		
	}
	
	@Override
	public ShoppingResultView buyCart(String cartId, String creditCardNr)
			throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
		
		//Cart Check:
		if (cartId == null)
			throwInvalidCartId("Cart Identifier cannot be null!");
		String cartIdtrim = cartId.trim();
		if (cartIdtrim.length() == 0)
			throwInvalidCartId("Cart identifier cannot be empty or whitespace!");
			
		Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
		boolean hasSpecialChar = pattern.matcher(cartId).find();
		if (hasSpecialChar)
			throwInvalidCartId("Cart identifier must be alphanumeric!");
		
		if (Mediator.getInstance().getCart(cartId) == null)
			throwInvalidCartId("This cart does not exist!");
		if (Mediator.getInstance().getCart(cartId).isCartEmpty())
			throwEmptyCart("This cart is empty!");
		
		//Card Check
		if (creditCardNr == null)
			throwInvalidCreditCard("Card Identifier cannot be null!");
		String creditCardNrtrim = cartId.trim();
		if (creditCardNrtrim.length() == 0)
			throwInvalidCreditCard("Card identifier cannot be empty or whitespace!");
		
		Pattern cardpattern = Pattern.compile("[^0-9]");
		hasSpecialChar = cardpattern.matcher(creditCardNr).find();
		if (hasSpecialChar || creditCardNr.length() != 16)
			throwInvalidCreditCard("Card identifier must have only 16 numbers!");
		

		try {
			CreditCardClient ccc = new CreditCardClient(cccURL);

			if (ccc.validateNumber(creditCardNr)){	
				
				List<Item> buyCart = Mediator.getInstance().getCart(cartId).getProducts();
				
				SupplierClient S = null;
				UDDINaming uddinn = endpointManager.getUddiNaming();
				
				List<Item> dropped = new ArrayList<Item>();
				List<Item> purchased = new ArrayList<Item>();
				
				for(Item i: buyCart){
					
					String supId = i.getSupplierId();
								
					String url;
					try {
						url = uddinn.lookup(supId);
						S = new SupplierClient(url);
						
						try {
							
							if (S.getProduct(i.getId()) != null){	
								String pId = S.buyProduct(i.getId(), i.getQuantity());	
								if (pId == null)
									dropped.add(i);
								else
									purchased.add(i);
							}
						} catch (BadProductId_Exception | BadQuantity_Exception | InsufficientQuantity_Exception e) {
							System.err.println("Caught exception from Supplier :: " + e);
						}
						
					} catch (UDDINamingException | SupplierClientException e) {
						System.err.println("Caught exception:" + e);
					}
					
				}
				String result;
				if (dropped.isEmpty())
					result = "COMPLETE";
				
				else if (purchased.isEmpty())
					result = "EMPTY";
				
				else
					result = "PARTIAL";
				
				String finalId = Mediator.getInstance().addPurchase(result, purchased, dropped);
				
				ShoppingResultView view = newShoppingResultView(finalId);
				
				return view;
			} else throwInvalidCreditCard("Card doesn't exist");
		} catch (CreditCardClientException e) {
			System.err.println("Caught exception:" + e);
			
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
			System.err.println("Caught exception in UDDINaming:" + e);
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
				System.err.println("Caught exception:" + e);
			}	
		}
		return null;
	}
	
	@Override
	public void clear() {
		Mediator.getInstance().reset();
		

		UDDINaming uddinn = endpointManager.getUddiNaming();
		List<String> availableSupplierswsURL = null;
		try {
			availableSupplierswsURL = (List<String>) uddinn.list("A57_Supplier%");
		} catch (UDDINamingException e) {
			System.err.println("Caught exception in UDDINaming:" + e);
		}
		
		for (String url : availableSupplierswsURL) {
			SupplierClient S = null;
			try {
				S = new SupplierClient(url);
				S.clear();
				
			} catch (SupplierClientException e) {
				System.err.println("Caught exception:" + e);
			}	
		}
	}
	
	@Override
	public List<CartView> listCarts() {
		
		List<CartView> listView = new ArrayList<CartView>();
		String med = endpointManager.getWSUrl();
		
		if (!med.contains("8071"))
			listView = listCartsUp;
		
		for (Cart c: Mediator.getInstance().getCartList()){
			
			//Limpar variaveis antigas para nao repetir informacao
			
			CartView cartView = new CartView();
			cartView.setCartId(c.getcartID());
			List<CartItemView> itemList = cartView.getItems();
			itemList.clear();
			
			for (Item i: c.getProducts()){
				
				CartItemView cartItemView = newCartItemView(i);
				
				itemList.add(cartItemView);
			}
			
			listView.add(cartView);
		}	
		
		if (med.contains("8071"))
			updateCarts(listView);
		
		return listView;
	}
	
	
	@Override
	public List<ShoppingResultView> shopHistory() {
		List<ShoppingResultView> lSRV = new ArrayList<ShoppingResultView>();
		String med = endpointManager.getWSUrl();
		if(!med.contains("8071"))  //se for mediator secundario, vai buscar lista.
			lSRV = shopHistoryUp;
		List<String> orderedPurchaseIds = Mediator.getInstance().getPurchasesIDs();
				
		List<CartItemView> drop = new ArrayList<CartItemView>();
		List<CartItemView> purc = new ArrayList<CartItemView>();
			
		for (String id: orderedPurchaseIds){
					
			drop.clear();
			purc.clear();
			
			ShoppingResultView view = newShoppingResultView(id);
			
			lSRV.add(view);
		}
		if(med.contains("8071"))	//faz update da lista caso seja mediator principal	
				updateShopHistory(lSRV);
		return lSRV; 
	}

	
	public List<ItemView> listItems() {
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
	
	private CartItemView newCartItemView(Item i) {
		CartItemView cartItemView = new CartItemView();
		ItemView itemView = new ItemView();
		ItemIdView itemIdView = new ItemIdView();
		
		itemIdView.setProductId(i.getId());
		itemIdView.setSupplierId(i.getSupplierId());
		
		itemView.setItemId(itemIdView); //Isto e o item c o id do product e o fornecedor
		itemView.setDesc(i.getDescription());
		itemView.setPrice(i.getPrice());
		
		cartItemView.setItem(itemView);
		cartItemView.setQuantity(i.getQuantity());
		
		return cartItemView;
	}
	
	
	private ShoppingResultView newShoppingResultView(String id) {
		
		ShoppingResultView view = new ShoppingResultView();
		view.setId(id);
		
		Purchase pp = Mediator.getInstance().getPurchase(id);
		
		view.setResult(Result.fromValue(pp.getResult()));
		view.setTotalPrice(pp.getFinalPrice());
		
		List<CartItemView> drop = view.getDroppedItems();
		List<CartItemView> purc = view.getPurchasedItems();
		
		for (Item i: pp.getDroppedItemList()){
			CartItemView cartItemView = newCartItemView(i);
			drop.add(cartItemView);
		}
		
		for (Item j: pp.getPurchasedItemList()){
			CartItemView cartItemView = newCartItemView(j);
			purc.add(cartItemView);
		}	
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

	@Override
	public void updateShopHistory(List<ShoppingResultView> ShopResults) {
		shopHistoryUp.clear();
		for (ShoppingResultView shop: ShopResults) {
			shopHistoryUp.add(shop);
		}
	}

	@Override
	public void updateCart(List<CartView> CartViews) {
		listCartsUp.clear();
		for (CartView cart: CartViews) {
			listCartsUp.add(cart);
		}
		
	}
}
