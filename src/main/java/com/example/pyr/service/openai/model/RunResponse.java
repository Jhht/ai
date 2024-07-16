package com.example.pyr.service.openai.model;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
public class RunResponse {
	
	private final static List<String> INVALID_STATUSES = Arrays.asList("cancelling", "cancelled", "failed", "expired");
	private final static String VALID_STATUSE = "completed";

	
	private String id;
	
	private String status;
	
	@JsonProperty("thread_id")
	private String threadId;
	
	public RunResponse() {
		
	}
	
	public boolean isCompleted() {
		return VALID_STATUSE.equalsIgnoreCase(status);
	}
	
	public boolean isFail() {
		return INVALID_STATUSES.contains(status);
	}

}
