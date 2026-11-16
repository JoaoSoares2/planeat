package br.com.fiap.planeat.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para transferência e deserialização de planejamentos semanais na API REST.
 */
public class PlanejamentoDTO {
    private Long id;
    private Long usuarioId;
    private String titulo;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String status;
    private List<ItemPlanejamentoDTO> refeicoes;

    public static class ItemPlanejamentoDTO {
        private Long id;
        private Long receitaId;
        private String nomeReceita;
        private String diaSemana;
        private String tipoRefeicao;

        public ItemPlanejamentoDTO() {
        }

        public ItemPlanejamentoDTO(Long receitaId, String diaSemana, String tipoRefeicao) {
            this.receitaId = receitaId;
            this.diaSemana = diaSemana;
            this.tipoRefeicao = tipoRefeicao;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getReceitaId() {
            return receitaId;
        }

        public void setReceitaId(Long receitaId) {
            this.receitaId = receitaId;
        }

        public String getNomeReceita() {
            return nomeReceita;
        }

        public void setNomeReceita(String nomeReceita) {
            this.nomeReceita = nomeReceita;
        }

        public String getDiaSemana() {
            return diaSemana;
        }

        public void setDiaSemana(String diaSemana) {
            this.diaSemana = diaSemana;
        }

        public String getTipoRefeicao() {
            return tipoRefeicao;
        }

        public void setTipoRefeicao(String tipoRefeicao) {
            this.tipoRefeicao = tipoRefeicao;
        }
    }

    public PlanejamentoDTO() {
        this.refeicoes = new ArrayList<>();
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

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ItemPlanejamentoDTO> getRefeicoes() {
        return refeicoes;
    }

    public void setRefeicoes(List<ItemPlanejamentoDTO> refeicoes) {
        this.refeicoes = refeicoes != null ? refeicoes : new ArrayList<>();
    }
}
