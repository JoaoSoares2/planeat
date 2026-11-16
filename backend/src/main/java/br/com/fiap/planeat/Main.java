package br.com.fiap.planeat;

import br.com.fiap.planeat.factory.ConnectionFactory;
import br.com.fiap.planeat.service.CargaDadosService;

import java.sql.Connection;

/**
 * Ponto de entrada da aplicação Java PlanEat.
 * Inicializa os módulos de persistência JDBC nativa e serviços de domínio da Sprint 2.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("🌿 PlanEat - Back-end Java (CitizenTech FIAP 2026/2)");
        System.out.println("Status: Sprint 2 - CRUDs e Persistência JDBC Concluída");
        System.out.println("Configuração JDBC:");
        System.out.println("  URL: " + ConnectionFactory.getUrl());
        System.out.println("  Usuário: " + ConnectionFactory.getUser());
        System.out.println("=================================================");

        try {
            System.out.println("Testando conectividade com o banco de dados...");
            try (Connection conn = ConnectionFactory.getConnection()) {
                System.out.println("✅ Conexão JDBC estabelecida com sucesso!");
                System.out.println("Executando verificação de carga de dados inicial...");
                CargaDadosService seeder = new CargaDadosService();
                seeder.executarCargaInicial();
                System.out.println("✅ Carga inicial finalizada.");
            }
        } catch (Exception e) {
            System.out.println("ℹ️ Banco de dados relacional não disponível no momento (" + e.getMessage() + ").");
            System.out.println("   Para conectar a um banco real, configure as variáveis DB_URL, DB_USER e DB_PASS.");
            System.out.println("   As classes de modelo, DAOs JDBC (ReceitaDAO, IngredienteDAO, CategoriaDAO, UsuarioDAO)");
            System.out.println("   e os serviços de negócio (ReceitaService, etc.) estão totalmente compilados e prontos.");
        }

        System.out.println("=================================================");
        System.out.println("🌿 PlanEat - Pronto para operações da Sprint 2.");
        System.out.println("=================================================");
    }
}
