package org.komparator.security.handler;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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

public class AtMostOnceHandler implements SOAPHandler<SOAPMessageContext> {
	
	public static final String CONTEXT_PROPERTY = "my.request";
	private AtomicInteger requestId = new AtomicInteger(0);
	
	private Map<String,LocalDateTime> tokenMap = new ConcurrentHashMap<>(); //<String, Return>
	
	private String generateRequestId() {
		// relying on AtomicInteger to make sure assigned number is unique
		int rId = requestId.incrementAndGet();
		return Integer.toString(rId);
	}

@Override
	public Set<QName> getHeaders() {
		return null;
	}

	/** The handleFault method is invoked for fault message processing. */
	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		System.out.println("AtMostOnceHandler: Ignoring fault message...");
		return true;
	}

	
        @Override
	public boolean handleMessage(SOAPMessageContext smc) {
        	
        System.out.println("AtMostOnceHandler:");

    	Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
    	
    	try {
    		// get SOAP envelope
			SOAPMessage msg = smc.getMessage();
			SOAPPart sp = msg.getSOAPPart();
			SOAPEnvelope se = sp.getEnvelope();
			SOAPHeader sh = se.getHeader();
			
			if (outboundElement.booleanValue()) {
				//Se a mensagem estiver a sair
				System.out.println("AtMostOnceHandler: Writing header in outbound SOAP message...");
				
				if (sh == null)
					sh = se.addHeader();
				
				// add header element (name, namespace prefix, namespace)
				Name name = se.createName("RequestId", "ns", "http://komparator");
				
				SOAPHeaderElement element = sh.addHeaderElement(name);
				String newId = generateRequestId();
				element.addTextNode(newId);
				//add SoapBody content to hash as return? FIXME
				
				
				System.out.println("AtMostOnceHandler: Sending message... ");
			

			} else {
				System.out.println("AtMostOnceHandler: Reading header in inbound SOAP message...");

				// check header
				if (sh == null) {
					System.out.println("AtMostOnceHandler: Header not found!");
					return true;
				}

				// get first header element
				Name name = se.createName("tokenHeader", "ns", "http://komparator");
				Iterator it = sh.getChildElements(name);
				// check header element
				if (!it.hasNext()) {
					System.out.println("AtMostOnceHandler: Header element not found.");
					return true;
				}
				SOAPElement element = (SOAPElement) it.next();

				// get header element value *********************************************************
				String value = element.getValue();
				
				/* FIXME
				if (!tokenMap.containsKey(value) ){
					// print received header
					System.out.println("AtMostOnceHandler: Header token is fresh!");
				
					LocalDateTime timestamp = LocalDateTime.now();
					
					tokenMap.put(value, timestamp);
					
					//Limpa tokens com mais de 10 minutos caso haja muitos tokens na lista
					if (tokenMap.size() > 50){
						cleanTokenMap();
					}

					// put header in a property context
					smc.put(CONTEXT_PROPERTY, CryptoUtil.randomTokenGenerator());
					// set property scope to application client/server class can
					// access it
					smc.setScope(CONTEXT_PROPERTY, Scope.APPLICATION);
					
				}
				else{
					
	    				System.out.println("AtMostOnceHandler: The received message has already been received before - not fresh enough.");
	    				throw new RuntimeException();
	    		}
	    		*/
			
			}
		}  catch (SOAPException se) {
			System.err.println("AtMostOnceHandler: "+ se);
			throw new RuntimeException();
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
