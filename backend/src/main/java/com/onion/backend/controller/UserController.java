package com.onion.backend.controller;

import javax.naming.AuthenticationException;
import java.util.List;

import com.onion.backend.dto.LoginUserDto;
import com.onion.backend.dto.SignUpUserDto;
import com.onion.backend.entity.User;
import com.onion.backend.jwt.JwtUtil;
import com.onion.backend.service.CustomUserDetailsService;
import com.onion.backend.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtil jwtUtil;


    @GetMapping("/")
    public ResponseEntity<List<User>> getUsers() {
        List<User> users = userService.getUsers();
        return ResponseEntity.ok(users);
    }


    @PostMapping("/signup")
    public ResponseEntity<User> createUser(@RequestBody SignUpUserDto signUpUserDto) {
        User user = userService.createUser(signUpUserDto);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginUserDto loginUserDto, HttpServletResponse response) throws AuthenticationException {
        System.out.println("username : " + loginUserDto.getUsername() + ", password : " + loginUserDto.getPassword());

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginUserDto.getUsername(), loginUserDto.getPassword())
        );
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(loginUserDto.getUsername());

        String token = jwtUtil.generateToken(userDetails.getUsername());

        // JWT를 쿠키에 저장 (HttpOnly 옵션으로 XSS 공격 방지)
        Cookie cookie = new Cookie("onion_token", token);
        cookie.setHttpOnly(true);  // 자바스크립트에서 접근 불가능하게 설정
        cookie.setPath("/");       // 쿠키를 보낼 경로 설정
        cookie.setMaxAge(60 * 60); // 쿠키의 유효기간 (1시간)

        // 응답에 쿠키 추가
        response.addCookie(cookie);

        return token;
    }

    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {
        // JWT를 쿠키에 저장 (HttpOnly 옵션으로 XSS 공격 방지)
        Cookie cookie = new Cookie("onion_token", null);
        cookie.setHttpOnly(true);  // 자바스크립트에서 접근 불가능하게 설정
        cookie.setPath("/");       // 쿠키를 보낼 경로 설정
        cookie.setMaxAge(0); // 쿠키의 유효기간 (1시간)

        // 응답에 쿠키 추가
        response.addCookie(cookie);
    }

    @PostMapping("/token/validation")
    @ResponseStatus(HttpStatus.OK)
    public void jwtValidate(@RequestParam String username, @RequestParam String token) {
        System.out.println("/token/validation 유입 파라메터. username : " + username + ", token : "+token);

        if(!jwtUtil.validateToken(username, token))
        {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This username is not allowed");
        }
    }

}
