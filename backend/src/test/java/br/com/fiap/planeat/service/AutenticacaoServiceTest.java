package br.com.fiap.planeat.service;

import br.com.fiap.planeat.dao.UsuarioDAO;
import br.com.fiap.planeat.model.Usuario;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutenticacaoServiceTest {
    @Test
    void registrationStoresAHashAndNormalizesEmail() {
        InMemoryUsuarioDAO dao = new InMemoryUsuarioDAO();
        AutenticacaoService service = new AutenticacaoService(dao);

        Usuario created = service.cadastrar("Ana Silva", "ANA@example.com", "senha-segura");

        assertEquals("ana@example.com", created.getEmail());
        assertNotEqualsPassword("senha-segura", created.getSenha());
        assertTrue(service.autenticar("ana@example.com", "senha-segura").isPresent());
        assertFalse(service.autenticar("ana@example.com", "senha-incorreta").isPresent());
    }

    @Test
    void successfulLegacyPlaintextLoginImmediatelyRehashesThePassword() {
        InMemoryUsuarioDAO dao = new InMemoryUsuarioDAO();
        Usuario legacy = new Usuario(7L, "Pessoa", "pessoa@example.com", "senha-antiga");
        dao.usuarios.put(legacy.getEmail(), legacy);
        AutenticacaoService service = new AutenticacaoService(dao);

        assertTrue(service.autenticar(legacy.getEmail(), "senha-antiga").isPresent());
        assertNotEqualsPassword("senha-antiga", legacy.getSenha());
    }

    private static void assertNotEqualsPassword(String password, String storedValue) {
        org.junit.jupiter.api.Assertions.assertNotEquals(password, storedValue);
        assertTrue(storedValue.startsWith("pbkdf2-sha256$"));
    }

    private static final class InMemoryUsuarioDAO extends UsuarioDAO {
        private final java.util.Map<String, Usuario> usuarios = new java.util.HashMap<>();

        @Override
        public Optional<Usuario> buscarPorEmail(String email) {
            return Optional.ofNullable(usuarios.get(email.toLowerCase(java.util.Locale.ROOT)));
        }

        @Override
        public Long inserir(Usuario usuario) {
            usuario.setId(1L);
            usuarios.put(usuario.getEmail(), usuario);
            return usuario.getId();
        }

        @Override
        public void atualizarSenha(Long usuarioId, String senhaHash) {
            usuarios.values().stream()
                    .filter(usuario -> usuarioId.equals(usuario.getId()))
                    .findFirst()
                    .orElseThrow()
                    .setSenha(senhaHash);
        }
    }
}
