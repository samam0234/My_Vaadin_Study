package study.vaadin.api;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import study.vaadin.api.ApiModels.CommentItem;
import study.vaadin.api.ApiModels.CommentWrite;
import study.vaadin.api.ApiModels.Data;
import study.vaadin.service.CommentService;

@RestController
@RequestMapping("/api")
public class CommentController {

    private final CommentService comments;

    public CommentController(CommentService comments) {
        this.comments = comments;
    }

    @GetMapping("/posts/{id}/comments")
    Data<List<CommentItem>> list(@PathVariable long id) {
        return new Data<>(comments.list(id));
    }

    @PostMapping("/posts/{id}/comments")
    ResponseEntity<Data<CommentItem>> create(@PathVariable long id, @Valid @RequestBody CommentWrite request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new Data<>(comments.create(id, request)));
    }

    @DeleteMapping("/comments/{id}")
    ResponseEntity<Void> delete(@PathVariable long id) {
        comments.delete(id);
        return ResponseEntity.noContent().build();
    }
}
