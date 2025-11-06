package com.example.cadastro.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Document(collection = "prontuarios")
public class Prontuario {
    @Id
    private String id;

    private Integer id_animal_sql;
    private Instant data_atualizacao;
    private List<Map<String, Object>> historico_medico;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Integer getId_animal_sql() { return id_animal_sql; }
    public void setId_animal_sql(Integer id_animal_sql) { this.id_animal_sql = id_animal_sql; }

    public Instant getData_atualizacao() { return data_atualizacao; }
    public void setData_atualizacao(Instant data_atualizacao) { this.data_atualizacao = data_atualizacao; }

    public List<Map<String, Object>> getHistorico_medico() { return historico_medico; }
    public void setHistorico_medico(List<Map<String, Object>> historico_medico) { this.historico_medico = historico_medico; }
}
