package com.onion.backend.repository;

import java.util.List;

import com.onion.backend.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.authentication.jaas.JaasPasswordCallbackHandler;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query("SELECT a FROM Article a WHERE a.board.id = :boardId ORDER BY a.createdDate DESC")
    List<Article> findTop10ByBoardIdOrderByCreatedDateDesc(@Param("boardId") Long boardId);
    @Query("SELECT a FROM Article a WHERE a.board.id = :boardId AND a.id < :articleId ORDER BY a.createdDate DESC")
    List<Article> findTop10ByBoardIdAndArticleIdLessThanOrderByCreatedDateDesc(@Param("boardId") Long boardId,
                                                                               @Param("articleId") Long articleId);
    @Query("SELECT a FROM Article a WHERE a.board.id = :boardId AND a.id > :articleId ORDER BY a.createdDate DESC")
    List<Article> findTop10ByBoardIdAndArticleIdGreaterThanOrderByCreatedDateDesc(@Param("boardId") Long boardId,
                                                                                  @Param("articleId") Long articleId);

}
