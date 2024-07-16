package com.example.pyr.service.elastic.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
public class Query {
	
	private Match match;

}
