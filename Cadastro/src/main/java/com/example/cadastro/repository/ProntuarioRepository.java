package com.example.cadastro.repository;

import com.example.cadastro.document.Prontuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ProntuarioRepository extends MongoRepository<Prontuario, String> {
    Optional<Prontuario> findById_animal_sql(Integer id_animal_sql);
}
