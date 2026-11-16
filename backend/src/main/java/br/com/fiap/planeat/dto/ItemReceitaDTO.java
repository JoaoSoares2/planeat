package br.com.fiap.planeat.dto;

/**
 * DTO para transferência dos itens/ingredientes de uma receita na API.
 */
public class ItemReceitaDTO {
    private Long ingredienteId;
    private String nomeIngrediente;
    private String categoria;
    private Double quantidade;
    private String unidadeMedida;

    public ItemReceitaDTO() {
    }

    public ItemReceitaDTO(Long ingredienteId, Double quantidade, String unidadeMedida) {
        this.ingredienteId = ingredienteId;
        this.quantidade = quantidade;
        this.unidadeMedida = unidadeMedida;
    }

    public ItemReceitaDTO(Long ingredienteId, String nomeIngrediente, String categoria, Double quantidade, String unidadeMedida) {
        this.ingredienteId = ingredienteId;
        this.nomeIngrediente = nomeIngrediente;
        this.categoria = categoria;
        this.quantidade = quantidade;
        this.unidadeMedida = unidadeMedida;
    }

    public Long getIngredienteId() {
        return ingredienteId;
    }

    public void setIngredienteId(Long ingredienteId) {
        this.ingredienteId = ingredienteId;
    }

    public String getNomeIngrediente() {
        return nomeIngrediente;
    }

    public void setNomeIngrediente(String nomeIngrediente) {
        this.nomeIngrediente = nomeIngrediente;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Double quantidade) {
        this.quantidade = quantidade;
    }

    public String getUnidadeMedida() {
        return unidadeMedida;
    }

    public void setUnidadeMedida(String unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }
}
