package br.com.fiap.planeat.model;

import br.com.fiap.planeat.exception.RegraNegocioException;

import java.time.LocalDate;

/**
 * Associa uma receita a um dia da semana e refeição no plano alimentar.
 * Representa a granularidade diária da matriz semanal.
 */
public class ItemPlanejamento extends EntidadeBase {
    private Long planejamentoId;
    private Receita receita;
    private String diaSemana;
    private String tipoRefeicao;

    public ItemPlanejamento() {
        super();
    }

    public ItemPlanejamento(Receita receita, String diaSemana, String tipoRefeicao) {
        super();
        this.receita = receita;
        this.diaSemana = diaSemana;
        this.tipoRefeicao = tipoRefeicao;
    }

    public ItemPlanejamento(Long planejamentoId, Receita receita, String diaSemana, String tipoRefeicao) {
        super();
        this.planejamentoId = planejamentoId;
        this.receita = receita;
        this.diaSemana = diaSemana;
        this.tipoRefeicao = tipoRefeicao;
    }

    public ItemPlanejamento(Long id, Long planejamentoId, Receita receita, String diaSemana, String tipoRefeicao) {
        super(id);
        this.planejamentoId = planejamentoId;
        this.receita = receita;
        this.diaSemana = diaSemana;
        this.tipoRefeicao = tipoRefeicao;
    }

    public ItemPlanejamento(Long id, LocalDate dataCriacao, Long planejamentoId, Receita receita, String diaSemana, String tipoRefeicao) {
        super(id, dataCriacao);
        this.planejamentoId = planejamentoId;
        this.receita = receita;
        this.diaSemana = diaSemana;
        this.tipoRefeicao = tipoRefeicao;
    }

    public Long getPlanejamentoId() {
        return planejamentoId;
    }

    public void setPlanejamentoId(Long planejamentoId) {
        this.planejamentoId = planejamentoId;
    }

    public Receita getReceita() {
        return receita;
    }

    public void setReceita(Receita receita) {
        this.receita = receita;
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

    public void validarConsistencia() {
        if (receita == null) {
            throw new RegraNegocioException("A receita associada ao item de planejamento é obrigatória.");
        }
        if (diaSemana == null || diaSemana.trim().isEmpty()) {
            throw new RegraNegocioException("O dia da semana é obrigatório no item de planejamento.");
        }
        if (tipoRefeicao == null || tipoRefeicao.trim().isEmpty()) {
            throw new RegraNegocioException("O tipo de refeição é obrigatório no item de planejamento.");
        }
    }

    @Override
    public String toString() {
        return "ItemPlanejamento{" +
                "id=" + id +
                ", planejamentoId=" + planejamentoId +
                ", receita=" + (receita != null ? receita.getNome() : null) +
                ", diaSemana='" + diaSemana + '\'' +
                ", tipoRefeicao='" + tipoRefeicao + '\'' +
                ", dataCriacao=" + dataCriacao +
                '}';
    }
}
