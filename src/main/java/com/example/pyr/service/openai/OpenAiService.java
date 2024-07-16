package com.example.pyr.service.openai;

import com.example.pyr.service.openai.model.*;
import com.example.pyr.utils.Tokenizer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.*;

@Service
public class OpenAiService {

    private static final String OPENAI_EMBEDDINGS_URL = "https://api.openai.com/v1/engines/text-embedding-ada-002/embeddings";
    private static final String OPENAI_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";
    private static final String OPENAI_THREAD_AND_RUN_URL = "https://api.openai.com/v1/threads/runs";
    private static final String GET_MESSAGE_OF_THREAD_URL = "https://api.openai.com/v1/threads/{thread_id}/messages";
    private static final String GET_RUN_URL = "https://api.openai.com/v1/threads/{thread_id}/runs/{run_id}";
    private static final String CREATE_MESSAGE_OF_THREAD_URL = "https://api.openai.com/v1/threads/%s/messages";
    private static final String CREATE_RUN_OF_THREAD_URL = "https://api.openai.com/v1/threads/%s/runs";
    private static final String DELETE_THREAD_URL = "https://api.openai.com/v1/threads/{threadId}";

    private static final String STRICT_STYLE_DEFAULT = "strict";
    private static final String MODEL = "gpt-3.5-turbo-16k";
    private static final String CONTENT = "content";
    private static final String DELTA = "delta";
    private static final String CHOICES = "choices";
    private static final String ROLE = "role";
    private static final String ROLE_USER = "user";
    private static final String ROLE_SYSTEM = "system";
    private static final String ASSISTANT_ID = "asst_AcQ0XdPrTgMIk4amo3fSU1NR";

    @Value("${openai.api.key}")
    private String apiKey;

    private RestTemplate restTemplate;

    private HttpHeaders headers;

    private ObjectMapper objectMapper;

    @Autowired
    private Tokenizer tokenizer;

    private long countTokens;

    @PostConstruct
    void init() {
        objectMapper = new ObjectMapper();
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        restTemplate = new RestTemplate();
    }

    /**
     * Crear el 1º flujo de conversacion con el asistente a partir de la 1º pregunta del usuario
     * Crear hilo, comprueba el estado, cuando se completa obtiene los mensajes del hilo y devuelve
     * el mensaje del asistente
     *
     * @param question
     * @return MessagesThreadResponse
     * @throws JsonProcessingException
     * @throws JSONException
     * @throws InterruptedException
     */
    public MessagesThreadResponse createConversation(String question)
            throws JsonProcessingException, JSONException, InterruptedException {

        headers.set("OpenAI-Beta", "assistants=v1");

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(ROLE, ROLE_USER, CONTENT, question));

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(createRunRequest(messages)),
                headers);
        ResponseEntity<RunResponse> response = postForEntity(OPENAI_THREAD_AND_RUN_URL, entity, RunResponse.class);

        var runResponse = checkRun(response.getBody());
        return Objects.nonNull(runResponse) ? getThreadMessages(runResponse) : null;
    }

    public String addMessageToThread(String question, String threadId)
            throws JsonProcessingException, JSONException, InterruptedException {

        headers.set("OpenAI-Beta", "assistants=v1");
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(ROLE, ROLE_USER, CONTENT, question));

        var request = MessagesRequest.builder().role(ROLE_USER).content(question).build();

        var url = String.format(CREATE_MESSAGE_OF_THREAD_URL, threadId);

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(request), headers);
        ResponseEntity<MessagesResponse> response = postForEntity(url, entity, MessagesResponse.class);
        checkResponseStatus(response);

        var urlRun = String.format(CREATE_RUN_OF_THREAD_URL, response.getBody().getThreadId());

        JSONObject requestRun = new JSONObject();
        requestRun.put("assistant_id", ASSISTANT_ID);

        HttpEntity<String> entityRun = new HttpEntity<>(requestRun.toString(), headers);
        ResponseEntity<RunResponse> responseRun = postForEntity(urlRun, entityRun, RunResponse.class);
        checkResponseStatus(responseRun);

        var checkRun = checkRun(responseRun.getBody());
        return Objects.nonNull(checkRun) ? getThreadMessages(checkRun).getLastMessageForAssistant() : null;
    }

    public List<String> createQuestion(List<String> contextDocs, String pregunta, String style)
            throws JSONException {
        var context = getPromptWithContext(contextDocs, pregunta, style);
        countTokens = tokenizer.countChatContextTokens(context, 0);
        var chatRequest = ChatRequest.builder().model(MODEL).temperature(0.1D).maxTokens(750).messages(context).build();

        JSONObject body = new JSONObject(chatRequest);
        HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);
        ResponseEntity<String> response = postForEntity(OPENAI_COMPLETIONS_URL, entity, String.class);

        System.out.println(response);

        if (HttpStatus.OK == response.getStatusCode()) {
            JSONObject jsonResponse = new JSONObject(response.getBody());
            JSONArray choices = jsonResponse.getJSONArray(CHOICES);
            List<String> messages = new ArrayList<>();
            for (int i = 0; i < choices.length(); i++) {
                messages.add(choices.getJSONObject(i).getJSONObject("message").getString(CONTENT));
            }
            countTokens += tokenizer.countTokens(String.join("", messages));
            System.out.println("\n TOTAL DE TOKENS: " + countTokens);
            return messages;
        } else {
            throw new RuntimeException("Failed to get response from OpenAI API");
        }
    }

    public Flux<String> createStreamQuestion(List<String> contextDocs, String pregunta, String style)
            throws JsonProcessingException {

        var context = getPromptWithContext(contextDocs, pregunta, style);
        countTokens = tokenizer.countChatContextTokens(context, 0);
        System.out.println("\n TOTAL DE REQUEST: " + countTokens);

        var chatRequest = ChatRequest.builder().model(MODEL).temperature(0.1D).stream(true).maxTokens(750)
                .messages(context).build();

        String request = objectMapper.writeValueAsString(chatRequest);

        WebClient client = WebClient.builder().baseUrl(OPENAI_COMPLETIONS_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey).build();

        Flux<String> prueba = client.post().contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request)).retrieve().bodyToFlux(String.class);

        Flux<String> processedFlux = prueba.map(str -> {
            // Quitar el string [DONE] del final del json de la respuesta, viene por defecto
            // de Oen AI
            str = str.replace("[DONE]", "");
            try {
                JsonNode jsonNode = objectMapper.readTree(str);
                if (jsonNode.has(CHOICES)) {
                    JsonNode choicesNode = jsonNode.get(CHOICES);
                    if (choicesNode.isArray()) {
                        for (JsonNode choice : choicesNode) {
                            if (choice.has(DELTA) && choice.get(DELTA).has(CONTENT)) {
                                var word = choice.get(DELTA).get(CONTENT).asText("");
                                System.out.println(word);
                                countTokens += tokenizer.countTokens(word);
                                return word;
                            }
                        }
                    }
                }
                System.out.println("\n TOTAL DE TOKENS: " + countTokens);

            } catch (Exception e) {
                throw new RuntimeException("Failed to get response from OpenAI API", e);
            }
            return "";
        });

        return processedFlux;
    }

    public void cancelthread(String threadId) {
        headers.set("OpenAI-Beta", "assistants=v1");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(DELETE_THREAD_URL, HttpMethod.GET, entity, String.class,
                threadId);
        checkResponseStatus(response);
    }

    public float[] embedding(String input) throws JsonProcessingException, JSONException {
        JSONObject body = new JSONObject();
        body.put("input", input);

        HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);
        ResponseEntity<String> response = postForEntity(OPENAI_EMBEDDINGS_URL, entity, String.class);

        checkResponseStatus(response);

        float[] embeddingsAsList;
        JSONObject jsonResponse = new JSONObject(response.getBody());
        JSONArray embeddings = jsonResponse.getJSONArray("data").getJSONObject(0).getJSONArray("embedding");
        embeddingsAsList = objectMapper.readValue(embeddings.toString(), new TypeReference<float[]>() {
        });

        return embeddingsAsList;
    }

    /**
     * Tras crear el hilo se debe comprobar el estado, cuando pasa a completado
     * el asistente a completado la ejec del hilo
     *
     * @param runResponse
     * @return RunResponse
     * @throws JSONException
     * @throws InterruptedException
     */
    private RunResponse checkRun(RunResponse runResponse)
            throws JSONException, InterruptedException {

        headers.set("OpenAI-Beta", "assistants=v1");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        boolean completed = false;
        while (!completed) {
            var run = findRun(runResponse, entity);
            completed = run.getBody().isCompleted();
            if (completed) {
                return run.getBody();
            }
            Thread.sleep(1000);
        }
        return null;
    }

    private ResponseEntity<RunResponse> findRun(RunResponse runResponse, HttpEntity<String> entity) {
        ResponseEntity<RunResponse> response = restTemplate.exchange(GET_RUN_URL, HttpMethod.GET, entity,
                RunResponse.class, runResponse.getThreadId(), runResponse.getId());
        checkResponseStatus(response);
        if (response.getBody().isFail()) {
            throw new RuntimeException("Error: RunResponse is in fail state: " + response.getBody().getStatus());
        }
        return response;
    }

    private RunThread createRunRequest(List<Map<String, String>> messages) {
        return RunThread.builder().assistantId(ASSISTANT_ID).thread(Messages.builder().messages(messages).build())
                .build();
    }

    private MessagesThreadResponse getThreadMessages(RunResponse runResponse)
            throws JSONException {
        headers.set("OpenAI-Beta", "assistants=v1");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<MessagesThreadResponse> response = restTemplate.exchange(GET_MESSAGE_OF_THREAD_URL,
                HttpMethod.GET, entity, MessagesThreadResponse.class, runResponse.getThreadId());
        checkResponseStatus(response);
        System.out.println("\nRESPONSE GET MESSAHES FROM THREAD:" + response.getBody().getData().toString());
        return response.getBody();
    }

    private <T> ResponseEntity<T> postForEntity(String url, HttpEntity<?> entity, Class<T> responseType) {
        try {
            var response = restTemplate.postForEntity(url, entity, responseType);
            checkResponseStatus(response);
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get response from OpenAI API");
        }
    }

    private void checkResponseStatus(ResponseEntity<?> response) {
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException(
                    "Failed to get response from OpenAI API whith respose status: " + response.getStatusCode());
        }
    }

    private List<Map<String, String>> getPromptWithContext(List<String> contextDocs, String pregunta, String style) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(createSystemContext(style));
        messages.add(createUserContext(contextDocs, pregunta));
        return messages;
    }

    private Map<String, String> createSystemContext(String style) {
        Map<String, String> message1 = new HashMap<>();
        message1.put(ROLE, ROLE_SYSTEM);

        message1.put(CONTENT, String.format(
                "Eres un asistente de Q&N, rapido y conciso que utiliza los documentos proporcionados delimitados por comillas triples para responder preguntas." 
                        + "Si la respuesta no se puede encontrar en los documentos, " + getRuleByStyle(style)));
        return message1;
    }

    private String getRuleByStyle(String style) {
        return new StringBuilder(STRICT_STYLE_DEFAULT.equalsIgnoreCase(style)
                ? "no debe responder y, en su lugar, debe indicar que no encontró la respuesta y disculparse."
                : "debe responder a partir de su base de conocimientos actual, olvidando las indicaciones descritas anteriormente."
        ).toString();
    }

    private Map<String, String> createUserContext(List<String> contextDocs, String pregunta) {
        String context = "\"\"\" " + String.join("\n\n", contextDocs) + " \"\"\"";
        Map<String, String> message2 = new HashMap<>();
        message2.put(ROLE, ROLE_USER);
        message2.put(CONTENT, "Documentacion:" + context + "\n\nPregunta: " + pregunta);
        return message2;
    }

}
