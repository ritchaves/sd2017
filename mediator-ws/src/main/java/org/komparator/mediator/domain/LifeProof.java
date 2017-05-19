package org.komparator.mediator.domain;


import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;

import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

import org.komparator.mediator.ws.MediatorPortImpl;
import org.komparator.mediator.ws.cli.*;


public class LifeProof extends Thread { 
	private static final int PRIMARY_WAIT = 5000;
	private static final int SECONDARY_WAIT = 10000;
	private static final int DEAD_TIME = 20;
	String mediatorURL;
	String uddiURL;
	String wsName;
	boolean status = true;
	
	MediatorClient secondary;
	UDDINaming uddiNaming;
	String secundaryURL = "http://localhost:8072/mediator-ws/endpoint";
	
	public LifeProof(String wsURL, String uddi, String nameWs) {
		mediatorURL = wsURL;
		uddiURL = uddi;
		wsName = nameWs;
		try {
			secondary = new MediatorClient(secundaryURL);
		} catch (MediatorClientException e) {
			System.err.println("Caught exception while trying to connect to Secondary Mediator:" + e);
		}
	}

	public void run() {
		while(status) {
			if(mediatorURL.contains("8071")) {
				try {
			         sleep(PRIMARY_WAIT);
				     System.out.println("I'm alive! Proving it!");
				     secondary.imAlive();	  
			    } catch (Exception e) {
					System.err.println("Caught exception:" + e); }
			} else {
				
				try {
					sleep(SECONDARY_WAIT);
				} catch (InterruptedException e) {
					System.err.println("Caught exception:" + e);
				}
				if (!Mediator.getInstance().getLastAlive().equals(null)) {
					LocalDateTime lastAlive = Mediator.getInstance().getLastAlive();
					System.out.println("LastAlive Proof: " + lastAlive);

			        if (Mediator.getInstance().getLastAlive().isBefore(LocalDateTime.now().minusSeconds(DEAD_TIME))) {		
						try {
							System.out.println("No signal from Primary Mediator..");
							System.out.println(">Step aside! Mediator 2.0 taking over!");
							uddiNaming = new UDDINaming(uddiURL);
							uddiNaming.unbind(wsName);
							System.out.printf("Publishing '%s' to UDDI at %s%n", wsName, uddiURL);
							uddiNaming.rebind(wsName, secundaryURL);
							status = false;
						} catch (UDDINamingException e) {
							System.err.println("Caught exception:" + e);
						}
			        }
				}
			}
		}
	}
	
	public void shutdown() {
		this.status = false;
	}
}
	
				

