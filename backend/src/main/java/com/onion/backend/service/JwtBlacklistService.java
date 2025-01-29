package com.onion.backend.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import com.onion.backend.entity.JwtBlacklist;
import com.onion.backend.jwt.JwtUtil;
import com.onion.backend.repository.JwtBlacklistRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtBlacklistService {

    private final JwtBlacklistRepository jwtBlacklistRepository;

    private final JwtUtil jwtUtil;

    // 블랙리스트에 토큰 추가
    public void addToBlacklist(String token, LocalDateTime expirationTime, String username) {
        JwtBlacklist blacklist = new JwtBlacklist();
        blacklist.setToken(token);
        blacklist.setExpirationTime(expirationTime);
        blacklist.setUsername(username);
        jwtBlacklistRepository.save(blacklist);
    }

    // 블랙리스트에 있는지 확인
    public boolean isBlacklisted(String token) {
        return jwtBlacklistRepository.findByToken(token).isPresent();
    }

    public boolean isTokenBlacklisted(String currentToken) {
        String username = jwtUtil.getUsernameFromToken(currentToken);
        Optional<JwtBlacklist> blacklistedToken = jwtBlacklistRepository.findTopByUsernameOrderByExpirationTime(username);

        if(blacklistedToken.isEmpty()) {
            return false;
        }

        Instant instant = jwtUtil.getExpirationDateFromToken(currentToken).toInstant();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        return !blacklistedToken.get().getExpirationTime().isAfter(localDateTime);
    }

    // 만료된 블랙리스트 토큰 삭제
    public void cleanupExpiredTokens() {
        jwtBlacklistRepository.deleteByExpirationTimeBefore(LocalDateTime.now());
    }

}
