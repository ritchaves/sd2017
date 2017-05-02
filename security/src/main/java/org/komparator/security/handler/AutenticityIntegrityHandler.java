package org.komparator.security.handler;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.komparator.security.CryptoUtil;

/**
 * This SOAPHandler outputs the contents of inbound and outbound messages.
 */
public class AutenticityIntegrityHandler implements SOAPHandler<SOAPMessageContext> {
	private CryptoUtil securityTools = new CryptoUtil();
	final static String CA_CERTIFICATE = "ca.cer";		//send help- ca? what about the suppliers cer?
	
	final static String KEYSTORE = "A57_Supplier1.jks";  //send help which supplier?
	final static String KEYSTORE_PASSWORD = "k1fFNszN";
	final static String KEY_ALIAS = "t57_mediator";
	final static String KEY_PASSWORD = "k1fFNszN";
	private static final String SIGNATURE_ALGO = "SHA256withRSA";		//send more welp

	
	@Override
	public Set<QName> getHeaders() {
		return null;
	}
	
	
    @Override
	public boolean handleMessage(SOAPMessageContext smc) {
        	Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        	Certificate certificate = securityTools.getX509CertificateFromResource(CA_CERTIFICATE);
        	
        	//msg going out
        	if(outbound) {
        		System.out.println("AutenticityIntegrityHandler: caught outbound SOAP message...");
        		
        		// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				
				byte[] plainBytes;

				PrivateKey privateKey = securityTools.getPrivateKeyFromKeyStoreResource(KEYSTORE,KEYSTORE_PASSWORD.toCharArray(), 
																						KEY_ALIAS, KEY_PASSWORD.toCharArray());
				//byte[] digitalSignature = securityTools.makeDigitalSignature(SIGNATURE_ALGO, privateKey, plainBytes);
				
        		
        	}
        	
        	
        	
        	
        	
        	//msg coming in
        	else {
        		SOAPMessage message = smc.getMessage();
        		
        		boolean result = securityTools.verifySignedCertificate(certificate, Certificate);
        		
        	//	verifyDigitalSignature
        		
        	}
        	
        	
        	
        	
        	
        	
		return true;
	}

	/** The handleFault method is invoked for fault message processing. */
	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		System.out.println("Ignoring fault message...");
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
