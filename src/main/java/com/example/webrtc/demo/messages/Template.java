package com.example.webrtc.demo.messages;

public class Template {
	private String event;
	private String roomId;

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public Template(String event, String roomId) {
		this.event = event;
		this.roomId = roomId;
	}
}
