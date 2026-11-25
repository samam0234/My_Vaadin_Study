package study.vaadin.api;

import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import study.vaadin.service.ApiException;

@RestControllerAdvice(basePackages = "study.vaadin.api")
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    ResponseEntity<Map<String, Object>> api(ApiException ex, HttpServletRequest request) {
        return body(ex.status(), ex.code(), ex.getMessage(), ex.fields(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> invalid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, List<String>> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fields.computeIfAbsent(error.getField(), key -> new java.util.ArrayList<>()).add(error.getDefaultMessage()));
        return body(400, "VALIDATION_ERROR", "입력값을 확인하세요.", fields, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, Object>> unreadable(HttpServletRequest request) {
        return body(400, "BAD_REQUEST", "요청 본문을 해석할 수 없습니다.", null, request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<Map<String, Object>> tooLarge(HttpServletRequest request) {
        return body(413, "PAYLOAD_TOO_LARGE", "업로드 한도를 넘었습니다.", null, request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> unexpected(Exception ex, HttpServletRequest request) {
        log.error("처리하지 못한 오류 requestId={}", request.getAttribute(RequestIdFilter.ATTRIBUTE), ex);
        return body(500, "INTERNAL", "서버 오류가 발생했습니다.", null, request);
    }

    private ResponseEntity<Map<String, Object>> body(int status, String code, String message, Map<String, List<String>> fields, HttpServletRequest request) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", code);
        error.put("message", message);
        if (fields != null) {
            error.put("fields", fields);
        }
        Object requestId = request.getAttribute(RequestIdFilter.ATTRIBUTE);
        error.put("requestId", requestId == null ? "" : requestId.toString());
        return ResponseEntity.status(HttpStatus.valueOf(status)).body(Map.of("error", error));
    }
}
