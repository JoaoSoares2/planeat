package br.com.fiap.planeat.controller;

import br.com.fiap.planeat.dto.ItemReceitaDTO;
import br.com.fiap.planeat.dto.ReceitaDTO;
import br.com.fiap.planeat.exception.EntidadeNaoEncontradaException;
import br.com.fiap.planeat.exception.RegraNegocioException;
import br.com.fiap.planeat.model.CategoriaAlimento;
import br.com.fiap.planeat.model.Ingrediente;
import br.com.fiap.planeat.model.ItemReceita;
import br.com.fiap.planeat.model.Receita;
import br.com.fiap.planeat.service.ReceitaService;
import br.com.fiap.planeat.security.AuthenticatedHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para o catálogo de receitas (/api/receitas).
 */
public class ReceitaController implements HttpHandler {

    private final ReceitaService receitaService;

    public ReceitaController() {
        this.receitaService = new ReceitaService();
    }

    public ReceitaController(ReceitaService receitaService) {
        this.receitaService = receitaService;
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
            HttpHelper.enviarErro(exchange, 500, "Erro interno ao processar a receita.");
        }
    }

    private void tratarGet(HttpExchange exchange, String path, Long usuarioId) throws IOException {
        Long id = HttpHelper.extrairIdDoPath(path);

        if (id != null) {
            Receita receita = receitaService.buscarPorId(id, usuarioId);
            HttpHelper.enviarRespostaJson(exchange, 200, converterParaDTO(receita));
        } else {
            String query = exchange.getRequestURI().getQuery();
            List<Receita> receitas;
            if (query != null && query.contains("tipo=")) {
                String tipo = query.split("tipo=")[1].split("&")[0];
                receitas = receitaService.listarPorTipoRefeicao(usuarioId, tipo);
            } else {
                receitas = receitaService.listarReceitas(usuarioId);
            }

            List<ReceitaDTO> dtos = new ArrayList<>();
            for (Receita r : receitas) {
                dtos.add(converterParaDTO(r));
            }
            HttpHelper.enviarRespostaJson(exchange, 200, dtos);
        }
    }

    private void tratarPost(HttpExchange exchange, Long usuarioId) throws IOException {
        String corpo = HttpHelper.lerCorpoRequisicao(exchange);
        ReceitaDTO dto = HttpHelper.getGson().fromJson(corpo, ReceitaDTO.class);

        Receita receita = converterParaEntidade(dto);
        Long idGerado = receitaService.cadastrarReceita(receita, usuarioId);

        Receita criada = receitaService.buscarPorId(idGerado, usuarioId);
        HttpHelper.enviarRespostaJson(exchange, 201, converterParaDTO(criada));
    }

    private void tratarPut(HttpExchange exchange, String path, Long usuarioId) throws IOException {
        Long id = HttpHelper.extrairIdDoPath(path);
        if (id == null) {
            HttpHelper.enviarErro(exchange, 400, "ID da receita não informado na rota.");
            return;
        }

        String corpo = HttpHelper.lerCorpoRequisicao(exchange);
        ReceitaDTO dto = HttpHelper.getGson().fromJson(corpo, ReceitaDTO.class);
        dto.setId(id);

        Receita receita = converterParaEntidade(dto);
        receitaService.atualizarReceita(receita, usuarioId);

        Receita atualizada = receitaService.buscarPorId(id, usuarioId);
        HttpHelper.enviarRespostaJson(exchange, 200, converterParaDTO(atualizada));
    }

    private void tratarDelete(HttpExchange exchange, String path, Long usuarioId) throws IOException {
        Long id = HttpHelper.extrairIdDoPath(path);
        if (id == null) {
            HttpHelper.enviarErro(exchange, 400, "ID da receita não informado para exclusão.");
            return;
        }

        receitaService.excluirReceita(id, usuarioId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("mensagem", "Receita com ID " + id + " excluída com sucesso.");
        HttpHelper.enviarRespostaJson(exchange, 200, resp);
    }

    private ReceitaDTO converterParaDTO(Receita r) {
        ReceitaDTO dto = new ReceitaDTO();
        dto.setId(r.getId());
        dto.setNome(r.getNome());
        dto.setModoPreparo(r.getModoPreparo());
        dto.setPorcoes(r.getPorcoes());
        dto.setTempoPreparoMinutos(r.getTempoPreparoMinutos());
        dto.setTipoRefeicao(r.getTipoRefeicao());
        dto.setDataCriacao(r.getDataCriacao());

        List<ItemReceitaDTO> itemDTOs = new ArrayList<>();
        if (r.getIngredientes() != null) {
            for (ItemReceita item : r.getIngredientes()) {
                ItemReceitaDTO itemDTO = new ItemReceitaDTO();
                if (item.getIngrediente() != null) {
                    itemDTO.setIngredienteId(item.getIngrediente().getId());
                    itemDTO.setNomeIngrediente(item.getIngrediente().getNome());
                    if (item.getIngrediente().getCategoria() != null) {
                        itemDTO.setCategoria(item.getIngrediente().getCategoria().getNome());
                    }
                }
                itemDTO.setQuantidade(item.getQuantidade());
                itemDTO.setUnidadeMedida(item.getUnidadeMedida());
                itemDTOs.add(itemDTO);
            }
        }
        dto.setIngredientes(itemDTOs);
        return dto;
    }

    private Receita converterParaEntidade(ReceitaDTO dto) {
        Receita r = new Receita();
        r.setId(dto.getId());
        r.setNome(dto.getNome());
        r.setModoPreparo(dto.getModoPreparo());
        r.setPorcoes(dto.getPorcoes());
        r.setTempoPreparoMinutos(dto.getTempoPreparoMinutos());
        r.setTipoRefeicao(dto.getTipoRefeicao());

        List<ItemReceita> itens = new ArrayList<>();
        if (dto.getIngredientes() != null) {
            for (ItemReceitaDTO idto : dto.getIngredientes()) {
                Ingrediente ing = new Ingrediente();
                ing.setId(idto.getIngredienteId());
                ing.setNome(idto.getNomeIngrediente());
                if (idto.getCategoria() != null) {
                    ing.setCategoria(new CategoriaAlimento(idto.getCategoria()));
                }
                ItemReceita item = new ItemReceita(ing, idto.getQuantidade(), idto.getUnidadeMedida());
                itens.add(item);
            }
        }
        r.setIngredientes(itens);
        return r;
    }
}
