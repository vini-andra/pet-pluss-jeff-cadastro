package com.example.cadastro.model;

import jakarta.persistence.*;

@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_cliente;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(unique = true, length = 11)
    private String cpf;

    @Column(length = 15)
    private String telefone;

    public Integer getId_cliente() { return id_cliente; }
    public void setId_cliente(Integer id_cliente) { this.id_cliente = id_cliente; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
}
