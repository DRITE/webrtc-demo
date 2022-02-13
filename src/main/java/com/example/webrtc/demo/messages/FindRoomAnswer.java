package com.example.webrtc.demo.messages;

import com.example.webrtc.demo.Offer;

public class FindRoomAnswer {
	private final String event = "findRoomAnswer";
	private boolean success;

	public Offer getOffer() {
		return offer;
	}

	public void setOffer(Offer offer) {
		this.offer = offer;
	}

	private Offer offer;

	public FindRoomAnswer(boolean success) {
		this.success = success;
	}

	public String getEvent() {
		return event;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
}
