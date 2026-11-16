package br.com.fiap.planeat.model;

import br.com.fiap.planeat.exception.RegraNegocioException;

import java.time.LocalDate;

/**
 * Categoria de classificação dos ingredientes para setorização no supermercado.
 * Exemplos: Hortifrúti, Açougue & Peixaria, Mercearia & Grãos, Laticínios & Ovos.
 */
public class CategoriaAlimento extends EntidadeBase {
    private String nome;

    public CategoriaAlimento() {
        super();
    }

    public CategoriaAlimento(String nome) {
        super();
        this.nome = nome;
    }

    public CategoriaAlimento(Long id, String nome) {
        super(id);
        this.nome = nome;
    }

    public CategoriaAlimento(Long id, LocalDate dataCriacao, String nome) {
        super(id, dataCriacao);
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void validarConsistencia() {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RegraNegocioException("O nome da categoria é obrigatório.");
        }
    }

    @Override
    public String toString() {
        return "CategoriaAlimento{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", dataCriacao=" + dataCriacao +
                '}';
    }
}
