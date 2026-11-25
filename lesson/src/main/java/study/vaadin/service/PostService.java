package study.vaadin.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.vaadin.api.ApiModels.Author;
import study.vaadin.api.ApiModels.PageMeta;
import study.vaadin.api.ApiModels.PostCard;
import study.vaadin.api.ApiModels.PostDetail;
import study.vaadin.api.ApiModels.PostPage;
import study.vaadin.api.ApiModels.PostPatch;
import study.vaadin.api.ApiModels.PostWrite;
import study.vaadin.domain.Post;
import study.vaadin.domain.UserAccount;
import study.vaadin.live.LiveBoard;
import study.vaadin.mail.MailGateway;
import study.vaadin.repo.PostRepository;
import study.vaadin.security.CurrentUser;

@Service
public class PostService {

    private final PostRepository posts;
    private final CurrentUser currentUser;
    private final MailGateway mail;
    private final LiveBoard liveBoard;

    public PostService(PostRepository posts, CurrentUser currentUser, MailGateway mail, LiveBoard liveBoard) {
        this.posts = posts;
        this.currentUser = currentUser;
        this.mail = mail;
        this.liveBoard = liveBoard;
    }

    @Transactional(readOnly = true)
    public PostPage list(String q, Integer page, Integer limit, String sort) {
        PageQuery query = PageQuery.of(page, limit, q, sort);
        Page<Post> result = posts.search(query.q(), query.pageable());
        List<PostCard> cards = result.getContent().stream().map(this::card).toList();
        int totalPages = result.getTotalElements() == 0 ? 0 : result.getTotalPages();
        return new PostPage(cards, new PageMeta(query.page(), query.limit(), result.getTotalElements(), totalPages, query.q()));
    }

    @Transactional(readOnly = true)
    public PostDetail find(long id) {
        return detail(load(id));
    }

    @Transactional
    public PostDetail create(PostWrite request) {
        UserAccount author = currentUser.require();
        Post post = new Post();
        post.setTitle(request.title().trim());
        post.setBody(request.body().trim());
        post.setTags(cleanTags(request.tags()));
        post.setAuthor(author);
        posts.save(post);
        mail.notifyNewPost(post.getTitle());
        liveBoard.publish(author.getNickname() + " 님이 글을 등록했습니다.");
        return detail(post);
    }

    @Transactional
    public PostDetail update(long id, PostPatch request) {
        Post post = load(id);
        requireAuthor(post);
        if (request.title() != null) {
            String title = request.title().trim();
            if (title.isEmpty() || title.length() > 200) {
                throw new ApiException(400, "VALIDATION_ERROR", "입력값을 확인하세요.", field("title", "1~200자로 입력하세요."));
            }
            post.setTitle(title);
        }
        if (request.body() != null) {
            String body = request.body().trim();
            if (body.isEmpty() || body.length() > 8000) {
                throw new ApiException(400, "VALIDATION_ERROR", "입력값을 확인하세요.", field("body", "본문을 입력하세요."));
            }
            post.setBody(body);
        }
        if (request.tags() != null) {
            post.setTags(cleanTags(request.tags()));
        }
        liveBoard.publish("글이 수정되었습니다. #" + id);
        return detail(post);
    }

    @Transactional
    public void delete(long id) {
        Post post = load(id);
        requireAuthor(post);
        posts.delete(post);
        liveBoard.publish("글이 삭제되었습니다. #" + id);
    }

    private Post load(long id) {
        return posts.findById(id).orElseThrow(() -> new ApiException(404, "NOT_FOUND", "글을 찾을 수 없습니다."));
    }

    private void requireAuthor(Post post) {
        UserAccount user = currentUser.require();
        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new ApiException(403, "FORBIDDEN", "작성자만 수정하거나 삭제할 수 있습니다.");
        }
    }

    private PostCard card(Post post) {
        return new PostCard(post.getId(), post.getTitle(), author(post), post.getCommentCount(), post.getCreatedAt());
    }

    private PostDetail detail(Post post) {
        post.getTags().size();
        return new PostDetail(
                post.getId(),
                post.getTitle(),
                post.getBody(),
                List.copyOf(post.getTags()),
                author(post),
                post.getCommentCount(),
                post.getCreatedAt(),
                post.getUpdatedAt());
    }

    private Author author(Post post) {
        UserAccount account = post.getAuthor();
        return new Author(account.getId(), account.getNickname());
    }

    private List<String> cleanTags(List<String> tags) {
        if (tags == null) {
            return new ArrayList<>();
        }
        return tags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(tag -> tag.trim().toLowerCase(Locale.ROOT))
                .distinct()
                .limit(8)
                .toList();
    }

    private java.util.Map<String, List<String>> field(String name, String message) {
        LinkedHashMap<String, List<String>> fields = new LinkedHashMap<>();
        fields.put(name, List.of(message));
        return fields;
    }
}
