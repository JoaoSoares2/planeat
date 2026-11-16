package br.com.fiap.planeat.dao;

import br.com.fiap.planeat.exception.DatabasePersistenceException;
import br.com.fiap.planeat.factory.ConnectionFactory;
import br.com.fiap.planeat.model.CategoriaAlimento;
import br.com.fiap.planeat.model.Ingrediente;
import br.com.fiap.planeat.model.ItemReceita;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data Access Object para a entidade Receita (tabela T_PE_RECEITA)
 * e o relacionamento N:N com ingredientes (tabela T_PE_RECEITA_INGREDIENTE) via JDBC nativo.
 */
public class ReceitaDAO {

    private static final String SQL_INSERT_RECEITA =
            "INSERT INTO T_PE_RECEITA (ID_USUARIO, NM_RECEITA, DS_MODO_PREPARO, NR_PORCOES, NR_TEMPO_MINUTOS, DS_TIPO_REFEICAO, DT_CRIACAO) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_ITEM =
            "INSERT INTO T_PE_RECEITA_INGREDIENTE (ID_RECEITA, ID_INGREDIENTE, VL_QUANTIDADE, DS_UNIDADE_MEDIDA) " +
            "VALUES (?, ?, ?, ?)";

    private static final String SQL_SELECT_RECEITA_BY_ID =
            "SELECT r.ID_RECEITA, r.ID_USUARIO, r.NM_RECEITA, r.DS_MODO_PREPARO, r.NR_PORCOES, " +
            "       r.NR_TEMPO_MINUTOS, r.DS_TIPO_REFEICAO, r.DT_CRIACAO, " +
            "       u.NM_USUARIO, u.DS_EMAIL " +
            "FROM T_PE_RECEITA r " +
            "LEFT JOIN T_PE_USUARIO u ON r.ID_USUARIO = u.ID_USUARIO " +
            "WHERE r.ID_RECEITA = ?";

    private static final String SQL_SELECT_RECEITA_BY_ID_OWNER =
            SQL_SELECT_RECEITA_BY_ID + " AND r.ID_USUARIO = ?";

    private static final String SQL_SELECT_ITEMS_BY_RECEITA =
            "SELECT ri.ID_RECEITA, ri.VL_QUANTIDADE, ri.DS_UNIDADE_MEDIDA, " +
            "       i.ID_INGREDIENTE, i.NM_INGREDIENTE, i.DS_UNIDADE_PADRAO, " +
            "       c.ID_CATEGORIA, c.NM_CATEGORIA " +
            "FROM T_PE_RECEITA_INGREDIENTE ri " +
            "JOIN T_PE_INGREDIENTE i ON ri.ID_INGREDIENTE = i.ID_INGREDIENTE " +
            "JOIN T_PE_CATEGORIA c ON i.ID_CATEGORIA = c.ID_CATEGORIA " +
            "WHERE ri.ID_RECEITA = ? " +
            "ORDER BY c.NM_CATEGORIA ASC, i.NM_INGREDIENTE ASC";

    private static final String SQL_SELECT_ALL_RECEITAS =
            "SELECT r.ID_RECEITA, r.ID_USUARIO, r.NM_RECEITA, r.DS_MODO_PREPARO, r.NR_PORCOES, " +
            "       r.NR_TEMPO_MINUTOS, r.DS_TIPO_REFEICAO, r.DT_CRIACAO, " +
            "       u.NM_USUARIO, u.DS_EMAIL " +
            "FROM T_PE_RECEITA r " +
            "LEFT JOIN T_PE_USUARIO u ON r.ID_USUARIO = u.ID_USUARIO " +
            "ORDER BY r.NM_RECEITA ASC";

    private static final String SQL_SELECT_USER_RECEITAS =
            SQL_SELECT_ALL_RECEITAS.replace("ORDER BY", "WHERE r.ID_USUARIO = ? ORDER BY");

    private static final String SQL_SELECT_BY_TIPO_REFEICAO =
            "SELECT r.ID_RECEITA, r.ID_USUARIO, r.NM_RECEITA, r.DS_MODO_PREPARO, r.NR_PORCOES, " +
            "       r.NR_TEMPO_MINUTOS, r.DS_TIPO_REFEICAO, r.DT_CRIACAO, " +
            "       u.NM_USUARIO, u.DS_EMAIL " +
            "FROM T_PE_RECEITA r " +
            "LEFT JOIN T_PE_USUARIO u ON r.ID_USUARIO = u.ID_USUARIO " +
            "WHERE LOWER(r.DS_TIPO_REFEICAO) = LOWER(?) " +
            "ORDER BY r.NM_RECEITA ASC";

    private static final String SQL_SELECT_USER_BY_TIPO_REFEICAO =
            SQL_SELECT_BY_TIPO_REFEICAO.replace("ORDER BY", "AND r.ID_USUARIO = ? ORDER BY");

    private static final String SQL_UPDATE_RECEITA =
            "UPDATE T_PE_RECEITA SET NM_RECEITA = ?, DS_MODO_PREPARO = ?, " +
            "       NR_PORCOES = ?, NR_TEMPO_MINUTOS = ?, DS_TIPO_REFEICAO = ? " +
            "WHERE ID_RECEITA = ? AND ID_USUARIO = ?";

    private static final String SQL_DELETE_ITEMS =
            "DELETE FROM T_PE_RECEITA_INGREDIENTE WHERE ID_RECEITA = ?";

    private static final String SQL_DELETE_RECEITA_BY_OWNER =
            "DELETE FROM T_PE_RECEITA WHERE ID_RECEITA = ? AND ID_USUARIO = ?";

    private static final String SQL_DELETE_ITEMS_BY_OWNER =
            "DELETE FROM T_PE_RECEITA_INGREDIENTE WHERE ID_RECEITA IN " +
            "(SELECT ID_RECEITA FROM T_PE_RECEITA WHERE ID_RECEITA = ? AND ID_USUARIO = ?)";
    /**
     * Insere uma receita e todos os seus ingredientes associados em transação segura (ACID).
     *
     * @param receita Receita contendo a lista de ingredientes fracionados.
     * @return ID gerado para a receita.
     */
    public Long inserir(Receita receita) {
        Connection conn = null;
        PreparedStatement stmtReceita = null;
        PreparedStatement stmtItem = null;
        ResultSet rs = null;
        boolean autoCommitOriginal = true;

        try {
            conn = ConnectionFactory.getConnection();
            autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                stmtReceita = conn.prepareStatement(SQL_INSERT_RECEITA, new String[]{"ID_RECEITA"});
            } catch (Exception ex) {
                stmtReceita = conn.prepareStatement(SQL_INSERT_RECEITA, Statement.RETURN_GENERATED_KEYS);
            }

            Long usuarioId = receita.getUsuarioId() != null ? receita.getUsuarioId() :
                    (receita.getUsuario() != null ? receita.getUsuario().getId() : null);
            if (usuarioId == null) {
                throw new DatabasePersistenceException("Receita deve estar associada a um usuário.");
            }

            stmtReceita.setLong(1, usuarioId);
            stmtReceita.setString(2, receita.getNome());
            stmtReceita.setString(3, receita.getModoPreparo());
            stmtReceita.setInt(4, receita.getPorcoes());
            stmtReceita.setInt(5, receita.getTempoPreparoMinutos());
            stmtReceita.setString(6, receita.getTipoRefeicao());

            LocalDate dataCriacao = receita.getDataCriacao() != null ? receita.getDataCriacao() : LocalDate.now();
            stmtReceita.setDate(7, Date.valueOf(dataCriacao));

            stmtReceita.executeUpdate();

            rs = stmtReceita.getGeneratedKeys();
            Long idReceita;
            if (rs.next()) {
                idReceita = rs.getLong(1);
                receita.setId(idReceita);
            } else {
                throw new DatabasePersistenceException("Falha ao recuperar a chave primária da receita inserida.");
            }

            if (receita.getIngredientes() != null && !receita.getIngredientes().isEmpty()) {
                stmtItem = conn.prepareStatement(SQL_INSERT_ITEM);
                for (ItemReceita item : receita.getIngredientes()) {
                    Long idIngrediente = item.getIngrediente() != null ? item.getIngrediente().getId() : null;
                    if (idIngrediente == null) {
                        throw new DatabasePersistenceException("Ingrediente sem ID não pode ser associado à receita.");
                    }
                    stmtItem.setLong(1, idReceita);
                    stmtItem.setLong(2, idIngrediente);
                    stmtItem.setDouble(3, item.getQuantidade());
                    stmtItem.setString(4, item.getUnidadeMedida());
                    stmtItem.addBatch();
                }
                stmtItem.executeBatch();
            }

            conn.commit();
            return idReceita;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    // erro de rollback secundário
                }
            }
            throw new DatabasePersistenceException("Erro na transação de inserção de receita: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(autoCommitOriginal);
                } catch (SQLException ignored) {
                }
            }
            ConnectionFactory.fecharRecursos(rs, stmtReceita, stmtItem, conn);
        }
    }

    /**
     * Localiza uma receita pelo ID, incluindo todos os ingredientes associados N:N e dados do autor.
     *
     * @param id ID da receita.
     * @return Optional contendo a receita completa.
     */
    public Optional<Receita> buscarPorId(Long id) {
        return buscarPorId(id, null);
    }

    public Optional<Receita> buscarPorId(Long id, Long usuarioId) {
        Connection conn = null;
        PreparedStatement stmtReceita = null;
        PreparedStatement stmtItens = null;
        ResultSet rsReceita = null;
        ResultSet rsItens = null;

        try {
            conn = ConnectionFactory.getConnection();
            stmtReceita = conn.prepareStatement(usuarioId == null ? SQL_SELECT_RECEITA_BY_ID : SQL_SELECT_RECEITA_BY_ID_OWNER);
            stmtReceita.setLong(1, id);
            if (usuarioId != null) stmtReceita.setLong(2, usuarioId);
            rsReceita = stmtReceita.executeQuery();

            if (!rsReceita.next()) {
                return Optional.empty();
            }

            Receita receita = mapearReceita(rsReceita);

            stmtItens = conn.prepareStatement(SQL_SELECT_ITEMS_BY_RECEITA);
            stmtItens.setLong(1, id);
            rsItens = stmtItens.executeQuery();

            List<ItemReceita> itens = new ArrayList<>();
            while (rsItens.next()) {
                itens.add(mapearItemReceita(rsItens));
            }
            receita.setIngredientes(itens);

            return Optional.of(receita);
        } catch (SQLException e) {
            throw new DatabasePersistenceException("Erro ao buscar receita por ID: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(rsItens, stmtItens, rsReceita, stmtReceita, conn);
        }
    }

    /**
     * Retorna todas as receitas cadastradas no sistema, com seus respectivos ingredientes.
     *
     * @return Lista de receitas.
     */
    public List<Receita> listarTodos() {
        return listarReceitas(null, null);
    }

    public List<Receita> listarPorUsuario(Long usuarioId) {
        return listarReceitas(usuarioId, null);
    }

    private List<Receita> listarReceitas(Long usuarioId, String tipoRefeicao) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Receita> receitas = new ArrayList<>();

        try {
            conn = ConnectionFactory.getConnection();
            boolean porTipo = tipoRefeicao != null;
            String sql = porTipo
                    ? (usuarioId == null ? SQL_SELECT_BY_TIPO_REFEICAO : SQL_SELECT_USER_BY_TIPO_REFEICAO)
                    : (usuarioId == null ? SQL_SELECT_ALL_RECEITAS : SQL_SELECT_USER_RECEITAS);
            stmt = conn.prepareStatement(sql);
            if (porTipo) {
                stmt.setString(1, tipoRefeicao);
                if (usuarioId != null) stmt.setLong(2, usuarioId);
            } else if (usuarioId != null) {
                stmt.setLong(1, usuarioId);
            }
            rs = stmt.executeQuery();

            while (rs.next()) {
                receitas.add(mapearReceita(rs));
            }

            // Carrega os ingredientes para cada receita
            for (Receita r : receitas) {
                carregarIngredientes(conn, r);
            }

            return receitas;
        } catch (SQLException e) {
            throw new DatabasePersistenceException("Erro ao listar receitas: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(rs, stmt, conn);
        }
    }

    /**
     * Retorna receitas filtradas por tipo de refeição (ex: Almoço, Jantar).
     *
     * @param tipoRefeicao Tipo da refeição.
     * @return Lista de receitas filtradas.
     */
    public List<Receita> listarPorTipoRefeicao(String tipoRefeicao) {
        return listarReceitas(null, tipoRefeicao);
    }

    public List<Receita> listarPorTipoRefeicao(Long usuarioId, String tipoRefeicao) {
        return listarReceitas(usuarioId, tipoRefeicao);
    }

    /**
     * Atualiza os dados cadastrais da receita e sincroniza a lista de ingredientes associados.
     *
     * @param receita Receita com novos dados e lista de ingredientes.
     */
    public void atualizar(Receita receita, Long usuarioId) {
        Connection conn = null;
        PreparedStatement stmtReceita = null;
        PreparedStatement stmtDeleteItems = null;
        PreparedStatement stmtInsertItem = null;
        boolean autoCommitOriginal = true;

        try {
            conn = ConnectionFactory.getConnection();
            autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);

            stmtReceita = conn.prepareStatement(SQL_UPDATE_RECEITA);
            stmtReceita.setString(1, receita.getNome());
            stmtReceita.setString(2, receita.getModoPreparo());
            stmtReceita.setInt(3, receita.getPorcoes());
            stmtReceita.setInt(4, receita.getTempoPreparoMinutos());
            stmtReceita.setString(5, receita.getTipoRefeicao());
            stmtReceita.setLong(6, receita.getId());
            stmtReceita.setLong(7, usuarioId);

            int linhasAfetadas = stmtReceita.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new DatabasePersistenceException("Nenhuma receita localizada com o ID " + receita.getId() + " para atualização.");
            }

            // Remove os itens antigos da associativa
            stmtDeleteItems = conn.prepareStatement(SQL_DELETE_ITEMS);
            stmtDeleteItems.setLong(1, receita.getId());
            stmtDeleteItems.executeUpdate();

            // Insere a nova lista de ingredientes
            if (receita.getIngredientes() != null && !receita.getIngredientes().isEmpty()) {
                stmtInsertItem = conn.prepareStatement(SQL_INSERT_ITEM);
                for (ItemReceita item : receita.getIngredientes()) {
                    Long idIngrediente = item.getIngrediente() != null ? item.getIngrediente().getId() : null;
                    if (idIngrediente == null) {
                        throw new DatabasePersistenceException("Ingrediente sem ID não pode ser associado à receita.");
                    }
                    stmtInsertItem.setLong(1, receita.getId());
                    stmtInsertItem.setLong(2, idIngrediente);
                    stmtInsertItem.setDouble(3, item.getQuantidade());
                    stmtInsertItem.setString(4, item.getUnidadeMedida());
                    stmtInsertItem.addBatch();
                }
                stmtInsertItem.executeBatch();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            throw new DatabasePersistenceException("Erro na transação de atualização de receita: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(autoCommitOriginal);
                } catch (SQLException ignored) {
                }
            }
            ConnectionFactory.fecharRecursos(stmtInsertItem, stmtDeleteItems, stmtReceita, conn);
        }
    }

    /**
     * Remove uma receita do banco de dados e suas referências na tabela associativa.
     *
     * @param id Identificador da receita a excluir.
     */
    public void excluir(Long id, Long usuarioId) {
        Connection conn = null;
        PreparedStatement stmtDeleteItems = null;
        PreparedStatement stmtDeleteReceita = null;
        boolean autoCommitOriginal = true;

        try {
            conn = ConnectionFactory.getConnection();
            autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);

            stmtDeleteItems = conn.prepareStatement(SQL_DELETE_ITEMS_BY_OWNER);
            stmtDeleteItems.setLong(1, id);
            stmtDeleteItems.setLong(2, usuarioId);
            stmtDeleteItems.executeUpdate();

            stmtDeleteReceita = conn.prepareStatement(SQL_DELETE_RECEITA_BY_OWNER);
            stmtDeleteReceita.setLong(1, id);
            stmtDeleteReceita.setLong(2, usuarioId);
            if (stmtDeleteReceita.executeUpdate() == 0) {
                throw new DatabasePersistenceException("Receita não encontrada para o usuário autenticado.");
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            throw new DatabasePersistenceException("Erro na transação de exclusão de receita: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(autoCommitOriginal);
                } catch (SQLException ignored) {
                }
            }
            ConnectionFactory.fecharRecursos(stmtDeleteItems, stmtDeleteReceita, conn);
        }
    }

    private void carregarIngredientes(Connection conn, Receita receita) throws SQLException {
        try (PreparedStatement stmtItens = conn.prepareStatement(SQL_SELECT_ITEMS_BY_RECEITA)) {
            stmtItens.setLong(1, receita.getId());
            try (ResultSet rsItens = stmtItens.executeQuery()) {
                List<ItemReceita> itens = new ArrayList<>();
                while (rsItens.next()) {
                    itens.add(mapearItemReceita(rsItens));
                }
                receita.setIngredientes(itens);
            }
        }
    }

    private Receita mapearReceita(ResultSet rs) throws SQLException {
        Long id = rs.getLong("ID_RECEITA");
        Long usuarioId = rs.getLong("ID_USUARIO");
        String nome = rs.getString("NM_RECEITA");
        String modoPreparo = rs.getString("DS_MODO_PREPARO");
        Integer porcoes = rs.getInt("NR_PORCOES");
        Integer tempoPreparo = rs.getInt("NR_TEMPO_MINUTOS");
        String tipoRefeicao = rs.getString("DS_TIPO_REFEICAO");
        Date dtCriacao = rs.getDate("DT_CRIACAO");
        LocalDate dataCriacao = dtCriacao != null ? dtCriacao.toLocalDate() : LocalDate.now();

        Receita receita = new Receita(id, dataCriacao, usuarioId, nome, modoPreparo, porcoes, tempoPreparo, tipoRefeicao);

        String nomeUsuario = rs.getString("NM_USUARIO");
        if (nomeUsuario != null) {
            Usuario u = new Usuario();
            u.setId(usuarioId);
            u.setNome(nomeUsuario);
            u.setEmail(rs.getString("DS_EMAIL"));
            receita.setUsuario(u);
        }

        return receita;
    }

    private ItemReceita mapearItemReceita(ResultSet rs) throws SQLException {
        Long idIngrediente = rs.getLong("ID_INGREDIENTE");
        String nomeIngrediente = rs.getString("NM_INGREDIENTE");
        String unidadePadrao = rs.getString("DS_UNIDADE_PADRAO");

        Long idCategoria = rs.getLong("ID_CATEGORIA");
        String nomeCategoria = rs.getString("NM_CATEGORIA");
        CategoriaAlimento categoria = new CategoriaAlimento(idCategoria, nomeCategoria);

        Ingrediente ingrediente = new Ingrediente(idIngrediente, nomeIngrediente, categoria, unidadePadrao);

        Double quantidade = rs.getDouble("VL_QUANTIDADE");
        String unidadeMedida = rs.getString("DS_UNIDADE_MEDIDA");

        return new ItemReceita(ingrediente, quantidade, unidadeMedida);
    }
}
