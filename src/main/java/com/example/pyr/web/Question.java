package com.example.pyr.web;

import io.micrometer.common.util.StringUtils;
import lombok.Data;

@Data
public class Question {

	private String question;
	
	public boolean isValid() {
		return StringUtils.isNotBlank(question);
	}
}
