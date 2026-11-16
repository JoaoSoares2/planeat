package br.com.fiap.planeat.controller;

import br.com.fiap.planeat.dto.ItemListaComprasDTO;
import br.com.fiap.planeat.dto.PlanejamentoDTO;
import br.com.fiap.planeat.dto.RelatorioDesperdicioDTO;
import br.com.fiap.planeat.exception.EntidadeNaoEncontradaException;
import br.com.fiap.planeat.exception.RegraNegocioException;
import br.com.fiap.planeat.model.ItemListaCompras;
import br.com.fiap.planeat.model.ItemPlanejamento;
import br.com.fiap.planeat.model.PlanejamentoSemanal;
import br.com.fiap.planeat.model.Receita;
import br.com.fiap.planeat.service.ListaComprasService;
import br.com.fiap.planeat.service.PlanejamentoService;
import br.com.fiap.planeat.security.AuthenticatedHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestão de planos semanais e geração de lista de compras consolidada.
 * Rotas: /api/planejamentos, /api/planejamentos/{id}, /api/planejamentos/{id}/lista-compras, /api/planejamentos/{id}/desperdicio
 */
public class PlanejamentoController implements HttpHandler {

    private final PlanejamentoService planejamentoService;
    private final ListaComprasService listaComprasService;

    public PlanejamentoController() {
        this.planejamentoService = new PlanejamentoService();
        this.listaComprasService = new ListaComprasService();
    }

    public PlanejamentoController(PlanejamentoService planejamentoService, ListaComprasService listaComprasService) {
        this.planejamentoService = planejamentoService;
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
            if ("GET".equalsIgnoreCase(method)) {
                tratarGet(exchange, path, usuarioId);
            } else if ("POST".equalsIgnoreCase(method)) {
                tratarPost(exchange, usuarioId);
            } else if ("PUT".equalsIgnoreCase(method)) {
                tratarPut(exchange, path, usuarioId);
            } else if ("DELETE".equalsIgnoreCase(method)) {
                tratarDelete(exchange, path, usuarioId);
            } else {
                HttpHelper.enviarErro(exchange, 405, "Método HTTP não permitido: " + method);
            }
        } catch (EntidadeNaoEncontradaException e) {
            HttpHelper.enviarErro(exchange, 404, e.getMessage());
        } catch (RegraNegocioException e) {
            HttpHelper.enviarErro(exchange, 400, e.getMessage());
        } catch (Exception e) {
            HttpHelper.enviarErro(exchange, 500, "Erro interno ao processar o planejamento.");
        }
    }

    private void tratarGet(HttpExchange exchange, String path, Long usuarioId) throws IOException {
        // Ex: /api/planejamentos/{id}/lista-compras
        if (path.contains("/lista-compras")) {
            Long planoId = extrairIdPlanejamento(path, "/lista-compras");
            if (planoId == null) {
                HttpHelper.enviarErro(exchange, 400, "ID do planejamento não identificado na rota.");
                return;
            }

            PlanejamentoSemanal plano = planejamentoService.buscarPorId(planoId, usuarioId);
            List<ItemListaCompras> itens = listaComprasService.gerarListaCompras(planoId, usuarioId);

            Map<String, Object> resposta = new LinkedHashMap<>();
            resposta.put("planejamentoId", planoId);
            resposta.put("titulo", plano.getTitulo());
            resposta.put("totalItens", itens.size());

            List<ItemListaComprasDTO> itensDTO = new ArrayList<>();
            for (ItemListaCompras item : itens) {
                ItemListaComprasDTO dto = new ItemListaComprasDTO();
                dto.setId(item.getId());
                if (item.getIngrediente() != null) {
                    dto.setIngredienteId(item.getIngrediente().getId());
                    dto.setNome(item.getIngrediente().getNome());
                    if (item.getIngrediente().getCategoria() != null) {
                        dto.setCategoria(item.getIngrediente().getCategoria().getNome());
                    }
                }
                dto.setQuantidadeTotal(item.getQuantidadeTotal());
                dto.setUnidadeMedida(item.getUnidadeMedida());
                dto.setComprado(Boolean.TRUE.equals(item.getComprado()));
                itensDTO.add(dto);
            }
            resposta.put("itens", itensDTO);

            HttpHelper.enviarRespostaJson(exchange, 200, resposta);
            return;
        }

        // Ex: /api/planejamentos/{id}/desperdicio
        if (path.contains("/desperdicio")) {
            Long planoId = extrairIdPlanejamento(path, "/desperdicio");
            if (planoId == null) {
                HttpHelper.enviarErro(exchange, 400, "ID do planejamento não identificado na rota.");
                return;
            }
            RelatorioDesperdicioDTO relatorio = listaComprasService.calcularEstimativaDesperdicio(planoId, usuarioId);
            HttpHelper.enviarRespostaJson(exchange, 200, relatorio);
            return;
        }

        // Ex: /api/planejamentos/{id} ou /api/planejamentos
        Long id = HttpHelper.extrairIdDoPath(path);
        if (id != null) {
            PlanejamentoSemanal plano = planejamentoService.buscarPorId(id, usuarioId);
            HttpHelper.enviarRespostaJson(exchange, 200, converterParaDTO(plano));
        } else {
            List<PlanejamentoSemanal> planos = planejamentoService.listarPlanejamentos(usuarioId);
            List<PlanejamentoDTO> dtos = new ArrayList<>();
            for (PlanejamentoSemanal p : planos) {
                dtos.add(converterParaDTO(p));
            }
            HttpHelper.enviarRespostaJson(exchange, 200, dtos);
        }
    }

    private void tratarPost(HttpExchange exchange, Long usuarioId) throws IOException {
        String corpo = HttpHelper.lerCorpoRequisicao(exchange);
        PlanejamentoDTO dto = HttpHelper.getGson().fromJson(corpo, PlanejamentoDTO.class);

        PlanejamentoSemanal plano = converterParaEntidade(dto);
        Long idGerado = planejamentoService.cadastrar(plano, usuarioId);

        // Gera a lista de compras inicial consolidada automaticamente
        listaComprasService.gerarListaCompras(idGerado, usuarioId);

        PlanejamentoSemanal criado = planejamentoService.buscarPorId(idGerado, usuarioId);
        HttpHelper.enviarRespostaJson(exchange, 201, converterParaDTO(criado));
    }

    private void tratarPut(HttpExchange exchange, String path, Long usuarioId) throws IOException {
        Long id = HttpHelper.extrairIdDoPath(path);
        if (id == null) {
            HttpHelper.enviarErro(exchange, 400, "ID do planejamento não informado na rota.");
            return;
        }
        PlanejamentoDTO dto = HttpHelper.getGson().fromJson(
                HttpHelper.lerCorpoRequisicao(exchange), PlanejamentoDTO.class);
        PlanejamentoSemanal plano = converterParaEntidade(dto);
        plano.setId(id);
        planejamentoService.atualizar(plano, usuarioId);
        listaComprasService.regerarListaCompras(id, usuarioId);
        HttpHelper.enviarRespostaJson(exchange, 200,
                converterParaDTO(planejamentoService.buscarPorId(id, usuarioId)));
    }

    private void tratarDelete(HttpExchange exchange, String path, Long usuarioId) throws IOException {
        Long id = HttpHelper.extrairIdDoPath(path);
        if (id == null) {
            HttpHelper.enviarErro(exchange, 400, "ID do planejamento não informado para exclusão.");
            return;
        }
        planejamentoService.excluir(id, usuarioId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("mensagem", "Planejamento com ID " + id + " excluído com sucesso.");
        HttpHelper.enviarRespostaJson(exchange, 200, resp);
    }

    private Long extrairIdPlanejamento(String path, String sufixo) {
        try {
            String semSufixo = path.substring(0, path.indexOf(sufixo));
            return HttpHelper.extrairIdDoPath(semSufixo);
        } catch (Exception e) {
            return null;
        }
    }

    private PlanejamentoDTO converterParaDTO(PlanejamentoSemanal p) {
        PlanejamentoDTO dto = new PlanejamentoDTO();
        dto.setId(p.getId());
        dto.setTitulo(p.getTitulo());
        dto.setDataInicio(p.getDataInicio());
        dto.setDataFim(p.getDataFim());
        dto.setStatus(p.getStatus());

        List<PlanejamentoDTO.ItemPlanejamentoDTO> itensDTO = new ArrayList<>();
        if (p.getRefeicoes() != null) {
            for (ItemPlanejamento ip : p.getRefeicoes()) {
                PlanejamentoDTO.ItemPlanejamentoDTO idto = new PlanejamentoDTO.ItemPlanejamentoDTO();
                idto.setId(ip.getId());
                if (ip.getReceita() != null) {
                    idto.setReceitaId(ip.getReceita().getId());
                    idto.setNomeReceita(ip.getReceita().getNome());
                }
                idto.setDiaSemana(ip.getDiaSemana());
                idto.setTipoRefeicao(ip.getTipoRefeicao());
                itensDTO.add(idto);
            }
        }
        dto.setRefeicoes(itensDTO);
        return dto;
    }

    private PlanejamentoSemanal converterParaEntidade(PlanejamentoDTO dto) {
        PlanejamentoSemanal p = new PlanejamentoSemanal();
        p.setId(dto.getId());
        p.setTitulo(dto.getTitulo());
        p.setDataInicio(dto.getDataInicio());
        p.setDataFim(dto.getDataFim());
        p.setStatus(dto.getStatus() != null ? dto.getStatus() : "ATIVO");

        List<ItemPlanejamento> itens = new ArrayList<>();
        if (dto.getRefeicoes() != null) {
            for (PlanejamentoDTO.ItemPlanejamentoDTO idto : dto.getRefeicoes()) {
                Receita r = new Receita();
                r.setId(idto.getReceitaId());
                r.setNome(idto.getNomeReceita());
                ItemPlanejamento ip = new ItemPlanejamento(r, idto.getDiaSemana(), idto.getTipoRefeicao());
                itens.add(ip);
            }
        }
        p.setRefeicoes(itens);
        return p;
    }
}
