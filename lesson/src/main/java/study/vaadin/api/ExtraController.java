package study.vaadin.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import study.vaadin.ai.SummaryClient;
import study.vaadin.api.ApiModels.Checkout;
import study.vaadin.api.ApiModels.Data;
import study.vaadin.api.ApiModels.Summary;
import study.vaadin.config.AppProperties;
import study.vaadin.pay.CheckoutClient;
import study.vaadin.service.ApiException;

@RestController
public class ExtraController {

    private final AppProperties properties;
    private final SummaryClient summaries;
    private final CheckoutClient checkout;

    public ExtraController(AppProperties properties, SummaryClient summaries, CheckoutClient checkout) {
        this.properties = properties;
        this.summaries = summaries;
        this.checkout = checkout;
    }

    @GetMapping(value = "/api-docs", produces = "application/yaml")
    ClassPathResource openApi() {
        return new ClassPathResource("openapi.yaml");
    }

    @PostMapping("/api/uploads")
    Data<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.getSize() > properties.uploadMaxBytes()) {
            throw new ApiException(413, "PAYLOAD_TOO_LARGE", "업로드 한도를 넘었습니다.");
        }
        String original = file.getOriginalFilename() == null ? "file.bin" : Path.of(file.getOriginalFilename()).getFileName().toString();
        Path dir = Path.of(properties.uploadDir());
        Files.createDirectories(dir);
        Path target = dir.resolve(UUID.randomUUID() + "-" + original);
        file.transferTo(target);
        return new Data<>(Map.of("name", original, "size", file.getSize(), "stored", target.getFileName().toString()));
    }

    @PostMapping("/api/ai/summarize")
    Data<Summary> summarize(@RequestParam String text) {
        return new Data<>(summaries.summarize(text));
    }

    @PostMapping("/api/ai/ask")
    Data<Summary> ask(@RequestParam String text, @RequestParam String question) {
        return new Data<>(summaries.ask(text, question));
    }

    @GetMapping(value = "/api/ai/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter stream(@RequestParam String text) {
        SseEmitter emitter = new SseEmitter(30_000L);
        Thread.ofVirtual().start(() -> {
            try {
                for (String token : summaries.tokens(text)) {
                    emitter.send(token);
                    Thread.sleep(20);
                }
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }

    @PostMapping("/api/checkout")
    ResponseEntity<Data<Checkout>> checkout(@RequestParam(defaultValue = "게시판 이용권") String title) {
        return ResponseEntity.ok(new Data<>(checkout.start(title)));
    }
}
