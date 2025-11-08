package com.example.cadastro.config;

import com.example.cadastro.model.Animal;
import com.example.cadastro.model.Cliente;
import com.example.cadastro.model.GrupoUsuario;
import com.example.cadastro.model.Usuario;
import com.example.cadastro.repository.AnimalRepository;
import com.example.cadastro.repository.ClienteRepository;
import com.example.cadastro.repository.GrupoUsuarioRepository;
import com.example.cadastro.repository.UsuarioRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

/**
 * Inicializador de dados para ambiente de desenvolvimento (profile "dev").
 *
 * Cria alguns registros de exemplo para que você possa testar a API sem precisar
 * executar um PostgreSQL externo. Os dados são persistidos no datasource ativo
 * (no profile dev o H2 em memória é usado).
 */
@Component
@Profile("dev")
public class DataInitializer {

    private final ClienteRepository clienteRepository;
    private final AnimalRepository animalRepository;
    private final GrupoUsuarioRepository grupoUsuarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(ClienteRepository clienteRepository,
                           AnimalRepository animalRepository,
                           GrupoUsuarioRepository grupoUsuarioRepository,
                           UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.animalRepository = animalRepository;
        this.grupoUsuarioRepository = grupoUsuarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    @Transactional
    public void init() {
        // Não inserir dados se já existir algo
        if (usuarioRepository.count() > 0 || clienteRepository.count() > 0) {
            return;
        }

        // Criar grupos
        GrupoUsuario admin = new GrupoUsuario();
        admin.setNome_grupo("ADMIN");
        grupoUsuarioRepository.save(admin);

        GrupoUsuario user = new GrupoUsuario();
        user.setNome_grupo("USER");
        grupoUsuarioRepository.save(user);

        // Criar usuário administrador de exemplo
        Usuario uAdmin = new Usuario();
        uAdmin.setId_usuario("USR_DEV_ADMIN");
        uAdmin.setNome("Administrador Dev");
        uAdmin.setEmail("admin@local.dev");
        uAdmin.setHash_senha(passwordEncoder.encode("admin123"));
        uAdmin.setGrupo(admin);
        uAdmin.setAtivo(true);
        usuarioRepository.save(uAdmin);

        // Criar usuário comum de exemplo
        Usuario uUser = new Usuario();
        uUser.setId_usuario("USR_DEV_1");
        uUser.setNome("Usuário Dev");
        uUser.setEmail("user@local.dev");
        uUser.setHash_senha(passwordEncoder.encode("senha123"));
        uUser.setGrupo(user);
        uUser.setAtivo(true);
        usuarioRepository.save(uUser);

        // Criar cliente e animal de exemplo
        Cliente c = new Cliente();
        c.setNome("João Silva");
        c.setCpf("12345678901");
        c.setTelefone("(11)99999-9999");
        clienteRepository.save(c);

        Animal a = new Animal();
        a.setCliente(c);
        a.setNome("Rex");
        a.setEspecie("Canina");
        a.setRaca("Vira-lata");
        animalRepository.save(a);
    }
}
