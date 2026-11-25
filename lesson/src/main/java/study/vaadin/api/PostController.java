package study.vaadin.api;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import study.vaadin.api.ApiModels.Data;
import study.vaadin.api.ApiModels.PostDetail;
import study.vaadin.api.ApiModels.PostPage;
import study.vaadin.api.ApiModels.PostPatch;
import study.vaadin.api.ApiModels.PostWrite;
import study.vaadin.service.PostService;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService posts;

    public PostController(PostService posts) {
        this.posts = posts;
    }

    @GetMapping
    PostPage list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String sort) {
        return posts.list(q, page, limit, sort);
    }

    @GetMapping("/{id}")
    Data<PostDetail> find(@PathVariable long id) {
        return new Data<>(posts.find(id));
    }

    @PostMapping
    ResponseEntity<Data<PostDetail>> create(@Valid @RequestBody PostWrite request) {
        PostDetail created = posts.create(request);
        return ResponseEntity.created(URI.create("/api/posts/" + created.id())).body(new Data<>(created));
    }

    @PatchMapping("/{id}")
    Data<PostDetail> update(@PathVariable long id, @RequestBody PostPatch request) {
        return new Data<>(posts.update(id, request));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable long id) {
        posts.delete(id);
        return ResponseEntity.noContent().build();
    }
}
