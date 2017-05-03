package org.komparator.security.handler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TokenDetails {
	
	private String author;
	
	private LocalDateTime timestamp;
	
	private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
	
	//*************************************
	
	public TokenDetails(String auth){
		author = auth;
		timestamp = LocalDateTime.now();
	}
	
	//*************************************

	public String getAuthor() {
		return author;
	}

	public void setToken(String auth) {
		this.author = auth;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
	
	public String timeToString(LocalDateTime ts){
		return dtf.format(ts);
	}
	
	public LocalDateTime stringToTime(String ts){
		return LocalDateTime.parse(ts, dtf);
	}
	
	public boolean isOldToken(){
		/*Se o token tiver mais de 20 segundos - too old*/
		LocalDateTime now = LocalDateTime.now();
		long secondsBetween = ChronoUnit.SECONDS.between(timestamp, now);
		return (secondsBetween > 20);
	}
}
