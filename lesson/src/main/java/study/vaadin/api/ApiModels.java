package study.vaadin.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public final class ApiModels {

    private ApiModels() {
    }

    public record Author(long id, String nickname) {
    }

    public record PostCard(long id, String title, Author author, int commentCount, Instant createdAt) {
    }

    public record PostDetail(
            long id,
            String title,
            String body,
            List<String> tags,
            Author author,
            int commentCount,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record PageMeta(int page, int limit, long total, int totalPages, String q) {
    }

    public record PostPage(List<PostCard> data, PageMeta meta) {
    }

    public record CommentItem(long id, String body, Author author, Instant createdAt) {
    }

    public record UserPublic(long id, String email, String nickname) {
    }

    public record LoginResult(UserPublic user, String accessToken) {
    }

    public record Data<T>(T data) {
    }

    public record Signup(
            @Email(message = "이메일 형식을 확인하세요.") @NotBlank(message = "이메일을 입력하세요.") String email,
            @NotBlank(message = "비밀번호를 입력하세요.") @Size(min = 8, max = 100, message = "비밀번호는 8자 이상입니다.") String password,
            @NotBlank(message = "별명을 입력하세요.") @Size(max = 40, message = "별명은 40자 이하입니다.") String nickname
    ) {
    }

    public record Login(
            @Email(message = "이메일 형식을 확인하세요.") @NotBlank(message = "이메일을 입력하세요.") String email,
            @NotBlank(message = "비밀번호를 입력하세요.") String password
    ) {
    }

    public record PostWrite(
            @NotBlank(message = "제목을 입력하세요.") @Size(max = 200, message = "1~200자로 입력하세요.") String title,
            @NotBlank(message = "본문을 입력하세요.") @Size(max = 8000, message = "본문은 8000자 이하입니다.") String body,
            List<@Size(max = 30, message = "태그는 30자 이하입니다.") String> tags
    ) {
    }

    public record PostPatch(String title, String body, List<String> tags) {
    }

    public record CommentWrite(
            @NotBlank(message = "댓글을 입력하세요.") @Size(max = 2000, message = "댓글은 2000자 이하입니다.") String body
    ) {
    }

    public record Checkout(String url, String mode) {
    }

    public record Summary(String summary, List<String> sources) {
    }
}
