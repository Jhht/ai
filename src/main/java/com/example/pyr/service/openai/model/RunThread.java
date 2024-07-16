package com.example.pyr.service.openai.model;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
public class RunThread {
	
	@JsonProperty("assistant_id")
	private String assistantId;
	
	
	private Messages thread;

}
