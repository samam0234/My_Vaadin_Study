package study.vaadin.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.core.env.Environment;

@Route("api-try")
@PageTitle("API 시험")
@PermitAll
public class ApiTryView extends VerticalLayout {

    public ApiTryView(Environment environment) {
        TextField email = new TextField("이메일");
        email.setValue("mina@example.com");
        TextField password = new TextField("비밀번호");
        password.setValue("password123");
        TextField title = new TextField("제목");
        TextArea body = new TextArea("본문");
        Paragraph result = new Paragraph("아직 보내지 않았습니다.");
        Button send = new Button("API로 등록");
        send.setDisableOnClick(true);
        send.addClickListener(event -> {
            try {
                result.setText(call(environment, email.getValue(), password.getValue(), title.getValue(), body.getValue()));
            } finally {
                send.setEnabled(true);
            }
        });
        add(new H2("상태 코드로 화면 바꾸기"), email, password, title, body, send, result,
                new Paragraph("201은 성공, 400은 검증 실패, 422가 오면 400과 같이 입력 오류로 보여 줍니다. 이 서버의 공통 스펙은 검증 실패를 400으로 보냅니다."));
    }

    private String call(Environment environment, String email, String password, String title, String body) {
        int port = environment.getProperty("local.server.port", Integer.class,
                environment.getProperty("server.port", Integer.class, 8080));
        String base = "http://127.0.0.1:" + port;
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> login = client.send(request(base + "/api/auth/login",
                    "{\"email\":\"" + escape(email) + "\",\"password\":\"" + escape(password) + "\"}", null),
                    HttpResponse.BodyHandlers.ofString());
            if (login.statusCode() != 200) {
                return login.statusCode() + " 로그인에 실패했습니다.";
            }
            String token = between(login.body(), "\"accessToken\":\"", "\"");
            HttpResponse<String> created = client.send(request(base + "/api/posts",
                    "{\"title\":\"" + escape(title) + "\",\"body\":\"" + escape(body) + "\",\"tags\":[]}", token),
                    HttpResponse.BodyHandlers.ofString());
            return switch (created.statusCode()) {
                case 201 -> "201 등록되었습니다.";
                case 400, 422 -> created.statusCode() + " 입력값을 확인하세요. " + created.body();
                case 401 -> "401 로그인이 필요합니다.";
                default -> created.statusCode() + " " + created.body();
            };
        } catch (Exception ex) {
            return "요청을 보내지 못했습니다.";
        }
    }

    private HttpRequest request(String url, String json, String token) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder.build();
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String between(String text, String start, String end) {
        int from = text.indexOf(start);
        if (from < 0) {
            return "";
        }
        int value = from + start.length();
        int to = text.indexOf(end, value);
        return to < 0 ? "" : text.substring(value, to);
    }
}
