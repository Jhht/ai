package com.example.pyr.service.elastic;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;

@Configuration
public class Config {

	@Value("${elasticsearch.username}")
	private String username;

	@Value("${elasticsearch.password}")
	private String pass;

	@Value("${elasticsearch.url}")
	private String elasticUrl;

	@Bean
	@Primary
	ElasticsearchClient elasticsearchClient() {
		return new ElasticsearchClient(restClientTransport());
	}

	@Bean
	@Primary
	RestClientTransport restClientTransport() {
		return new RestClientTransport(restClient(), new JacksonJsonpMapper());
	}

	@Bean
	@Primary
	RestClient restClient() {
		final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, pass));
		return RestClient.builder(HttpHost.create(elasticUrl)).setHttpClientConfigCallback(httpClientBuilder -> 
						httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)).build();
	}

}
