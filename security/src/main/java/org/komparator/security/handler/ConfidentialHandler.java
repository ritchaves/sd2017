package org.komparator.security.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
	
	public static final String ENTITY_NAME = "A57_Mediator";
	public static final String OPERATION_NAME = "buyCart";
	final static String CA_CERTIFICATE = "/ca.cer";
	
	private CryptoUtil securityTools = new CryptoUtil();

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
	       
	        /*buscar certificado correcto*/
	        Certificate certificate = CryptoUtil.getX509CertificateFromResource(CA_CERTIFICATE);
	    	String certificateSource = SecurityManager.compareURL(urlSOAP, ENTITY_NAME);
	    	Certificate certificateMediator = SecurityManager.getCertificateFromSource(certificateSource);
			boolean result = CryptoUtil.verifySignedCertificate(certificateMediator, certificate);
	    	PublicKey publicKey = null;
			if (result)
				publicKey = CryptoUtil.getPublicKeyFromCertificate(certificateMediator);
			else
				throw new RuntimeException();
	

        
         //outbound envia -> encripta //inbound recebe ->desencripta
        	if (outboundElement.booleanValue()) {
        		
		        QName svcn = (QName) smc.get(MessageContext.WSDL_SERVICE);  /*Obter nome do serviço e da operacao*/
		        QName opn = (QName) smc.get(MessageContext.WSDL_OPERATION);
		        
		        if(!opn.getLocalPart().equals(OPERATION_NAME)) {return false;}
		        
		        NodeList children = sb.getFirstChild().getChildNodes(); /* Obter argumentos da mensagem*/
		        
		        for (int i = 0; i < children.getLength(); i++) {
		        	Node argument = children.item(i);
		        	if (argument.getNodeName().equals("creditCardNr")) { /*Para cada nó verifica-se se corresponde ao argumento q é preciso*/
		        		String secretArgument = argument.getTextContent();
		        		
		        		//cipher message w symmetric key
		        		byte[] plainBytes = parseBase64Binary(secretArgument);
		        		byte[] cipheredArg = CryptoUtil.asymCipher(plainBytes, publicKey);
		       
		        		String encodedSecretArg = printBase64Binary(cipheredArg);
		        		
		        		argument.setTextContent(encodedSecretArg);
		        		msg.saveChanges();
		        	}
		        }
        	}
        	else {
        		
        	}
	        
        } catch (SOAPException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | CertificateException | IOException es) {
        	System.err.println(es);
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
