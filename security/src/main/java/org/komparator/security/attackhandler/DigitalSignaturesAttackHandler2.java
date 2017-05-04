package org.komparator.security.attackhandler;

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
import static javax.xml.bind.DatatypeConverter.printHexBinary;

import org.komparator.security.CryptoUtil;
import org.komparator.security.SecurityManager;

/**
 * This SOAPHandler outputs the contents of inbound and outbound messages.
 */
public class DigitalSignaturesAttackHandler2 implements SOAPHandler<SOAPMessageContext> {
	
	final static String CA_CERTIFICATE = "/ca.cer";	
	final static String KEYSTORE = "/A57_Mediator.jks";  //to confirm
	final static String KEYSTORE_PASSWORD = "k1fFNszN";
	final static String KEY_ALIAS = "/a57_mediator";
	final static String KEY_PASSWORD = "k1fFNszN";
	private static final String SIGNATURE_ALGO = "SHA256withRSA";	
	private static final String SUPPLIER_ENTITY = "A57_Supplier%";

	
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
        		
	        	//Attack
	        	if(outbound) {
	        		System.out.println("AutenticityIntegrityHandler: caught outbound SOAP message...");
	        		
	        		// get SOAP envelope
					SOAPMessage msg = smc.getMessage();
					SOAPPart sp = msg.getSOAPPart();
					SOAPEnvelope se = sp.getEnvelope();

					
					// add header
					SOAPHeader sh = se.getHeader();
					if (sh == null)
						sh = se.addHeader();
					
					byte[] message = se.getTextContent().getBytes();	
					
					System.out.println("AttackHandler: DigitalSignatures! Attack!");
					for (int i = 0; i < message.length; i++) {
						message[i] = 0;	
					}
					System.out.println(printHexBinary(message));
					System.out.println("      ^");
	
					PrivateKey privateKey = CryptoUtil.getPrivateKeyFromKeyStoreResource(KEYSTORE, KEYSTORE_PASSWORD.toCharArray(), KEY_ALIAS, KEY_PASSWORD.toCharArray());
					byte[] digitalSignature = CryptoUtil.makeDigitalSignature(privateKey, message);
					
					String updatedContent = printBase64Binary(digitalSignature);
					se.setTextContent(updatedContent);
					msg.saveChanges();
					System.out.println("AttackHandler: DigitalSignatures terminating... succeeded");
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


