package com.example.pyr.service.openai.model;

import java.util.List;

import org.apache.logging.log4j.util.Strings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
public class MessagesResponse {

	private String id;
	
	@JsonProperty("thread_id")
	private String threadId;
	
	private String role;
	
	private List<MessagesContentResponse> content;
	
	@JsonProperty("run_id")
	private String runId;
	
	public MessagesResponse() {
		
	}
	
	public String getText() {
		return content.stream().findFirst().map(t -> t.getText()).orElse(Strings.EMPTY);
	}
	
}
