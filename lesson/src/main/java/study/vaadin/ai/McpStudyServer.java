package study.vaadin.ai;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 표준 입력으로 JSON-RPC 한 줄을 받아 문서 발췌를 돌려주는 학습용 MCP 서버.
 * 실행: mvn -q -DskipTests exec:java -Dexec.mainClass=study.vaadin.ai.McpStudyServer
 */
public final class McpStudyServer {

    private static final String DOCUMENT = """
            Vaadin Flow는 서버가 화면 상태를 세션에 두고 브라우저와 동기화한다.
            Hilla는 React 화면에서 BrowserCallable 서비스를 호출한다.
            문서에 없는 내용은 추측하지 않고 찾을 수 없습니다라고 답한다.
            """;
    private static final Pattern METHOD = Pattern.compile("\"method\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern ID = Pattern.compile("\"id\"\\s*:\\s*(\\d+|null)");
    private static final Pattern QUERY = Pattern.compile("\"query\"\\s*:\\s*\"([^\"]*)\"");

    private McpStudyServer() {
    }

    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.isBlank()) {
                System.out.println(handle(line));
            }
        }
    }

    static String handle(String line) {
        String method = group(METHOD, line, "unknown");
        String id = group(ID, line, "null");
        return switch (method) {
            case "initialize" -> result(id, "{\"protocolVersion\":\"2025-06-18\",\"serverInfo\":{\"name\":\"vaadin-study\",\"version\":\"0.1.0\"},\"capabilities\":{\"tools\":{}}}");
            case "tools/list" -> result(id, "{\"tools\":[{\"name\":\"search_document\",\"description\":\"학습 문서에서 발췌를 찾는다\",\"inputSchema\":{\"type\":\"object\",\"properties\":{\"query\":{\"type\":\"string\"}},\"required\":[\"query\"]}}]}");
            case "tools/call" -> result(id, "{\"content\":[{\"type\":\"text\",\"text\":\"" + escape(search(group(QUERY, line, ""))) + "\"}]}");
            default -> "{\"jsonrpc\":\"2.0\",\"id\":" + id + ",\"error\":{\"code\":-32601,\"message\":\"unknown method\"}}";
        };
    }

    static String search(String query) {
        if (query.isBlank()) {
            return "질문을 입력해 주세요.";
        }
        for (String sentence : DOCUMENT.split("\\n")) {
            if (sentence.contains(query) || query.contains("Vaadin") && sentence.contains("Vaadin")) {
                return sentence.trim();
            }
        }
        return "문서에서 찾을 수 없습니다.";
    }

    private static String result(String id, String payload) {
        return "{\"jsonrpc\":\"2.0\",\"id\":" + id + ",\"result\":" + payload + "}";
    }

    private static String group(Pattern pattern, String line, String fallback) {
        Matcher matcher = pattern.matcher(line);
        return matcher.find() ? matcher.group(1) : fallback;
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
