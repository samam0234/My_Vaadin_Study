package study.vaadin.ai;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class McpStudyServerTest {

    @Test
    void searchFindsVaadinSentenceAndRefusesUnknown() {
        String found = McpStudyServer.handle("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"search_document\",\"arguments\":{\"query\":\"Hilla\"}}}");
        assertTrue(found.contains("BrowserCallable"));
        String missing = McpStudyServer.search("양자컴퓨터 조리법");
        assertTrue(missing.contains("찾을 수 없습니다"));
    }
}
