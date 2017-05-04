//package org.komparator.security.handler;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.security.PublicKey;
//import java.security.cert.Certificate;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Set;
//
//
//import static javax.xml.bind.DatatypeConverter.printBase64Binary;
//import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
//
//import javax.xml.namespace.QName;
//import javax.xml.soap.SOAPBody;
//import javax.xml.soap.SOAPEnvelope;
//import javax.xml.soap.SOAPException;
//import javax.xml.soap.SOAPHeader;
//import javax.xml.soap.SOAPMessage;
//import javax.xml.soap.SOAPPart;
//import javax.xml.ws.BindingProvider;
//import javax.xml.ws.handler.MessageContext;
//import javax.xml.ws.handler.soap.SOAPHandler;
//import javax.xml.ws.handler.soap.SOAPMessageContext;
//
//import org.komparator.security.CryptoUtil;
//import org.komparator.mediator.*;
//
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//
///**
// * This SOAPHandler outputs the contents of inbound and outbound messages.
// */
//public class ConfidentialHandler implements SOAPHandler<SOAPMessageContext> {
//	
//	public static final String OPERATION_NAME = "BuyCart";
//	final static String CA_CERTIFICATE = "/ca.cer";
//	
//	private CryptoUtil securityTools = new CryptoUtil();
//
//        @Override
//	public Set<QName> getHeaders() {
//		return null;
//	}
//
//        @Override
//	public boolean handleMessage(SOAPMessageContext smc) {
//        System.out.println("ConfidentialHandler");
//        
//        Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
//        Certificate certificate = securityTools.getX509CertificateFromResource(CA_CERTIFICATE);
//		boolean result = CryptoUtil.verifySignedCertificate(certificateReceived, certificate);
//
//
//        
//        try {
//        	if (outboundElement.booleanValue()) {
//	        	PublicKey publicKey = securityTools.getPublicKeyFromCertificate(certificate);
//	        	
//		        SOAPMessage msg = smc.getMessage();
//		        SOAPPart sp = msg.getSOAPPart();    /*Obter Conteudo da mensagem SOAP*/
//		        SOAPEnvelope se = sp.getEnvelope();
//		        SOAPBody sb = se.getBody();
//		        SOAPHeader sh = se.getHeader();
//		        if (sh == null) { sh = se.addHeader(); }
//		        
//		        QName svcn = (QName) smc.get(MessageContext.WSDL_SERVICE);  /*Obter nome do serviço e da operacao*/
//		        QName opn = (QName) smc.get(MessageContext.WSDL_OPERATION);
//		        
//		        if(!opn.getLocalPart().equals()) {return false;}
//		        
//		        NodeList children = sb.getFirstChild().getChildNodes(); /* Obter argumentos da mensagem*/
//		        
//		        for (int i = 0; i < children.getLength(); i++) {
//		        	Node argument = children.item(i);
//		        	if (argument.getNodeName().equals(/*CartId*/)) { /*Para cada nó verifica-se se corresponde ao argumento q é preciso*/
//		        		String secretArgument = argument.getTextContent();
//		        		
//		        		//cipher message w symmetric key
//		        		byte[] plainBytes = parseBase64Binary(secretArgument);
//		        		byte[] cipheredArg = securityTools.asymCipher(plainBytes, publicKey);
//		       
//		        		String encodedSecretArg = printBase64Binary(cipheredArg);
//		        		
//		        		argument.setTextContent(encodedSecretArg);
//		        		msg.saveChanges();
//		        	}
//		        }
//        	}
//        	else {
//        		
//        	}
//	        
//        } catch (SOAPException se) {
//        	System.out.println("Erro");
//        }
//		return true;
//	}
//
//    /** The handleFault method is invoked for fault message processing. */
//    @Override
//    public boolean handleFault(SOAPMessageContext smc) {
//    	System.out.println("Ignoring fault message...");
//    	return true;
//    }
//    	
//	/**
//	 * Called at the conclusion of a message exchange pattern just prior to the
//	 * JAX-WS runtime dispatching a message, fault or exception.
//	 */
//	@Override
//	public void close(MessageContext messageContext) {
//		// nothing to clean up
//	}
//	
//}
