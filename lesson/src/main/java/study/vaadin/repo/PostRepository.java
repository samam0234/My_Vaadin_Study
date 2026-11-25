package study.vaadin.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.vaadin.domain.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = "author")
    @Query("""
            select p from Post p
            where (:q is null or :q = '' or lower(p.title) like lower(concat('%', :q, '%'))
               or lower(p.body) like lower(concat('%', :q, '%')))
            """)
    Page<Post> search(@Param("q") String q, Pageable pageable);
}
