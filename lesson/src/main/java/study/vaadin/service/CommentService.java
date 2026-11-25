package study.vaadin.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.vaadin.api.ApiModels.Author;
import study.vaadin.api.ApiModels.CommentItem;
import study.vaadin.api.ApiModels.CommentWrite;
import study.vaadin.domain.Comment;
import study.vaadin.domain.Post;
import study.vaadin.domain.UserAccount;
import study.vaadin.repo.CommentRepository;
import study.vaadin.repo.PostRepository;
import study.vaadin.security.CurrentUser;

@Service
public class CommentService {

    private final CommentRepository comments;
    private final PostRepository posts;
    private final CurrentUser currentUser;

    public CommentService(CommentRepository comments, PostRepository posts, CurrentUser currentUser) {
        this.comments = comments;
        this.posts = posts;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public List<CommentItem> list(long postId) {
        requirePost(postId);
        return comments.findByPostIdOrderByCreatedAtAsc(postId).stream()
                .map(comment -> new CommentItem(
                        comment.getId(),
                        comment.getBody(),
                        new Author(comment.getAuthor().getId(), comment.getAuthor().getNickname()),
                        comment.getCreatedAt()))
                .toList();
    }

    @Transactional
    public CommentItem create(long postId, CommentWrite request) {
        Post post = requirePost(postId);
        UserAccount author = currentUser.require();
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setBody(request.body().trim());
        comments.save(comment);
        post.setCommentCount(post.getCommentCount() + 1);
        return new CommentItem(comment.getId(), comment.getBody(), new Author(author.getId(), author.getNickname()), comment.getCreatedAt());
    }

    @Transactional
    public void delete(long commentId) {
        Comment comment = comments.findById(commentId)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "댓글을 찾을 수 없습니다."));
        UserAccount user = currentUser.require();
        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new ApiException(403, "FORBIDDEN", "작성자만 삭제할 수 있습니다.");
        }
        Post post = comment.getPost();
        comments.delete(comment);
        post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
    }

    private Post requirePost(long postId) {
        return posts.findById(postId).orElseThrow(() -> new ApiException(404, "NOT_FOUND", "글을 찾을 수 없습니다."));
    }
}
