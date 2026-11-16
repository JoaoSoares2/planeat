package br.com.fiap.planeat.model;

import br.com.fiap.planeat.exception.RegraNegocioException;

import java.time.LocalDate;

/**
 * Entidade de ingrediente do catálogo padronizado.
 * Evita compras duplicadas e inconsistências na lista consolidada.
 */
public class Ingrediente extends EntidadeBase {
    private String nome;
    private CategoriaAlimento categoria;
    private String unidadePadrao;

    public Ingrediente() {
        super();
    }

    public Ingrediente(String nome, CategoriaAlimento categoria, String unidadePadrao) {
        super();
        this.nome = nome;
        this.categoria = categoria;
        this.unidadePadrao = unidadePadrao;
    }

    public Ingrediente(Long id, String nome, CategoriaAlimento categoria, String unidadePadrao) {
        super(id);
        this.nome = nome;
        this.categoria = categoria;
        this.unidadePadrao = unidadePadrao;
    }

    public Ingrediente(Long id, LocalDate dataCriacao, String nome, CategoriaAlimento categoria, String unidadePadrao) {
        super(id, dataCriacao);
        this.nome = nome;
        this.categoria = categoria;
        this.unidadePadrao = unidadePadrao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public CategoriaAlimento getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaAlimento categoria) {
        this.categoria = categoria;
    }

    public String getUnidadePadrao() {
        return unidadePadrao;
    }

    public void setUnidadePadrao(String unidadePadrao) {
        this.unidadePadrao = unidadePadrao;
    }

    public void validarConsistencia() {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RegraNegocioException("O nome do ingrediente é obrigatório.");
        }
        if (categoria == null) {
            throw new RegraNegocioException("A categoria do ingrediente é obrigatória.");
        }
        if (unidadePadrao == null || unidadePadrao.trim().isEmpty()) {
            throw new RegraNegocioException("A unidade de medida padrão é obrigatória.");
        }
    }

    @Override
    public String toString() {
        return "Ingrediente{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", categoria=" + (categoria != null ? categoria.getNome() : null) +
                ", unidadePadrao='" + unidadePadrao + '\'' +
                ", dataCriacao=" + dataCriacao +
                '}';
    }
}
