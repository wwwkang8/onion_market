package com.onion.backend.jwt;

import java.io.IOException;
import java.security.Security;

import com.onion.backend.entity.JwtBlacklist;
import com.onion.backend.service.JwtBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private final UserDetailsService userDetailsService;

    private final JwtBlacklistService jwtBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Swagger 관련 요청이면 필터를 건너뛰기
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/swagger-ui") ||
            requestURI.startsWith("/v3/api-docs")||
            requestURI.startsWith("/api/users/login")||
            requestURI.startsWith("/api/users/signup")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);

        log.info("JwtAuthenticationFilter 입력된 토큰 : " + token);

        String username = jwtUtil.extractUsername(token);
        if(token != null && jwtUtil.validateToken(token, username)
            && !(jwtBlacklistService.isTokenBlacklisted(token))) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if(bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
