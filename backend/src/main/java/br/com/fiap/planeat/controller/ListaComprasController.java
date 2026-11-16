package br.com.fiap.planeat.controller;

import br.com.fiap.planeat.dto.ItemListaComprasDTO;
import br.com.fiap.planeat.exception.EntidadeNaoEncontradaException;
import br.com.fiap.planeat.exception.RegraNegocioException;
import br.com.fiap.planeat.model.ItemListaCompras;
import br.com.fiap.planeat.service.ListaComprasService;
import br.com.fiap.planeat.security.AuthenticatedHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

/**
 * Controlador REST para itens do checklist mobile de compras (/api/itens-lista).
 * Suporta a alternância em tempo real da flag de comprado (PATCH /api/itens-lista/{id}/status).
 */
public class ListaComprasController implements HttpHandler {

    private final ListaComprasService listaComprasService;

    public ListaComprasController() {
        this.listaComprasService = new ListaComprasService();
    }

    public ListaComprasController(ListaComprasService listaComprasService) {
        this.listaComprasService = listaComprasService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpHelper.aplicarCorsEVerificarOptions(exchange)) {
            return;
        }

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        Long usuarioId = AuthenticatedHandler.usuarioAutenticado(exchange).getId();

        try {
            Long id = HttpHelper.extrairIdDoPath(path);
            if (id == null) {
                HttpHelper.enviarErro(exchange, 400, "Identificador do item de lista não informado.");
                return;
            }

            if ("PATCH".equalsIgnoreCase(method) || ("POST".equalsIgnoreCase(method) && path.endsWith("/status"))) {
                String corpo = HttpHelper.lerCorpoRequisicao(exchange);
                ItemListaCompras itemAtualizado;

                if (corpo != null && !corpo.isBlank() && corpo.contains("comprado")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = HttpHelper.getGson().fromJson(corpo, Map.class);
                    boolean comprado = Boolean.TRUE.equals(map.get("comprado"));
                    listaComprasService.atualizarStatusComprado(id, comprado, usuarioId);
                    itemAtualizado = new ItemListaCompras();
                    itemAtualizado.setId(id);
                    itemAtualizado.setComprado(comprado);
                } else {
                    itemAtualizado = listaComprasService.alternarStatus(id, usuarioId);
                }

                ItemListaComprasDTO dto = new ItemListaComprasDTO();
                dto.setId(itemAtualizado.getId());
                dto.setComprado(itemAtualizado.getComprado());

                HttpHelper.enviarRespostaJson(exchange, 200, dto);
            } else {
                HttpHelper.enviarErro(exchange, 405, "Método HTTP não permitido para esta rota: " + method);
            }
        } catch (EntidadeNaoEncontradaException e) {
            HttpHelper.enviarErro(exchange, 404, e.getMessage());
        } catch (RegraNegocioException e) {
            HttpHelper.enviarErro(exchange, 400, e.getMessage());
        } catch (Exception e) {
            HttpHelper.enviarErro(exchange, 500, "Erro interno ao atualizar a lista de compras.");
        }
    }
}
