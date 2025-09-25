package com.example.community_blog.repositories;

import com.example.community_blog.models.PostModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT p FROM PostModel p " +
            "LEFT JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.comments " +
            "LEFT JOIN FETCH p.tags " +
            "WHERE p.id = :id")
    Optional<PostModel> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT p FROM PostModel p WHERE p.id != :id ORDER BY (p.likeCount * 10 + p.shareCount * 5 + p.saveCount * 2) DESC")
    Page<PostModel> findNotable(@Param("id") Long id, Pageable pageable);
}
