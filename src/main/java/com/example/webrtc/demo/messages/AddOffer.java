package com.example.webrtc.demo.messages;

import com.example.webrtc.demo.Offer;

public class AddOffer extends Template{

	private Offer offer;

	public AddOffer(String event, String roomId, Offer offer) {
		super(event, roomId);
		this.offer = offer;
	}

	public AddOffer(String event, String roomId) {
		super(event, roomId);
	}

	public Offer getOffer() {
		return offer;
	}

	public void setOffer(Offer offer) {
		this.offer = offer;
	}
}
