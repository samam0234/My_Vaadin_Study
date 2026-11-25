package study.vaadin.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DotEnv {

    private DotEnv() {
    }

    public static void load() {
        Path file = Path.of(".env");
        if (!Files.exists(file)) {
            file = Path.of("lesson", ".env");
        }
        if (!Files.exists(file)) {
            return;
        }
        try {
            for (String raw : Files.readAllLines(file)) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
                    continue;
                }
                int split = line.indexOf('=');
                String key = line.substring(0, split).trim();
                String value = line.substring(split + 1).trim();
                if (!key.isEmpty() && System.getenv(key) == null && System.getProperty(key) == null) {
                    System.setProperty(key, value);
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException(".env 파일을 읽지 못했습니다.", ex);
        }
    }
}
