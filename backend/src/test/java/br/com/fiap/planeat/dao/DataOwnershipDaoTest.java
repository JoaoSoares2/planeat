package br.com.fiap.planeat.dao;

import br.com.fiap.planeat.exception.DatabasePersistenceException;
import br.com.fiap.planeat.factory.ConnectionFactory;
import br.com.fiap.planeat.model.PlanejamentoSemanal;
import br.com.fiap.planeat.model.Receita;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataOwnershipDaoTest {
    @AfterEach
    void resetConnection() {
        ConnectionFactory.resetCredentials();
    }

    @Test
    void recipeReadsAndWritesAreScopedToTheirOwner() throws Exception {
        initializeDatabase();
        try (Connection connection = ConnectionFactory.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO T_PE_USUARIO VALUES (10, 'Ana', 'ana@example.com')");
            statement.execute("INSERT INTO T_PE_USUARIO VALUES (20, 'Bia', 'bia@example.com')");
            statement.execute("INSERT INTO T_PE_RECEITA (ID_RECEITA, ID_USUARIO, NM_RECEITA, DS_MODO_PREPARO, NR_PORCOES, NR_TEMPO_MINUTOS, DS_TIPO_REFEICAO, DT_CRIACAO) " +
                    "VALUES (101, 10, 'Receita Ana', 'Preparo', 2, 20, 'Almoço', CURRENT_DATE)");
            statement.execute("INSERT INTO T_PE_RECEITA (ID_RECEITA, ID_USUARIO, NM_RECEITA, DS_MODO_PREPARO, NR_PORCOES, NR_TEMPO_MINUTOS, DS_TIPO_REFEICAO, DT_CRIACAO) " +
                    "VALUES (202, 20, 'Receita Bia', 'Preparo', 2, 20, 'Jantar', CURRENT_DATE)");
        }

        ReceitaDAO dao = new ReceitaDAO();
        assertEquals(List.of(101L), dao.listarPorUsuario(10L).stream().map(Receita::getId).toList());
        assertEquals(List.of(202L), dao.listarPorUsuario(20L).stream().map(Receita::getId).toList());
        assertTrue(dao.buscarPorId(101L, 20L).isEmpty());

        Receita forgedUpdate = new Receita(101L, 20L, "Alterada", "Preparo", 2, 20, "Almoço");
        assertThrows(DatabasePersistenceException.class, () -> dao.atualizar(forgedUpdate, 20L));
        assertThrows(DatabasePersistenceException.class, () -> dao.excluir(101L, 20L));
        assertEquals("Receita Ana", dao.buscarPorId(101L, 10L).orElseThrow().getNome());
    }

    @Test
    void planningReadsAndDeletesAreScopedToTheirOwner() throws Exception {
        initializeDatabase();
        try (Connection connection = ConnectionFactory.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO T_PE_USUARIO VALUES (10, 'Ana', 'ana@example.com')");
            statement.execute("INSERT INTO T_PE_USUARIO VALUES (20, 'Bia', 'bia@example.com')");
        }
        PlanejamentoDAO dao = new PlanejamentoDAO();
        PlanejamentoSemanal ana = new PlanejamentoSemanal("Plano Ana", LocalDate.now(), LocalDate.now().plusDays(6));
        ana.setUsuarioId(10L);
        PlanejamentoSemanal bia = new PlanejamentoSemanal("Plano Bia", LocalDate.now(), LocalDate.now().plusDays(6));
        bia.setUsuarioId(20L);
        Long anaId = dao.inserir(ana);
        Long biaId = dao.inserir(bia);

        assertEquals(List.of(anaId), dao.listarPorUsuario(10L).stream().map(PlanejamentoSemanal::getId).toList());
        assertEquals(List.of(biaId), dao.listarPorUsuario(20L).stream().map(PlanejamentoSemanal::getId).toList());
        assertTrue(dao.buscarPorId(anaId, 20L).isEmpty());
        assertThrows(DatabasePersistenceException.class, () -> dao.excluir(anaId, 20L));
        assertFalse(dao.buscarPorId(anaId, 10L).isEmpty());
    }

    @Test
    void shoppingItemStatusCanOnlyBeChangedByItsOwner() throws Exception {
        initializeDatabase();
        try (Connection connection = ConnectionFactory.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO T_PE_USUARIO VALUES (10, 'Ana', 'ana@example.com')");
            statement.execute("INSERT INTO T_PE_USUARIO VALUES (20, 'Bia', 'bia@example.com')");
            statement.execute("INSERT INTO T_PE_CATEGORIA VALUES (1, 'Hortifrúti')");
            statement.execute("INSERT INTO T_PE_INGREDIENTE VALUES (1, 'Tomate', 'g', 1)");
            statement.execute("INSERT INTO T_PE_PLANEJAMENTO VALUES (301, 10, 'Plano Ana', CURRENT_DATE, CURRENT_DATE, 'ATIVO')");
            statement.execute("INSERT INTO T_PE_PLANEJAMENTO VALUES (302, 20, 'Plano Bia', CURRENT_DATE, CURRENT_DATE, 'ATIVO')");
            statement.execute("INSERT INTO T_PE_LISTA_COMPRAS VALUES (401, 301, CURRENT_DATE, 'PENDENTE')");
            statement.execute("INSERT INTO T_PE_LISTA_COMPRAS VALUES (402, 302, CURRENT_DATE, 'PENDENTE')");
            statement.execute("INSERT INTO T_PE_ITEM_LISTA VALUES (501, 401, 1, 200, 'g', 'N')");
            statement.execute("INSERT INTO T_PE_ITEM_LISTA VALUES (502, 402, 1, 100, 'g', 'N')");
        }

        ListaComprasDAO dao = new ListaComprasDAO();
        assertTrue(dao.buscarItemPorId(501L, 20L).isEmpty());
        assertThrows(DatabasePersistenceException.class, () -> dao.atualizarStatusComprado(501L, true, 20L));
        dao.atualizarStatusComprado(501L, true, 10L);

        assertTrue(dao.buscarItemPorId(501L, 10L).orElseThrow().getComprado());
        assertFalse(dao.buscarItemPorId(502L, 20L).orElseThrow().getComprado());
    }

    private void initializeDatabase() throws Exception {
        String databaseName = "planeat_ownership_" + System.nanoTime();
        ConnectionFactory.setCredentials("jdbc:h2:mem:" + databaseName + ";MODE=Oracle;DB_CLOSE_DELAY=-1", "sa", "");
        try (Connection connection = ConnectionFactory.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE T_PE_USUARIO (ID_USUARIO BIGINT PRIMARY KEY, NM_USUARIO VARCHAR(100), DS_EMAIL VARCHAR(100))");
            statement.execute("CREATE TABLE T_PE_CATEGORIA (ID_CATEGORIA BIGINT PRIMARY KEY, NM_CATEGORIA VARCHAR(50))");
            statement.execute("CREATE TABLE T_PE_INGREDIENTE (ID_INGREDIENTE BIGINT PRIMARY KEY, NM_INGREDIENTE VARCHAR(100), DS_UNIDADE_PADRAO VARCHAR(20), ID_CATEGORIA BIGINT)");
            statement.execute("CREATE TABLE T_PE_RECEITA (ID_RECEITA BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, ID_USUARIO BIGINT NOT NULL, NM_RECEITA VARCHAR(120), DS_MODO_PREPARO VARCHAR(1000), NR_PORCOES INT, NR_TEMPO_MINUTOS INT, DS_TIPO_REFEICAO VARCHAR(30), DT_CRIACAO DATE, FOREIGN KEY (ID_USUARIO) REFERENCES T_PE_USUARIO(ID_USUARIO))");
            statement.execute("CREATE TABLE T_PE_RECEITA_INGREDIENTE (ID_RECEITA BIGINT, ID_INGREDIENTE BIGINT, VL_QUANTIDADE DECIMAL(10,2), DS_UNIDADE_MEDIDA VARCHAR(20))");
            statement.execute("CREATE TABLE T_PE_PLANEJAMENTO (ID_PLANEJAMENTO BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, ID_USUARIO BIGINT NOT NULL, NM_TITULO VARCHAR(100), DT_INICIO DATE, DT_FIM DATE, DS_STATUS VARCHAR(20), FOREIGN KEY (ID_USUARIO) REFERENCES T_PE_USUARIO(ID_USUARIO))");
            statement.execute("CREATE TABLE T_PE_ITEM_PLANEJAMENTO (ID_ITEM_PLANEJAMENTO BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, ID_PLANEJAMENTO BIGINT, ID_RECEITA BIGINT, DS_DIA_SEMANA VARCHAR(20), DS_REFEICAO VARCHAR(30))");
            statement.execute("CREATE TABLE T_PE_LISTA_COMPRAS (ID_LISTA BIGINT PRIMARY KEY, ID_PLANEJAMENTO BIGINT, DT_GERACAO DATE, DS_STATUS VARCHAR(20))");
            statement.execute("CREATE TABLE T_PE_ITEM_LISTA (ID_ITEM_LISTA BIGINT PRIMARY KEY, ID_LISTA BIGINT, ID_INGREDIENTE BIGINT, VL_QUANTIDADE_TOTAL DECIMAL(10,2), DS_UNIDADE_MEDIDA VARCHAR(20), FL_COMPRADO CHAR(1))");
            statement.execute("CREATE TABLE T_PE_SESSAO (DS_TOKEN_HASH VARCHAR(64) PRIMARY KEY, ID_USUARIO BIGINT NOT NULL, DT_EXPIRACAO TIMESTAMP NOT NULL)");
        }
    }
}
