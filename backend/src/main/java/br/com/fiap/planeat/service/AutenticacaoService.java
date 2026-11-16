package br.com.fiap.planeat.service;

import br.com.fiap.planeat.dao.UsuarioDAO;
import br.com.fiap.planeat.exception.RegraNegocioException;
import br.com.fiap.planeat.model.Usuario;
import br.com.fiap.planeat.security.PasswordHasher;

import java.util.Locale;
import java.util.Optional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.regex.Pattern;

public class AutenticacaoService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final UsuarioDAO usuarioDAO;

    public AutenticacaoService() {
        this(new UsuarioDAO());
    }

    public AutenticacaoService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public Usuario cadastrar(String nome, String email, String senha) {
        String nomeNormalizado = nome == null ? "" : nome.trim();
        String emailNormalizado = normalizarEmail(email);
        validarCredenciais(nomeNormalizado, emailNormalizado, senha);
        if (nomeNormalizado.length() > 100 || emailNormalizado.length() > 100) {
            throw new RegraNegocioException("Nome ou e-mail excede o limite permitido.");
        }
        if (usuarioDAO.buscarPorEmail(emailNormalizado).isPresent()) {
            throw new EmailJaCadastradoException();
        }
        Usuario usuario = new Usuario(nomeNormalizado, emailNormalizado, PasswordHasher.hash(senha));
        usuario.validarConsistencia();
        usuarioDAO.inserir(usuario);
        return usuario;
    }

    public Optional<Usuario> autenticar(String email, String senha) {
        if (email == null || senha == null) return Optional.empty();
        Optional<Usuario> encontrado = usuarioDAO.buscarPorEmail(normalizarEmail(email));
        if (encontrado.isEmpty()) return Optional.empty();
        Usuario usuario = encontrado.get();
        String senhaArmazenada = usuario.getSenha();
        if (PasswordHasher.matches(senha, senhaArmazenada)) return Optional.of(usuario);
        if (senhaArmazenada != null && !senhaArmazenada.startsWith("$2") &&
                MessageDigest.isEqual(senha.getBytes(StandardCharsets.UTF_8),
                        senhaArmazenada.getBytes(StandardCharsets.UTF_8))) {
            usuarioDAO.atualizarSenha(usuario.getId(), PasswordHasher.hash(senha));
            return Optional.of(usuario);
        }
        return Optional.empty();
    }

    public Optional<Usuario> buscarUsuario(Long id) {
        return usuarioDAO.buscarPorId(id);
    }

    private static void validarCredenciais(String nome, String email, String senha) {
        if (nome.isBlank()) throw new RegraNegocioException("O nome completo é obrigatório.");
        if (!EMAIL_PATTERN.matcher(email).matches()) throw new RegraNegocioException("Informe um e-mail válido.");
        if (senha == null || senha.length() < 8) {
            throw new RegraNegocioException("A senha deve ter pelo menos 8 caracteres.");
        }
    }

    private static String normalizarEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    public static final class EmailJaCadastradoException extends RuntimeException {
        public EmailJaCadastradoException() {
            super("Já existe uma conta cadastrada com este e-mail.");
        }
    }
}
