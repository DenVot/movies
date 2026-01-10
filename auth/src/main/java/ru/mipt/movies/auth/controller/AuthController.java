package ru.mipt.movies.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mipt.movies.auth.dto.AuthResponse;
import ru.mipt.movies.auth.dto.LoginRequest;
import ru.mipt.movies.auth.dto.RegisterRequest;
import ru.mipt.movies.auth.dto.UserInfoResponse;
import ru.mipt.movies.auth.service.AuthService;

@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    private void setJwtCookie(String token, HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        response.addCookie(cookie);
    }

    private String getJwtFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request, HttpServletResponse response) {
        try {
            String token = authService.register(request);
            setJwtCookie(token, response);
            return ResponseEntity.ok(new AuthResponse("Registration successful"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            String token = authService.login(request);
            setJwtCookie(token, response);
            return ResponseEntity.ok(new AuthResponse("Login successful"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser(HttpServletRequest request) {
        try {
            String token = getJwtFromCookie(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            AuthService.UserInfo userInfo = authService.getUserInfo(token);
            return ResponseEntity.ok(new UserInfoResponse(userInfo.getUsername(), userInfo.getRole()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
