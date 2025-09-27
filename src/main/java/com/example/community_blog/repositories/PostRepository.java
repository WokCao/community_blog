package com.example.community_blog.repositories;

import com.example.community_blog.models.PostModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<PostModel, Long> {

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update PostModel p set p.viewCount = p.viewCount + 1 where p.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @EntityGraph(attributePaths = {"author", "comments", "tags", "likedBy", "dislikedBy"})
    Optional<PostModel> findById(Long id);

    @Query("SELECT p FROM PostModel p WHERE p.id != :id ORDER BY (SIZE(p.likedBy) * 10 + p.shareCount * 5 + p.saveCount * 2) DESC")
    Page<PostModel> findNotable(@Param("id") Long id, Pageable pageable);

    @Query("SELECT p FROM PostModel p WHERE p.author.id = :authorId ORDER BY (SIZE(p.likedBy) * 10 + p.shareCount * 5 + p.saveCount * 2) DESC")
    Page<PostModel> findAllByAuthorId(@Param("authorId") Long authorId, Pageable pageable);

    @Query(
            value = """
            SELECT p.*, SUM(SIMILARITY(t.tags, kw)) as relevance
            FROM posts p
            JOIN post_tags t ON p.id = t.post_id,
            unnest(ARRAY[:keywords]) kw
            WHERE p.id != :id
            GROUP BY p.id
            HAVING SUM(SIMILARITY(t.tags, kw)) > 0.3
            """,
            countQuery = """
            SELECT COUNT(*) FROM (
                SELECT p.id
                FROM posts p
                JOIN post_tags t ON p.id = t.post_id,
                unnest(ARRAY[:keywords]) kw
                GROUP BY p.id
                HAVING SUM(SIMILARITY(t.tags, kw)) > 0.3
            ) AS matching_posts
            """,
            nativeQuery = true
    )
    Page<PostModel> searchPostsByTagsFuzzy(@Param("id") Long id, @Param("keywords") String[] keywords, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.viewCount), 0) FROM PostModel p WHERE p.author.id = :authorId")
    Long sumViewCountByAuthorId(@Param("authorId") Long authorId);

    @Query("SELECT COALESCE(SUM(SIZE(p.likedBy)), 0) FROM PostModel p WHERE p.author.id = :authorId")
    Long sumLikeCountByAuthorId(@Param("authorId") Long authorId);
}
