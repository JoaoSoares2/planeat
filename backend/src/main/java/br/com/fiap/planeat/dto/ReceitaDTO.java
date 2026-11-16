package br.com.fiap.planeat.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para transferência e deserialização de receitas na API REST.
 */
public class ReceitaDTO {
    private Long id;
    private Long usuarioId;
    private String nome;
    private String modoPreparo;
    private Integer porcoes;
    private Integer tempoPreparoMinutos;
    private String tipoRefeicao;
    private LocalDate dataCriacao;
    private List<ItemReceitaDTO> ingredientes;

    public ReceitaDTO() {
        this.ingredientes = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getModoPreparo() {
        return modoPreparo;
    }

    public void setModoPreparo(String modoPreparo) {
        this.modoPreparo = modoPreparo;
    }

    public Integer getPorcoes() {
        return porcoes;
    }

    public void setPorcoes(Integer porcoes) {
        this.porcoes = porcoes;
    }

    public Integer getTempoPreparoMinutos() {
        return tempoPreparoMinutos;
    }

    public void setTempoPreparoMinutos(Integer tempoPreparoMinutos) {
        this.tempoPreparoMinutos = tempoPreparoMinutos;
    }

    public String getTipoRefeicao() {
        return tipoRefeicao;
    }

    public void setTipoRefeicao(String tipoRefeicao) {
        this.tipoRefeicao = tipoRefeicao;
    }

    public LocalDate getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public List<ItemReceitaDTO> getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(List<ItemReceitaDTO> ingredientes) {
        this.ingredientes = ingredientes != null ? ingredientes : new ArrayList<>();
    }
}
