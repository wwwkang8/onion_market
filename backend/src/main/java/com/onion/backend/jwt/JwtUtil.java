package com.onion.backend.jwt;

import java.security.Key;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    //private String secretKey = "sBfue5IysjLNcX3nm8o9/wCJcwtrzHz4edHTlZg4huE=";

    private long expirationTime = 3600000;

    // Secret Key (64 bytes 이상 권장)
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1시간 (밀리초 단위)

    /**
     * JWT 토큰 생성
     *
     * @param username 사용자 이름
     * @return 생성된 JWT 토큰
     */
    public static String generateToken(String username) {

        String token = Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(SECRET_KEY)
            .compact();

        System.out.println("토큰 : " + token);

        return token;
    }

    /**
     * JWT 토큰에서 클레임 추출
     *
     * @param token JWT 토큰
     * @return 추출된 클레임
     */
    public static Claims extractClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(SECRET_KEY)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    /**
     * JWT 토큰에서 사용자 이름 추출
     *
     * @param token JWT 토큰
     * @return 사용자 이름
     */
    public static String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * JWT 토큰이 만료되었는지 여부 확인
     *
     * @param token JWT 토큰
     * @return 만료 여부
     */
    public static boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    /**
     * JWT 토큰 유효성 검증
     *
     * @param token    JWT 토큰
     * @param username 사용자 이름
     * @return 유효한 토큰인지 여부
     */
    public static boolean validateToken(String token, String username) {
        return (extractUsername(token).equals(username) && !isTokenExpired(token));
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                        .setSigningKey(SECRET_KEY)
                        .parseClaimsJws(token)
                        .getBody();

        return claims.getSubject();
    }

    public Date getExpirationDateFromToken(String token)
    {
        Claims claims = Jwts.parser()
            .setSigningKey(SECRET_KEY)
            .parseClaimsJws(token)
            .getBody();

        return claims.getExpiration();
    }



}
