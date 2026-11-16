package br.com.fiap.planeat.model;

import br.com.fiap.planeat.exception.RegraNegocioException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade de planejamento de refeições para a semana.
 * Permite ao cidadão organizar seu consumo alimentar prévio e eliminar compras excedentes.
 */
public class PlanejamentoSemanal extends EntidadeBase {
    private Long usuarioId;
    private Usuario usuario;
    private String titulo;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String status;
    private List<ItemPlanejamento> refeicoes;

    public PlanejamentoSemanal() {
        super();
        this.status = "ATIVO";
        this.refeicoes = new ArrayList<>();
    }

    public PlanejamentoSemanal(String titulo, LocalDate dataInicio, LocalDate dataFim) {
        super();
        this.titulo = titulo;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.status = "ATIVO";
        this.refeicoes = new ArrayList<>();
    }

    public PlanejamentoSemanal(Long id, Long usuarioId, String titulo, LocalDate dataInicio, LocalDate dataFim, String status) {
        super(id);
        this.usuarioId = usuarioId;
        this.titulo = titulo;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.status = status != null ? status : "ATIVO";
        this.refeicoes = new ArrayList<>();
    }

    public PlanejamentoSemanal(Long id, LocalDate dataCriacao, Long usuarioId, String titulo, LocalDate dataInicio, LocalDate dataFim, String status) {
        super(id, dataCriacao);
        this.usuarioId = usuarioId;
        this.titulo = titulo;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.status = status != null ? status : "ATIVO";
        this.refeicoes = new ArrayList<>();
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        if (usuario != null && usuario.getId() != null) {
            this.usuarioId = usuario.getId();
        }
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

    public List<ItemPlanejamento> getRefeicoes() {
        return refeicoes;
    }

    public void setRefeicoes(List<ItemPlanejamento> refeicoes) {
        this.refeicoes = refeicoes != null ? refeicoes : new ArrayList<>();
    }

    public void adicionarRefeicao(ItemPlanejamento item) {
        if (item != null) {
            this.refeicoes.add(item);
        }
    }

    public void removerRefeicao(Long itemPlanejamentoId) {
        if (itemPlanejamentoId != null) {
            this.refeicoes.removeIf(item -> itemPlanejamentoId.equals(item.getId()));
        }
    }

    /**
     * Valida se o período de datas é cronologicamente válido (data fim posterior ou igual a data início).
     */
    public boolean isPeriodoValido() {
        return dataInicio != null && dataFim != null && !dataFim.isBefore(dataInicio);
    }

    /**
     * Valida as regras de negócio para consistência do planejamento semanal.
     */
    public void validarConsistencia() {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new RegraNegocioException("O título do planejamento semanal é obrigatório.");
        }
        if (dataInicio == null || dataFim == null) {
            throw new RegraNegocioException("As datas de início e término do planejamento são obrigatórias.");
        }
        if (!isPeriodoValido()) {
            throw new RegraNegocioException("A data de término do planejamento deve ser igual ou posterior à data de início.");
        }
        if (status == null || (!status.equals("ATIVO") && !status.equals("CONCLUIDO") && !status.equals("CANCELADO"))) {
            throw new RegraNegocioException("Status de planejamento inválido. Valores aceitos: ATIVO, CONCLUIDO, CANCELADO.");
        }
    }

    @Override
    public String toString() {
        return "PlanejamentoSemanal{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", dataInicio=" + dataInicio +
                ", dataFim=" + dataFim +
                ", status='" + status + '\'' +
                ", totalRefeicoes=" + (refeicoes != null ? refeicoes.size() : 0) +
                ", dataCriacao=" + dataCriacao +
                '}';
    }
}
