package com.example.pyr.service.elastic.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class Title {
	
	private String query;
	
	/**
	 * Representa el multiplicador de similitud, cuanto más alto mas similitud con la query Title
	 */
    private double boost;

}
