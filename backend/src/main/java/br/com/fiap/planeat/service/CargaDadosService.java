package br.com.fiap.planeat.service;

import br.com.fiap.planeat.dao.CategoriaDAO;
import br.com.fiap.planeat.dao.IngredienteDAO;
import br.com.fiap.planeat.dao.PlanejamentoDAO;
import br.com.fiap.planeat.dao.ReceitaDAO;
import br.com.fiap.planeat.dao.UsuarioDAO;
import br.com.fiap.planeat.model.CategoriaAlimento;
import br.com.fiap.planeat.model.Ingrediente;
import br.com.fiap.planeat.model.ItemPlanejamento;
import br.com.fiap.planeat.model.ItemReceita;
import br.com.fiap.planeat.model.PlanejamentoSemanal;
import br.com.fiap.planeat.model.Receita;
import br.com.fiap.planeat.model.Usuario;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Serviço responsável pela carga inicial de dados (Seed) para homologação e execução do MVP.
 */
public class CargaDadosService {

    private static final Logger LOGGER = Logger.getLogger(CargaDadosService.class.getName());

    private final CategoriaDAO categoriaDAO;
    private final IngredienteDAO ingredienteDAO;
    private final UsuarioDAO usuarioDAO;
    private final ReceitaDAO receitaDAO;
    private final PlanejamentoDAO planejamentoDAO;
    private final ListaComprasService listaComprasService;

    public CargaDadosService() {
        this.categoriaDAO = new CategoriaDAO();
        this.ingredienteDAO = new IngredienteDAO();
        this.usuarioDAO = new UsuarioDAO();
        this.receitaDAO = new ReceitaDAO();
        this.planejamentoDAO = new PlanejamentoDAO();
        this.listaComprasService = new ListaComprasService();
    }

    public CargaDadosService(CategoriaDAO categoriaDAO, IngredienteDAO ingredienteDAO, UsuarioDAO usuarioDAO,
                             ReceitaDAO receitaDAO, PlanejamentoDAO planejamentoDAO, ListaComprasService listaComprasService) {
        this.categoriaDAO = categoriaDAO;
        this.ingredienteDAO = ingredienteDAO;
        this.usuarioDAO = usuarioDAO;
        this.receitaDAO = receitaDAO;
        this.planejamentoDAO = planejamentoDAO;
        this.listaComprasService = listaComprasService;
    }

    /**
     * Executa a carga inicial completa no banco se as tabelas estiverem vazias.
     */
    public void executarCargaInicial() {
        LOGGER.info("Iniciando verificação de carga de dados inicial (PlanEat)...");

        Map<String, CategoriaAlimento> categorias = popularCategoriasSeNecessario();
        Map<String, Ingrediente> ingredientes = popularIngredientesSeNecessario(categorias);
        Usuario usuario = popularUsuarioSeNecessario();
        List<Receita> receitas = popularReceitasSeNecessario(usuario, ingredientes);
        popularPlanejamentoSeNecessario(usuario, receitas);

        LOGGER.info("Carga de dados inicial verificada com sucesso.");
    }

    private Map<String, CategoriaAlimento> popularCategoriasSeNecessario() {
        Map<String, CategoriaAlimento> mapaCategorias = new HashMap<>();
        List<CategoriaAlimento> cadastradas = categoriaDAO.listarTodos();

        if (cadastradas.isEmpty()) {
            LOGGER.info("Tabela T_PE_CATEGORIA vazia. Inserindo categorias básicas...");
            String[] nomes = {
                    "Hortifrúti",
                    "Açougue & Peixaria",
                    "Mercearia & Grãos",
                    "Laticínios & Ovos",
                    "Temperos & Especiarias"
            };
            for (String nome : nomes) {
                CategoriaAlimento cat = new CategoriaAlimento(nome);
                categoriaDAO.inserir(cat);
                mapaCategorias.put(nome, cat);
            }
        } else {
            for (CategoriaAlimento cat : cadastradas) {
                mapaCategorias.put(cat.getNome(), cat);
            }
        }
        return mapaCategorias;
    }

    private Map<String, Ingrediente> popularIngredientesSeNecessario(Map<String, CategoriaAlimento> categorias) {
        Map<String, Ingrediente> mapaIngredientes = new HashMap<>();
        List<Ingrediente> cadastrados = ingredienteDAO.listarTodos();

        if (cadastrados.isEmpty()) {
            LOGGER.info("Tabela T_PE_INGREDIENTE vazia. Inserindo ingredientes do catálogo básico...");

            cadastrarIngrediente(mapaIngredientes, "Cebola Roxa", categorias.get("Hortifrúti"), "g");
            cadastrarIngrediente(mapaIngredientes, "Tomate Italiano", categorias.get("Hortifrúti"), "g");
            cadastrarIngrediente(mapaIngredientes, "Batata Inglesa", categorias.get("Hortifrúti"), "g");
            cadastrarIngrediente(mapaIngredientes, "Cenoura", categorias.get("Hortifrúti"), "g");

            cadastrarIngrediente(mapaIngredientes, "Peito de Frango", categorias.get("Açougue & Peixaria"), "g");

            cadastrarIngrediente(mapaIngredientes, "Arroz Integral", categorias.get("Mercearia & Grãos"), "g");
            cadastrarIngrediente(mapaIngredientes, "Azeite de Oliva Extra Virgem", categorias.get("Mercearia & Grãos"), "ml");
            cadastrarIngrediente(mapaIngredientes, "Feijão Carioca", categorias.get("Mercearia & Grãos"), "g");

            cadastrarIngrediente(mapaIngredientes, "Ovos Caipiras", categorias.get("Laticínios & Ovos"), "un");

            cadastrarIngrediente(mapaIngredientes, "Alho", categorias.get("Temperos & Especiarias"), "g");
            cadastrarIngrediente(mapaIngredientes, "Sal Refinado", categorias.get("Temperos & Especiarias"), "g");
        } else {
            for (Ingrediente ing : cadastrados) {
                mapaIngredientes.put(ing.getNome(), ing);
            }
        }
        return mapaIngredientes;
    }

    private void cadastrarIngrediente(Map<String, Ingrediente> mapa, String nome, CategoriaAlimento cat, String unidade) {
        if (cat != null) {
            Ingrediente ing = new Ingrediente(nome, cat, unidade);
            ingredienteDAO.inserir(ing);
            mapa.put(nome, ing);
        }
    }

    private Usuario popularUsuarioSeNecessario() {
        List<Usuario> usuarios = usuarioDAO.listarTodos();
        if (usuarios.isEmpty()) {
            LOGGER.info("Tabela T_PE_USUARIO vazia. Inserindo usuário demonstrativo...");
            Usuario usuario = new Usuario("Carlos Eduardo Mendes", "carlos.mendes@email.com", "senha123");
            usuarioDAO.inserir(usuario);
            return usuario;
        }
        return usuarios.get(0);
    }

    private List<Receita> popularReceitasSeNecessario(Usuario usuario, Map<String, Ingrediente> ingredientes) {
        List<Receita> receitas = receitaDAO.listarTodos();
        if (receitas.isEmpty() && usuario != null) {
            LOGGER.info("Tabela T_PE_RECEITA vazia. Inserindo receitas demonstrativas com N:N...");

            Receita r1 = new Receita(
                    "Frango Grelhado com Tomate e Cebola",
                    "Tempere o frango com azeite. Grelhe e refogue os tomates e cebolas fatiadas.",
                    2,
                    25,
                    "Almoço"
            );
            r1.setUsuarioId(usuario.getId());

            if (ingredientes.containsKey("Peito de Frango")) {
                r1.adicionarIngrediente(new ItemReceita(ingredientes.get("Peito de Frango"), 400.0, "g"));
            }
            if (ingredientes.containsKey("Cebola Roxa")) {
                r1.adicionarIngrediente(new ItemReceita(ingredientes.get("Cebola Roxa"), 150.0, "g"));
            }
            if (ingredientes.containsKey("Tomate Italiano")) {
                r1.adicionarIngrediente(new ItemReceita(ingredientes.get("Tomate Italiano"), 200.0, "g"));
            }
            if (ingredientes.containsKey("Azeite de Oliva Extra Virgem")) {
                r1.adicionarIngrediente(new ItemReceita(ingredientes.get("Azeite de Oliva Extra Virgem"), 20.0, "ml"));
            }

            if (!r1.getIngredientes().isEmpty()) {
                receitaDAO.inserir(r1);
            }

            Receita r2 = new Receita(
                    "Omelete Nutritiva de Tomate",
                    "Bata os ovos, pique o tomate e a cebola. Cozinhe em frigideira antiaderente.",
                    1,
                    15,
                    "Jantar"
            );
            r2.setUsuarioId(usuario.getId());

            if (ingredientes.containsKey("Ovos Caipiras")) {
                r2.adicionarIngrediente(new ItemReceita(ingredientes.get("Ovos Caipiras"), 3.0, "un"));
            }
            if (ingredientes.containsKey("Cebola Roxa")) {
                r2.adicionarIngrediente(new ItemReceita(ingredientes.get("Cebola Roxa"), 80.0, "g"));
            }
            if (ingredientes.containsKey("Tomate Italiano")) {
                r2.adicionarIngrediente(new ItemReceita(ingredientes.get("Tomate Italiano"), 120.0, "g"));
            }

            if (!r2.getIngredientes().isEmpty()) {
                receitaDAO.inserir(r2);
            }

            return receitaDAO.listarTodos();
        }
        return receitas;
    }

    private void popularPlanejamentoSeNecessario(Usuario usuario, List<Receita> receitas) {
        List<PlanejamentoSemanal> planos = planejamentoDAO.listarTodos();
        if (planos.isEmpty() && usuario != null && !receitas.isEmpty()) {
            LOGGER.info("Tabela T_PE_PLANEJAMENTO vazia. Inserindo planejamento demonstrativo...");

            LocalDate hoje = LocalDate.now();
            PlanejamentoSemanal plano = new PlanejamentoSemanal(
                    "Cardápio Semanal Consciente - Semana 1",
                    hoje,
                    hoje.plusDays(6)
            );
            plano.setUsuarioId(usuario.getId());

            Receita r1 = receitas.get(0);
            Receita r2 = receitas.size() > 1 ? receitas.get(1) : r1;

            plano.adicionarRefeicao(new ItemPlanejamento(r1, "Segunda-feira", "Almoço"));
            plano.adicionarRefeicao(new ItemPlanejamento(r2, "Segunda-feira", "Jantar"));
            plano.adicionarRefeicao(new ItemPlanejamento(r1, "Quarta-feira", "Almoço"));

            Long idPlano = planejamentoDAO.inserir(plano);
            plano.setId(idPlano);

            // Pré-gera a lista de compras consolidada
            listaComprasService.gerarListaCompras(idPlano);
        }
    }
}
