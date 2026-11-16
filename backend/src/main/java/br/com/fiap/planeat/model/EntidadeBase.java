package br.com.fiap.planeat.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Classe base abstrata para herança de atributos comuns a todas as entidades do sistema.
 * Provê identificador único (ID), data de criação e operações de igualdade e hash code baseadas em identidade.
 */
public abstract class EntidadeBase {
    protected Long id;
    protected LocalDate dataCriacao;

    public EntidadeBase() {
        this.dataCriacao = LocalDate.now();
    }

    public EntidadeBase(Long id) {
        this();
        this.id = id;
    }

    public EntidadeBase(Long id, LocalDate dataCriacao) {
        this.id = id;
        this.dataCriacao = dataCriacao != null ? dataCriacao : LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntidadeBase that = (EntidadeBase) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", dataCriacao=" + dataCriacao +
                '}';
    }
}
