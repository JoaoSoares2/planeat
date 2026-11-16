package br.com.fiap.planeat.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

/**
 * Utilitário centralizado para manipulação de requisições e respostas HTTP no servidor nativo Java.
 */
public class HttpHelper {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (date, type, context) ->
                    new JsonPrimitive(date.toString()))
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, type, context) ->
                    LocalDate.parse(json.getAsString()))
            .setPrettyPrinting()
            .create();

    public static Gson getGson() {
        return GSON;
    }

    /**
     * Aplica os cabeçalhos de CORS e responde a requisições de preflight (OPTIONS).
     *
     * @return true se a requisição era OPTIONS e já foi finalizada; false para prosseguir com o handler.
     */
    public static boolean aplicarCorsEVerificarOptions(HttpExchange exchange) throws IOException {
        String origin = exchange.getRequestHeaders().getFirst("Origin");
        if (origemPermitida(origin)) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origin);
            exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
            exchange.getResponseHeaders().set("Vary", "Origin");
        }
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, X-Requested-With");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }

    public static boolean requisicaoDeNavegadorPermitida(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method) ||
                "OPTIONS".equalsIgnoreCase(method)) return true;
        if (!"XMLHttpRequest".equals(exchange.getRequestHeaders().getFirst("X-Requested-With"))) {
            return false;
        }
        String origin = exchange.getRequestHeaders().getFirst("Origin");
        return origin == null || origemPermitida(origin);
    }

    private static boolean origemPermitida(String origin) {
        return origin != null && allowedOrigins().contains(origin);
    }

    private static List<String> allowedOrigins() {
        String configured = System.getenv("CORS_ALLOWED_ORIGINS");
        if (configured == null || configured.isBlank()) {
            return List.of("http://localhost:5173", "http://127.0.0.1:5173");
        }
        return Arrays.stream(configured.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty() && !"*".equals(origin))
                .toList();
    }

    /**
     * Lê o corpo JSON da requisição HTTP como string UTF-8.
     */
    public static String lerCorpoRequisicao(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String linha;
            while ((linha = reader.readLine()) != null) {
                sb.append(linha);
            }
            return sb.toString();
        }
    }

    /**
     * Envia uma resposta JSON com o status HTTP correspondente.
     */
    public static void enviarRespostaJson(HttpExchange exchange, int statusCode, Object dados) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        String json = GSON.toJson(dados);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Envia uma resposta padronizada de erro em formato JSON.
     */
    public static void enviarErro(HttpExchange exchange, int statusCode, String mensagem) throws IOException {
        Map<String, Object> erro = new HashMap<>();
        erro.put("status", statusCode);
        erro.put("erro", mensagem);
        enviarRespostaJson(exchange, statusCode, erro);
    }

    /**
     * Extrai o identificador numérico ao final do path de uma URI (ex: /api/receitas/5 -> 5L).
     */
    public static Long extrairIdDoPath(String path) {
        if (path == null) return null;
        String[] partes = path.split("/");
        for (int i = partes.length - 1; i >= 0; i--) {
            String parte = partes[i];
            if (!parte.isBlank()) {
                try {
                    return Long.parseLong(parte);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }
}
