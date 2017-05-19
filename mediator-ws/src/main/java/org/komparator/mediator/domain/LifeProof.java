package org.komparator.mediator.domain;


import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;

import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

import org.komparator.mediator.ws.MediatorPortImpl;
import org.komparator.mediator.ws.cli.*;


public class LifeProof extends Thread { 
	private static final int DEAD_TIME = 30;
	String mediatorURL;
	String uddiURL;
	String wsName;
	int value = 5000;
	int value2 = 50000;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		while(status) {
			if(mediatorURL.contains("8071")) {
				try {
			         sleep(5000);
				     System.out.println("I'm alive! Proving it!");
				     secondary.imAlive();	  
			    } catch (Exception e) {
					System.err.println("Caught exception:" + e); }
			} else {
				//verificar o quão antigo é o ultimo imalive e se ultrapassar um dado intervalo de tempo
				//FIXME - fixed?
				//System.out.println("second mediator?");
				try {
					sleep(10000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
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
							uddiNaming.rebind(wsName, secundaryURL);
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
	
				

