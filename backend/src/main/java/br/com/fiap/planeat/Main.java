package br.com.fiap.planeat;

import br.com.fiap.planeat.controller.IngredienteController;
import br.com.fiap.planeat.controller.ListaComprasController;
import br.com.fiap.planeat.controller.AutenticacaoController;
import br.com.fiap.planeat.controller.HttpHelper;
import br.com.fiap.planeat.controller.PlanejamentoController;
import br.com.fiap.planeat.controller.ReceitaController;
import br.com.fiap.planeat.factory.ConnectionFactory;
import br.com.fiap.planeat.security.AuthenticatedHandler;
import br.com.fiap.planeat.security.SessionManager;
import br.com.fiap.planeat.service.CargaDadosService;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ponto de entrada da aplicação Java PlanEat.
 * Inicializa a persistência JDBC nativa, executa a carga inicial e disponibiliza a API REST HTTP.
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final int PORTA_PADRAO = 8080;
    private static HttpServer servidor;

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("🌿 PlanEat - Back-end Java (Global Solution FIAP 2026/2)");
        System.out.println("CitizenTech: Planejamento Alimentar e Combate ao Desperdício");
        System.out.println("==================================================================");

        // 1. Verificação de Conexão e Carga Inicial
        verificarBancoECargaInicial();

        // 2. Inicialização do Servidor HTTP REST Nativo
        int porta = obterPortaConfigurada();
        try {
            iniciarServidor(porta, SessionManager.fromEnvironment());
            System.out.println("==================================================================");
            System.out.println("🚀 Servidor HTTP REST ativo em: http://localhost:" + porta);
            System.out.println("------------------------------------------------------------------");
            System.out.println("Rotas Disponíveis para Integração Web:");
            System.out.println("  • POST   /api/auth/cadastro");
            System.out.println("  • POST   /api/auth/login");
            System.out.println("  • GET    /api/auth/sessao");
            System.out.println("  • POST   /api/auth/logout");
            System.out.println("  • GET    /api/receitas");
            System.out.println("  • POST   /api/receitas");
            System.out.println("  • GET    /api/receitas/{id}");
            System.out.println("  • PUT    /api/receitas/{id}");
            System.out.println("  • DELETE /api/receitas/{id}");
            System.out.println("  • GET    /api/ingredientes");
            System.out.println("  • GET    /api/categorias");
            System.out.println("  • GET    /api/planejamentos");
            System.out.println("  • POST   /api/planejamentos");
            System.out.println("  • PUT    /api/planejamentos/{id}");
            System.out.println("  • GET    /api/planejamentos/{id}/lista-compras (Query Analítica SQL)");
            System.out.println("  • GET    /api/planejamentos/{id}/desperdicio   (Relatório de Desperdício)");
            System.out.println("  • PATCH  /api/itens-lista/{id}/status          (Checklist Mobile)");
            System.out.println("==================================================================");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao inicializar o servidor HTTP na porta " + porta, e);
        }
    }

    public static synchronized void iniciarServidor(int porta) throws IOException {
        iniciarServidor(porta, SessionManager.fromEnvironment());
    }

    public static synchronized void iniciarServidor(int porta, SessionManager sessions) throws IOException {
        if (servidor != null) {
            return;
        }

        servidor = HttpServer.create(new InetSocketAddress(porta), 0);

        servidor.createContext("/api/status", exchange -> {
            if (HttpHelper.aplicarCorsEVerificarOptions(exchange)) return;
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            String json = "{\"sistema\": \"PlanEat API\", \"status\": \"OPERACIONAL\", \"versao\": \"1.0.0\"}";
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });

        servidor.createContext("/api/auth", new AutenticacaoController(sessions));
        servidor.createContext("/api/receitas", new AuthenticatedHandler(new ReceitaController(), sessions));
        servidor.createContext("/api/ingredientes", new AuthenticatedHandler(new IngredienteController(), sessions));
        servidor.createContext("/api/categorias", new AuthenticatedHandler(new IngredienteController(), sessions));
        servidor.createContext("/api/planejamentos", new AuthenticatedHandler(new PlanejamentoController(), sessions));
        servidor.createContext("/api/itens-lista", new AuthenticatedHandler(new ListaComprasController(), sessions));

        // Executor multi-thread para requisições concorrentes
        servidor.setExecutor(Executors.newFixedThreadPool(10));
        servidor.start();
    }

    public static synchronized void pararServidor() {
        if (servidor != null) {
            servidor.stop(1);
            servidor = null;
        }
    }

    private static void verificarBancoECargaInicial() {
        try {
            System.out.println("Verificando conexão com o banco relacional...");
            try (Connection conn = ConnectionFactory.getConnection()) {
                System.out.println("✅ Conexão JDBC estabelecida com sucesso!");
                System.out.println("Executando verificação de carga inicial de dados...");
                CargaDadosService seeder = new CargaDadosService();
                seeder.executarCargaInicial();
                System.out.println("✅ Base de dados homologada e pronta.");
            }
        } catch (Exception e) {
            System.out.println("ℹ️ Banco de dados relacional em rede não disponível (" + e.getMessage() + ").");
            System.out.println("   Dica: Configure DB_URL, DB_USER e DB_PASS para conexão ao banco da FIAP ou local.");
            System.out.println("   O servidor HTTP REST inicializará normalmente para testes de rotas e integração.");
        }
    }

    private static int obterPortaConfigurada() {
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isBlank()) {
            try {
                return Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return PORTA_PADRAO;
    }
}
