package com.example.pyr.service.elastic;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.pyr.service.elastic.model.HibridRequest;
import com.example.pyr.service.elastic.model.Knn;
import com.example.pyr.service.elastic.model.Match;
import com.example.pyr.service.elastic.model.Message;
import com.example.pyr.service.elastic.model.Query;
import com.example.pyr.service.elastic.model.Title;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.indices.AnalyzeRequest;

@Service
public class ElasticSearchService {

	private static final String DEFAULT_INDEX = "oposita-index";

	@Autowired
	private ElasticsearchClient client;

	@Autowired
	private RestClient restClient;

	public void create(Message message) throws Exception {
		try {
			JSONObject jsonResponse = new JSONObject(message);
			IndexRequest<String> req = IndexRequest.of(b -> b.index(DEFAULT_INDEX)
					.withJson(new ByteArrayInputStream(jsonResponse.toString().getBytes(StandardCharsets.UTF_8))));
			client.index(req);

		} catch (Exception e) {
			throw new Exception("Error no controlado", e);
		}
	}

	public void createBulk(List<Message> messages) throws Exception {

		try {
			List<BulkOperation> operations = new ArrayList<>();
			messages.forEach(message -> {
				IndexOperation<Message> ope = IndexOperation.of(o -> o.document(message));
				operations.add(BulkOperation.of(t -> t.index(ope)));
			});

			var vulRequest = BulkRequest.of(b -> {
				b.operations(operations);
				b.index(DEFAULT_INDEX);
				return b;
			});
			client.bulk(vulRequest);

		} catch (Exception e) {
			throw new Exception("Error no controlado", e);
		}
	}
	
	public List<Message> hybridSearch(float[] vector, String quiestionNormalize) throws Exception {

		var searchRequest = buildSearchRequest(vector, quiestionNormalize, 4);
		Request request = new Request("POST", "/" + DEFAULT_INDEX + "/_search");
		request.setJsonEntity(new JSONObject(searchRequest).toString());

		try {
			Response response = restClient.performRequest(request);

			JSONObject responseBody = new JSONObject(EntityUtils.toString(response.getEntity()));
			JSONArray hits = responseBody.getJSONObject("hits").getJSONArray("hits");
			System.out.println(responseBody);
			List<Message> messages = new ArrayList<Message>();
			for (int i = 0; i < hits.length(); i++) {
				JSONObject hit = hits.getJSONObject(i);
				messages.add(Message.builder().text(hit.getJSONObject("_source").getString("text")).build());
			}
			return messages;
		} catch (Exception e) {
			throw new Exception("Error no controlado", e);
		}
	}

	public void delete(String id) throws Exception {

		try {
			DeleteRequest.Builder builder = new DeleteRequest.Builder();

			builder.id(id);
			builder.index(DEFAULT_INDEX);
			client.delete(builder.build());

		} catch (Exception e) {
			throw new Exception("Error no controlado", e);
		}
	}

	public String normalizeText(String message) throws Exception {

		try {
			AnalyzeRequest.Builder builder = new AnalyzeRequest.Builder();
			builder.analyzer("rebuilt_spanish").index(DEFAULT_INDEX);
			builder.text(message);

			var response = client.indices().analyze(builder.build());

			StringBuilder sb = new StringBuilder();
			response.tokens().forEach(token -> sb.append(token.token()).append(" "));
			return sb.toString().trim();
		} catch (Exception e) {
			throw new Exception("Error no controlado", e);
		}
	}

	private HibridRequest buildSearchRequest(float[] vector, String matchText, int size) {

		Title title = Title.builder().query(matchText).boost(0.8D).build();

		Query query = Query.builder().match(Match.builder().title(title).build()).build();

		Knn knn = Knn.builder().field("vector").k(100).num_candidates(1000).query_vector(vector).boost(0.2D).build();

		return HibridRequest.builder().query(query).knn(knn).size(size).build();
	}

}
