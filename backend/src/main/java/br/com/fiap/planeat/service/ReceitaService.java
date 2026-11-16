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
    public Long cadastrarReceita(Receita receita, Long usuarioId) {
        if (receita == null) {
            throw new RegraNegocioException("Dados da receita não podem ser nulos.");
        }
        if (usuarioId == null || usuarioId <= 0) {
            throw new RegraNegocioException("Usuário autenticado inválido.");
        }

        receita.setUsuarioId(usuarioId);
        receita.validarConsistencia();
        consolidarIngredientesDuplicados(receita);
        usuarioDAO.buscarPorId(usuarioId)
                .ifPresentOrElse(receita::setUsuario, () -> {
                    throw new RegraNegocioException("Usuário autenticado não encontrado.");
                });

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
    public Receita buscarPorId(Long id, Long usuarioId) {
        if (id == null || id <= 0) {
            throw new RegraNegocioException("ID da receita informado é inválido: " + id);
        }
        return receitaDAO.buscarPorId(id, usuarioId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(Receita.class, id));
    }

    public List<Receita> listarReceitas(Long usuarioId) {
        return receitaDAO.listarPorUsuario(usuarioId);
    }

    /**
     * Filtra receitas pelo tipo de refeição.
     *
     * @param tipoRefeicao Café da Manhã, Almoço, Jantar, Lanche.
     * @return Lista de receitas correspondentes.
     */
    public List<Receita> listarPorTipoRefeicao(Long usuarioId, String tipoRefeicao) {
        if (tipoRefeicao == null || tipoRefeicao.trim().isEmpty()) {
            return listarReceitas(usuarioId);
        }
        return receitaDAO.listarPorTipoRefeicao(usuarioId, tipoRefeicao.trim());
    }

    /**
     * Atualiza os dados de uma receita existente após validações de negócio.
     *
     * @param receita Receita contendo o ID e os novos dados.
     */
    public void atualizarReceita(Receita receita, Long usuarioId) {
        if (receita == null || receita.getId() == null || receita.getId() <= 0) {
            throw new RegraNegocioException("ID da receita é obrigatório para atualização.");
        }

        // Valida se a receita existe previamente
        buscarPorId(receita.getId(), usuarioId);

        receita.setUsuarioId(usuarioId);
        receita.validarConsistencia();
        consolidarIngredientesDuplicados(receita);

        receitaDAO.atualizar(receita, usuarioId);
    }

    /**
     * Remove uma receita existente do sistema.
     *
     * @param id Identificador da receita a remover.
     */
    public void excluirReceita(Long id, Long usuarioId) {
        if (id == null || id <= 0) {
            throw new RegraNegocioException("ID inválido para exclusão de receita.");
        }
        // Garante que a receita existe antes de tentar a exclusão
        buscarPorId(id, usuarioId);
        receitaDAO.excluir(id, usuarioId);
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
