package com.example.pyr.service.openai.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConversationStyle {
	STRICT("strict"), CREATIVE("creative");
	
	private String value;

}
