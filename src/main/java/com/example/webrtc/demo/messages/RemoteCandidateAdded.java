package com.example.webrtc.demo.messages;

import com.example.webrtc.demo.Candidate;

public class RemoteCandidateAdded {
	private final String event = "remoteCandidateAdded";
	private Candidate candidate;

	public RemoteCandidateAdded(Candidate candidate) {
		this.candidate = candidate;
	}

	public String getEvent() {
		return event;
	}

	public Candidate getCandidate() {
		return candidate;
	}

	public void setCandidate(Candidate candidate) {
		this.candidate = candidate;
	}
}
