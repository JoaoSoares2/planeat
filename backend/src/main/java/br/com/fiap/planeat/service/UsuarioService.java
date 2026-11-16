package br.com.fiap.planeat.service;

import br.com.fiap.planeat.dao.UsuarioDAO;
import br.com.fiap.planeat.exception.EntidadeNaoEncontradaException;
import br.com.fiap.planeat.exception.RegraNegocioException;
import br.com.fiap.planeat.model.Usuario;

import java.util.List;

/**
 * Camada de serviço e regras de negócio para gestão de usuários cidadãos.
 */
public class UsuarioService {

    private final UsuarioDAO usuarioDAO;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    public UsuarioService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public Long cadastrar(Usuario usuario) {
        if (usuario == null) {
            throw new RegraNegocioException("Dados do usuário não podem ser nulos.");
        }
        usuario.validarConsistencia();

        if (usuarioDAO.buscarPorEmail(usuario.getEmail()).isPresent()) {
            throw new RegraNegocioException("Já existe um usuário cadastrado com o e-mail: " + usuario.getEmail());
        }

        return usuarioDAO.inserir(usuario);
    }

    public Usuario buscarPorId(Long id) {
        if (id == null || id <= 0) {
            throw new RegraNegocioException("ID de usuário inválido: " + id);
        }
        return usuarioDAO.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(Usuario.class, id));
    }

    public Usuario buscarPorEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new RegraNegocioException("E-mail informado é inválido.");
        }
        return usuarioDAO.buscarPorEmail(email.trim())
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado para o e-mail: " + email));
    }

    public List<Usuario> listarTodos() {
        return usuarioDAO.listarTodos();
    }

    public boolean autenticar(String email, String senha) {
        if (email == null || senha == null) {
            return false;
        }
        return usuarioDAO.buscarPorEmail(email.trim())
                .map(u -> u.validarAcesso(senha))
                .orElse(false);
    }

    public void atualizar(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            throw new RegraNegocioException("ID do usuário é obrigatório para atualização.");
        }
        buscarPorId(usuario.getId());
        usuario.validarConsistencia();
        usuarioDAO.atualizar(usuario);
    }

    public void excluir(Long id) {
        buscarPorId(id);
        usuarioDAO.excluir(id);
    }
}
