package org.komparator.security.handler;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;


import org.komparator.security.CryptoUtil;
import org.komparator.security.SecurityManager;

/**
 * This SOAPHandler outputs the contents of inbound and outbound messages.
 */
public class AutenticityIntegrityHandler implements SOAPHandler<SOAPMessageContext> {
	
	final static String CA_CERTIFICATE = "/ca.cer";	
	final static String KEYSTORE = "/A57_Mediator.jks";  //send help -which supplier?
	final static String KEYSTORE_PASSWORD = "k1fFNszN";
	final static String KEY_ALIAS = "/a57_mediator";
	final static String KEY_PASSWORD = "k1fFNszN";
	private static final String SIGNATURE_ALGO = "SHA256withRSA";	
	private static String SUPPLIER_ENTITY = "A57_Supplier%";

	
	@Override
	public Set<QName> getHeaders() {
		return null;
	}
	
	
    @Override
	public boolean handleMessage(SOAPMessageContext smc) {
        	Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        	String urlSoap = (String) smc.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        	
        	try {
        		Certificate certificateCA = CryptoUtil.getX509CertificateFromResource(CA_CERTIFICATE);
        		
	        	//msg going out
	        	if(outbound) {
	        		System.out.println("AutenticityIntegrityHandler: caught outbound SOAP message...");
	        		
	        		// get SOAP envelope
					SOAPMessage msg = smc.getMessage();
					SOAPPart sp = msg.getSOAPPart();
					SOAPEnvelope se = sp.getEnvelope();
					//to make digital signature- TODO. CHANGE TO ENVELOPE NOT BODY,
					SOAPBody sb = se.getBody();
					
					// add header
					SOAPHeader sh = se.getHeader();
					if (sh == null)
						sh = se.addHeader();
					
					
					byte[] plainBytes = sb.getTextContent().getBytes();	
					//CA not needed for this case besides ca.cer? 
	
					PrivateKey privateKey = CryptoUtil.getPrivateKeyFromKeyStoreResource(KEYSTORE, KEYSTORE_PASSWORD.toCharArray(), KEY_ALIAS, KEY_PASSWORD.toCharArray());
					byte[] digitalSignature = CryptoUtil.makeDigitalSignature(privateKey, plainBytes);
					
					//how to add digitalsignature? not sure if im very smart or wat wat this is all wrong
					String updatedContent = printBase64Binary(digitalSignature);
					sb.setTextContent(updatedContent);
					msg.saveChanges();	 		
	        	}
	        	
	
	        	//msg coming in - inbound
	        	else {
	        		
	        		//acess ca to get certificate
	        		Certificate certificateReceived = SecurityManager.getCertificateFromSource(SecurityManager.compareURL(urlSoap, SUPPLIER_ENTITY));        		
	        		boolean result = CryptoUtil.verifySignedCertificate(certificateReceived, certificateCA);
	        		
	        		if(!result) {	
	        			//certificated not emmited by CA, discarding msg
	        			System.out.println("AutenticityIntegrityHandler: Certificated was not emited by CA, ignoring this message.");
        				throw new RuntimeException();
	        		}
	        		
	        		else {
	        			SOAPMessage msg = smc.getMessage();
						SOAPPart sp = msg.getSOAPPart();
						SOAPEnvelope se = sp.getEnvelope();
						//to make digital signature-TODO CHANGE TO ENVELOPE
						SOAPBody sb = se.getBody();
						
						// add header
						SOAPHeader sh = se.getHeader();
						if (sh == null)
							sh = se.addHeader();
						
						//SEND MORE WELP- which certificated? received? how to get it?
						PublicKey publicKey = CryptoUtil.getPublicKeyFromCertificate(certificateReceived);
						
						byte[] bytesToVerify = sb.getTextContent().getBytes();
						byte[] signature = parseBase64Binary("SIGNATURE_ALGO");
	        			boolean verifyDS = CryptoUtil.verifyDigitalSignature(publicKey, bytesToVerify, signature);
	        			
	        			if(!verifyDS) {
	        				System.out.println("AutenticityIntegrityHandler: Message was changed, ignoring it.");
	        				//TODO- what else?
	        				throw new RuntimeException();
	        			}
	        			
	        			//forgetting anything else?
	        		}
	        		
	        	}
        	
        	} catch (SOAPException se) {
        		System.out.println(se);
        	} catch (CertificateException se) {
				System.out.println(se);
			} catch (IOException se) {
				System.out.println(se);
			} catch (InvalidKeyException se) {
				System.out.println(se);
			} catch (NoSuchAlgorithmException se) {
				System.out.println(se);
			} catch (SignatureException se) {
				System.out.println(se);
			} catch (UnrecoverableKeyException se) {
				System.out.println(se);
			} catch (KeyStoreException se) {
				System.out.println(se);
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
