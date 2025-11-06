package com.example.cadastro.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "animais")
public class Animal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_animal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @Column(length = 50)
    private String nome;

    @Column(length = 30)
    private String especie;

    @Column(length = 30)
    private String raca;

    private LocalDate data_nascimento;

    public Integer getId_animal() { return id_animal; }
    public void setId_animal(Integer id_animal) { this.id_animal = id_animal; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }

    public String getRaca() { return raca; }
    public void setRaca(String raca) { this.raca = raca; }

    public LocalDate getData_nascimento() { return data_nascimento; }
    public void setData_nascimento(LocalDate data_nascimento) { this.data_nascimento = data_nascimento; }
}
