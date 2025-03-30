package com.onion.backend.service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import com.onion.backend.dto.WriteArticleDto;
import com.onion.backend.entity.Article;
import com.onion.backend.entity.Board;
import com.onion.backend.entity.User;
import com.onion.backend.exception.ResourceNotFoundException;
import com.onion.backend.repository.ArticleRepository;
import com.onion.backend.repository.BoardRepository;
import com.onion.backend.repository.UserRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleService {

    private final BoardRepository boardRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ARTICLE_CACHE_KEY = "article:";

    @Transactional
    public Article writeArticle(WriteArticleDto dto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Optional<User> author = userRepository.findByUsername(userDetails.getUsername());
        Optional<Board> board = boardRepository.findById(dto.getBoardId());
        if (author.isEmpty()) {
            throw new ResourceNotFoundException("author not found");
        }
        if (board.isEmpty()) {
            throw new ResourceNotFoundException("board not found");
        }

        Article article = new Article();
        article.setBoard(board.get());
        article.setAuthor(author.get());
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        articleRepository.save(article);

        // ✅ Redis 캐싱 처리 (5분 동안 유지)
//        String redisKey = ARTICLE_CACHE_KEY + article.getId();
//        redisTemplate.opsForValue().set(redisKey, article);
//        redisTemplate.expire(redisKey, Duration.ofMinutes(5));  // TTL 설정
//        log.info("Article cached: {}", redisKey);

        return article;
    }

    /**
     * 글 조회(Redis -> DB)
     * */
    public Article getArticle(Long boardId, Long articleId) {
        String redisKey = ARTICLE_CACHE_KEY + articleId;
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();

        // Redis 캐싱 확인
        if (redisTemplate.hasKey(redisKey)) {
            log.info("Cache hit: {}", redisKey);
            return (Article) ops.get(redisKey);
        }

        //Redis에 없으면 DB에서 조회 후 캐싱
        Article article = articleRepository.findById(articleId)
            .orElseThrow(() -> new ResourceNotFoundException("Article not found"));

        redisTemplate.opsForValue().set(redisKey, article);
        redisTemplate.expire(redisKey, Duration.ofMinutes(5));  // TTL 설정
        log.info("Cache miss, stored in Redis: {}", redisKey);

        return article;
    }

    public List<Article> firstGetArticle(Long boardId) {
        return articleRepository.findTop10ByBoardIdOrderByCreatedDateDesc(boardId);
    }
    public List<Article> getOldArticle(Long boardId, Long articleId) {
        return articleRepository.findTop10ByBoardIdAndArticleIdLessThanOrderByCreatedDateDesc(boardId, articleId);
    }
    public List<Article> getNewArticle(Long boardId, Long articleId) {
        return articleRepository.findTop10ByBoardIdAndArticleIdGreaterThanOrderByCreatedDateDesc(boardId, articleId);
    }

//    public List<Article> getArticleQueryDsl(Long boardId, Long articleId) {
//
//        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
//
//
//
//
//    }
}
