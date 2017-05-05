package org.komparator.security.handler;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
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
import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;


import org.komparator.security.CryptoUtil;
import org.komparator.security.SecurityManager;

/**
 * This SOAPHandler outputs the contents of inbound and outbound messages.
 */
public class AutenticityIntegrityHandler implements SOAPHandler<SOAPMessageContext> {
	
	final static String CA_CERTIFICATE = "ca.cer";
	final static String PASSWORD = "k1fFNszN";
	private static final String SIGNATURE_ALGO = "SHA256withRSA";	
	private static final String SUPPLIER_ENTITY = "A57_Supplier%";
	
	private String keystore = "A57_Mediator.jks"; 
	private String key_alias = "a57_mediator";
	
	public static final String CONTEXT_PROPERTY = "my.NAME";
	
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
        	System.out.println("url--- " + urlSoap);
        	
        	try {
        		
        		// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPHeader sh = se.getHeader();
        		
	        	//msg going out
	        	if(outbound) {
	        		
	        		System.out.println("AutenticityIntegrityHandler: caught outbound SOAP message...");
	        		
	        		String receiver = secManager.compareURL(urlSoap,SUPPLIER_ENTITY);
	        		if(receiver.equals(null))
	        			receiver = "A57_Mediator";
	        		
	        		//last minute hack
	        		Name name = se.createName("receiver", "ns", "http://helper");
					SOAPHeaderElement element = sh.addHeaderElement(name);
					element.addTextNode(receiver);
	        		//end of hack
					
					byte[] message = se.getTextContent().getBytes();
					//digest the message with SHA
					byte[] digestedMessage = CryptoUtil.digest(message);
					
					KeyStore ks = CryptoUtil.readKeystoreFromResource(keystore, PASSWORD.toCharArray());
					PrivateKey privateKey = CryptoUtil.getPrivateKeyFromKeyStore(key_alias, PASSWORD.toCharArray(), ks);
					
					System.out.println("Check private key: is it null?? -------> " +(privateKey == null));
					byte[] digitalSignature = CryptoUtil.makeDigitalSignature(privateKey, digestedMessage);
					
					String updatedContent = printBase64Binary(digitalSignature);
					
					//last minute hack
	        		Name nameS = se.createName("signature", "ns", "http://signature");
					SOAPHeaderElement elementS = sh.addHeaderElement(nameS);
					elementS.addTextNode(updatedContent);
	        		//end of hack
					
	        	}
	        	
	
	        	//msg coming in - inbound
	        	else {
	        		System.out.println("AutenticityIntegrityHandler: caught inbound SOAP message...");
	        		
	        		//hack continuation
	        		Name name = se.createName("receiver", "ns", "http://helper");
					Iterator it = sh.getChildElements(name);
					// check header element
					SOAPElement element = (SOAPElement) it.next();
					String myname = element.getValue();
					//end hack
					
					//my other bad hack
					Name nameS = se.createName("signature", "ns", "http://signature");
					Iterator ite = sh.getChildElements(nameS);
					// check header element
					SOAPElement elementS = (SOAPElement) ite.next();
					String signatureHeader = element.getValue();
					//end hack
					
	        		Certificate certificateCA = CryptoUtil.getX509CertificateFromResource(CA_CERTIFICATE);
	        		Certificate certificateReceived = secManager.getCertificateFromSource(myname);
	        	
	        		boolean result = CryptoUtil.verifySignedCertificate(certificateReceived, certificateCA);
	        		
	        		if(!result) {	
	        			//certificated not emmited by CA, discarding msg
	        			System.out.println("AutenticityIntegrityHandler: Certificated was not emited by CA, ignoring this message.");
        				throw new RuntimeException();
	        		}
	        		
	        		else{
						
						PublicKey publicKey = CryptoUtil.getPublicKeyFromCertificate(certificateReceived);
						
						byte[] bytesToVerify = signatureHeader.getBytes();
						byte[] signature = parseBase64Binary(SIGNATURE_ALGO);
	        			boolean verifyDS = CryptoUtil.verifyDigitalSignature(publicKey, bytesToVerify, signature);
	        			
	        			if(!verifyDS) {
	        				System.out.println("AutenticityIntegrityHandler: Message was changed, ignoring it.");
	        				throw new RuntimeException();
	        			}
	        			else
	        				System.out.println("AutenticityIntegrityHandler: inbound SOAP message appears to be OK.");
	        		} 
	        		// put header in a property context
					smc.put(CONTEXT_PROPERTY, myname);
					// set property scope to application client/server class can
					// access it
					smc.setScope(CONTEXT_PROPERTY, Scope.APPLICATION);
	        	}
        	
        	} catch (SOAPException | CertificateException | IOException | NoSuchAlgorithmException 
        			| UnrecoverableKeyException | KeyStoreException | InvalidKeyException | SignatureException se) {
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
	
	public String getPath(String keystoreName){
		if (keystoreName.contains("Mediator"))
			return "/mediator-ws/src/main/resources/A57_Mediator.jks";
		else{
			return "/supplier-ws/src/main/resources/"+ keystoreName +".jks";
		}
		
	}
	
}
