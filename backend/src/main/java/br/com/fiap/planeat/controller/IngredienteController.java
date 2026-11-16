package br.com.fiap.planeat.controller;

import br.com.fiap.planeat.exception.EntidadeNaoEncontradaException;
import br.com.fiap.planeat.exception.RegraNegocioException;
import br.com.fiap.planeat.model.CategoriaAlimento;
import br.com.fiap.planeat.model.Ingrediente;
import br.com.fiap.planeat.service.CategoriaService;
import br.com.fiap.planeat.service.IngredienteService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para catálogo de ingredientes e categorias (/api/ingredientes e /api/categorias).
 */
public class IngredienteController implements HttpHandler {

    private final IngredienteService ingredienteService;
    private final CategoriaService categoriaService;

    public IngredienteController() {
        this.ingredienteService = new IngredienteService();
        this.categoriaService = new CategoriaService();
    }

    public IngredienteController(IngredienteService ingredienteService, CategoriaService categoriaService) {
        this.ingredienteService = ingredienteService;
        this.categoriaService = categoriaService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpHelper.aplicarCorsEVerificarOptions(exchange)) {
            return;
        }

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("GET".equalsIgnoreCase(method)) {
                if (path.contains("/categorias")) {
                    List<CategoriaAlimento> categorias = categoriaService.listarTodas();
                    HttpHelper.enviarRespostaJson(exchange, 200, categorias);
                } else {
                    Long id = HttpHelper.extrairIdDoPath(path);
                    if (id != null) {
                        Ingrediente ingrediente = ingredienteService.buscarPorId(id);
                        HttpHelper.enviarRespostaJson(exchange, 200, ingrediente);
                    } else {
                        List<Ingrediente> ingredientes = ingredienteService.listarTodos();
                        HttpHelper.enviarRespostaJson(exchange, 200, ingredientes);
                    }
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                String corpo = HttpHelper.lerCorpoRequisicao(exchange);
                Ingrediente ingrediente = HttpHelper.getGson().fromJson(corpo, Ingrediente.class);
                Long id = ingredienteService.cadastrar(ingrediente);
                Ingrediente criado = ingredienteService.buscarPorId(id);
                HttpHelper.enviarRespostaJson(exchange, 201, criado);
            } else {
                HttpHelper.enviarErro(exchange, 405, "Método HTTP não suportado: " + method);
            }
        } catch (EntidadeNaoEncontradaException e) {
            HttpHelper.enviarErro(exchange, 404, e.getMessage());
        } catch (RegraNegocioException e) {
            HttpHelper.enviarErro(exchange, 400, e.getMessage());
        } catch (Exception e) {
            HttpHelper.enviarErro(exchange, 500, "Erro interno no servidor: " + e.getMessage());
        }
    }
}
