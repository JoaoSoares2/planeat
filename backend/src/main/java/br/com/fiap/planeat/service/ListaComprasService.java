package br.com.fiap.planeat.service;

import br.com.fiap.planeat.dao.ListaComprasDAO;
import br.com.fiap.planeat.dao.PlanejamentoDAO;
import br.com.fiap.planeat.dao.ReceitaDAO;
import br.com.fiap.planeat.dto.RelatorioDesperdicioDTO;
import br.com.fiap.planeat.exception.EntidadeNaoEncontradaException;
import br.com.fiap.planeat.exception.RegraNegocioException;
import br.com.fiap.planeat.model.ItemListaCompras;
import br.com.fiap.planeat.model.ItemPlanejamento;
import br.com.fiap.planeat.model.ItemReceita;
import br.com.fiap.planeat.model.PlanejamentoSemanal;
import br.com.fiap.planeat.model.Receita;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

/**
 * Serviço responsável pelo processamento inteligente do funil Planeja -> Processa -> Executa:
 * consolidação matemática de ingredientes repetidos e gestão do checklist de supermercado.
 */
public class ListaComprasService {

    private final PlanejamentoDAO planejamentoDAO;
    private final ListaComprasDAO listaComprasDAO;
    private final ReceitaDAO receitaDAO;

    public ListaComprasService() {
        this.planejamentoDAO = new PlanejamentoDAO();
        this.listaComprasDAO = new ListaComprasDAO();
        this.receitaDAO = new ReceitaDAO();
    }

    public ListaComprasService(PlanejamentoDAO planejamentoDAO, ListaComprasDAO listaComprasDAO, ReceitaDAO receitaDAO) {
        this.planejamentoDAO = planejamentoDAO;
        this.listaComprasDAO = listaComprasDAO;
        this.receitaDAO = receitaDAO;
    }

    /**
     * Gera ou recupera a lista de compras consolidada para um planejamento semanal:
     * 1. Verifica se já existem itens persistidos na base para preservar as checagens do checklist;
     * 2. Caso contrário, calcula a soma exata de insumos das refeições planejadas e persiste no banco;
     * 3. Retorna os itens organizados por setor/categoria de mercado.
     *
     * @param planejamentoId Identificador do planejamento semanal.
     * @return Lista de compras com quantidades unificadas e ordenadas.
     */
    public List<ItemListaCompras> gerarListaCompras(Long planejamentoId) {
        return gerarListaCompras(planejamentoId, null);
    }

    public List<ItemListaCompras> gerarListaCompras(Long planejamentoId, Long usuarioId) {
        if (planejamentoId == null || planejamentoId <= 0) {
            throw new RegraNegocioException("ID de planejamento inválido: " + planejamentoId);
        }

        PlanejamentoSemanal plano = (usuarioId == null
                ? planejamentoDAO.buscarPorId(planejamentoId)
                : planejamentoDAO.buscarPorId(planejamentoId, usuarioId))
                .orElseThrow(() -> new EntidadeNaoEncontradaException(PlanejamentoSemanal.class, planejamentoId));

        // 1. Tenta recuperar lista já persistida
        List<ItemListaCompras> itensPersistidos = listaComprasDAO.buscarItensPorPlanejamento(planejamentoId);
        if (!itensPersistidos.isEmpty()) {
            return itensPersistidos;
        }

        // 2. Se ainda não persistida, processa as receitas alocadas
        List<Receita> receitasDoPlano = new ArrayList<>();
        if (plano.getRefeicoes() != null) {
            for (ItemPlanejamento ip : plano.getRefeicoes()) {
                if (ip.getReceita() != null && ip.getReceita().getId() != null) {
                    (usuarioId == null
                            ? receitaDAO.buscarPorId(ip.getReceita().getId())
                            : receitaDAO.buscarPorId(ip.getReceita().getId(), usuarioId))
                            .ifPresent(receitasDoPlano::add);
                }
            }
        }

        // 3. Aplica o algoritmo de consolidação inteligente
        List<ItemListaCompras> itensCalculados = consolidarIngredientes(receitasDoPlano);

        // Se o cálculo via Java estiver vazio, tenta recorrer à query analítica SQL
        if (itensCalculados.isEmpty()) {
            itensCalculados = planejamentoDAO.buscarListaConsolidadaPorPlanejamento(planejamentoId);
        }

        // 4. Persiste no banco relacional (T_PE_LISTA_COMPRAS e T_PE_ITEM_LISTA)
        if (!itensCalculados.isEmpty()) {
            listaComprasDAO.salvarListaConsolidada(planejamentoId, itensCalculados);
        }

        return itensCalculados;
    }

    public List<ItemListaCompras> regerarListaCompras(Long planejamentoId, Long usuarioId) {
        if (planejamentoId == null || planejamentoId <= 0) {
            throw new RegraNegocioException("ID de planejamento inválido: " + planejamentoId);
        }
        PlanejamentoSemanal plano = planejamentoDAO.buscarPorId(planejamentoId, usuarioId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(PlanejamentoSemanal.class, planejamentoId));
        List<ItemListaCompras> itensAnteriores = listaComprasDAO.buscarItensPorPlanejamento(planejamentoId);
        Map<String, Boolean> statusAnterior = new LinkedHashMap<>();
        for (ItemListaCompras item : itensAnteriores) {
            Long ingredienteId = item.getIngrediente() == null ? null : item.getIngrediente().getId();
            if (ingredienteId != null) {
                statusAnterior.put(chaveItem(ingredienteId, item.getUnidadeMedida()),
                        Boolean.TRUE.equals(item.getComprado()));
            }
        }

        List<Receita> receitas = new ArrayList<>();
        if (plano.getRefeicoes() != null) {
            for (ItemPlanejamento refeicao : plano.getRefeicoes()) {
                if (refeicao.getReceita() != null && refeicao.getReceita().getId() != null) {
                    receitaDAO.buscarPorId(refeicao.getReceita().getId(), usuarioId).ifPresent(receitas::add);
                }
            }
        }
        List<ItemListaCompras> itens = consolidarIngredientes(receitas);
        for (ItemListaCompras item : itens) {
            Long ingredienteId = item.getIngrediente().getId();
            item.setComprado(statusAnterior.getOrDefault(
                    chaveItem(ingredienteId, item.getUnidadeMedida()), false));
        }
        listaComprasDAO.salvarListaConsolidada(planejamentoId, itens);
        return listaComprasDAO.buscarItensPorPlanejamento(planejamentoId);
    }

    private String chaveItem(Long ingredienteId, String unidade) {
        return ingredienteId + "_" + (unidade == null ? "" : unidade.trim().toLowerCase(Locale.ROOT));
    }

    /**
     * Algoritmo de consolidação algébrica de doses e unificação de insumos repetidos.
     * Agrupa pelo ID do ingrediente e unidade de medida, somando fracionamentos.
     */
    public List<ItemListaCompras> consolidarIngredientes(List<Receita> receitas) {
        Map<String, ItemListaCompras> mapaConsolidado = new LinkedHashMap<>();

        if (receitas == null) {
            return new ArrayList<>();
        }

        for (Receita receita : receitas) {
            if (receita.getIngredientes() == null) {
                continue;
            }
            for (ItemReceita item : receita.getIngredientes()) {
                if (item.getIngrediente() == null || item.getIngrediente().getId() == null) {
                    continue;
                }
                String unidade = item.getUnidadeMedida() != null ? item.getUnidadeMedida().trim().toLowerCase() : "";
                String chave = item.getIngrediente().getId() + "_" + unidade;

                if (mapaConsolidado.containsKey(chave)) {
                    ItemListaCompras existente = mapaConsolidado.get(chave);
                    double soma = existente.getQuantidadeTotal() + (item.getQuantidade() != null ? item.getQuantidade() : 0.0);
                    existente.setQuantidadeTotal(Math.round(soma * 100.0) / 100.0);
                } else {
                    ItemListaCompras novo = new ItemListaCompras(
                            item.getIngrediente(),
                            item.getQuantidade() != null ? item.getQuantidade() : 0.0,
                            item.getUnidadeMedida(),
                            false
                    );
                    mapaConsolidado.put(chave, novo);
                }
            }
        }

        List<ItemListaCompras> resultado = new ArrayList<>(mapaConsolidado.values());
        resultado.sort(Comparator.comparing(
                (ItemListaCompras i) -> i.getIngrediente() != null && i.getIngrediente().getCategoria() != null ?
                        i.getIngrediente().getCategoria().getNome() : ""
        ).thenComparing(
                (ItemListaCompras i) -> i.getIngrediente() != null ? i.getIngrediente().getNome() : ""
        ));

        return resultado;
    }

    /**
     * Alterna a flag de item comprado no checklist do supermercado ('S' <-> 'N').
     *
     * @param itemListaId Identificador do item consolidado.
     * @param comprado Novo status booleano.
     */
    public void atualizarStatusComprado(Long itemListaId, boolean comprado, Long usuarioId) {
        if (itemListaId == null || itemListaId <= 0) {
            throw new RegraNegocioException("ID do item da lista inválido: " + itemListaId);
        }
        listaComprasDAO.buscarItemPorId(itemListaId, usuarioId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(ItemListaCompras.class, itemListaId));

        listaComprasDAO.atualizarStatusComprado(itemListaId, comprado, usuarioId);
    }

    /**
     * Alterna automaticamente o status de comprado de um item (toggle).
     *
     * @param itemListaId ID do item.
     * @return Item com o status atualizado.
     */
    public ItemListaCompras alternarStatus(Long itemListaId, Long usuarioId) {
        if (itemListaId == null || itemListaId <= 0) {
            throw new RegraNegocioException("ID do item da lista inválido: " + itemListaId);
        }
        ItemListaCompras item = listaComprasDAO.buscarItemPorId(itemListaId, usuarioId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(ItemListaCompras.class, itemListaId));

        boolean novoStatus = !Boolean.TRUE.equals(item.getComprado());
        listaComprasDAO.atualizarStatusComprado(itemListaId, novoStatus, usuarioId);
        item.setComprado(novoStatus);
        return item;
    }

    /**
     * Calcula métricas e relatório de desperdício alimentar evitado por reaproveitamento de insumos.
     *
     * @param planejamentoId Identificador do plano semanal.
     * @return DTO com percentuais de aproveitamento e indicadores CitizenTech.
     */
    public RelatorioDesperdicioDTO calcularEstimativaDesperdicio(Long planejamentoId, Long usuarioId) {
        if (planejamentoId == null || planejamentoId <= 0) {
            throw new RegraNegocioException("ID de planejamento inválido: " + planejamentoId);
        }
        planejamentoDAO.buscarPorId(planejamentoId, usuarioId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(PlanejamentoSemanal.class, planejamentoId));
        return planejamentoDAO.buscarRelatorioDesperdicio(planejamentoId);
    }
}
