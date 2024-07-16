package com.example.pyr.service.openai.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
public class ChatRequest implements Serializable{
	
	private static final long serialVersionUID = 2519756468727787636L;

	private String model;
	
	private Boolean stream;
	
	private List<Map<String, String>> messages;
	
	@JsonProperty("max_tokens")
	private Integer maxTokens;
	
	private Double temperature;
	
	@JsonProperty("top_p")
	private Double top;

}
