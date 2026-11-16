package br.com.fiap.planeat.dao;

import br.com.fiap.planeat.exception.DatabasePersistenceException;
import br.com.fiap.planeat.factory.ConnectionFactory;
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
 * Data Access Object para a entidade Usuario (tabela T_PE_USUARIO) via JDBC nativo.
 */
public class UsuarioDAO {

    private static final String SQL_INSERT =
            "INSERT INTO T_PE_USUARIO (NM_USUARIO, DS_EMAIL, DS_SENHA, DT_CADASTRO) VALUES (?, ?, ?, ?)";
    private static final String SQL_SELECT_BY_ID =
            "SELECT ID_USUARIO, NM_USUARIO, DS_EMAIL, DS_SENHA, DT_CADASTRO FROM T_PE_USUARIO WHERE ID_USUARIO = ?";
    private static final String SQL_SELECT_BY_EMAIL =
            "SELECT ID_USUARIO, NM_USUARIO, DS_EMAIL, DS_SENHA, DT_CADASTRO FROM T_PE_USUARIO WHERE LOWER(DS_EMAIL) = LOWER(?)";
    private static final String SQL_SELECT_ALL =
            "SELECT ID_USUARIO, NM_USUARIO, DS_EMAIL, DS_SENHA, DT_CADASTRO FROM T_PE_USUARIO ORDER BY NM_USUARIO ASC";
    private static final String SQL_UPDATE =
            "UPDATE T_PE_USUARIO SET NM_USUARIO = ?, DS_EMAIL = ?, DS_SENHA = ? WHERE ID_USUARIO = ?";
    private static final String SQL_DELETE =
            "DELETE FROM T_PE_USUARIO WHERE ID_USUARIO = ?";

    /**
     * Insere um novo usuário na tabela T_PE_USUARIO e recupera o ID gerado.
     *
     * @param usuario Instância do usuário a ser persistido.
     * @return ID gerado pelo banco.
     */
    public Long inserir(Usuario usuario) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            try {
                stmt = conn.prepareStatement(SQL_INSERT, new String[]{"ID_USUARIO"});
            } catch (Exception ex) {
                stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            }

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getSenha());
            LocalDate dt = usuario.getDataCriacao() != null ? usuario.getDataCriacao() : LocalDate.now();
            stmt.setDate(4, Date.valueOf(dt));

            stmt.executeUpdate();

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                Long idGerado = rs.getLong(1);
                usuario.setId(idGerado);
                return idGerado;
            } else {
                throw new DatabasePersistenceException("Falha ao recuperar a chave primária gerada para o usuário.");
            }
        } catch (SQLException e) {
            throw new DatabasePersistenceException("Erro ao inserir usuário no banco de dados: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(rs, stmt, conn);
        }
    }

    /**
     * Localiza um usuário pelo seu ID único.
     *
     * @param id Identificador do usuário.
     * @return Optional contendo o usuário, se encontrado.
     */
    public Optional<Usuario> buscarPorId(Long id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_BY_ID);
            stmt.setLong(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapearUsuario(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabasePersistenceException("Erro ao buscar usuário por ID: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(rs, stmt, conn);
        }
    }

    /**
     * Localiza um usuário pelo seu e-mail cadastrado (case-insensitive).
     *
     * @param email E-mail para consulta.
     * @return Optional com o usuário encontrado.
     */
    public Optional<Usuario> buscarPorEmail(String email) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_BY_EMAIL);
            stmt.setString(1, email);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapearUsuario(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabasePersistenceException("Erro ao buscar usuário por e-mail: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(rs, stmt, conn);
        }
    }

    /**
     * Retorna todos os usuários cadastrados na base de dados.
     *
     * @return Lista com todos os usuários ordenados por nome.
     */
    public List<Usuario> listarTodos() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Usuario> usuarios = new ArrayList<>();

        try {
            conn = ConnectionFactory.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_ALL);
            rs = stmt.executeQuery();

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
            return usuarios;
        } catch (SQLException e) {
            throw new DatabasePersistenceException("Erro ao listar usuários: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(rs, stmt, conn);
        }
    }

    /**
     * Atualiza os dados de nome, e-mail e senha de um usuário existente.
     *
     * @param usuario Usuário contendo o ID e os novos dados.
     */
    public void atualizar(Usuario usuario) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            stmt = conn.prepareStatement(SQL_UPDATE);
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getSenha());
            stmt.setLong(4, usuario.getId());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new DatabasePersistenceException("Nenhum usuário localizado com o ID " + usuario.getId() + " para atualização.");
            }
        } catch (SQLException e) {
            throw new DatabasePersistenceException("Erro ao atualizar dados do usuário: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(stmt, conn);
        }
    }

    /**
     * Remove um usuário pelo seu ID único.
     *
     * @param id Identificador do usuário a excluir.
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
            throw new DatabasePersistenceException("Erro ao excluir usuário: " + e.getMessage(), e);
        } finally {
            ConnectionFactory.fecharRecursos(stmt, conn);
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Long id = rs.getLong("ID_USUARIO");
        String nome = rs.getString("NM_USUARIO");
        String email = rs.getString("DS_EMAIL");
        String senha = rs.getString("DS_SENHA");
        Date dt = rs.getDate("DT_CADASTRO");
        LocalDate dataCriacao = dt != null ? dt.toLocalDate() : LocalDate.now();

        return new Usuario(id, dataCriacao, nome, email, senha);
    }
}
