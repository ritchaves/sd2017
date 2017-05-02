package org.komparator.security.handler;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.komparator.security.CryptoUtil;

/**
 * This SOAPHandler outputs the contents of inbound and outbound messages.
 * (que é basicamente evitar que um atacante envie a msg 1000 vezes)
				Fazer no CryptoUtil uma funcao para gerar numeros realmente aleatorios (procurar nos exemplos do lab, está lá ja feito)
				if (mensagem a sair){
										get Token
										addToken to header
										enviar msg
									}
									else{
										get Token
										if (token is old)
											reject
										else
											store Token
											accept msg
									}
 */
public class FreshHandler implements SOAPHandler<SOAPMessageContext> {
	
	public static final String CONTEXT_PROPERTY = "my.token";
	
	private CryptoUtil securityTools = new CryptoUtil();

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
    	String endpointAddress = (String) smc.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
    	
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

				String num = securityTools.randomTokenGenerator();
				element.addTextNode(num);
				
				//TODO Guardar o token e o autor correspondente numa lista/hashmap para verificar a origem da 
				//msg e elemento a false para mostrar que ainda nao foi lida vez nenhuma
				
				

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
				
				//check if token is OK
				/*Averiguar a tal lista de tokens e autores. Verificar se o autor é igual ao endpoint e se está a true*/
				
				Boolean alreadySent = false; //FIXME
				    
	    			if (!alreadySent){
	    				// print received header
						System.out.println("FreshHandler: Header token is fresh!");
						// put header in a property context
						smc.put(CONTEXT_PROPERTY, value);
						// set property scope to application client/server class can
						// access it
						smc.setScope(CONTEXT_PROPERTY, Scope.APPLICATION);
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

	
}
