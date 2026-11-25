package study.vaadin.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("notes")
public class NoteDocument {

    @Id
    private String id;
    private String title;
    private String body;

    public NoteDocument() {
    }

    public NoteDocument(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }
}
