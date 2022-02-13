package com.example.webrtc.demo;

import com.example.webrtc.demo.messages.AddOffer;
import com.example.webrtc.demo.messages.Answer;
import com.example.webrtc.demo.messages.FindRoomAnswer;
import com.example.webrtc.demo.messages.RemoteCandidateAdded;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SocketHandler extends TextWebSocketHandler {

	List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
	Map<String, Room> rooms = new HashMap<>();

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws InterruptedException, IOException, IOException {

		ObjectMapper mapper = new ObjectMapper();
		HashMap<String,String> messageObj = mapper.readValue(message.getPayload(), HashMap.class);

		if (messageObj.get("event").equals("createRoom")) {
			String roomId = messageObj.get("roomId");
			rooms.put(roomId, new Room(roomId, session.getId()));
		}

		if (messageObj.get("event").equals("addOffer")) {
			JsonNode messageNode = new ObjectMapper().readTree(message.getPayload());

			Offer offer = new Offer(messageNode.get("offer").get("type").textValue(), messageNode.get("offer").get("sdp").textValue());
			rooms.get(messageNode.get("roomId").textValue()).setOffer(offer);
		}

		if (messageObj.get("event").equals("findRoom")) {
			String roomId = messageObj.get("roomId");
			Room r = rooms.get(roomId);
			boolean success = r != null;
			FindRoomAnswer findRoomAnswer = new FindRoomAnswer(success);
			if (success) {
				findRoomAnswer.setOffer(r.getOffer());
			}
			TextMessage ans = new TextMessage(mapper.writeValueAsString(findRoomAnswer));
			session.sendMessage(ans);

		}

		if (messageObj.get("event").equals("roomWithAnswer")) {
			JsonNode messageNode = new ObjectMapper().readTree(message.getPayload());
			Offer answer = new Offer(messageNode.get("roomWithAnswer").get("type").textValue(), messageNode.get("roomWithAnswer").get("sdp").textValue());
			Room r = rooms.get(messageNode.get("roomId").textValue());
			r.setAnswer(answer);
			r.setCalleeId(session.getId());

			WebSocketSession callerSession = sessions.stream().filter(i -> r.getCallerId().equals(i.getId())).findFirst().orElse(null);
			if (callerSession != null) {
				Answer toCaller = new Answer(answer);
				TextMessage ans = new TextMessage(mapper.writeValueAsString(toCaller));
				callerSession.sendMessage(ans);

				// send caller candidate to callee below
				List<Candidate> callerCandidates = r.getCallerCandidates();
				if (callerCandidates != null) {
					for (Candidate candidate: callerCandidates) {
						RemoteCandidateAdded toCallee = new RemoteCandidateAdded(candidate);
						TextMessage candidateAns = new TextMessage(mapper.writeValueAsString(toCallee));
						session.sendMessage(candidateAns);
					}
				}
//				WebSocketSession calleeSession = r.getCalleeId() != null ? sessions.stream().filter(i -> r.getCalleeId().equals(i.getId())).findFirst().orElse(null) : null;
//				if (calleeSession != null) {
//					RemoteCandidateAdded toCallee = new RemoteCandidateAdded(candidate);
//					TextMessage ans = new TextMessage(mapper.writeValueAsString(toCallee));
//					calleeSession.sendMessage(ans);
//				} else {
//					//todo tell caller there is no callee
//				}
				// send caller candidate to callee above
			} else {
				// todo tell callee that caller is absent
			}
		}

		if (messageObj.get("event").equals("addCallerCandidate")) {
			JsonNode messageNode = new ObjectMapper().readTree(message.getPayload());
			Room r = rooms.get(messageNode.get("roomId").textValue());
			if (r != null) {
				JsonNode candidateNode = messageNode.get("candidate");
				Candidate candidate = new Candidate(candidateNode.get("candidate").textValue(), candidateNode.get("sdpMLineIndex").asInt(), candidateNode.get("sdpMid").textValue());
				r.addCallerCandidate(candidate);

//				// send caller candidate to callee below
//				WebSocketSession calleeSession = r.getCalleeId() != null ? sessions.stream().filter(i -> r.getCalleeId().equals(i.getId())).findFirst().orElse(null) : null;
//				if (calleeSession != null) {
//					RemoteCandidateAdded toCallee = new RemoteCandidateAdded(candidate);
//					TextMessage ans = new TextMessage(mapper.writeValueAsString(toCallee));
//					calleeSession.sendMessage(ans);
//				} else {
//					//todo tell caller there is no callee
//				}
//				// send caller candidate to callee above

			} else {
				// todo tell caller there is no room?
			}

		}

		if (messageObj.get("event").equals("addCalleeCandidate")) {
			JsonNode messageNode = new ObjectMapper().readTree(message.getPayload());
			Room r = rooms.get(messageNode.get("roomId").textValue());
			if (r != null) {
				JsonNode candidateNode = messageNode.get("candidate");
				Candidate candidate = new Candidate(candidateNode.get("candidate").textValue(), candidateNode.get("sdpMLineIndex").asInt(), candidateNode.get("sdpMid").textValue());
				r.addCalleeCandidate(candidate);

				// send callee candidate to caller below
				WebSocketSession callerSession = r.getCallerId() != null ? sessions.stream().filter(i -> r.getCallerId().equals(i.getId())).findFirst().orElse(null) : null;
				if (callerSession != null) {
					RemoteCandidateAdded toCaller = new RemoteCandidateAdded(candidate);
					TextMessage ans = new TextMessage(mapper.writeValueAsString(toCaller));
					callerSession.sendMessage(ans);
				} else {
					//todo tell callee there is no caller
				}
				// send callee candidate to caller above

			} else {
				// todo tell callee there is no room?
			}

		}


//		for (WebSocketSession webSocketSession : sessions) {
//			if (webSocketSession.isOpen() && !session.getId().equals(webSocketSession.getId())) {
//				webSocketSession.sendMessage(message);
//			}
//		}
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		sessions.add(session);
	}
}

