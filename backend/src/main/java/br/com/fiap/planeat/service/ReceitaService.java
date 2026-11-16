package br.com.fiap.planeat.service;

import br.com.fiap.planeat.dao.IngredienteDAO;
import br.com.fiap.planeat.dao.ReceitaDAO;
import br.com.fiap.planeat.dao.UsuarioDAO;
import br.com.fiap.planeat.exception.EntidadeNaoEncontradaException;
import br.com.fiap.planeat.exception.RegraNegocioException;
import br.com.fiap.planeat.model.Ingrediente;
import br.com.fiap.planeat.model.ItemReceita;
import br.com.fiap.planeat.model.Receita;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Camada de serviço e regras de negócio para o ciclo de vida de receitas culinárias.
 */
public class ReceitaService {

    private final ReceitaDAO receitaDAO;
    private final IngredienteDAO ingredienteDAO;
    private final UsuarioDAO usuarioDAO;

    public ReceitaService() {
        this.receitaDAO = new ReceitaDAO();
        this.ingredienteDAO = new IngredienteDAO();
        this.usuarioDAO = new UsuarioDAO();
    }

    public ReceitaService(ReceitaDAO receitaDAO) {
        this.receitaDAO = receitaDAO;
        this.ingredienteDAO = new IngredienteDAO();
        this.usuarioDAO = new UsuarioDAO();
    }

    public ReceitaService(ReceitaDAO receitaDAO, IngredienteDAO ingredienteDAO, UsuarioDAO usuarioDAO) {
        this.receitaDAO = receitaDAO;
        this.ingredienteDAO = ingredienteDAO;
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * Cadastra uma nova receita culinária aplicando as regras de validação de negócio:
     * - Campos obrigatórios preenchidos;
     * - Porções e tempo de preparo estritamente positivos;
     * - Ao menos um ingrediente com dose positiva;
     * - Ingredientes associados devem existir no catálogo;
     * - Consolidação preventiva de duplicidades de ingredientes na mesma dose.
     *
     * @param receita Objeto receita com lista de ingredientes.
     * @return ID gerado pelo banco relacional.
     */
    public Long cadastrarReceita(Receita receita) {
        if (receita == null) {
            throw new RegraNegocioException("Dados da receita não podem ser nulos.");
        }

        // Validação intrínseca do modelo
        receita.validarConsistencia();

        // Validação e normalização de ingredientes
        consolidarIngredientesDuplicados(receita);

        // Validação de existência do usuário
        if (receita.getUsuarioId() != null && receita.getUsuarioId() > 0) {
            usuarioDAO.buscarPorId(receita.getUsuarioId()).ifPresentOrElse(
                    receita::setUsuario,
                    () -> {
                        // Se o ID informado não existir, podemos vincular a um autor padrão ou registrar
                        if (receita.getUsuario() == null) {
                            receita.setUsuarioId(1L);
                        }
                    }
            );
        } else {
            receita.setUsuarioId(1L);
        }

        // Validação de integridade dos ingredientes no catálogo
        for (ItemReceita item : receita.getIngredientes()) {
            if (item.getIngrediente() == null || item.getIngrediente().getId() == null) {
                throw new RegraNegocioException("Ingrediente sem identificador não pode ser adicionado à receita.");
            }
            Long ingId = item.getIngrediente().getId();
            Ingrediente ingredienteCadastrado = ingredienteDAO.buscarPorId(ingId)
                    .orElseThrow(() -> new RegraNegocioException("Ingrediente com ID " + ingId + " não encontrado no catálogo."));
            item.setIngrediente(ingredienteCadastrado);
        }

        return receitaDAO.inserir(receita);
    }

    /**
     * Recupera uma receita pelo seu ID com validação de presença.
     *
     * @param id Identificador da receita.
     * @return Receita encontrada.
     * @throws EntidadeNaoEncontradaException se o registro não existir.
     */
    public Receita buscarPorId(Long id) {
        if (id == null || id <= 0) {
            throw new RegraNegocioException("ID da receita informado é inválido: " + id);
        }
        return receitaDAO.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(Receita.class, id));
    }

    /**
     * Lista todas as receitas cadastradas no sistema.
     *
     * @return Lista de receitas.
     */
    public List<Receita> listarReceitas() {
        return receitaDAO.listarTodos();
    }

    /**
     * Filtra receitas pelo tipo de refeição.
     *
     * @param tipoRefeicao Café da Manhã, Almoço, Jantar, Lanche.
     * @return Lista de receitas correspondentes.
     */
    public List<Receita> listarPorTipoRefeicao(String tipoRefeicao) {
        if (tipoRefeicao == null || tipoRefeicao.trim().isEmpty()) {
            return listarReceitas();
        }
        return receitaDAO.listarPorTipoRefeicao(tipoRefeicao.trim());
    }

    /**
     * Atualiza os dados de uma receita existente após validações de negócio.
     *
     * @param receita Receita contendo o ID e os novos dados.
     */
    public void atualizarReceita(Receita receita) {
        if (receita == null || receita.getId() == null || receita.getId() <= 0) {
            throw new RegraNegocioException("ID da receita é obrigatório para atualização.");
        }

        // Valida se a receita existe previamente
        buscarPorId(receita.getId());

        // Validação de negócio
        receita.validarConsistencia();
        consolidarIngredientesDuplicados(receita);

        receitaDAO.atualizar(receita);
    }

    /**
     * Remove uma receita existente do sistema.
     *
     * @param id Identificador da receita a remover.
     */
    public void excluirReceita(Long id) {
        if (id == null || id <= 0) {
            throw new RegraNegocioException("ID inválido para exclusão de receita.");
        }
        // Garante que a receita existe antes de tentar a exclusão
        buscarPorId(id);
        receitaDAO.excluir(id);
    }

    /**
     * Combina automaticamente ingredientes idênticos informados na mesma receita com a mesma unidade de medida.
     */
    private void consolidarIngredientesDuplicados(Receita receita) {
        if (receita.getIngredientes() == null || receita.getIngredientes().isEmpty()) {
            return;
        }

        Map<String, ItemReceita> mapa = new LinkedHashMap<>();
        for (ItemReceita item : receita.getIngredientes()) {
            if (item.getIngrediente() == null || item.getIngrediente().getId() == null) {
                continue;
            }
            String chave = item.getIngrediente().getId() + "_" + item.getUnidadeMedida().trim().toLowerCase();
            if (mapa.containsKey(chave)) {
                ItemReceita existente = mapa.get(chave);
                existente.setQuantidade(existente.getQuantidade() + item.getQuantidade());
            } else {
                mapa.put(chave, item);
            }
        }
        receita.setIngredientes(new ArrayList<>(mapa.values()));
    }
}
