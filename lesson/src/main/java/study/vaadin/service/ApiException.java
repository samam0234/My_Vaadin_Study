package study.vaadin.service;

import java.util.Map;

public class ApiException extends RuntimeException {

    private final int status;
    private final String code;
    private final Map<String, java.util.List<String>> fields;

    public ApiException(int status, String code, String message) {
        this(status, code, message, null);
    }

    public ApiException(int status, String code, String message, Map<String, java.util.List<String>> fields) {
        super(message);
        this.status = status;
        this.code = code;
        this.fields = fields;
    }

    public int status() {
        return status;
    }

    public String code() {
        return code;
    }

    public Map<String, java.util.List<String>> fields() {
        return fields;
    }
}
