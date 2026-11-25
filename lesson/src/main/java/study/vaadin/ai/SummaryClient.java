package study.vaadin.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import study.vaadin.api.ApiModels.Summary;
import study.vaadin.config.AppProperties;

@Service
public class SummaryClient {

    private final AppProperties properties;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public SummaryClient(AppProperties properties) {
        this.properties = properties;
    }

    public Summary summarize(String text) {
        String source = text == null ? "" : text.trim();
        if (source.isEmpty()) {
            return new Summary("요약할 문장이 없습니다.", List.of());
        }
        if ("http".equals(properties.aiProvider()) && properties.aiUrl() != null && !properties.aiUrl().isBlank()) {
            return remote(source);
        }
        return local(source);
    }

    public Summary ask(String text, String question) {
        String source = text == null ? "" : text;
        String query = question == null ? "" : question.trim();
        List<String> hits = excerpts(source, query);
        if (hits.isEmpty()) {
            return new Summary("문서에서 찾을 수 없습니다.", List.of());
        }
        return new Summary(String.join(" ", hits), hits);
    }

    public List<String> tokens(String text) {
        String summary = summarize(text).summary();
        List<String> tokens = new ArrayList<>();
        StringBuilder word = new StringBuilder();
        for (int i = 0; i < summary.length(); i++) {
            char ch = summary.charAt(i);
            word.append(ch);
            if (Character.isWhitespace(ch) || word.length() >= 8) {
                tokens.add(word.toString());
                word.setLength(0);
            }
        }
        if (!word.isEmpty()) {
            tokens.add(word.toString());
        }
        return tokens;
    }

    private Summary local(String source) {
        String compact = source.replaceAll("\\s+", " ");
        String summary = compact.length() <= 120 ? compact : compact.substring(0, 120) + "…";
        return new Summary(summary, List.of(excerpt(compact)));
    }

    private Summary remote(String source) {
        try {
            String body = "{\"model\":\"" + escape(properties.aiModel()) + "\",\"prompt\":\"" + escape(source) + "\"}";
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(properties.aiUrl()))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body));
            if (properties.aiKey() != null && !properties.aiKey().isBlank()) {
                builder.header("Authorization", "Bearer " + properties.aiKey());
            }
            HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                return new Summary("AI 응답을 가져오지 못했습니다.", List.of());
            }
            return new Summary(response.body(), List.of(excerpt(source)));
        } catch (Exception ex) {
            return new Summary("AI 응답을 가져오지 못했습니다.", List.of());
        }
    }

    private List<String> excerpts(String source, String question) {
        if (question.isEmpty()) {
            return List.of();
        }
        String[] sentences = source.split("(?<=[.!?。])\\s+|\\n+");
        List<String> hits = new ArrayList<>();
        for (String sentence : sentences) {
            if (sentence.toLowerCase().contains(question.toLowerCase()) || sharesWord(sentence, question)) {
                hits.add(sentence.trim());
            }
            if (hits.size() == 3) {
                break;
            }
        }
        return hits;
    }

    private boolean sharesWord(String sentence, String question) {
        for (String word : question.split("\\s+")) {
            if (word.length() >= 2 && sentence.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private String excerpt(String text) {
        return text.length() <= 80 ? text : text.substring(0, 80);
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }
}
