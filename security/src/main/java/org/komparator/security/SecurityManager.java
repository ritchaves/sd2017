package org.komparator.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import pt.ulisboa.tecnico.sdis.ws.cli.CAClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClientException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;


public class SecurityManager {
	
	private String uddiURL = "http://a57:k1fFNszN@uddi.sd.rnl.tecnico.ulisboa.pt:9090/";
	
	private String caURL = "http://sec.sd.rnl.tecnico.ulisboa.pt:8081/ca";
	
	private static UDDINaming uddiNaming;
	
	static CAClient caClient = null;
	
	public SecurityManager() {
		try {
			uddiNaming = new UDDINaming(uddiURL);
		} catch (UDDINamingException e) {
			System.err.println("Security Manager - Not an available uddi url.");
		}
		try {
			caClient = new CAClient(caURL);
		} catch (CAClientException e) {
			System.err.println("Security Manager - Nor an available CA url");
		} 
	}
	
	public String compareURL(String urlSOAP, String entity) {
		Collection<UDDIRecord> availableSupplierswsURL = new ArrayList<UDDIRecord>();
		try { 
			availableSupplierswsURL = uddiNaming.listRecords(entity);
			for (UDDIRecord r: availableSupplierswsURL) {
				if (r.getUrl().equals(urlSOAP)) {
					return r.getOrgName();
				}
			}
			
		} catch (UDDINamingException e) {
			System.err.println("Caught exception from UDDINaming" + e);
		}
		return null;
	}
	
	public Certificate getCertificateFromSource(String entityName) throws CertificateException {
		String certificateName = caClient.getCertificate(entityName);
		return CryptoUtil.getX509CertificateFromPEMString(certificateName);
	}
	
}
