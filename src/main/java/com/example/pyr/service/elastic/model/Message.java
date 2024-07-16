package com.example.pyr.service.elastic.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
@Builder
@Data
@AllArgsConstructor
public class Message implements Serializable{
	
	private static final long serialVersionUID = -6108505472881104762L;
	
	@JsonProperty("_id")
	private String id;
	
	private int number;
	
	private String text;
	
	private float[] vector;
	
	private float score;

}
