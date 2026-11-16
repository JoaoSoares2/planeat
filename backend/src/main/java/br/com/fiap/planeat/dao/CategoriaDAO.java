package br.com.fiap.planeat.dao;

import br.com.fiap.planeat.exception.DatabasePersistenceException;
import br.com.fiap.planeat.factory.ConnectionFactory;
import br.com.fiap.planeat.model.CategoriaAlimento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object para a entidade CategoriaAlimento (tabela T_PE_CATEGORIA) via JDBC nativo.
 */
public class CategoriaDAO {

    private static final String SQL_INSERT =
            "INSERT INTO T_PE_CATEGORIA (NM_CATEGORIA) VALUES (?)";
    private static final String SQL_SELECT_BY_ID =
            "SELECT ID_CATEGORIA, NM_CATEGORIA FROM T_PE_CATEGORIA WHERE ID_CATEGORIA = ?";
    private static final String SQL_SELECT_BY_NAME =
            "SELECT ID_CATEGORIA, NM_CATEGORIA FROM T_PE_CATEGORIA WHERE LOWER(NM_CATEGORIA) = LOWER(?)";
    private static final String SQL_SELECT_ALL =
            "SELECT ID_CATEGORIA, NM_CATEGORIA FROM T_PE_CATEGORIA ORDER BY NM_CATEGORIA ASC";
    private static final String SQL_UPDATE =
            "UPDATE T_PE_CATEGORIA SET NM_CATEGORIA = ? WHERE ID_CATEGORIA = ?";
    private static final String SQL_DELETE =
            "DELETE FROM T_PE_CATEGORIA WHERE ID_CATEGORIA = ?";

    /**
     * Insere uma nova categoria de alimento na base e retorna o ID gerado.
     *
     * @param categoria Categoria a ser persistida.
     * @return ID gerado pelo banco.
     */
    public Long inserir(CategoriaAlimento categoria) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            try {
                stmt = conn.prepareStatement(SQL_INSERT, new String[]{"ID_CATEGORIA"});
            } catch (Exception ex) {
                stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            }

            stmt.setString(1, categoria.getNome());
            stmt.executeUpdate();

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                Long idGerado = rs.getLong(1);
                categoria.setId(idGerado);
                return idGerado;
            } else {
                throw new DatabasePersistenceException("Falha ao recuperar a chave primária gerada para a categoria.");
            }
        } catch (SQLException e) {
            throw new DatabasePersistenceException("Erro ao inserir categoria no banco: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(rs, stmt, conn);
        }
    }

    /**
     * Localiza uma categoria pelo seu identificador único.
     *
     * @param id ID da categoria.
     * @return Optional com a categoria, se encontrada.
     */
    public Optional<CategoriaAlimento> buscarPorId(Long id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_BY_ID);
            stmt.setLong(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapearCategoria(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabasePersistenceException("Erro ao buscar categoria por ID: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(rs, stmt, conn);
        }
    }

    /**
     * Localiza uma categoria pelo seu nome exato (case-insensitive).
     *
     * @param nome Nome da categoria.
     * @return Optional com a categoria encontrada.
     */
    public Optional<CategoriaAlimento> buscarPorNome(String nome) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_BY_NAME);
            stmt.setString(1, nome);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapearCategoria(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabasePersistenceException("Erro ao buscar categoria por nome: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(rs, stmt, conn);
        }
    }

    /**
     * Retorna todas as categorias cadastradas na base de dados.
     *
     * @return Lista de categorias em ordem alfabética.
     */
    public List<CategoriaAlimento> listarTodos() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<CategoriaAlimento> categorias = new ArrayList<>();

        try {
            conn = ConnectionFactory.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_ALL);
            rs = stmt.executeQuery();

            while (rs.next()) {
                categorias.add(mapearCategoria(rs));
            }
            return categorias;
        } catch (SQLException e) {
            throw new DatabasePersistenceException("Erro ao listar categorias: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(rs, stmt, conn);
        }
    }

    /**
     * Atualiza o nome de uma categoria existente.
     *
     * @param categoria Categoria com dados atualizados.
     */
    public void atualizar(CategoriaAlimento categoria) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            stmt = conn.prepareStatement(SQL_UPDATE);
            stmt.setString(1, categoria.getNome());
            stmt.setLong(2, categoria.getId());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new DatabasePersistenceException("Nenhuma categoria localizada com o ID " + categoria.getId() + " para atualização.");
            }
        } catch (SQLException e) {
            throw new DatabasePersistenceException("Erro ao atualizar categoria: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(stmt, conn);
        }
    }

    /**
     * Exclui uma categoria pelo seu ID.
     *
     * @param id Identificador da categoria.
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
            throw new DatabasePersistenceException("Erro ao excluir categoria: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(stmt, conn);
        }
    }

    private CategoriaAlimento mapearCategoria(ResultSet rs) throws SQLException {
        Long id = rs.getLong("ID_CATEGORIA");
        String nome = rs.getString("NM_CATEGORIA");
        return new CategoriaAlimento(id, nome);
    }
}
