package com.example.pyr.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.ModelType;

import jakarta.annotation.PostConstruct;

@Component
public class Tokenizer {

	private EncodingRegistry registry = Encodings.newLazyEncodingRegistry();

	private Encoding tokenizer; 
	
	private long totalCount;
	
	@PostConstruct
	void init(){
		tokenizer = registry.getEncodingForModel(ModelType.GPT_3_5_TURBO);
	}
	
	public List<Integer> encode(String content){
		return tokenizer.encode(content);
	}
	
	public String decode( List<Integer> content){
		return tokenizer.decode(content);
	}
	
	public long countTokens(String... strings){
	    return Stream.of(strings)
	        .flatMap(s -> encode(s).stream())
	        .count();
	}
	
	public List<String> divide(String data, long maxTokenListItem){
		List<String> result = new ArrayList<>();
	    List<Integer> encodedData = encode(data);
	    int length = encodedData.size();
	    for (int i = 0; i < length; i += maxTokenListItem) {
	        List<Integer> sublist = encodedData.subList(i, Math.min(length, i + (int)maxTokenListItem));
	        String decodedSublist = decode(sublist);
	        result.add(decodedSublist);
	    }
	    return result;
	}
	
	public long countChatContextTokens(List<Map<String, String>> context, long countValue){
		totalCount = countValue;
		//TODO: Es la forma que proporciona la co de open ai para contar los token es la requet (https://platform.openai.com/docs/guides/gpt/managing-tokens), 
		// haciendo pruebas contra la web para contar que tienen me devuelve los mismos resultados
		// sin embarlo la respuesta de open ai que nos dice cuantos tokens hay en la request siempre falla por 3, por eso sumamos 3 al final
		context.forEach(map ->{
			totalCount+=4; // Se suman 4 por cada mensaje
			map.forEach((k, v) -> {				
				if (k.equalsIgnoreCase("name")) {
					totalCount -= 1; // Si existe en role se omite, entonces restamos por que el role siempre suma 1
                }
				totalCount+=tokenizer.countTokens(v); // Subtract 1 token if there's a name (role is always 1 token)
                
			});
			totalCount+= 2; // Se suman dos por cada mensaje, por como se dividen los mensajes
		});
		return totalCount-=3; // Suma or descuadre de la respuesta 
	}
}
