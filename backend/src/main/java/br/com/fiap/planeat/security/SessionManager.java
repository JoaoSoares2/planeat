package br.com.fiap.planeat.security;

import com.sun.net.httpserver.HttpExchange;

import br.com.fiap.planeat.dao.SessaoDAO;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

public final class SessionManager {
    public static final String COOKIE_NAME = "planeat_session";
    private static final long SESSION_SECONDS = 8 * 60 * 60;
    private static final SecureRandom RANDOM = new SecureRandom();
    private final boolean secureCookie;
    private final SessaoDAO sessaoDAO;

    public SessionManager(boolean secureCookie) {
        this(secureCookie, new SessaoDAO());
    }

    public SessionManager(boolean secureCookie, SessaoDAO sessaoDAO) {
        this.secureCookie = secureCookie;
        this.sessaoDAO = sessaoDAO;
    }

    public static SessionManager fromEnvironment() {
        boolean secure = !"false".equalsIgnoreCase(System.getenv("COOKIE_SECURE"));
        return new SessionManager(secure);
    }

    public void setSessionCookie(HttpExchange exchange, Long userId) {
        byte[] randomToken = new byte[32];
        RANDOM.nextBytes(randomToken);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomToken);
        Instant expiresAt = Instant.now().plusSeconds(SESSION_SECONDS);
        sessaoDAO.excluirExpiradas();
        sessaoDAO.inserir(hash(token), userId, expiresAt);
        exchange.getResponseHeaders().add("Set-Cookie", COOKIE_NAME + "=" + token +
                "; Path=/api; Max-Age=" + SESSION_SECONDS + "; HttpOnly; SameSite=Lax" +
                (secureCookie ? "; Secure" : ""));
    }

    public Optional<Long> authenticate(HttpExchange exchange) {
        String token = cookieValue(exchange, COOKIE_NAME);
        if (token == null) return Optional.empty();
        try {
            if (Base64.getUrlDecoder().decode(token).length != 32) return Optional.empty();
            return sessaoDAO.buscarUsuarioIdAtivo(hash(token));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public void clearSessionCookie(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Set-Cookie", COOKIE_NAME +
                "=; Path=/api; Max-Age=0; HttpOnly; SameSite=Lax" +
                (secureCookie ? "; Secure" : ""));
        String token = cookieValue(exchange, COOKIE_NAME);
        if (token != null) {
            try {
                if (Base64.getUrlDecoder().decode(token).length == 32) {
                    sessaoDAO.excluir(hash(token));
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 não está disponível.", ex);
        }
    }

    private static String cookieValue(HttpExchange exchange, String name) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader == null) return null;
        for (String cookie : cookieHeader.split(";")) {
            String[] pair = cookie.trim().split("=", 2);
            if (pair.length == 2 && name.equals(pair[0])) return pair[1];
        }
        return null;
    }
}
