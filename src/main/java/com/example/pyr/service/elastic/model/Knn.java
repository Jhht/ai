package com.example.pyr.service.elastic.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
public class Knn {
	/**
	 *  Campo de búsqueda de vectores
	 */
	private String field;
	
	/**
	 * Nº de vecinos devueltos
	 */
    private int k;
    
    /**
	 * Nº de candidatos observados para la búsuqeda
	 */
    private int num_candidates;
    
    /**
	 * Array de vectores
	 */
    private float[] query_vector;
    
    /**
	 * Representa el multiplicador de similitud, 
	 * cuanto más alto mas similitud tendran los documentos buscados por knn
	 */
    private double boost;

}
