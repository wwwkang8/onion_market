package com.onion.backend.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.onion.backend.dto.WriteArticleDto;
import com.onion.backend.entity.Article;
import com.onion.backend.service.ArticleService;
import com.onion.backend.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards")
@Slf4j
public class ArticleController {

    private final AuthenticationManager authenticationManager;
    private final ArticleService articleService;
    private final CommentService commentService;

    @PostMapping("/{boardId}/articles")
    public ResponseEntity<Article> writeArticle(@RequestBody WriteArticleDto writeArticleDto) {
        return ResponseEntity.ok(articleService.writeArticle(writeArticleDto));
    }

    @GetMapping("/{boardId}/articles")
    public ResponseEntity<List<Article>> getArticle(@PathVariable Long boardId,
                                                    @RequestParam(required = false) Long lastId,
                                                    @RequestParam(required = false) Long firstId) {
        if (lastId != null) {
            return ResponseEntity.ok(articleService.getOldArticle(boardId, lastId));
        }
        if (firstId != null) {
            return ResponseEntity.ok(articleService.getNewArticle(boardId, firstId));
        }
        return ResponseEntity.ok(articleService.firstGetArticle(boardId));
    }

    @GetMapping("/{boardId}/articles/{articleId}")
    public ResponseEntity<Article> getArticleWithComment(@PathVariable Long boardId, @PathVariable Long articleId) {
        CompletableFuture<Article> article = commentService.getArticleWithComment(boardId, articleId);
        return ResponseEntity.ok(article.resultNow());
    }

    // Redis 사용한 조회 기능
    @GetMapping("/{boardId}/articles/redis/{articleId}")
    public ResponseEntity<Article> getArticle(@PathVariable Long boardId, @PathVariable Long articleId) {
        return ResponseEntity.ok(articleService.getArticle(boardId, articleId));
    }

    //QueryDsl 연습
//    public ResponseEntity<Article> getArticleQueryDsl(@PathVariable Long boardId, @PathVariable Long articleId) {
//        return ResponseEntity.ok(articleService.getArticleQueryDsl(boardId, articleId));
//    }

}
