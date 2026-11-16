package br.com.fiap.planeat.security;

import br.com.fiap.planeat.controller.HttpHelper;
import br.com.fiap.planeat.dao.UsuarioDAO;
import br.com.fiap.planeat.model.Usuario;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Optional;

public final class AuthenticatedHandler implements HttpHandler {
    private static final String USER_ATTRIBUTE = "planeat.authenticatedUser";
    private final HttpHandler delegate;
    private final SessionManager sessions;
    private final UsuarioDAO usuarioDAO;

    public AuthenticatedHandler(HttpHandler delegate, SessionManager sessions) {
        this(delegate, sessions, new UsuarioDAO());
    }

    public AuthenticatedHandler(HttpHandler delegate, SessionManager sessions, UsuarioDAO usuarioDAO) {
        this.delegate = delegate;
        this.sessions = sessions;
        this.usuarioDAO = usuarioDAO;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpHelper.aplicarCorsEVerificarOptions(exchange)) return;
        Optional<Long> userId = sessions.authenticate(exchange);
        if (userId.isEmpty()) {
            HttpHelper.enviarErro(exchange, 401, "Sessão inválida ou expirada.");
            return;
        }
        Optional<Usuario> usuario = usuarioDAO.buscarPorId(userId.get());
        if (usuario.isEmpty()) {
            HttpHelper.enviarErro(exchange, 401, "Sessão inválida ou expirada.");
            return;
        }
        exchange.setAttribute(USER_ATTRIBUTE, usuario.get());
        if (!HttpHelper.requisicaoDeNavegadorPermitida(exchange)) {
            HttpHelper.enviarErro(exchange, 403, "Requisição não autorizada.");
            return;
        }
        delegate.handle(exchange);
    }

    public static Usuario usuarioAutenticado(HttpExchange exchange) {
        Object usuario = exchange.getAttribute(USER_ATTRIBUTE);
        if (!(usuario instanceof Usuario)) {
            throw new IllegalStateException("A rota exige um usuário autenticado.");
        }
        return (Usuario) usuario;
    }
}
