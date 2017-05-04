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


public class CertificateAttackHandler implements SOAPHandler<SOAPMessageContext>  {

		
	final static String CA_CERTIFICATE = "/fake.cer";
		
		@Override
		public Set<QName> getHeaders() {
			return null;
		}
		
		
	    @Override
		public boolean handleMessage(SOAPMessageContext smc) {
	        	Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
	        	String urlSoap = (String) smc.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
	        	
	        	try {
	        		
	        		//Attack
		        	if(outbound) {
		        		
		        		// get SOAP envelope
						SOAPMessage msg = smc.getMessage();
						SOAPPart sp = msg.getSOAPPart();
						SOAPEnvelope se = sp.getEnvelope();

						
						// add header
						SOAPHeader sh = se.getHeader();
						if (sh == null)
							sh = se.addHeader();
						
						Certificate certificateCA = CryptoUtil.getX509CertificateFromResource(CA_CERTIFICATE);
		        		PublicKey publicKey = CryptoUtil.getPublicKeyFromCertificate(certificateCA);

						
		        	}
		        	
	        	} catch (SOAPException se) {
	        		System.out.println(se);
	        	} catch (CertificateException se) {
					System.out.println(se);
				} catch (IOException e) {
					System.out.println(e);
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



