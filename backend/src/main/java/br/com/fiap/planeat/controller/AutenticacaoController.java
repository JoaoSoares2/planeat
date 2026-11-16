package br.com.fiap.planeat.controller;

import br.com.fiap.planeat.model.Usuario;
import br.com.fiap.planeat.security.SessionManager;
import br.com.fiap.planeat.service.AutenticacaoService;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class AutenticacaoController implements HttpHandler {
    private final AutenticacaoService autenticacaoService;
    private final SessionManager sessions;

    public AutenticacaoController(SessionManager sessions) {
        this(new AutenticacaoService(), sessions);
    }

    public AutenticacaoController(AutenticacaoService autenticacaoService, SessionManager sessions) {
        this.autenticacaoService = autenticacaoService;
        this.sessions = sessions;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpHelper.aplicarCorsEVerificarOptions(exchange)) return;
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        if (!HttpHelper.requisicaoDeNavegadorPermitida(exchange)) {
            HttpHelper.enviarErro(exchange, 403, "Requisição não autorizada.");
            return;
        }
        try {
            if ("/api/auth/cadastro".equals(path) && "POST".equalsIgnoreCase(method)) {
                cadastrar(exchange);
            } else if ("/api/auth/login".equals(path) && "POST".equalsIgnoreCase(method)) {
                login(exchange);
            } else if ("/api/auth/sessao".equals(path) && "GET".equalsIgnoreCase(method)) {
                sessao(exchange);
            } else if ("/api/auth/logout".equals(path) && "POST".equalsIgnoreCase(method)) {
                sessions.clearSessionCookie(exchange);
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
            } else {
                HttpHelper.enviarErro(exchange, 404, "Rota de autenticação não encontrada.");
            }
        } catch (AutenticacaoService.EmailJaCadastradoException ex) {
            HttpHelper.enviarErro(exchange, 409, ex.getMessage());
        } catch (br.com.fiap.planeat.exception.RegraNegocioException ex) {
            HttpHelper.enviarErro(exchange, 400, ex.getMessage());
        } catch (JsonParseException | IllegalStateException ex) {
            HttpHelper.enviarErro(exchange, 400, "Os dados enviados são inválidos.");
        } catch (Exception ex) {
            HttpHelper.enviarErro(exchange, 500, "Não foi possível concluir a operação de autenticação.");
        }
    }

    private void cadastrar(HttpExchange exchange) throws IOException {
        CadastroRequest request = HttpHelper.getGson().fromJson(
                HttpHelper.lerCorpoRequisicao(exchange), CadastroRequest.class);
        if (request == null) {
            HttpHelper.enviarErro(exchange, 400, "Informe os dados para cadastro.");
            return;
        }
        Usuario usuario = autenticacaoService.cadastrar(request.nome, request.email, request.senha);
        HttpHelper.enviarRespostaJson(exchange, 201, usuarioSeguro(usuario));
    }

    private void login(HttpExchange exchange) throws IOException {
        LoginRequest request = HttpHelper.getGson().fromJson(
                HttpHelper.lerCorpoRequisicao(exchange), LoginRequest.class);
        if (request == null) {
            HttpHelper.enviarErro(exchange, 400, "Informe o e-mail e a senha.");
            return;
        }
        Optional<Usuario> usuario = autenticacaoService.autenticar(request.email, request.senha);
        if (usuario.isEmpty()) {
            HttpHelper.enviarErro(exchange, 401, "E-mail ou senha inválidos.");
            return;
        }
        sessions.setSessionCookie(exchange, usuario.get().getId());
        HttpHelper.enviarRespostaJson(exchange, 200, usuarioSeguro(usuario.get()));
    }

    private void sessao(HttpExchange exchange) throws IOException {
        Optional<Long> userId = sessions.authenticate(exchange);
        Optional<Usuario> usuario = userId.flatMap(autenticacaoService::buscarUsuario);
        if (usuario.isEmpty()) {
            sessions.clearSessionCookie(exchange);
            HttpHelper.enviarErro(exchange, 401, "Sessão inválida ou expirada.");
            return;
        }
        HttpHelper.enviarRespostaJson(exchange, 200, usuarioSeguro(usuario.get()));
    }

    private Map<String, Object> usuarioSeguro(Usuario usuario) {
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("id", usuario.getId());
        resposta.put("nome", usuario.getNome());
        resposta.put("email", usuario.getEmail());
        return resposta;
    }

    private static final class CadastroRequest {
        private String nome;
        private String email;
        private String senha;
    }

    private static final class LoginRequest {
        private String email;
        private String senha;
    }
}
