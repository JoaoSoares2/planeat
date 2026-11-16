package br.com.fiap.planeat.dao;

import br.com.fiap.planeat.dto.RelatorioDesperdicioDTO;
import br.com.fiap.planeat.exception.DatabasePersistenceException;
import br.com.fiap.planeat.factory.ConnectionFactory;
import br.com.fiap.planeat.model.CategoriaAlimento;
import br.com.fiap.planeat.model.Ingrediente;
import br.com.fiap.planeat.model.ItemListaCompras;
import br.com.fiap.planeat.model.ItemPlanejamento;
import br.com.fiap.planeat.model.PlanejamentoSemanal;
import br.com.fiap.planeat.model.Receita;
import br.com.fiap.planeat.model.Usuario;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object para a entidade PlanejamentoSemanal (tabela T_PE_PLANEJAMENTO)
 * e alocação de refeições (tabela T_PE_ITEM_PLANEJAMENTO) via JDBC nativo.
 */
public class PlanejamentoDAO {

    private static final String SQL_INSERT_PLANEJAMENTO =
            "INSERT INTO T_PE_PLANEJAMENTO (ID_USUARIO, NM_TITULO, DT_INICIO, DT_FIM, DS_STATUS) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_ITEM_PLANEJAMENTO =
            "INSERT INTO T_PE_ITEM_PLANEJAMENTO (ID_PLANEJAMENTO, ID_RECEITA, DS_DIA_SEMANA, DS_REFEICAO) " +
            "VALUES (?, ?, ?, ?)";

    private static final String SQL_SELECT_BY_ID =
            "SELECT p.ID_PLANEJAMENTO, p.ID_USUARIO, p.NM_TITULO, p.DT_INICIO, p.DT_FIM, p.DS_STATUS, " +
            "       u.NM_USUARIO, u.DS_EMAIL " +
            "FROM T_PE_PLANEJAMENTO p " +
            "LEFT JOIN T_PE_USUARIO u ON p.ID_USUARIO = u.ID_USUARIO " +
            "WHERE p.ID_PLANEJAMENTO = ?";

    private static final String SQL_SELECT_BY_ID_OWNER =
            SQL_SELECT_BY_ID + " AND p.ID_USUARIO = ?";

    private static final String SQL_SELECT_ITEMS_BY_PLANEJAMENTO =
            "SELECT ip.ID_ITEM_PLANEJAMENTO, ip.ID_PLANEJAMENTO, ip.DS_DIA_SEMANA, ip.DS_REFEICAO, " +
            "       r.ID_RECEITA, r.NM_RECEITA, r.DS_MODO_PREPARO, r.NR_PORCOES, r.NR_TEMPO_MINUTOS, r.DS_TIPO_REFEICAO " +
            "FROM T_PE_ITEM_PLANEJAMENTO ip " +
            "JOIN T_PE_RECEITA r ON ip.ID_RECEITA = r.ID_RECEITA " +
            "WHERE ip.ID_PLANEJAMENTO = ? " +
            "ORDER BY ip.ID_ITEM_PLANEJAMENTO ASC";

    private static final String SQL_SELECT_ALL =
            "SELECT p.ID_PLANEJAMENTO, p.ID_USUARIO, p.NM_TITULO, p.DT_INICIO, p.DT_FIM, p.DS_STATUS, " +
            "       u.NM_USUARIO, u.DS_EMAIL " +
            "FROM T_PE_PLANEJAMENTO p " +
            "LEFT JOIN T_PE_USUARIO u ON p.ID_USUARIO = u.ID_USUARIO " +
            "ORDER BY p.DT_INICIO DESC";

    private static final String SQL_SELECT_BY_OWNER =
            SQL_SELECT_ALL.replace("ORDER BY", "WHERE p.ID_USUARIO = ? ORDER BY");

    private static final String SQL_UPDATE_PLANEJAMENTO =
            "UPDATE T_PE_PLANEJAMENTO SET NM_TITULO = ?, DT_INICIO = ?, DT_FIM = ?, DS_STATUS = ? " +
            "WHERE ID_PLANEJAMENTO = ? AND ID_USUARIO = ?";

    private static final String SQL_DELETE_ITEMS =
            "DELETE FROM T_PE_ITEM_PLANEJAMENTO WHERE ID_PLANEJAMENTO = ?";

    private static final String SQL_DELETE_PLANEJAMENTO_BY_OWNER =
            "DELETE FROM T_PE_PLANEJAMENTO WHERE ID_PLANEJAMENTO = ? AND ID_USUARIO = ?";

    private static final String SQL_DELETE_ITEMS_BY_OWNER =
            "DELETE FROM T_PE_ITEM_PLANEJAMENTO WHERE ID_PLANEJAMENTO IN " +
            "(SELECT ID_PLANEJAMENTO FROM T_PE_PLANEJAMENTO WHERE ID_PLANEJAMENTO = ? AND ID_USUARIO = ?)";
    // Query Analítica SQL com JOIN, SUM, GROUP BY e ORDER BY
    private static final String SQL_QUERY_ANALITICA_CONSOLIDACAO =
            "SELECT " +
            "    c.ID_CATEGORIA, " +
            "    c.NM_CATEGORIA AS CATEGORIA_MERCADO, " +
            "    i.ID_INGREDIENTE, " +
            "    i.NM_INGREDIENTE AS ALIMENTO, " +
            "    i.DS_UNIDADE_PADRAO, " +
            "    SUM(ri.VL_QUANTIDADE) AS QUANTIDADE_TOTAL, " +
            "    ri.DS_UNIDADE_MEDIDA AS UNIDADE, " +
            "    COUNT(DISTINCT r.ID_RECEITA) AS TOTAL_RECEITAS_QUE_UTILIZAM " +
            "FROM T_PE_PLANEJAMENTO p " +
            "JOIN T_PE_ITEM_PLANEJAMENTO ip ON p.ID_PLANEJAMENTO = ip.ID_PLANEJAMENTO " +
            "JOIN T_PE_RECEITA r ON ip.ID_RECEITA = r.ID_RECEITA " +
            "JOIN T_PE_RECEITA_INGREDIENTE ri ON r.ID_RECEITA = ri.ID_RECEITA " +
            "JOIN T_PE_INGREDIENTE i ON ri.ID_INGREDIENTE = i.ID_INGREDIENTE " +
            "JOIN T_PE_CATEGORIA c ON i.ID_CATEGORIA = c.ID_CATEGORIA " +
            "WHERE p.ID_PLANEJAMENTO = ? " +
            "GROUP BY " +
            "    c.ID_CATEGORIA, " +
            "    c.NM_CATEGORIA, " +
            "    i.ID_INGREDIENTE, " +
            "    i.NM_INGREDIENTE, " +
            "    i.DS_UNIDADE_PADRAO, " +
            "    ri.DS_UNIDADE_MEDIDA " +
            "ORDER BY " +
            "    c.NM_CATEGORIA ASC, " +
            "    i.NM_INGREDIENTE ASC";

    // Query Analítica de Avaliação de Desperdício Evitado
    private static final String SQL_QUERY_ANALITICA_DESPERDICIO =
            "SELECT " +
            "    i.NM_INGREDIENTE, " +
            "    c.NM_CATEGORIA, " +
            "    COUNT(ip.ID_ITEM_PLANEJAMENTO) AS FREQUENCIA_NO_CARDAPIO, " +
            "    SUM(ri.VL_QUANTIDADE) AS CONSUMO_TOTAL_PLANEJADO, " +
            "    ri.DS_UNIDADE_MEDIDA, " +
            "    CASE " +
            "        WHEN COUNT(ip.ID_ITEM_PLANEJAMENTO) > 1 THEN 'Alto Aproveitamento (Desperdício Zero)' " +
            "        ELSE 'Item Pontual' " +
            "    END AS INDICADOR_CONSUMO_CONSCIENTE " +
            "FROM T_PE_PLANEJAMENTO p " +
            "JOIN T_PE_ITEM_PLANEJAMENTO ip ON p.ID_PLANEJAMENTO = ip.ID_PLANEJAMENTO " +
            "JOIN T_PE_RECEITA_INGREDIENTE ri ON ip.ID_RECEITA = ri.ID_RECEITA " +
            "JOIN T_PE_INGREDIENTE i ON ri.ID_INGREDIENTE = i.ID_INGREDIENTE " +
            "JOIN T_PE_CATEGORIA c ON i.ID_CATEGORIA = c.ID_CATEGORIA " +
            "WHERE p.ID_PLANEJAMENTO = ? " +
            "GROUP BY " +
            "    i.NM_INGREDIENTE, " +
            "    c.NM_CATEGORIA, " +
            "    ri.DS_UNIDADE_MEDIDA " +
            "ORDER BY FREQUENCIA_NO_CARDAPIO DESC";

    /**
     * Insere um novo planejamento semanal com as refeições alocadas em transação atômica.
     *
     * @param plano Objeto de planejamento semanal com itens.
     * @return ID gerado pelo banco.
     */
    public Long inserir(PlanejamentoSemanal plano) {
        Connection conn = null;
        PreparedStatement stmtPlano = null;
        PreparedStatement stmtItem = null;
        ResultSet rs = null;
        boolean autoCommitOriginal = true;

        try {
            conn = ConnectionFactory.getConnection();
            autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                stmtPlano = conn.prepareStatement(SQL_INSERT_PLANEJAMENTO, new String[]{"ID_PLANEJAMENTO"});
            } catch (Exception ex) {
                stmtPlano = conn.prepareStatement(SQL_INSERT_PLANEJAMENTO, Statement.RETURN_GENERATED_KEYS);
            }

            Long usuarioId = plano.getUsuarioId() != null ? plano.getUsuarioId() :
                    (plano.getUsuario() != null ? plano.getUsuario().getId() : null);
            if (usuarioId == null) {
                throw new DatabasePersistenceException("Planejamento deve estar associado a um usuário.");
            }

            stmtPlano.setLong(1, usuarioId);
            stmtPlano.setString(2, plano.getTitulo());
            stmtPlano.setDate(3, Date.valueOf(plano.getDataInicio()));
            stmtPlano.setDate(4, Date.valueOf(plano.getDataFim()));
            stmtPlano.setString(5, plano.getStatus() != null ? plano.getStatus() : "ATIVO");

            stmtPlano.executeUpdate();

            rs = stmtPlano.getGeneratedKeys();
            Long idPlano;
            if (rs.next()) {
                idPlano = rs.getLong(1);
                plano.setId(idPlano);
            } else {
                throw new DatabasePersistenceException("Falha ao recuperar ID gerado para o planejamento.");
            }

            if (plano.getRefeicoes() != null && !plano.getRefeicoes().isEmpty()) {
                stmtItem = conn.prepareStatement(SQL_INSERT_ITEM_PLANEJAMENTO);
                for (ItemPlanejamento item : plano.getRefeicoes()) {
                    Long idReceita = item.getReceita() != null ? item.getReceita().getId() : null;
                    if (idReceita == null) {
                        throw new DatabasePersistenceException("Receita sem ID não pode ser alocada no planejamento.");
                    }
                    stmtItem.setLong(1, idPlano);
                    stmtItem.setLong(2, idReceita);
                    stmtItem.setString(3, item.getDiaSemana());
                    stmtItem.setString(4, item.getTipoRefeicao());
                    stmtItem.addBatch();
                }
                stmtItem.executeBatch();
            }

            conn.commit();
            return idPlano;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            throw new DatabasePersistenceException("Erro na transação ao cadastrar planejamento semanal: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(autoCommitOriginal);
                } catch (SQLException ignored) {
                }
            }
            ConnectionFactory.fecharRecursos(rs, stmtItem, stmtPlano, conn);
        }
    }

    /**
     * Localiza um planejamento semanal pelo ID, incluindo todas as refeições alocadas.
     *
     * @param id ID do planejamento.
     * @return Optional contendo o planejamento montado.
     */
    public Optional<PlanejamentoSemanal> buscarPorId(Long id) {
        return buscarPorId(id, null);
    }

    public Optional<PlanejamentoSemanal> buscarPorId(Long id, Long usuarioId) {
        Connection conn = null;
        PreparedStatement stmtPlano = null;
        PreparedStatement stmtItens = null;
        ResultSet rsPlano = null;
        ResultSet rsItens = null;

        try {
            conn = ConnectionFactory.getConnection();
            stmtPlano = conn.prepareStatement(usuarioId == null ? SQL_SELECT_BY_ID : SQL_SELECT_BY_ID_OWNER);
            stmtPlano.setLong(1, id);
            if (usuarioId != null) stmtPlano.setLong(2, usuarioId);
            rsPlano = stmtPlano.executeQuery();

            if (!rsPlano.next()) {
                return Optional.empty();
            }

            PlanejamentoSemanal plano = mapearPlanejamento(rsPlano);

            stmtItens = conn.prepareStatement(SQL_SELECT_ITEMS_BY_PLANEJAMENTO);
            stmtItens.setLong(1, id);
            rsItens = stmtItens.executeQuery();

            List<ItemPlanejamento> itens = new ArrayList<>();
            while (rsItens.next()) {
                itens.add(mapearItemPlanejamento(rsItens));
            }
            plano.setRefeicoes(itens);

            return Optional.of(plano);
        } catch (SQLException e) {
            throw new DatabasePersistenceException("Erro ao buscar planejamento semanal por ID: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(rsItens, stmtItens, rsPlano, stmtPlano, conn);
        }
    }

    /**
     * Retorna a lista de todos os planejamentos semanais cadastrados.
     *
     * @return Lista de planos semanais.
     */
    public List<PlanejamentoSemanal> listarTodos() {
        return listarPlanejamentos(null);
    }

    public List<PlanejamentoSemanal> listarPorUsuario(Long usuarioId) {
        return listarPlanejamentos(usuarioId);
    }

    private List<PlanejamentoSemanal> listarPlanejamentos(Long usuarioId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<PlanejamentoSemanal> planos = new ArrayList<>();

        try {
            conn = ConnectionFactory.getConnection();
            stmt = conn.prepareStatement(usuarioId == null ? SQL_SELECT_ALL : SQL_SELECT_BY_OWNER);
            if (usuarioId != null) stmt.setLong(1, usuarioId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                planos.add(mapearPlanejamento(rs));
            }

            for (PlanejamentoSemanal p : planos) {
                carregarRefeicoes(conn, p);
            }

            return planos;
        } catch (SQLException e) {
            throw new DatabasePersistenceException("Erro ao listar planejamentos semanais: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(rs, stmt, conn);
        }
    }

    /**
     * Atualiza dados e refeições alocadas no planejamento semanal.
     */
    public void atualizar(PlanejamentoSemanal plano, Long usuarioId) {
        Connection conn = null;
        PreparedStatement stmtPlano = null;
        PreparedStatement stmtDelete = null;
        PreparedStatement stmtInsert = null;
        boolean autoCommitOriginal = true;

        try {
            conn = ConnectionFactory.getConnection();
            autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);

            stmtPlano = conn.prepareStatement(SQL_UPDATE_PLANEJAMENTO);
            stmtPlano.setString(1, plano.getTitulo());
            stmtPlano.setDate(2, Date.valueOf(plano.getDataInicio()));
            stmtPlano.setDate(3, Date.valueOf(plano.getDataFim()));
            stmtPlano.setString(4, plano.getStatus());
            stmtPlano.setLong(5, plano.getId());
            stmtPlano.setLong(6, usuarioId);

            int rows = stmtPlano.executeUpdate();
            if (rows == 0) {
                throw new DatabasePersistenceException("Nenhum planejamento encontrado com ID " + plano.getId());
            }

            stmtDelete = conn.prepareStatement(SQL_DELETE_ITEMS);
            stmtDelete.setLong(1, plano.getId());
            stmtDelete.executeUpdate();

            if (plano.getRefeicoes() != null && !plano.getRefeicoes().isEmpty()) {
                stmtInsert = conn.prepareStatement(SQL_INSERT_ITEM_PLANEJAMENTO);
                for (ItemPlanejamento item : plano.getRefeicoes()) {
                    Long idReceita = item.getReceita() != null ? item.getReceita().getId() : null;
                    if (idReceita == null) {
                        throw new DatabasePersistenceException("Receita sem ID não pode ser alocada no planejamento.");
                    }
                    stmtInsert.setLong(1, plano.getId());
                    stmtInsert.setLong(2, idReceita);
                    stmtInsert.setString(3, item.getDiaSemana());
                    stmtInsert.setString(4, item.getTipoRefeicao());
                    stmtInsert.addBatch();
                }
                stmtInsert.executeBatch();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            throw new DatabasePersistenceException("Erro ao atualizar planejamento semanal: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(autoCommitOriginal);
                } catch (SQLException ignored) {
                }
            }
            ConnectionFactory.fecharRecursos(stmtInsert, stmtDelete, stmtPlano, conn);
        }
    }

    /**
     * Remove um planejamento semanal e suas refeições alocadas.
     *
     * @param id ID do planejamento.
     */
    public void excluir(Long id, Long usuarioId) {
        Connection conn = null;
        PreparedStatement stmtDeleteItems = null;
        PreparedStatement stmtDeletePlano = null;
        boolean autoCommitOriginal = true;

        try {
            conn = ConnectionFactory.getConnection();
            autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);

            stmtDeleteItems = conn.prepareStatement(SQL_DELETE_ITEMS_BY_OWNER);
            stmtDeleteItems.setLong(1, id);
            stmtDeleteItems.setLong(2, usuarioId);
            stmtDeleteItems.executeUpdate();

            stmtDeletePlano = conn.prepareStatement(SQL_DELETE_PLANEJAMENTO_BY_OWNER);
            stmtDeletePlano.setLong(1, id);
            stmtDeletePlano.setLong(2, usuarioId);
            if (stmtDeletePlano.executeUpdate() == 0) {
                throw new DatabasePersistenceException("Planejamento não encontrado para o usuário autenticado.");
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            throw new DatabasePersistenceException("Erro ao excluir planejamento: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(autoCommitOriginal);
                } catch (SQLException ignored) {
                }
            }
            ConnectionFactory.fecharRecursos(stmtDeleteItems, stmtDeletePlano, conn);
        }
    }

    /**
     * Executa a QUERY ANALÍTICA SQL com JOIN, SUM, GROUP BY e ORDER BY
     * para consolidar a lista de compras a partir de todas as refeições do planejamento.
     *
     * @param planejamentoId ID do planejamento semanal.
     * @return Lista consolidada de itens de compra sem duplicidades.
     */
    public List<ItemListaCompras> buscarListaConsolidadaPorPlanejamento(Long planejamentoId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<ItemListaCompras> listaConsolidada = new ArrayList<>();

        try {
            conn = ConnectionFactory.getConnection();
            stmt = conn.prepareStatement(SQL_QUERY_ANALITICA_CONSOLIDACAO);
            stmt.setLong(1, planejamentoId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Long idIngrediente = rs.getLong("ID_INGREDIENTE");
                String nomeAlimento = rs.getString("ALIMENTO");
                String unidadePadrao = rs.getString("DS_UNIDADE_PADRAO");

                Long idCategoria = rs.getLong("ID_CATEGORIA");
                String nomeCategoria = rs.getString("CATEGORIA_MERCADO");
                CategoriaAlimento categoria = new CategoriaAlimento(idCategoria, nomeCategoria);

                Ingrediente ingrediente = new Ingrediente(idIngrediente, nomeAlimento, categoria, unidadePadrao);

                Double quantidadeTotal = rs.getDouble("QUANTIDADE_TOTAL");
                String unidadeMedida = rs.getString("UNIDADE");

                ItemListaCompras item = new ItemListaCompras(ingrediente, quantidadeTotal, unidadeMedida, false);
                listaConsolidada.add(item);
            }

            return listaConsolidada;
        } catch (SQLException e) {
            throw new DatabasePersistenceException("Erro ao executar query analítica de consolidação de compras: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(rs, stmt, conn);
        }
    }

    /**
     * Executa a query analítica de aproveitamento para calcular as métricas de desperdício evitado.
     *
     * @param planejamentoId ID do planejamento.
     * @return DTO com o relatório de métricas calculadas.
     */
    public RelatorioDesperdicioDTO buscarRelatorioDesperdicio(Long planejamentoId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            Optional<PlanejamentoSemanal> planoOpt = buscarPorId(planejamentoId);
            PlanejamentoSemanal plano = planoOpt.orElseThrow(() ->
                    new DatabasePersistenceException("Planejamento com ID " + planejamentoId + " não localizado."));

            conn = ConnectionFactory.getConnection();
            stmt = conn.prepareStatement(SQL_QUERY_ANALITICA_DESPERDICIO);
            stmt.setLong(1, planejamentoId);
            rs = stmt.executeQuery();

            RelatorioDesperdicioDTO relatorio = new RelatorioDesperdicioDTO();
            relatorio.setPlanejamentoId(planejamentoId);
            relatorio.setTituloPlanejamento(plano.getTitulo());
            relatorio.setTotalRefeicoesPlanejadas(plano.getRefeicoes() != null ? plano.getRefeicoes().size() : 0);

            int totalDistintos = 0;
            int totalReaproveitados = 0;

            while (rs.next()) {
                totalDistintos++;
                String nome = rs.getString("NM_INGREDIENTE");
                String categoria = rs.getString("NM_CATEGORIA");
                int freq = rs.getInt("FREQUENCIA_NO_CARDAPIO");
                double consumoTotal = rs.getDouble("CONSUMO_TOTAL_PLANEJADO");
                String unidade = rs.getString("DS_UNIDADE_MEDIDA");
                String indicador = rs.getString("INDICADOR_CONSUMO_CONSCIENTE");

                if (freq > 1) {
                    totalReaproveitados++;
                }

                RelatorioDesperdicioDTO.ItemAproveitamentoDTO itemDTO =
                        new RelatorioDesperdicioDTO.ItemAproveitamentoDTO(nome, categoria, freq, consumoTotal, unidade, indicador);
                relatorio.getItensAproveitamento().add(itemDTO);
            }

            relatorio.setTotalIngredientesDistintos(totalDistintos);
            relatorio.setTotalItensReaproveitados(totalReaproveitados);

            double taxa = totalDistintos > 0 ? ((double) totalReaproveitados / totalDistintos) * 100.0 : 0.0;
            relatorio.setTaxaAproveitamentoPercentual(Math.round(taxa * 10.0) / 10.0);

            return relatorio;
        } catch (SQLException e) {
            throw new DatabasePersistenceException("Erro ao gerar relatório analítico de desperdício: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(rs, stmt, conn);
        }
    }

    private void carregarRefeicoes(Connection conn, PlanejamentoSemanal plano) throws SQLException {
        try (PreparedStatement stmtItens = conn.prepareStatement(SQL_SELECT_ITEMS_BY_PLANEJAMENTO)) {
            stmtItens.setLong(1, plano.getId());
            try (ResultSet rsItens = stmtItens.executeQuery()) {
                List<ItemPlanejamento> itens = new ArrayList<>();
                while (rsItens.next()) {
                    itens.add(mapearItemPlanejamento(rsItens));
                }
                plano.setRefeicoes(itens);
            }
        }
    }

    private PlanejamentoSemanal mapearPlanejamento(ResultSet rs) throws SQLException {
        Long id = rs.getLong("ID_PLANEJAMENTO");
        Long usuarioId = rs.getLong("ID_USUARIO");
        String titulo = rs.getString("NM_TITULO");
        Date dtInicio = rs.getDate("DT_INICIO");
        Date dtFim = rs.getDate("DT_FIM");
        String status = rs.getString("DS_STATUS");

        LocalDate dataInicio = dtInicio != null ? dtInicio.toLocalDate() : LocalDate.now();
        LocalDate dataFim = dtFim != null ? dtFim.toLocalDate() : LocalDate.now().plusDays(7);

        PlanejamentoSemanal plano = new PlanejamentoSemanal(id, usuarioId, titulo, dataInicio, dataFim, status);

        String nomeUsuario = rs.getString("NM_USUARIO");
        if (nomeUsuario != null) {
            Usuario u = new Usuario();
            u.setId(usuarioId);
            u.setNome(nomeUsuario);
            u.setEmail(rs.getString("DS_EMAIL"));
            plano.setUsuario(u);
        }

        return plano;
    }

    private ItemPlanejamento mapearItemPlanejamento(ResultSet rs) throws SQLException {
        Long idItem = rs.getLong("ID_ITEM_PLANEJAMENTO");
        Long idPlanejamento = rs.getLong("ID_PLANEJAMENTO");
        String diaSemana = rs.getString("DS_DIA_SEMANA");
        String tipoRefeicao = rs.getString("DS_REFEICAO");

        Long idReceita = rs.getLong("ID_RECEITA");
        String nomeReceita = rs.getString("NM_RECEITA");
        String modoPreparo = rs.getString("DS_MODO_PREPARO");
        Integer porcoes = rs.getInt("NR_PORCOES");
        Integer tempoMinutos = rs.getInt("NR_TEMPO_MINUTOS");
        String tipo = rs.getString("DS_TIPO_REFEICAO");

        Receita receita = new Receita(idReceita, null, nomeReceita, modoPreparo, porcoes, tempoMinutos, tipo);

        return new ItemPlanejamento(idItem, idPlanejamento, receita, diaSemana, tipoRefeicao);
    }
}
