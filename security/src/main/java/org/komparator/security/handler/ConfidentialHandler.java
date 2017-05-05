package org.komparator.security.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Set;


import static javax.xml.bind.DatatypeConverter.printBase64Binary;
import static javax.xml.bind.DatatypeConverter.parseBase64Binary;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
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

import org.komparator.security.CryptoUtil;
import org.komparator.security.SecurityManager;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This SOAPHandler outputs the contents of inbound and outbound messages.
 */
public class ConfidentialHandler implements SOAPHandler<SOAPMessageContext> {
	
	final static String KEYSTORE = "/A57_Mediator.jks";  
	final static String KEYSTORE_PASSWORD = "k1fFNszN";
	final static String KEY_ALIAS = "/a57_mediator";
	final static String KEY_PASSWORD = "k1fFNszN";
	public static final String ENTITY_NAME = "A57_Mediator";
	public static final String OPERATION_NAME = "buyCart";
	final static String CA_CERTIFICATE = "ca.cer";
	
	private SecurityManager secManager = new SecurityManager();
	
        @Override
	public Set<QName> getHeaders() {
		return null;
	}

        @Override
	public boolean handleMessage(SOAPMessageContext smc) {
        System.out.println("ConfidentialHandler");
        
        Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        
        try {
        	
	        SOAPMessage msg = smc.getMessage();
	        SOAPPart sp = msg.getSOAPPart();    /*Obter Conteudo da mensagem SOAP*/
	        SOAPEnvelope se = sp.getEnvelope();
	        SOAPBody sb = se.getBody();
	        SOAPHeader sh = se.getHeader();
	        if (sh == null) { sh = se.addHeader(); }
	        
	        String urlSOAP = (String) smc.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
	       
	        /*Obter nome do serviço e da operacao*/
//			QName svcn = (QName) smc.get(MessageContext.WSDL_SERVICE);  
	        QName opn = (QName) smc.get(MessageContext.WSDL_OPERATION);
	        if(!opn.getLocalPart().equals(OPERATION_NAME)) {return true;}
	        
	        /* Obter argumentos da mensagem*/
	        NodeList children = sb.getFirstChild().getChildNodes(); 
	        
	        
         //outbound envia -> encripta //inbound recebe ->desencripta
        	if (outboundElement.booleanValue()) {
        		System.out.println("0");
    	        /*buscar certificado correcto*/
    	        Certificate certificate = CryptoUtil.getX509CertificateFromResource(CA_CERTIFICATE);
    	        System.out.println("1");
    	    	String certificateSource = secManager.compareURL(urlSOAP, ENTITY_NAME);
    	    	Certificate certificateMediator = secManager.getCertificateFromSource(certificateSource);
    	    	System.out.println("2");
    			boolean result = CryptoUtil.verifySignedCertificate(certificateMediator, certificate);
    			System.out.println("3");
    	    	PublicKey publicKey = null;
    			if (result)
    				publicKey = CryptoUtil.getPublicKeyFromCertificate(certificateMediator);
    			else
    				throw new RuntimeException();
    			
    			
        		for (int i = 0; i < children.getLength(); i++) {
		        	Node argument = children.item(i);
		        	if (argument.getNodeName().equals("creditCardNr")) { /*Para cada nó verifica-se se corresponde ao argumento q é preciso*/
		        		String secretArgument = argument.getTextContent();
		        		
		        		//cipher message w publickey
		        		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		        		byteOut.write(secretArgument.getBytes());
//		        		byte[] plainBytes = parseBase64Binary(secretArgument);
		        		byte[] cipheredArg = CryptoUtil.asymCipher(byteOut.toByteArray(), publicKey);
		       
		        		String encodedSecretArg = printBase64Binary(cipheredArg);
		        		
		        		argument.setTextContent(encodedSecretArg);
		        		msg.saveChanges();
		        	}
		        }
        	}
        	else {
		        for (int i = 0; i < children.getLength(); i++) {
		        	Node argument = children.item(i);
		        	if (argument.getNodeName().equals("creditCardNr")) { 
		        		String secretArgument = argument.getTextContent();
		        		
		        		//decipher message w privateKey
		        		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		        		byteOut.write(secretArgument.getBytes());
		        		//byte[] plainBytes = parseBase64Binary(secretArgument);
		        		PrivateKey privateKey = CryptoUtil.getPrivateKeyFromKeyStoreResource(KEYSTORE, KEYSTORE_PASSWORD.toCharArray(), KEY_ALIAS, KEY_PASSWORD.toCharArray());
		        		byte[] decipheredBytes = CryptoUtil.asymDecipher(byteOut.toByteArray(), privateKey);
		        		
		        		String encodedSecretArg = printBase64Binary(decipheredBytes);
		        		argument.setTextContent(encodedSecretArg);
		        		msg.saveChanges();
		        	}
		        }
        	}
        } catch (SOAPException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | 
        			BadPaddingException | CertificateException | IOException | UnrecoverableKeyException | KeyStoreException es) {
        	System.err.println("ConfidentialHandler: " + es);
        }
		return true;
	}

    /** The handleFault method is invoked for fault message processing. */
    @Override
    public boolean handleFault(SOAPMessageContext smc) {
    	System.out.println("ConfidentialHandler: Ignoring fault message...");
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
