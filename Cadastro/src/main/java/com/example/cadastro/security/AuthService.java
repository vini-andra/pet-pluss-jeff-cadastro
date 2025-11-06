package com.example.cadastro.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    record TokenInfo(String email, Instant expiresAt) {}

    private final Map<String, TokenInfo> store = new ConcurrentHashMap<>();

    public String createToken(String email) {
        String token = UUID.randomUUID().toString();
        store.put(token, new TokenInfo(email, Instant.now().plusSeconds(60 * 60)));
        return token;
    }

    public String validate(String token) {
        if (token == null) return null;
        TokenInfo t = store.get(token);
        if (t == null) return null;
        if (t.expiresAt.isBefore(Instant.now())) {
            store.remove(token);
            return null;
        }
        return t.email;
    }
}
