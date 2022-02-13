package com.example.webrtc.demo;

public class Candidate {
	private String candidate;
	private int sdpMLineIndex;
	private String sdpMid;

	public String getCandidate() {
		return candidate;
	}

	public void setCandidate(String candidate) {
		this.candidate = candidate;
	}

	public int getSdpMLineIndex() {
		return sdpMLineIndex;
	}

	public void setSdpMLineIndex(int sdpMLineIndex) {
		this.sdpMLineIndex = sdpMLineIndex;
	}

	public String getSdpMid() {
		return sdpMid;
	}

	public void setSdpMid(String sdpMid) {
		this.sdpMid = sdpMid;
	}

	public Candidate(String candidate, int sdpMLineIndex, String sdpMid) {
		this.candidate = candidate;
		this.sdpMLineIndex = sdpMLineIndex;
		this.sdpMid = sdpMid;
	}
}
