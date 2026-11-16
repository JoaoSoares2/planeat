package br.com.fiap.planeat.service;

import br.com.fiap.planeat.dao.UsuarioDAO;
import br.com.fiap.planeat.exception.EntidadeNaoEncontradaException;
import br.com.fiap.planeat.exception.RegraNegocioException;
import br.com.fiap.planeat.model.Usuario;
import br.com.fiap.planeat.security.PasswordHasher;

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
        validarPoliticaSenha(usuario.getSenha());
        usuario.setSenha(PasswordHasher.hash(usuario.getSenha()));

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
        return new AutenticacaoService(usuarioDAO).autenticar(email, senha).isPresent();
    }

    public void atualizar(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            throw new RegraNegocioException("ID do usuário é obrigatório para atualização.");
        }
        buscarPorId(usuario.getId());
        usuario.validarConsistencia();
        if (!usuario.getSenha().startsWith("pbkdf2-sha256$")) {
            validarPoliticaSenha(usuario.getSenha());
            usuario.setSenha(PasswordHasher.hash(usuario.getSenha()));
        }
        usuarioDAO.atualizar(usuario);
    }

    public void excluir(Long id) {
        buscarPorId(id);
        usuarioDAO.excluir(id);
    }

    private void validarPoliticaSenha(String senha) {
        if (senha == null || senha.length() < 8) {
            throw new RegraNegocioException("A senha deve ter pelo menos 8 caracteres.");
        }
    }
}
