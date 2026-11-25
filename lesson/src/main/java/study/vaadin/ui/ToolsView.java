package study.vaadin.ui;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import study.vaadin.ai.SummaryClient;
import study.vaadin.config.AppProperties;
import study.vaadin.mongo.MongoNotes;
import study.vaadin.pay.CheckoutClient;
import org.springframework.beans.factory.ObjectProvider;

@Route("tools")
@PageTitle("도구")
@PermitAll
public class ToolsView extends VerticalLayout {

    public ToolsView(AppProperties properties, SummaryClient summaries, CheckoutClient checkout, ObjectProvider<MongoNotes> mongo) {
        Paragraph status = new Paragraph("");
        TextArea document = new TextArea("문서");
        document.setWidthFull();
        TextField question = new TextField("질문");
        Button ask = new Button("질문", event -> {
            var answer = summaries.ask(document.getValue(), question.getValue());
            status.setText(answer.summary());
        });
        Button pay = new Button("결제 시작", event -> {
            var result = checkout.start("게시판 이용권");
            status.setText(result.mode() + " " + result.url());
        });

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setMaxFileSize((int) Math.min(properties.uploadMaxBytes(), Integer.MAX_VALUE));
        upload.addSucceededListener(event -> {
            try {
                Path dir = Path.of(properties.uploadDir());
                Files.createDirectories(dir);
                Path target = dir.resolve(UUID.randomUUID() + "-" + event.getFileName());
                Files.copy(buffer.getInputStream(), target);
                status.setText("저장: " + target.getFileName());
            } catch (IOException ex) {
                status.setText("파일을 저장하지 못했습니다.");
            }
        });

        Button mongoSave = new Button("Mongo 메모", event -> {
            MongoNotes notes = mongo.getIfAvailable();
            if (notes == null) {
                status.setText("Mongo 리포지토리가 없습니다.");
                return;
            }
            try {
                var saved = notes.save(question.getValue().isBlank() ? "메모" : question.getValue(), document.getValue());
                status.setText("Mongo id " + saved.getId() + " / 개수 " + notes.count());
            } catch (RuntimeException ex) {
                status.setText("MongoDB에 연결되지 않았습니다. docker compose up -d mongo 후 mongo 프로파일로 실행하세요.");
            }
        });

        add(new H2("파일·결제·AI·Mongo"), document, question, ask, pay, upload, mongoSave, status);
    }
}
