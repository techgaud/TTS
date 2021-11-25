package com.ttsplugin.main;

public class TTSMessage {
	public String message;
	public int voice, distance;
	public long time;
	
	public TTSMessage(String message, int voice, int distance, long time) {
		this.message = message;
		this.voice = voice;
		this.distance = distance;
		this.time = time;
	}
}
