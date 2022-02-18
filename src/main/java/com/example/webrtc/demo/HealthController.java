package com.example.webrtc.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HealthController { // todo decide whether it is necessary

	/**
	 * https://stackoverflow.com/a/58696157
	 * looks strange but I'll allow it
	 * @return
	 */
	@CrossOrigin(origins = "*") //fixme
	@RequestMapping(method = RequestMethod.GET, value = "/health")
	public ResponseEntity health() {
		return ResponseEntity.ok().body("okkkk");
	}
}
