package com.example.pyr.web;

import com.example.pyr.service.elastic.ElasticSearchService;
import com.example.pyr.service.elastic.model.Message;
import com.example.pyr.service.openai.OpenAiService;
import com.example.pyr.utils.PdfReader;
import com.example.pyr.utils.Tokenizer;
import io.micrometer.common.util.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
public class Controller {

    private static final String THREAD_COKKIE = "threadId";

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private OpenAiService openAiService;

    @Autowired
    private PdfReader pdfReader;

    @Autowired
    private Tokenizer tokenizer;

    @PostMapping("/ask")
    public ResponseEntity<String> enviarMensaje(@RequestBody Question question) throws Exception {
        if (!question.isValid()) {
            return ResponseEntity.noContent().build();
        }

        var candidates = searchCandidates(question).stream().map(m -> m.getText()).collect(Collectors.toList());

        String response = String.join("\n\n",
                openAiService.createQuestion(candidates, question.getQuestion(), "strict"));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/ask/stream")
    public ResponseEntity<Flux<String>> chatStreams(@RequestBody Question question) throws Exception {
        if (!question.isValid()) {
            return ResponseEntity.noContent().build();
        }

        var candidates = searchCandidates(question).stream().map(m -> m.getText()).collect(Collectors.toList());

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(openAiService.createStreamQuestion(candidates, question.getQuestion(), "strict"));

    }

    @PostMapping("/conversations/start")
    public ResponseEntity<String> conversation(@RequestBody Question question, ServerWebExchange exchange) {

        MultiValueMap<String, HttpCookie> cookies = exchange.getRequest().getCookies();

        HttpCookie cookie = cookies.getFirst(THREAD_COKKIE);
        String threadId = Objects.nonNull(cookie) ? cookie.getValue() : null;
        try {
            if (StringUtils.isNotEmpty(threadId)) {
                var response = openAiService.addMessageToThread(question.getQuestion(), threadId);
                System.out.println("THREAD RESPONSE " + response);
                return ResponseEntity.ok(response);
            } else {
                var conversation = openAiService.createConversation(question.getQuestion());
                addThreadCookie(exchange, ResponseCookie.from(THREAD_COKKIE, conversation.getThreadId())
                        .maxAge(60));

                System.out.println("CONVERSATION RESPONSE " + conversation);
                return ResponseEntity.ok(conversation.getLastMessageForAssistant());
            }
        } catch (Exception e) {
            addThreadCookie(exchange, ResponseCookie.from(THREAD_COKKIE, Strings.EMPTY));
            return ResponseEntity.badRequest().build();
        }

    }

    @DeleteMapping("/cancel/thread/{threadId}")
    public ResponseEntity<String> cancelThread(@PathVariable String threadId, ServerWebExchange exchange) {

        MultiValueMap<String, HttpCookie> cookies = exchange.getRequest().getCookies();

        //TODO: Pendiente tratar la cookie
        HttpCookie cookie = cookies.getFirst(THREAD_COKKIE);
        String threadId2 = Objects.nonNull(cookie) ? cookie.getValue() : null;
        
        openAiService.cancelthread(threadId);
        return ResponseEntity.ok().build();

    }

    @PostMapping("/documents/upload")
    public ResponseEntity<String> load() throws Exception {

        List<String> pages = pdfReader.readPdfPerPage("docs/regimen-juridico-del-sector-publico.pdf");

        List<Message> messages = new ArrayList<>();

        for (int page = 2; page < pages.size(); page++) {
            var pag = pages.get(page);
            float[] vector = openAiService.embedding(elasticSearchService.normalizeText(pag));
            messages.add(Message.builder().text(pag).vector(vector).number(page).build());
        }

        elasticSearchService.createBulk(messages);

        return ResponseEntity.ok("TODO OK");
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) throws Exception {
        elasticSearchService.delete(id);
        return ResponseEntity.ok("TODO OK");
    }

    private static void addThreadCookie(ServerWebExchange exchange, ResponseCookie.ResponseCookieBuilder THREAD_COKKIE) {
        exchange.getResponse().addCookie(THREAD_COKKIE.path("/").httpOnly(true).build());
    }

    private List<Message> searchCandidates(Question question) throws Exception {
        var normalizeText = elasticSearchService.normalizeText(question.getQuestion());
        float[] vector = openAiService.embedding(normalizeText);
        System.out.println("\n TOKENS PREGUNTA:" + tokenizer.countTokens(normalizeText));
        var response = elasticSearchService.hybridSearch(vector, normalizeText);
        return response;
    }

}
