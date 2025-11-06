package com.example.cadastro.controller;

import com.example.cadastro.model.Usuario;
import com.example.cadastro.repository.UsuarioRepository;
import com.example.cadastro.security.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    public AuthController(AuthenticationManager authenticationManager,
                          UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String senha = body.get("senha");

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, senha));

        Usuario u = usuarioRepository.findByEmail(email).orElseThrow();
        String token = authService.createToken(email);
        return ResponseEntity.ok(Map.of("token", token, "email", u.getEmail(), "nome", u.getNome()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String senha = body.get("senha");
        String nome = body.get("nome");

        if (usuarioRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email já cadastrado"));
        }
        Usuario u = new Usuario();
        u.setId_usuario("USR_" + System.currentTimeMillis()); // placeholder; prefer DB function
        u.setEmail(email);
        u.setNome(nome);
        u.setHash_senha(passwordEncoder.encode(senha));
        usuarioRepository.save(u);
        return ResponseEntity.ok(Map.of("created", true));
    }
}
