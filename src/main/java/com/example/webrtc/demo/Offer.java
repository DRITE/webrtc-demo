package com.example.webrtc.demo;

public class Offer {
	private String type;

	public String getType() {
		return type;
	}

	public String getSdp() {
		return sdp;
	}

	public Offer(String type, String sdp) {
		this.type = type;
		this.sdp = sdp;
	}

	private String sdp;
}
