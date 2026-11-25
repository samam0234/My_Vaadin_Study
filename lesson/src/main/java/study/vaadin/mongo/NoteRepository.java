package study.vaadin.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface NoteRepository extends MongoRepository<NoteDocument, String> {
}
