package org.komparator.security.attackhandler;


import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;


/*
 * Este ataque consiste em alterar os tokens das mensagens de saida para um token permanente para provar que tokens iguais nao sao aceites
 * */
public class AntiFreshnessAttack implements SOAPHandler<SOAPMessageContext> {
	
	private String existingToken = "this-is-a-bad-token-deal-with-it";

	public static final String CONTEXT_PROPERTY = "my.token";
	
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		System.out.println("Initiating an attack to your freshness...");
		Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		
		try {
    		// get SOAP envelope
			SOAPMessage msg = smc.getMessage();
			SOAPPart sp = msg.getSOAPPart();
			SOAPEnvelope se = sp.getEnvelope();
			SOAPHeader sh = se.getHeader();
			
			if (outboundElement.booleanValue()) {
				//Se a mensagem estiver a sair		
				
				Name name = se.createName("tokenHeader", "ns", "http://komparator");
				Iterator it = sh.getChildElements(name);
				it.next();
				it.remove();
				
				SOAPHeaderElement newelement = sh.addHeaderElement(name);
				
				newelement.addTextNode(existingToken);
			}

		}  catch (SOAPException se) {
				System.err.println("Freshness Attack: "+ se);
		}
		return true;
		
	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void close(MessageContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<QName> getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}
