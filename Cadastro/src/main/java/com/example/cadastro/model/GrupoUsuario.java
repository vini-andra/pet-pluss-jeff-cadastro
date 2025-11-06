package com.example.cadastro.model;

import jakarta.persistence.*;

@Entity
@Table(name = "grupos_usuarios")
public class GrupoUsuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_grupo;

    @Column(unique = true, nullable = false, length = 50)
    private String nome_grupo;

    public Integer getId_grupo() { return id_grupo; }
    public void setId_grupo(Integer id_grupo) { this.id_grupo = id_grupo; }

    public String getNome_grupo() { return nome_grupo; }
    public void setNome_grupo(String nome_grupo) { this.nome_grupo = nome_grupo; }
}
