package org.komparator.security.handler;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
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

import com.sun.xml.ws.developer.JAXWSProperties;

/**
 * This SOAPHandler outputs the contents of inbound and outbound messages.
 */
public class AutenticityIntegrityHandler implements SOAPHandler<SOAPMessageContext> {
	
	final static String CA_CERTIFICATE = "ca.cer";	
	final static String KEYSTORE = "/A57_Mediator.jks";  //to confirm
	final static String KEYSTORE_PASSWORD = "k1fFNszN";
	final static String KEY_ALIAS = "/a57_mediator";
	final static String KEY_PASSWORD = "k1fFNszN";
	private static final String SIGNATURE_ALGO = "SHA256withRSA";	
	private static final String SUPPLIER_ENTITY = "A57_Supplier%";
	
	private SecurityManager secManager = new SecurityManager();

	
	@Override
	public Set<QName> getHeaders() {
		return null;
	}
	
	
    @Override
	public boolean handleMessage(SOAPMessageContext smc) {
    		System.out.println("AutenticityIntegrityHandler");
    		
        	Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        	String urlSoap = (String) smc.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        	System.out.println("FIRST URL --------------------> " + urlSoap);
        	
        	try {
        		
        		// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				
        		Certificate certificateCA = CryptoUtil.getX509CertificateFromResource(CA_CERTIFICATE);
        		
	        	//msg going out
	        	if(outbound) {
	        		System.out.println("AutenticityIntegrityHandler: caught outbound SOAP message...");
					
					byte[] message = se.getTextContent().getBytes();
	
					PrivateKey privateKey = CryptoUtil.getPrivateKeyFromKeyStoreResource(KEYSTORE, KEYSTORE_PASSWORD.toCharArray(), KEY_ALIAS, KEY_PASSWORD.toCharArray());
					//digest the message with SHA
					byte[] digestedMessage = CryptoUtil.digest(message);
					byte[] digitalSignature = CryptoUtil.makeDigitalSignature(privateKey, digestedMessage);
					
					String updatedContent = printBase64Binary(digitalSignature);
					se.setTextContent(updatedContent);
					msg.saveChanges();	 		
	        	}
	        	
	
	        	//msg coming in - inbound
	        	else {
	        		System.out.println("AutenticityIntegrityHandler: caught inbound SOAP message...");
	        		
	        		//String urlSoap = (String) smc.get(JAXWSProperties.HTTP_REQUEST_URL);
	        		//System.out.println("DEBUG --------------------> " + urlSoap);
	        		//acess ca to get certificate
	        		Certificate certificateReceived = secManager.getCertificateFromSource(secManager.compareURL(urlSoap, SUPPLIER_ENTITY));        		
	        		boolean result = CryptoUtil.verifySignedCertificate(certificateReceived, certificateCA);
	        		
	        		if(!result) {	
	        			//certificated not emmited by CA, discarding msg
	        			System.out.println("AutenticityIntegrityHandler: Certificated was not emited by CA, ignoring this message.");
        				throw new RuntimeException();
	        		}
	        		
	        		else{
						
						PublicKey publicKey = CryptoUtil.getPublicKeyFromCertificate(certificateReceived);
						
						byte[] bytesToVerify = se.getTextContent().getBytes();
						byte[] signature = parseBase64Binary(SIGNATURE_ALGO);
	        			boolean verifyDS = CryptoUtil.verifyDigitalSignature(publicKey, bytesToVerify, signature);
	        			
	        			if(!verifyDS) {
	        				System.out.println("AutenticityIntegrityHandler: Message was changed, ignoring it.");
	        				throw new RuntimeException();
	        			}
	        			else
	        				System.out.println("AutenticityIntegrityHandler: inbound SOAP message appears to be OK.");
	        		}       		
	        	}
        	
        	} catch (SOAPException se) {
        		System.err.println("AutenticityIntegrityHandler: " + se);
        	} catch (CertificateException se) {
        		System.err.println("AutenticityIntegrityHandler: " + se);
			} catch (IOException se) {
				System.err.println("AutenticityIntegrityHandler: " + se);
			} catch (InvalidKeyException se) {
				System.err.println("AutenticityIntegrityHandler: " + se);
			} catch (NoSuchAlgorithmException se) {
				System.err.println("AutenticityIntegrityHandler: " + se);
			} catch (SignatureException se) {
				System.err.println("AutenticityIntegrityHandler: " + se);
			} catch (UnrecoverableKeyException se) {
				System.err.println("AutenticityIntegrityHandler: " + se);
			} catch (KeyStoreException se) {
				System.err.println("AutenticityIntegrityHandler: " + se);
			} catch (Exception se) {
				System.err.println("AutenticityIntegrityHandler: " + se);
			}
		return true;
	}

	/** The handleFault method is invoked for fault message processing. */
	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		System.out.println("AutenticityIntegrityHandler: Ignoring fault message...");
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
