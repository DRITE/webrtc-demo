package com.example.webrtc.demo.messages;

import com.example.webrtc.demo.Offer;

public class Answer {
	private final String event = "answer";
	private Offer answer;

	public String getEvent() {
		return event;
	}

	public Offer getAnswer() {
		return answer;
	}

	public void setAnswer(Offer answer) {
		this.answer = answer;
	}

	public Answer(Offer answer) {
		this.answer = answer;
	}
}
