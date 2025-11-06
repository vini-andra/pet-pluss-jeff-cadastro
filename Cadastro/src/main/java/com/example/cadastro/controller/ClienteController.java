package com.example.cadastro.controller;

import com.example.cadastro.model.Animal;
import com.example.cadastro.model.Cliente;
import com.example.cadastro.repository.AnimalRepository;
import com.example.cadastro.repository.ClienteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteRepository clienteRepository;
    private final AnimalRepository animalRepository;

    public ClienteController(ClienteRepository clienteRepository, AnimalRepository animalRepository) {
        this.clienteRepository = clienteRepository;
        this.animalRepository = animalRepository;
    }

    @GetMapping
    public List<Cliente> list() {
        return clienteRepository.findAll();
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> criarClienteComAnimal(@RequestBody Map<String, String> body) {
        Cliente c = new Cliente();
        c.setNome(body.get("nome_cliente"));
        c.setCpf(body.get("cpf"));
        c.setTelefone(body.get("telefone"));
        clienteRepository.save(c);

        Animal a = new Animal();
        a.setCliente(c);
        a.setNome(body.get("nome_animal"));
        a.setEspecie(body.get("especie"));
        a.setRaca(body.get("raca"));
        animalRepository.save(a);

        return ResponseEntity.ok(Map.of("clienteId", c.getId_cliente(), "animalId", a.getId_animal()));
    }

}
