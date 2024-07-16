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
public class MessagesThreadResponse {
	
	@JsonProperty("first_id")
	private String firstId;
	
	@JsonProperty("last_id")
	private String lastId;
	
	@JsonProperty("has_more")
	private boolean hasMore;
	
	private List<MessagesResponse> data;
	
	public MessagesThreadResponse() {}
	
	public String getLastMessageForAssistant() {
		return data.stream().filter(t -> t.getId().equalsIgnoreCase(getFirstId())).findFirst().map(t -> t.getText()).orElse(Strings.EMPTY);
	}
	
	public String getThreadId() {
		return data.stream().findFirst().map(t -> t.getThreadId()).orElse(Strings.EMPTY);
	}
	
}
