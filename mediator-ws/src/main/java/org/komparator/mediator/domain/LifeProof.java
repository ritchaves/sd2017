package org.komparator.mediator.domain;


import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;

import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

import org.komparator.mediator.ws.MediatorPortImpl;
import org.komparator.mediator.ws.cli.*;


public class LifeProof { 
	private static final int DEAD_TIME = 30;
	String mediatorURL;
	String uddiURL;
	String wsName;
	int value = 5000;
	
	MediatorClient secondary;
	UDDINaming uddiNaming;
	String secundaryURL = "http://localhost:8072/mediator-ws/endpoint";
	
	public LifeProof(String wsURL, String uddi, String nameWs) {
		mediatorURL = wsURL;
		uddiURL = uddi;
		wsName = nameWs;
	}
	public void run() {
		if(mediatorURL.contains("8071")) {
			try {
				secondary = new MediatorClient(secundaryURL);
				
		        Timer timer = new Timer();
		        timer.schedule(new TimerTask() {

		            @Override
		            public void run() {
		            	secondary.imAlive();
		            }
		        }, 0, value);
		    } catch (MediatorClientException e) {
				System.err.println("Caught exception:" + e); }
		} else {
			//verificar o quão antigo é o ultimo imalive e se ultrapassar um dado intervalo de tempo
			//FIXME - fixed?
			if(Mediator.getInstance().getLastAlive().isBefore(LocalDateTime.now().minusSeconds(DEAD_TIME))) {
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
