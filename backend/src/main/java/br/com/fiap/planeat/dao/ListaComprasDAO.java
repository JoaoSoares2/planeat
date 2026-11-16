package br.com.fiap.planeat.dao;

import br.com.fiap.planeat.exception.DatabasePersistenceException;
import br.com.fiap.planeat.factory.ConnectionFactory;
import br.com.fiap.planeat.model.CategoriaAlimento;
import br.com.fiap.planeat.model.Ingrediente;
import br.com.fiap.planeat.model.ItemListaCompras;

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
 * Data Access Object para as tabelas T_PE_LISTA_COMPRAS e T_PE_ITEM_LISTA via JDBC nativo.
 */
public class ListaComprasDAO {

    private static final String SQL_SELECT_LISTA_BY_PLAN =
            "SELECT ID_LISTA, ID_PLANEJAMENTO, DT_GERACAO, DS_STATUS FROM T_PE_LISTA_COMPRAS WHERE ID_PLANEJAMENTO = ?";

    private static final String SQL_INSERT_LISTA =
            "INSERT INTO T_PE_LISTA_COMPRAS (ID_PLANEJAMENTO, DT_GERACAO, DS_STATUS) VALUES (?, ?, ?)";

    private static final String SQL_SELECT_ITEMS_BY_LISTA =
            "SELECT il.ID_ITEM_LISTA, il.ID_LISTA, il.VL_QUANTIDADE_TOTAL, il.DS_UNIDADE_MEDIDA, il.FL_COMPRADO, " +
            "       i.ID_INGREDIENTE, i.NM_INGREDIENTE, i.DS_UNIDADE_PADRAO, " +
            "       c.ID_CATEGORIA, c.NM_CATEGORIA " +
            "FROM T_PE_ITEM_LISTA il " +
            "JOIN T_PE_INGREDIENTE i ON il.ID_INGREDIENTE = i.ID_INGREDIENTE " +
            "JOIN T_PE_CATEGORIA c ON i.ID_CATEGORIA = c.ID_CATEGORIA " +
            "WHERE il.ID_LISTA = ? " +
            "ORDER BY c.NM_CATEGORIA ASC, i.NM_INGREDIENTE ASC";

    private static final String SQL_SELECT_ITEM_BY_ID =
            "SELECT il.ID_ITEM_LISTA, il.ID_LISTA, il.VL_QUANTIDADE_TOTAL, il.DS_UNIDADE_MEDIDA, il.FL_COMPRADO, " +
            "       i.ID_INGREDIENTE, i.NM_INGREDIENTE, i.DS_UNIDADE_PADRAO, " +
            "       c.ID_CATEGORIA, c.NM_CATEGORIA " +
            "FROM T_PE_ITEM_LISTA il " +
            "JOIN T_PE_INGREDIENTE i ON il.ID_INGREDIENTE = i.ID_INGREDIENTE " +
            "JOIN T_PE_CATEGORIA c ON i.ID_CATEGORIA = c.ID_CATEGORIA " +
            "WHERE il.ID_ITEM_LISTA = ?";

    private static final String SQL_INSERT_ITEM_LISTA =
            "INSERT INTO T_PE_ITEM_LISTA (ID_LISTA, ID_INGREDIENTE, VL_QUANTIDADE_TOTAL, DS_UNIDADE_MEDIDA, FL_COMPRADO) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_DELETE_ITEMS_BY_LISTA =
            "DELETE FROM T_PE_ITEM_LISTA WHERE ID_LISTA = ?";

    private static final String SQL_UPDATE_STATUS_COMPRADO =
            "UPDATE T_PE_ITEM_LISTA SET FL_COMPRADO = ? WHERE ID_ITEM_LISTA = ?";

    /**
     * Localiza itens da lista de compras já persistida para o planejamento indicado.
     *
     * @param planejamentoId ID do plano semanal.
     * @return Lista com os itens persistidos, ou lista vazia se ainda não gerada.
     */
    public List<ItemListaCompras> buscarItensPorPlanejamento(Long planejamentoId) {
        Connection conn = null;
        PreparedStatement stmtLista = null;
        PreparedStatement stmtItens = null;
        ResultSet rsLista = null;
        ResultSet rsItens = null;
        List<ItemListaCompras> itens = new ArrayList<>();

        try {
            conn = ConnectionFactory.getConnection();
            stmtLista = conn.prepareStatement(SQL_SELECT_LISTA_BY_PLAN);
            stmtLista.setLong(1, planejamentoId);
            rsLista = stmtLista.executeQuery();

            if (!rsLista.next()) {
                return itens;
            }

            Long idLista = rsLista.getLong("ID_LISTA");

            stmtItens = conn.prepareStatement(SQL_SELECT_ITEMS_BY_LISTA);
            stmtItens.setLong(1, idLista);
            rsItens = stmtItens.executeQuery();

            while (rsItens.next()) {
                itens.add(mapearItemLista(rsItens));
            }

            return itens;
        } catch (SQLException e) {
            throw new DatabasePersistenceException("Erro ao buscar lista de compras por planejamento: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(rsItens, stmtItens, rsLista, stmtLista, conn);
        }
    }

    /**
     * Salva ou atualiza a lista de compras consolidada para um planejamento em transação ACID.
     *
     * @param planejamentoId ID do planejamento associado.
     * @param itens Lista de itens consolidados calculados.
     * @return ID da lista de compras gerada.
     */
    public Long salvarListaConsolidada(Long planejamentoId, List<ItemListaCompras> itens) {
        Connection conn = null;
        PreparedStatement stmtBusca = null;
        PreparedStatement stmtCriaLista = null;
        PreparedStatement stmtDeleteAntigos = null;
        PreparedStatement stmtInsereItem = null;
        ResultSet rsBusca = null;
        ResultSet rsChave = null;
        boolean autoCommitOriginal = true;

        try {
            conn = ConnectionFactory.getConnection();
            autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);

            Long idLista;

            stmtBusca = conn.prepareStatement(SQL_SELECT_LISTA_BY_PLAN);
            stmtBusca.setLong(1, planejamentoId);
            rsBusca = stmtBusca.executeQuery();

            if (rsBusca.next()) {
                idLista = rsBusca.getLong("ID_LISTA");
                stmtDeleteAntigos = conn.prepareStatement(SQL_DELETE_ITEMS_BY_LISTA);
                stmtDeleteAntigos.setLong(1, idLista);
                stmtDeleteAntigos.executeUpdate();
            } else {
                try {
                    stmtCriaLista = conn.prepareStatement(SQL_INSERT_LISTA, new String[]{"ID_LISTA"});
                } catch (Exception ex) {
                    stmtCriaLista = conn.prepareStatement(SQL_INSERT_LISTA, Statement.RETURN_GENERATED_KEYS);
                }
                stmtCriaLista.setLong(1, planejamentoId);
                stmtCriaLista.setDate(2, Date.valueOf(LocalDate.now()));
                stmtCriaLista.setString(3, "PENDENTE");
                stmtCriaLista.executeUpdate();

                rsChave = stmtCriaLista.getGeneratedKeys();
                if (rsChave.next()) {
                    idLista = rsChave.getLong(1);
                } else {
                    throw new DatabasePersistenceException("Falha ao recuperar ID da nova lista de compras.");
                }
            }

            if (itens != null && !itens.isEmpty()) {
                stmtInsereItem = conn.prepareStatement(SQL_INSERT_ITEM_LISTA, new String[]{"ID_ITEM_LISTA"});
                for (ItemListaCompras item : itens) {
                    item.setListaId(idLista);
                    Long idIngrediente = item.getIngrediente() != null ? item.getIngrediente().getId() : null;
                    if (idIngrediente == null) {
                        throw new DatabasePersistenceException("Ingrediente sem ID não pode compor a lista de compras.");
                    }
                    stmtInsereItem.setLong(1, idLista);
                    stmtInsereItem.setLong(2, idIngrediente);
                    stmtInsereItem.setDouble(3, item.getQuantidadeTotal());
                    stmtInsereItem.setString(4, item.getUnidadeMedida());
                    stmtInsereItem.setString(5, Boolean.TRUE.equals(item.getComprado()) ? "S" : "N");
                    stmtInsereItem.executeUpdate();

                    try (ResultSet rsi = stmtInsereItem.getGeneratedKeys()) {
                        if (rsi.next()) {
                            item.setId(rsi.getLong(1));
                        }
                    }
                }
            }

            conn.commit();
            return idLista;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            throw new DatabasePersistenceException("Erro na transação de persistência da lista de compras: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(autoCommitOriginal);
                } catch (SQLException ignored) {
                }
            }
            ConnectionFactory.fecharRecursos(rsChave, stmtInsereItem, stmtDeleteAntigos, stmtCriaLista, rsBusca, stmtBusca, conn);
        }
    }

    /**
     * Localiza um item individual da lista de compras pelo seu ID único.
     *
     * @param id ID do item da lista.
     * @return Optional com o item encontrado.
     */
    public Optional<ItemListaCompras> buscarItemPorId(Long id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_ITEM_BY_ID);
            stmt.setLong(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapearItemLista(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabasePersistenceException("Erro ao buscar item da lista de compras por ID: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(rs, stmt, conn);
        }
    }

    /**
     * Atualiza a flag de comprado ('S'/'N') de um item no checklist do supermercado.
     *
     * @param itemListaId ID do item.
     * @param comprado Novo status.
     */
    public void atualizarStatusComprado(Long itemListaId, boolean comprado) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            stmt = conn.prepareStatement(SQL_UPDATE_STATUS_COMPRADO);
            stmt.setString(1, comprado ? "S" : "N");
            stmt.setLong(2, itemListaId);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new DatabasePersistenceException("Nenhum item de lista de compras encontrado com ID " + itemListaId);
            }
        } catch (SQLException e) {
            throw new DatabasePersistenceException("Erro ao atualizar status do item no checklist de compras: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(stmt, conn);
        }
    }

    private ItemListaCompras mapearItemLista(ResultSet rs) throws SQLException {
        Long idItem = rs.getLong("ID_ITEM_LISTA");
        Long idLista = rs.getLong("ID_LISTA");
        Double qtdTotal = rs.getDouble("VL_QUANTIDADE_TOTAL");
        String unidade = rs.getString("DS_UNIDADE_MEDIDA");
        String flag = rs.getString("FL_COMPRADO");
        boolean comprado = "S".equalsIgnoreCase(flag);

        Long idIngrediente = rs.getLong("ID_INGREDIENTE");
        String nomeIngrediente = rs.getString("NM_INGREDIENTE");
        String unidadePadrao = rs.getString("DS_UNIDADE_PADRAO");

        Long idCat = rs.getLong("ID_CATEGORIA");
        String nomeCat = rs.getString("NM_CATEGORIA");
        CategoriaAlimento categoria = new CategoriaAlimento(idCat, nomeCat);

        Ingrediente ingrediente = new Ingrediente(idIngrediente, nomeIngrediente, categoria, unidadePadrao);

        return new ItemListaCompras(idItem, idLista, ingrediente, qtdTotal, unidade, comprado);
    }
}
