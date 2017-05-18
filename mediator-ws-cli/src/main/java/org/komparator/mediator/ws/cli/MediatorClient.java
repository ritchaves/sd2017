package org.komparator.mediator.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

//import org.komparator.mediator.ws.CartView;
//import org.komparator.mediator.ws.EmptyCart_Exception;
//import org.komparator.mediator.ws.InvalidCartId_Exception;
//import org.komparator.mediator.ws.InvalidCreditCard_Exception;
//import org.komparator.mediator.ws.InvalidItemId_Exception;
//import org.komparator.mediator.ws.InvalidQuantity_Exception;
//import org.komparator.mediator.ws.InvalidText_Exception;
//import org.komparator.mediator.ws.ItemIdView;
//import org.komparator.mediator.ws.ItemView;
//import org.komparator.mediator.ws.MediatorPortType;
//import org.komparator.mediator.ws.NotEnoughItems_Exception;
//import org.komparator.mediator.ws.ShoppingResultView;

// TODO uncomment after generate-sources
import org.komparator.mediator.ws.*;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;


/**
 * Client.
 *
 * Adds easier endpoint address configuration and 
 * UDDI lookup capability to the PortType generated by wsimport.
 */
public class MediatorClient implements MediatorPortType{
    // TODO uncomment after generate-sources 
    //implements MediatorPortType {

private static final int TIME_OUT = 5000;

	// TODO uncomment after generate-sources
    // /** WS service */
     MediatorService service = null;

// TODO uncomment after generate-sources
    // /** WS port (port type is the interface, port is the implementation) */
     MediatorPortType port = null;

    /** UDDI server URL */
    private String uddiURL = null;

    /** WS name */
    private String wsName = null;

    /** WS endpoint address */
    private String wsURL = null; // default value is defined inside WSDL

    public String getWsURL() {
        return wsURL;
    }

    /** output option **/
    private boolean verbose = false;

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /** constructor with provided web service URL */
    public MediatorClient(String wsURL) throws MediatorClientException {
        this.wsURL = wsURL;
        createStub();
    }

    /** constructor with provided UDDI location and name */
    public MediatorClient(String uddiURL, String wsName) throws MediatorClientException {
        this.uddiURL = uddiURL;
        this.wsName = wsName;
        uddiLookup();
        createStub();
    }

    /** UDDI lookup */
    private void uddiLookup() throws MediatorClientException {
        try {
            if (verbose)
                System.out.printf("Contacting UDDI at %s%n", uddiURL);
            UDDINaming uddiNaming = new UDDINaming(uddiURL);

            if (verbose)
                System.out.printf("Looking for '%s'%n", wsName);
            wsURL = uddiNaming.lookup(wsName);

        } catch (Exception e) {
            String msg = String.format("Client failed lookup on UDDI at %s!",
                    uddiURL);
            throw new MediatorClientException(msg, e);
        }

        if (wsURL == null) {
            String msg = String.format(
                    "Service with name %s not found on UDDI at %s", wsName,
                    uddiURL);
            throw new MediatorClientException(msg);
        }
    }

    /** Stub creation and configuration */
    private void createStub() {
        if (verbose)
            System.out.println("Creating stub ...");
// TODO uncomment after generate-sources
         service = new MediatorService();
         port = service.getMediatorPort();

        if (wsURL != null) {
            if (verbose)
                System.out.println("Setting endpoint address ...");
            BindingProvider bindingProvider = (BindingProvider) port;
            Map<String, Object> requestContext = bindingProvider
                    .getRequestContext();
            requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsURL);
            
            
            //FIXME - fixed?
            int receiveTimeout = TIME_OUT;
            
            final List<String> RECV_TIME_PROPS = new ArrayList<String>();
            RECV_TIME_PROPS.add("com.sun.xml.ws.request.timeout");
            RECV_TIME_PROPS.add("com.sun.xml.internal.ws.request.timeout");
            RECV_TIME_PROPS.add("javax.xml.ws.client.receiveTimeout");
            
            for  (String propName: RECV_TIME_PROPS)
            	requestContext.put(propName, receiveTimeout);
            System.out.printf("Set receive timeout to %d milliseconds%n", receiveTimeout);
        }
    }
    
	//I was here!!
    @Override
	public void imAlive() {
		port.imAlive();
	}
    
    @Override
	public void clear() {
		port.clear();
	}

	@Override
	public List<ItemView> getItems(String productId) throws InvalidItemId_Exception {
		try {
			return port.getItems(productId);
		}
		catch(WebServiceException wse) {
            System.out.println("Caught: " + wse);
            Throwable cause = wse.getCause();
            if (cause != null && cause instanceof SocketTimeoutException) {
                System.out.println("The cause was a timeout exception: " + cause);
                //semantic - at most once
                return port.getItems(productId);
            }
            else throw wse;
        }
	}

	@Override
	public List<CartView> listCarts() {
		return port.listCarts();
	}

	@Override
	public List<ItemView> searchItems(String descText) throws InvalidText_Exception {
		try {
			return port.searchItems(descText);
		}
		catch(WebServiceException wse) {
            System.out.println("Caught: " + wse);
            Throwable cause = wse.getCause();
            if (cause != null && (cause instanceof SocketTimeoutException || cause instanceof java.net.ConnectException)) {
                System.out.println("The cause was a timeout exception: " + cause);
            }
        }
		return null;
	}

	@Override
	public ShoppingResultView buyCart(String cartId, String creditCardNr)
			throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
		try {
			return port.buyCart(cartId, creditCardNr);
		}
		catch(WebServiceException wse) {
            System.out.println("Caught: " + wse);
            Throwable cause = wse.getCause();
            if (cause != null && cause instanceof SocketTimeoutException) {
                System.out.println("The cause was a timeout exception: " + cause);
            }
        }
		return null;
	}

	@Override
	public void addToCart(String cartId, ItemIdView itemId, int itemQty) throws InvalidCartId_Exception,
			InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		try {
			port.addToCart(cartId, itemId, itemQty);
		}
		catch(WebServiceException wse) {
            System.out.println("Caught: " + wse);
            Throwable cause = wse.getCause();
            if (cause != null && cause instanceof SocketTimeoutException) {
                System.out.println("The cause was a timeout exception: " + cause);
            }
        }		
	}

	@Override
	public String ping(String arg0) {
		return port.ping(arg0);
	}

	@Override
	public List<ShoppingResultView> shopHistory() {
		return port.shopHistory();
	}

	@Override
	public void updateShopHistory() {
		port.updateShopHistory();
		
	}

	@Override
	public void updateCart() {
		port.updateCart();
	}

}