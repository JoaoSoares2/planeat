package br.com.fiap.planeat.dao;

import br.com.fiap.planeat.exception.DatabasePersistenceException;
import br.com.fiap.planeat.factory.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

public class SessaoDAO {
    private static final String SQL_INSERT =
            "INSERT INTO T_PE_SESSAO (DS_TOKEN_HASH, ID_USUARIO, DT_EXPIRACAO) VALUES (?, ?, ?)";
    private static final String SQL_SELECT_ACTIVE =
            "SELECT ID_USUARIO FROM T_PE_SESSAO WHERE DS_TOKEN_HASH = ? AND DT_EXPIRACAO > CURRENT_TIMESTAMP";
    private static final String SQL_DELETE =
            "DELETE FROM T_PE_SESSAO WHERE DS_TOKEN_HASH = ?";
    private static final String SQL_DELETE_EXPIRED =
            "DELETE FROM T_PE_SESSAO WHERE DT_EXPIRACAO <= CURRENT_TIMESTAMP";

    public void inserir(String tokenHash, Long usuarioId, Instant expiracao) {
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {
            statement.setString(1, tokenHash);
            statement.setLong(2, usuarioId);
            statement.setTimestamp(3, Timestamp.from(expiracao));
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new DatabasePersistenceException("Não foi possível iniciar a sessão do usuário.", ex);
        }
    }

    public Optional<Long> buscarUsuarioIdAtivo(String tokenHash) {
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ACTIVE)) {
            statement.setString(1, tokenHash);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(result.getLong("ID_USUARIO")) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DatabasePersistenceException("Não foi possível validar a sessão.", ex);
        }
    }

    public void excluir(String tokenHash) {
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_DELETE)) {
            statement.setString(1, tokenHash);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new DatabasePersistenceException("Não foi possível encerrar a sessão.", ex);
        }
    }

    public void excluirExpiradas() {
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_DELETE_EXPIRED)) {
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new DatabasePersistenceException("Não foi possível remover sessões expiradas.", ex);
        }
    }
}
