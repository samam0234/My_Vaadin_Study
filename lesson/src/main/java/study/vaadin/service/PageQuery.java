package study.vaadin.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record PageQuery(int page, int limit, String q, String sort) {

    public static PageQuery of(Integer page, Integer limit, String q, String sort) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeLimit = limit == null || limit < 1 ? 10 : Math.min(limit, 50);
        String query = q == null ? "" : q.trim();
        String safeSort = switch (sort == null ? "" : sort) {
            case "createdAt", "-createdAt", "-commentCount" -> sort;
            default -> "-createdAt";
        };
        return new PageQuery(safePage, safeLimit, query, safeSort);
    }

    public Pageable pageable() {
        Sort sorting = switch (sort) {
            case "createdAt" -> Sort.by("createdAt").ascending();
            case "-commentCount" -> Sort.by("commentCount").descending().and(Sort.by("id").descending());
            default -> Sort.by("createdAt").descending().and(Sort.by("id").descending());
        };
        return PageRequest.of(page - 1, limit, sorting);
    }
}
