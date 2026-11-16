package br.com.fiap.planeat.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para representação de item consolidado na lista de compras.
 */
public class ItemListaComprasDTO {
    private Long id;
    private Long ingredienteId;
    private String nome;
    private String categoria;
    private Double quantidadeTotal;
    private String unidadeMedida;
    private Boolean comprado;

    public ItemListaComprasDTO() {
        this.comprado = false;
    }

    public ItemListaComprasDTO(Long ingredienteId, String nome, String categoria, Double quantidadeTotal, String unidadeMedida, Boolean comprado) {
        this.ingredienteId = ingredienteId;
        this.nome = nome;
        this.categoria = categoria;
        this.quantidadeTotal = quantidadeTotal;
        this.unidadeMedida = unidadeMedida;
        this.comprado = comprado != null ? comprado : false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIngredienteId() {
        return ingredienteId;
    }

    public void setIngredienteId(Long ingredienteId) {
        this.ingredienteId = ingredienteId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Double getQuantidadeTotal() {
        return quantidadeTotal;
    }

    public void setQuantidadeTotal(Double quantidadeTotal) {
        this.quantidadeTotal = quantidadeTotal;
    }

    public String getUnidadeMedida() {
        return unidadeMedida;
    }

    public void setUnidadeMedida(String unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }

    public Boolean getComprado() {
        return comprado;
    }

    public void setComprado(Boolean comprado) {
        this.comprado = comprado;
    }
}
