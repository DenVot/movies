package ru.mipt.movies.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mipt.movies.auth.dto.LoginRequest;
import ru.mipt.movies.auth.dto.RegisterRequest;
import ru.mipt.movies.auth.entity.Role;
import ru.mipt.movies.auth.entity.User;
import ru.mipt.movies.auth.repository.UserRepository;
import ru.mipt.movies.auth.util.JwtUtil;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        userRepository.save(user);
        return jwtUtil.generateToken(user.getUsername(), user.getRole());
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        return jwtUtil.generateToken(user.getUsername(), user.getRole());
    }

    public UserInfo getUserInfo(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new IllegalArgumentException("Invalid token");
        }
        
        String username = jwtUtil.extractUsername(token);
        Role role = jwtUtil.extractRole(token);
        
        return new UserInfo(username, role);
    }

    public static class UserInfo {
        private final String username;
        private final Role role;

        public UserInfo(String username, Role role) {
            this.username = username;
            this.role = role;
        }

        public String getUsername() {
            return username;
        }

        public Role getRole() {
            return role;
        }
    }
}
