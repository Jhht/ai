package com.example.pyr.service.openai.model;

import java.util.Objects;

import org.apache.logging.log4j.util.Strings;

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
public class MessagesContentResponse {

	private String type;
	
	private Text text;
	
	public MessagesContentResponse() {
		
	}
	
	public String getText() {
		return Objects.nonNull(text) ? text.getValue() : Strings.EMPTY;
	}
}
