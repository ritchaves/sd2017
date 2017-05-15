package org.komparator.mediator.domain;


import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import org.komparator.mediator.ws.cli.*;


public class LifeProof extends Thread { //TimerTask? - better not, tinha de ser feito um timer de 5 em 5 od se cria o lifeproof
	String mediatorURL;
	
	MediatorClient secondary;
	
	int value = 5;
	
	String secundaryURL = "http://localhost:8072/mediator-ws/endpoint";
	
	public LifeProof(String wsURL) {
		mediatorURL = wsURL;
	}
	public void run() {
		if(mediatorURL.contains("8071")) {
			try {
				secondary = new MediatorClient(secundaryURL);
				while(true) {
					nap(value);
					secondary.imAlive();
				}
			} catch (MediatorClientException e) {
				System.err.println("Caught exception:" + e);
			}
		}
		else {
			//verificar o quão antigo é o ultimo imalive e se ultrapassar um dado intervalo de tempo
			// uddiNaming = new UDDINaming(uddiURL);
			// uddiNaming.rebind(wsName, secondary.url);
			
		}
	}
	
	private void nap (int seconds) {
		try {
			sleep(seconds*1000);
		} catch (InterruptedException e) {
			System.out.printf("%s %s>%n  ", currentThread(), this);
			System.out.printf("Caught exception: %s%n", e);
		}
	}

}
