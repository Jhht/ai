package com.example.pyr.service.openai.model;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
public class Messages {

	private List<Map<String, String>> messages;

}
