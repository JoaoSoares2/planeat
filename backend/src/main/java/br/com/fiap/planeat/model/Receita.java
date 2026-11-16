package br.com.fiap.planeat.model;

import br.com.fiap.planeat.exception.RegraNegocioException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade de receita culinária com lista de ingredientes e porções.
 * Representa o núcleo do catálogo alimentar consciente do cidadão.
 */
public class Receita extends EntidadeBase {
    private Long usuarioId;
    private Usuario usuario;
    private String nome;
    private String modoPreparo;
    private Integer porcoes;
    private Integer tempoPreparoMinutos;
    private String tipoRefeicao;
    private List<ItemReceita> ingredientes;

    public Receita() {
        super();
        this.ingredientes = new ArrayList<>();
    }

    public Receita(String nome, String modoPreparo, Integer porcoes, Integer tempoPreparoMinutos, String tipoRefeicao) {
        super();
        this.nome = nome;
        this.modoPreparo = modoPreparo;
        this.porcoes = porcoes;
        this.tempoPreparoMinutos = tempoPreparoMinutos;
        this.tipoRefeicao = tipoRefeicao;
        this.ingredientes = new ArrayList<>();
    }

    public Receita(Long id, Long usuarioId, String nome, String modoPreparo, Integer porcoes, Integer tempoPreparoMinutos, String tipoRefeicao) {
        super(id);
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.modoPreparo = modoPreparo;
        this.porcoes = porcoes;
        this.tempoPreparoMinutos = tempoPreparoMinutos;
        this.tipoRefeicao = tipoRefeicao;
        this.ingredientes = new ArrayList<>();
    }

    public Receita(Long id, LocalDate dataCriacao, Long usuarioId, String nome, String modoPreparo, Integer porcoes, Integer tempoPreparoMinutos, String tipoRefeicao) {
        super(id, dataCriacao);
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.modoPreparo = modoPreparo;
        this.porcoes = porcoes;
        this.tempoPreparoMinutos = tempoPreparoMinutos;
        this.tipoRefeicao = tipoRefeicao;
        this.ingredientes = new ArrayList<>();
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

    public List<ItemReceita> getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(List<ItemReceita> ingredientes) {
        this.ingredientes = ingredientes != null ? ingredientes : new ArrayList<>();
    }

    /**
     * Adiciona um ingrediente com dose à receita.
     */
    public void adicionarIngrediente(ItemReceita item) {
        if (item != null) {
            this.ingredientes.add(item);
        }
    }

    /**
     * Remove um ingrediente da receita pelo seu identificador único.
     */
    public void removerIngrediente(Long ingredienteId) {
        if (ingredienteId != null) {
            this.ingredientes.removeIf(item ->
                    item.getIngrediente() != null && ingredienteId.equals(item.getIngrediente().getId())
            );
        }
    }

    /**
     * Valida as regras de negócio para a integridade da receita.
     */
    public void validarConsistencia() {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RegraNegocioException("O nome da receita é obrigatório.");
        }
        if (modoPreparo == null || modoPreparo.trim().isEmpty()) {
            throw new RegraNegocioException("O modo de preparo da receita é obrigatório.");
        }
        if (porcoes == null || porcoes <= 0) {
            throw new RegraNegocioException("O número de porções deve ser maior que zero.");
        }
        if (tempoPreparoMinutos == null || tempoPreparoMinutos <= 0) {
            throw new RegraNegocioException("O tempo de preparo em minutos deve ser maior que zero.");
        }
        if (tipoRefeicao == null || tipoRefeicao.trim().isEmpty()) {
            throw new RegraNegocioException("O tipo de refeição da receita é obrigatório.");
        }
        if (ingredientes == null || ingredientes.isEmpty()) {
            throw new RegraNegocioException("A receita deve possuir ao menos um ingrediente cadastrado.");
        }
        for (ItemReceita item : ingredientes) {
            item.validarConsistencia();
        }
    }

    @Override
    public String toString() {
        return "Receita{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", porcoes=" + porcoes +
                ", tempoPreparoMinutos=" + tempoPreparoMinutos +
                ", tipoRefeicao='" + tipoRefeicao + '\'' +
                ", totalIngredientes=" + (ingredientes != null ? ingredientes.size() : 0) +
                ", dataCriacao=" + dataCriacao +
                '}';
    }
}
