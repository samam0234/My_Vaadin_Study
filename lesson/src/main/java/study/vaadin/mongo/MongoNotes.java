package study.vaadin.mongo;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("mongo")
public class MongoNotes {

    private final NoteRepository notes;

    public MongoNotes(NoteRepository notes) {
        this.notes = notes;
    }

    public NoteDocument save(String title, String body) {
        return notes.save(new NoteDocument(title, body));
    }

    public long count() {
        return notes.count();
    }
}
