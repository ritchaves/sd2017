package org.komparator.mediator.ws;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.jws.WebService;
import javax.xml.ws.BindingProvider;


import org.komparator.supplier.ws.BadProductId;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.PurchaseView;
import org.komparator.supplier.ws.SupplierPortType;
import org.komparator.supplier.ws.SupplierService;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;

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
	private SupplierPortType port;
	
	public MediatorPortImpl(MediatorEndpointManager endpointManager) {
		this.endpointManager = endpointManager;
		
	port = null;

	}
	
	
/* SUGESTAO ----------------------------------------------------------	

	UDDINaming uddinn = endpointManager.getUddiNaming();
	private Collection<String> availableSupplierswsURL = uddinn.list("SupplierService");
	
	/** Stub creation and configuration
	private void createStub(wsURL) {
			System.out.println("Creating stub ...");
		Supservice = new SupplierService();
		Support = service.getSupplierPort();

		if (wsURL != null) {
			if (verbose)
				System.out.println("Setting endpoint address ...");
			BindingProvider bindingProvider = (BindingProvider) port;
			Map<String, Object> requestContext = bindingProvider.getRequestContext();
			requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsURL);
		}
	}
	
	A ideia é, errada xD, ir buscar todos os suppliers registados no UDDI. Depois, sempre que quisermos
	aceder a cada supplier, cria-se um "stub" para ele e fazem-se as operações nesse serviço dedicado e porto.
	Na iteração seguinte, usa-se o url seguinte da lista e fazem-se os mesmos passos
*/
	private void createStub(String wsURL) {
		System.out.println("Creating stub ...");
		SupplierService Supservice = new SupplierService();
		port = Supservice.getSupplierPort();

		if (wsURL != null) {	
			BindingProvider bindingProvider = (BindingProvider) port;
			Map<String, Object> requestContext = bindingProvider.getRequestContext();
			requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsURL);
		}
    }	
	
	@Override
	public void clear() {
		//Supplier.getInstance().reset();
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<ItemView> getItems(String productId) throws InvalidItemId_Exception {
		


		return null;

	}

	@Override
	public List<CartView> listCarts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ItemView> searchItems(String descText) throws InvalidText_Exception {
		UDDINaming uddinn = endpointManager.getUddiNaming();
		private Collection<String> availableSupplierswsURL = uddinn.list("A57_Supplier$");

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
		UDDINaming uddinn = endpointManager.getUddiNaming();
		List<String> availableSupplierswsURL = null;
		try {
			availableSupplierswsURL = (List<String>) uddinn.list("A57_Supplier$");
		} catch (UDDINamingException e) {
			//FIXME
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
				// FIXME
			}	
		}
		return null;
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
	public List<Cart> listCarts() {
		return null;
	}
	
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
		view.setItemId(product.getId());
		view.setDesc(product.getDescription());
		view.setPrice(product.getPrice());
		return view;
	}

	private ItemIdView newItemIdView(Product product, SupplierClient supplier) {
		ItemIdView view = new ItemIdView();
		view.setProductId(product.getId());
		view.setSupplierId(supplier.getWsURL());
		return view;
	}
	
	private CartItemView newCartItemView(ItemView item, Product product) {
		CartItemView view = new CartItemView();
		view.setItem(item);
		view.setQuantity(product.getQuantity());
	}
	
	private CartView newCartView(Cart cart) {
		CartView view = new CartView();
		view.setCartId(cart.getId());
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
	
	
}
