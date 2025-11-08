package com.example.cadastro.repository;

import com.example.cadastro.model.Animal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AnimalRepository extends JpaRepository<Animal, Integer> {
    // explicit JPQL to avoid Spring Data property path parsing issues with underscore-named id field
    @Query("select a from Animal a where a.cliente.id_cliente = :id")
    List<Animal> findByClienteId(@Param("id") Integer id);
}
