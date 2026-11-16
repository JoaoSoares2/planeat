package br.com.fiap.planeat.service;

import br.com.fiap.planeat.dao.CategoriaDAO;
import br.com.fiap.planeat.dao.IngredienteDAO;
import br.com.fiap.planeat.model.CategoriaAlimento;
import br.com.fiap.planeat.model.Ingrediente;

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

    public CargaDadosService() {
        this.categoriaDAO = new CategoriaDAO();
        this.ingredienteDAO = new IngredienteDAO();
    }

    /**
     * Executa a carga inicial completa no banco se as tabelas estiverem vazias.
     */
    public void executarCargaInicial() {
        LOGGER.info("Iniciando verificação de carga de dados inicial (PlanEat)...");

        Map<String, CategoriaAlimento> categorias = popularCategoriasSeNecessario();
        popularIngredientesSeNecessario(categorias);

        LOGGER.info("Catálogo global de ingredientes verificado; dados pessoais serão criados por cada usuário.");
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

}
