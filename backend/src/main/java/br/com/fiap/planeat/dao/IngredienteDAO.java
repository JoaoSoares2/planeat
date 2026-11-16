package br.com.fiap.planeat.dao;

import br.com.fiap.planeat.exception.DatabasePersistenceException;
import br.com.fiap.planeat.factory.ConnectionFactory;
import br.com.fiap.planeat.model.CategoriaAlimento;
import br.com.fiap.planeat.model.Ingrediente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object para a entidade Ingrediente (tabela T_PE_INGREDIENTE) via JDBC nativo.
 */
public class IngredienteDAO {

    private static final String SQL_INSERT =
            "INSERT INTO T_PE_INGREDIENTE (NM_INGREDIENTE, ID_CATEGORIA, DS_UNIDADE_PADRAO) VALUES (?, ?, ?)";

    private static final String SQL_SELECT_BASE =
            "SELECT i.ID_INGREDIENTE, i.NM_INGREDIENTE, i.DS_UNIDADE_PADRAO, " +
            "       c.ID_CATEGORIA, c.NM_CATEGORIA " +
            "FROM T_PE_INGREDIENTE i " +
            "JOIN T_PE_CATEGORIA c ON i.ID_CATEGORIA = c.ID_CATEGORIA ";

    private static final String SQL_SELECT_BY_ID =
            SQL_SELECT_BASE + "WHERE i.ID_INGREDIENTE = ?";

    private static final String SQL_SELECT_BY_NAME =
            SQL_SELECT_BASE + "WHERE LOWER(i.NM_INGREDIENTE) = LOWER(?)";

    private static final String SQL_SELECT_ALL =
            SQL_SELECT_BASE + "ORDER BY c.NM_CATEGORIA ASC, i.NM_INGREDIENTE ASC";

    private static final String SQL_SELECT_BY_CATEGORIA =
            SQL_SELECT_BASE + "WHERE i.ID_CATEGORIA = ? ORDER BY i.NM_INGREDIENTE ASC";

    private static final String SQL_UPDATE =
            "UPDATE T_PE_INGREDIENTE SET NM_INGREDIENTE = ?, ID_CATEGORIA = ?, DS_UNIDADE_PADRAO = ? WHERE ID_INGREDIENTE = ?";

    private static final String SQL_DELETE =
            "DELETE FROM T_PE_INGREDIENTE WHERE ID_INGREDIENTE = ?";

    /**
     * Insere um novo ingrediente no catálogo da base relacional.
     *
     * @param ingrediente Ingrediente com categoria associada.
     * @return ID gerado pelo banco.
     */
    public Long inserir(Ingrediente ingrediente) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            try {
                stmt = conn.prepareStatement(SQL_INSERT, new String[]{"ID_INGREDIENTE"});
            } catch (Exception ex) {
                stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            }

            stmt.setString(1, ingrediente.getNome());
            Long catId = ingrediente.getCategoria() != null ? ingrediente.getCategoria().getId() : null;
            if (catId == null) {
                throw new DatabasePersistenceException("Categoria associada ao ingrediente não possui ID válido.");
            }
            stmt.setLong(2, catId);
            stmt.setString(3, ingrediente.getUnidadePadrao());

            stmt.executeUpdate();

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                Long idGerado = rs.getLong(1);
                ingrediente.setId(idGerado);
                return idGerado;
            } else {
                throw new DatabasePersistenceException("Falha ao recuperar a chave primária gerada para o ingrediente.");
            }
        } catch (SQLException e) {
            throw new DatabasePersistenceException("Erro ao inserir ingrediente no banco: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(rs, stmt, conn);
        }
    }

    /**
     * Localiza um ingrediente pelo seu ID único, mapeando a respectiva categoria.
     *
     * @param id Identificador do ingrediente.
     * @return Optional com o ingrediente encontrado.
     */
    public Optional<Ingrediente> buscarPorId(Long id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_BY_ID);
            stmt.setLong(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapearIngrediente(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabasePersistenceException("Erro ao buscar ingrediente por ID: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(rs, stmt, conn);
        }
    }

    /**
     * Localiza um ingrediente pelo seu nome exato (case-insensitive).
     *
     * @param nome Nome do ingrediente.
     * @return Optional com o ingrediente localizado.
     */
    public Optional<Ingrediente> buscarPorNome(String nome) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_BY_NAME);
            stmt.setString(1, nome);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapearIngrediente(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabasePersistenceException("Erro ao buscar ingrediente por nome: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(rs, stmt, conn);
        }
    }

    /**
     * Retorna a lista completa de ingredientes do catálogo com as respectivas categorias.
     *
     * @return Lista de ingredientes ordenados por categoria e nome.
     */
    public List<Ingrediente> listarTodos() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Ingrediente> ingredientes = new ArrayList<>();

        try {
            conn = ConnectionFactory.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_ALL);
            rs = stmt.executeQuery();

            while (rs.next()) {
                ingredientes.add(mapearIngrediente(rs));
            }
            return ingredientes;
        } catch (SQLException e) {
            throw new DatabasePersistenceException("Erro ao listar catálogo de ingredientes: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(rs, stmt, conn);
        }
    }

    /**
     * Retorna os ingredientes pertencentes a uma categoria específica.
     *
     * @param categoriaId Identificador da categoria.
     * @return Lista de ingredientes filtrados.
     */
    public List<Ingrediente> listarPorCategoria(Long categoriaId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Ingrediente> ingredientes = new ArrayList<>();

        try {
            conn = ConnectionFactory.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_BY_CATEGORIA);
            stmt.setLong(1, categoriaId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                ingredientes.add(mapearIngrediente(rs));
            }
            return ingredientes;
        } catch (SQLException e) {
            throw new DatabasePersistenceException("Erro ao listar ingredientes por categoria: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(rs, stmt, conn);
        }
    }

    /**
     * Atualiza os dados de um ingrediente no catálogo.
     *
     * @param ingrediente Ingrediente contendo os novos dados.
     */
    public void atualizar(Ingrediente ingrediente) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            stmt = conn.prepareStatement(SQL_UPDATE);
            stmt.setString(1, ingrediente.getNome());
            Long catId = ingrediente.getCategoria() != null ? ingrediente.getCategoria().getId() : null;
            if (catId == null) {
                throw new DatabasePersistenceException("Categoria associada ao ingrediente não possui ID para atualização.");
            }
            stmt.setLong(2, catId);
            stmt.setString(3, ingrediente.getUnidadePadrao());
            stmt.setLong(4, ingrediente.getId());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new DatabasePersistenceException("Nenhum ingrediente localizado com o ID " + ingrediente.getId() + " para atualização.");
            }
        } catch (SQLException e) {
            throw new DatabasePersistenceException("Erro ao atualizar ingrediente: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(stmt, conn);
        }
    }

    /**
     * Exclui um ingrediente do catálogo pelo ID.
     *
     * @param id Identificador do ingrediente.
     */
    public void excluir(Long id) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            stmt = conn.prepareStatement(SQL_DELETE);
            stmt.setLong(1, id);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabasePersistenceException("Erro ao excluir ingrediente: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(stmt, conn);
        }
    }

    private Ingrediente mapearIngrediente(ResultSet rs) throws SQLException {
        Long id = rs.getLong("ID_INGREDIENTE");
        String nome = rs.getString("NM_INGREDIENTE");
        String unidadePadrao = rs.getString("DS_UNIDADE_PADRAO");

        Long idCategoria = rs.getLong("ID_CATEGORIA");
        String nomeCategoria = rs.getString("NM_CATEGORIA");
        CategoriaAlimento categoria = new CategoriaAlimento(idCategoria, nomeCategoria);

        return new Ingrediente(id, nome, categoria, unidadePadrao);
    }
}
