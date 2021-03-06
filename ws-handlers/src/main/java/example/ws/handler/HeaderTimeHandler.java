package example.ws.handler;

import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * This SOAPHandler shows how to set/get values from headers in inbound/outbound
 * SOAP messages.
 *
 * A header is created in an outbound message and is read on an inbound message.
 *
 * The value that is read from the header is placed in a SOAP message context
 * property that can be accessed by other handlers or by the application.
 */
public class HeaderTimeHandler implements SOAPHandler<SOAPMessageContext> {

	public static final String CONTEXT_PROPERTY = "my.property";

	//
	// Handler interface implementation
	//

	/**
	 * Gets the header blocks that can be processed by this Handler instance. If
	 * null, processes all.
	 */
	@Override
	public Set<QName> getHeaders() {
		return null;
	}

	/**
	 * The handleMessage method is invoked for normal processing of inbound and
	 * outbound messages.
	 */
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		System.out.println("AddHeaderHandler: Handling message.");

		Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"); //******************************************

		try {
			if (outboundElement.booleanValue()) {
				System.out.println("Writing header in outbound SOAP message...");

				// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();

				// add header
				SOAPHeader sh = se.getHeader();
				if (sh == null)
					sh = se.addHeader();

				// add header element (name, namespace prefix, namespace)
				Name name = se.createName("myHeader", "d", "http://demo");
				SOAPHeaderElement element = sh.addHeaderElement(name);

				// add header element value *************************************************************
				//int value = 22;
				//String valueString = Integer.toString(value);

				LocalDateTime now = LocalDateTime.now();

				element.addTextNode(dtf.format(now));

			} else {
				System.out.println("Reading header in inbound SOAP message...");

				// get SOAP envelope header
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPHeader sh = se.getHeader();

				// check header
				if (sh == null) {
					System.out.println("Header not found.");
					return true;
				}

				// get first header element
				Name name = se.createName("myHeader", "d", "http://demo");
				Iterator it = sh.getChildElements(name);
				// check header element
				if (!it.hasNext()) {
					System.out.println("Header element not found.");
					return true;
				}
				SOAPElement element = (SOAPElement) it.next();

				// get header element value *********************************************************
				String valueString = element.getValue();
				//int value = Integer.parseInt(valueString);
				LocalDateTime value = LocalDateTime.parse(valueString, dtf);

				//check if time is OK
				LocalDateTime endDate = LocalDateTime.now();
				long secondsInDay = ChronoUnit.SECONDS.between(value, endDate);
				    
	    			if (secondsInDay < 3){
	    				// print received header
						System.out.println("Header value (with time) is " + value);

						// put header in a property context
						smc.put(CONTEXT_PROPERTY, value);
						// set property scope to application client/server class can
						// access it
						smc.setScope(CONTEXT_PROPERTY, Scope.APPLICATION);
	    			}
	    			else{
	    				System.out.println("The received message is too old to be accepted.");
	    				throw new RuntimeException();
	    			}
    			
/*
 * deve lançar uma RuntimeException, que vai fazer com que a mensagem  - DONE
 * seja substituída por uma SOAP Fault e que a mensagem volte para o cliente. - Automatic
   O cliente vai depois receber uma SOAPFaultException. - E o erro que ele lanca e exactamente esse!
 * 
 * */			
			
			}
		}  catch (SOAPException se) {
			System.out.println(se);
		}

		return true;
	}

	/** The handleFault method is invoked for fault message processing. */
	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		
		return true;
	}

	/**
	 * Called at the conclusion of a message exchange pattern just prior to the
	 * JAX-WS runtime dispatching a message, fault or exception.
	 */
	@Override
	public void close(MessageContext messageContext) {
		// nothing to clean up
	}

}