package br.com.fiap.planeat.model;

import br.com.fiap.planeat.exception.RegraNegocioException;

import java.time.LocalDate;

/**
 * Entidade que representa o cidadão usuário da plataforma.
 */
public class Usuario extends EntidadeBase {
    private String nome;
    private String email;
    private String senha;

    public Usuario() {
        super();
    }

    public Usuario(String nome, String email, String senha) {
        super();
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    public Usuario(Long id, String nome, String email, String senha) {
        super(id);
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    public Usuario(Long id, LocalDate dataCriacao, String nome, String email, String senha) {
        super(id, dataCriacao);
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    /**
     * Valida se a senha informada corresponde à senha do usuário.
     */
    public boolean validarAcesso(String senhaInformada) {
        return this.senha != null && this.senha.equals(senhaInformada);
    }

    /**
     * Valida os campos obrigatórios do usuário para persistência.
     */
    public void validarConsistencia() {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RegraNegocioException("O nome do usuário é obrigatório.");
        }
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            throw new RegraNegocioException("E-mail do usuário inválido ou ausente.");
        }
        if (senha == null || senha.trim().isEmpty()) {
            throw new RegraNegocioException("A senha do usuário é obrigatória.");
        }
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", dataCriacao=" + dataCriacao +
                '}';
    }
}
