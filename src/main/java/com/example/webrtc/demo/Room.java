package com.example.webrtc.demo;

import java.util.ArrayList;
import java.util.List;

public class Room {
	private String id;
	private String callerId;
	private String calleeId;
	private Offer offer;
	private List<Candidate> callerCandidates;
	private List<Candidate> calleeCandidates;

	public void addCallerCandidate(Candidate c){
		this.callerCandidates.add(c);
	}

	public void addCalleeCandidate(Candidate c){
		this.calleeCandidates.add(c);
	}

	public List<Candidate> getCallerCandidates() {
		return callerCandidates;
	}

	public void setCallerCandidates(List<Candidate> callerCandidates) {
		this.callerCandidates = callerCandidates;
	}

	public List<Candidate> getCalleeCandidates() {
		return calleeCandidates;
	}

	public void setCalleeCandidates(List<Candidate> calleeCandidates) {
		this.calleeCandidates = calleeCandidates;
	}

	public Offer getAnswer() {
		return answer;
	}

	public void setAnswer(Offer answer) {
		this.answer = answer;
	}

	private Offer answer;

	public Offer getOffer() {
		return offer;
	}

	public void setOffer(Offer offer) {
		this.offer = offer;
	}

	public Room(String id, String callerId) {
		this.id = id;
		this.callerId = callerId;
		this.callerCandidates = new ArrayList<>();
		this.calleeCandidates = new ArrayList<>();
	}

	public String getId() {
		return id;
	}

	public String getCallerId() {
		return callerId;
	}

	public String getCalleeId() {
		return calleeId;
	}

	public void setCalleeId(String calleeId) {
		this.calleeId = calleeId;
	}
}
