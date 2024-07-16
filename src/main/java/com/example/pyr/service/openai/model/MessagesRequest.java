package com.example.pyr.service.openai.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
public class MessagesRequest {

	private String role;
	
	private String content;
	
	public MessagesRequest() {
		
	}
	
}
