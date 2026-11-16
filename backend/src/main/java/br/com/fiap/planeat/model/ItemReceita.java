package br.com.fiap.planeat.model;

import br.com.fiap.planeat.exception.RegraNegocioException;

import java.util.Objects;

/**
 * Representa a dose de um ingrediente associado a uma receita (Tabela associativa N:N).
 */
public class ItemReceita {
    private Ingrediente ingrediente;
    private Double quantidade;
    private String unidadeMedida;

    public ItemReceita() {
    }

    public ItemReceita(Ingrediente ingrediente, Double quantidade, String unidadeMedida) {
        this.ingrediente = ingrediente;
        this.quantidade = quantidade;
        this.unidadeMedida = unidadeMedida;
    }

    public Ingrediente getIngrediente() {
        return ingrediente;
    }

    public void setIngrediente(Ingrediente ingrediente) {
        this.ingrediente = ingrediente;
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

    public String formatarExibicao() {
        String nomeIngrediente = ingrediente != null ? ingrediente.getNome() : "";
        if (quantidade == null) {
            return nomeIngrediente;
        }
        return String.format("%.2f %s de %s", quantidade, unidadeMedida != null ? unidadeMedida : "", nomeIngrediente).trim();
    }

    public void validarConsistencia() {
        if (ingrediente == null) {
            throw new RegraNegocioException("O ingrediente é obrigatório no item da receita.");
        }
        if (quantidade == null || quantidade <= 0) {
            throw new RegraNegocioException("A quantidade do ingrediente deve ser maior que zero.");
        }
        if (unidadeMedida == null || unidadeMedida.trim().isEmpty()) {
            throw new RegraNegocioException("A unidade de medida do ingrediente é obrigatória.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemReceita that = (ItemReceita) o;
        return Objects.equals(ingrediente, that.ingrediente) &&
                Objects.equals(unidadeMedida, that.unidadeMedida);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingrediente, unidadeMedida);
    }

    @Override
    public String toString() {
        return "ItemReceita{" +
                "ingrediente=" + (ingrediente != null ? ingrediente.getNome() : null) +
                ", quantidade=" + quantidade +
                ", unidadeMedida='" + unidadeMedida + '\'' +
                '}';
    }
}
