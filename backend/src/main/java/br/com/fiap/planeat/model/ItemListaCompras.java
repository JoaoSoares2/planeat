package br.com.fiap.planeat.model;

import br.com.fiap.planeat.exception.RegraNegocioException;

import java.time.LocalDate;

/**
 * Representa um item consolidado na lista de compras (soma exata dos ingredientes).
 * Permite ao cidadão checar itens no supermercado em tempo real, evitando duplicidades.
 */
public class ItemListaCompras extends EntidadeBase {
    private Long listaId;
    private Ingrediente ingrediente;
    private Double quantidadeTotal;
    private String unidadeMedida;
    private Boolean comprado;

    public ItemListaCompras() {
        super();
        this.comprado = false;
    }

    public ItemListaCompras(Ingrediente ingrediente, Double quantidadeTotal, String unidadeMedida) {
        super();
        this.ingrediente = ingrediente;
        this.quantidadeTotal = quantidadeTotal;
        this.unidadeMedida = unidadeMedida;
        this.comprado = false;
    }

    public ItemListaCompras(Ingrediente ingrediente, Double quantidadeTotal, String unidadeMedida, Boolean comprado) {
        super();
        this.ingrediente = ingrediente;
        this.quantidadeTotal = quantidadeTotal;
        this.unidadeMedida = unidadeMedida;
        this.comprado = comprado != null ? comprado : false;
    }

    public ItemListaCompras(Long id, Long listaId, Ingrediente ingrediente, Double quantidadeTotal, String unidadeMedida, Boolean comprado) {
        super(id);
        this.listaId = listaId;
        this.ingrediente = ingrediente;
        this.quantidadeTotal = quantidadeTotal;
        this.unidadeMedida = unidadeMedida;
        this.comprado = comprado != null ? comprado : false;
    }

    public ItemListaCompras(Long id, LocalDate dataCriacao, Long listaId, Ingrediente ingrediente, Double quantidadeTotal, String unidadeMedida, Boolean comprado) {
        super(id, dataCriacao);
        this.listaId = listaId;
        this.ingrediente = ingrediente;
        this.quantidadeTotal = quantidadeTotal;
        this.unidadeMedida = unidadeMedida;
        this.comprado = comprado != null ? comprado : false;
    }

    public Long getListaId() {
        return listaId;
    }

    public void setListaId(Long listaId) {
        this.listaId = listaId;
    }

    public Ingrediente getIngrediente() {
        return ingrediente;
    }

    public void setIngrediente(Ingrediente ingrediente) {
        this.ingrediente = ingrediente;
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

    /**
     * Alterna o status do item entre comprado e pendente.
     */
    public void alternarStatusComprado() {
        this.comprado = !Boolean.TRUE.equals(this.comprado);
    }

    public void validarConsistencia() {
        if (ingrediente == null) {
            throw new RegraNegocioException("O ingrediente é obrigatório no item da lista de compras.");
        }
        if (quantidadeTotal == null || quantidadeTotal <= 0) {
            throw new RegraNegocioException("A quantidade total consolidada deve ser maior que zero.");
        }
        if (unidadeMedida == null || unidadeMedida.trim().isEmpty()) {
            throw new RegraNegocioException("A unidade de medida é obrigatória.");
        }
    }

    @Override
    public String toString() {
        return "ItemListaCompras{" +
                "id=" + id +
                ", ingrediente=" + (ingrediente != null ? ingrediente.getNome() : null) +
                ", quantidadeTotal=" + quantidadeTotal +
                ", unidadeMedida='" + unidadeMedida + '\'' +
                ", comprado=" + comprado +
                ", dataCriacao=" + dataCriacao +
                '}';
    }
}
