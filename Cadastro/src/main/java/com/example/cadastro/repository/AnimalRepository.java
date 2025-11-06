package com.example.cadastro.repository;

import com.example.cadastro.model.Animal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnimalRepository extends JpaRepository<Animal, Integer> {
    List<Animal> findByClienteId_cliente(Integer id_cliente);
}
