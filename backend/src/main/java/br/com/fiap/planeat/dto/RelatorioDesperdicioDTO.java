package br.com.fiap.planeat.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para métricas de impacto CitizenTech: aproveitamento de insumos e desperdício alimentar evitado.
 */
public class RelatorioDesperdicioDTO {
    private Long planejamentoId;
    private String tituloPlanejamento;
    private Integer totalRefeicoesPlanejadas;
    private Integer totalIngredientesDistintos;
    private Integer totalItensReaproveitados;
    private Double taxaAproveitamentoPercentual;
    private List<ItemAproveitamentoDTO> itensAproveitamento;

    public static class ItemAproveitamentoDTO {
        private String nomeIngrediente;
        private String categoria;
        private Integer frequenciaCardapio;
        private Double consumoTotalPlanejado;
        private String unidadeMedida;
        private String indicadorConsumoConsciente;

        public ItemAproveitamentoDTO() {
        }

        public ItemAproveitamentoDTO(String nomeIngrediente, String categoria, Integer frequenciaCardapio, Double consumoTotalPlanejado, String unidadeMedida, String indicadorConsumoConsciente) {
            this.nomeIngrediente = nomeIngrediente;
            this.categoria = categoria;
            this.frequenciaCardapio = frequenciaCardapio;
            this.consumoTotalPlanejado = consumoTotalPlanejado;
            this.unidadeMedida = unidadeMedida;
            this.indicadorConsumoConsciente = indicadorConsumoConsciente;
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

        public Integer getFrequenciaCardapio() {
            return frequenciaCardapio;
        }

        public void setFrequenciaCardapio(Integer frequenciaCardapio) {
            this.frequenciaCardapio = frequenciaCardapio;
        }

        public Double getConsumoTotalPlanejado() {
            return consumoTotalPlanejado;
        }

        public void setConsumoTotalPlanejado(Double consumoTotalPlanejado) {
            this.consumoTotalPlanejado = consumoTotalPlanejado;
        }

        public String getUnidadeMedida() {
            return unidadeMedida;
        }

        public void setUnidadeMedida(String unidadeMedida) {
            this.unidadeMedida = unidadeMedida;
        }

        public String getIndicadorConsumoConsciente() {
            return indicadorConsumoConsciente;
        }

        public void setIndicadorConsumoConsciente(String indicadorConsumoConsciente) {
            this.indicadorConsumoConsciente = indicadorConsumoConsciente;
        }
    }

    public RelatorioDesperdicioDTO() {
        this.itensAproveitamento = new ArrayList<>();
    }

    public Long getPlanejamentoId() {
        return planejamentoId;
    }

    public void setPlanejamentoId(Long planejamentoId) {
        this.planejamentoId = planejamentoId;
    }

    public String getTituloPlanejamento() {
        return tituloPlanejamento;
    }

    public void setTituloPlanejamento(String tituloPlanejamento) {
        this.tituloPlanejamento = tituloPlanejamento;
    }

    public Integer getTotalRefeicoesPlanejadas() {
        return totalRefeicoesPlanejadas;
    }

    public void setTotalRefeicoesPlanejadas(Integer totalRefeicoesPlanejadas) {
        this.totalRefeicoesPlanejadas = totalRefeicoesPlanejadas;
    }

    public Integer getTotalIngredientesDistintos() {
        return totalIngredientesDistintos;
    }

    public void setTotalIngredientesDistintos(Integer totalIngredientesDistintos) {
        this.totalIngredientesDistintos = totalIngredientesDistintos;
    }

    public Integer getTotalItensReaproveitados() {
        return totalItensReaproveitados;
    }

    public void setTotalItensReaproveitados(Integer totalItensReaproveitados) {
        this.totalItensReaproveitados = totalItensReaproveitados;
    }

    public Double getTaxaAproveitamentoPercentual() {
        return taxaAproveitamentoPercentual;
    }

    public void setTaxaAproveitamentoPercentual(Double taxaAproveitamentoPercentual) {
        this.taxaAproveitamentoPercentual = taxaAproveitamentoPercentual;
    }

    public List<ItemAproveitamentoDTO> getItensAproveitamento() {
        return itensAproveitamento;
    }

    public void setItensAproveitamento(List<ItemAproveitamentoDTO> itensAproveitamento) {
        this.itensAproveitamento = itensAproveitamento != null ? itensAproveitamento : new ArrayList<>();
    }
}
