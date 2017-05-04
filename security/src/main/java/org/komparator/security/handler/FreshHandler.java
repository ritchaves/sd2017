package org.komparator.security.handler;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

import org.komparator.security.CryptoUtil;

public class FreshHandler implements SOAPHandler<SOAPMessageContext> {
	
	public static final String CONTEXT_PROPERTY = "my.token";
	
	private Map<String,LocalDateTime> tokenMap = new ConcurrentHashMap<>();

@Override
	public Set<QName> getHeaders() {
		return null;
	}

	/** The handleFault method is invoked for fault message processing. */
	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		System.out.println("Ignoring fault message...");
		return true;
	}

	
        @Override
	public boolean handleMessage(SOAPMessageContext smc) {
        	
        System.out.println("FreshHandler: Handling message.");

    	Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);	
    	
    	try {
			if (outboundElement.booleanValue()) {
				//Se a mensagem estiver a sair
				System.out.println("FreshHandler: Writing header in outbound SOAP message...");

				// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();

				// add header
				SOAPHeader sh = se.getHeader();
				if (sh == null)
					sh = se.addHeader();

				// add header element (name, namespace prefix, namespace)
				Name name = se.createName("tokenHeader", "d", "http://demo");
				SOAPHeaderElement element = sh.addHeaderElement(name);

				String num = CryptoUtil.randomTokenGenerator();
				
				element.addTextNode(num);
			

			} else {
				System.out.println("FreshHandler: Reading header in inbound SOAP message...");

				// get SOAP envelope header
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPHeader sh = se.getHeader();

				// check header
				if (sh == null) {
					System.out.println("FreshHandler: Header not found.");
					return true;
				}

				// get first header element
				Name name = se.createName("myHeader", "d", "http://demo");
				Iterator it = sh.getChildElements(name);
				// check header element
				if (!it.hasNext()) {
					System.out.println("FreshHandler: Header element not found.");
					return true;
				}
				SOAPElement element = (SOAPElement) it.next();

				// get header element value *********************************************************
				String value = element.getValue();
				
				//se nao estiver la
				if (!tokenMap.containsKey(value) ){
					// print received header
					System.out.println("FreshHandler: Header token is fresh!");
					// put header in a property context
					smc.put(CONTEXT_PROPERTY, value);
					// set property scope to application client/server class can
					// access it
					smc.setScope(CONTEXT_PROPERTY, Scope.APPLICATION);
					
					LocalDateTime timestamp = LocalDateTime.now();
					
					tokenMap.put(value, timestamp);
					
					//Limpa tokens com mais de 10 minutos caso haja muitos tokens na lista
					if (tokenMap.size() > 50){
						cleanTokenMap();
					}
					
				}
				else{
	    				System.out.println("FreshHandler: The received message has already been received before - not fresh enough.");
	    				throw new RuntimeException();
	    		}
			
			}
		}  catch (SOAPException se) {
			System.out.println(se);
		}
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
	
	public void cleanTokenMap(){
		LocalDateTime now = LocalDateTime.now();
		for(Map.Entry<String,LocalDateTime> tok : tokenMap.entrySet()){
			
			long minutesBetween = ChronoUnit.MINUTES.between(tok.getValue(), now);
			if((minutesBetween > 15 )) //Se o token tiver mais de 15 minutos, e removido para libertar memoria
				tokenMap.remove(tok.getKey());
		}
	}
	
}
