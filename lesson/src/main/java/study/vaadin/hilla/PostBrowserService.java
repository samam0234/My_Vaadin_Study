package study.vaadin.hilla;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import java.util.List;
import study.vaadin.api.ApiModels.PostCard;
import study.vaadin.service.PostService;

@BrowserCallable
@AnonymousAllowed
public class PostBrowserService {

    private final PostService posts;

    public PostBrowserService(PostService posts) {
        this.posts = posts;
    }

    public List<PostCard> list(String q, int page, int limit) {
        return posts.list(q, page, limit, "-createdAt").data();
    }
}
