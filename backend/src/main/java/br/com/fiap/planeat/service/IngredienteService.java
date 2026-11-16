package br.com.fiap.planeat.service;

import br.com.fiap.planeat.dao.CategoriaDAO;
import br.com.fiap.planeat.dao.IngredienteDAO;
import br.com.fiap.planeat.exception.EntidadeNaoEncontradaException;
import br.com.fiap.planeat.exception.RegraNegocioException;
import br.com.fiap.planeat.model.CategoriaAlimento;
import br.com.fiap.planeat.model.Ingrediente;

import java.util.List;

/**
 * Camada de serviço e regras de negócio para o catálogo de ingredientes.
 */
public class IngredienteService {

    private final IngredienteDAO ingredienteDAO;
    private final CategoriaDAO categoriaDAO;

    public IngredienteService() {
        this.ingredienteDAO = new IngredienteDAO();
        this.categoriaDAO = new CategoriaDAO();
    }

    public IngredienteService(IngredienteDAO ingredienteDAO, CategoriaDAO categoriaDAO) {
        this.ingredienteDAO = ingredienteDAO;
        this.categoriaDAO = categoriaDAO;
    }

    public Long cadastrar(Ingrediente ingrediente) {
        if (ingrediente == null) {
            throw new RegraNegocioException("Dados do ingrediente não podem ser nulos.");
        }
        ingrediente.validarConsistencia();

        if (ingredienteDAO.buscarPorNome(ingrediente.getNome()).isPresent()) {
            throw new RegraNegocioException("Já existe um ingrediente cadastrado com o nome: " + ingrediente.getNome());
        }

        Long catId = ingrediente.getCategoria().getId();
        CategoriaAlimento categoria = categoriaDAO.buscarPorId(catId)
                .orElseThrow(() -> new RegraNegocioException("Categoria associada com ID " + catId + " não encontrada."));
        ingrediente.setCategoria(categoria);

        return ingredienteDAO.inserir(ingrediente);
    }

    public Ingrediente buscarPorId(Long id) {
        if (id == null || id <= 0) {
            throw new RegraNegocioException("ID do ingrediente inválido: " + id);
        }
        return ingredienteDAO.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(Ingrediente.class, id));
    }

    public List<Ingrediente> listarTodos() {
        return ingredienteDAO.listarTodos();
    }

    public List<Ingrediente> listarPorCategoria(Long categoriaId) {
        if (categoriaId == null || categoriaId <= 0) {
            throw new RegraNegocioException("ID da categoria inválido: " + categoriaId);
        }
        return ingredienteDAO.listarPorCategoria(categoriaId);
    }

    public void atualizar(Ingrediente ingrediente) {
        if (ingrediente == null || ingrediente.getId() == null) {
            throw new RegraNegocioException("ID do ingrediente é obrigatório para atualização.");
        }
        buscarPorId(ingrediente.getId());
        ingrediente.validarConsistencia();
        ingredienteDAO.atualizar(ingrediente);
    }

    public void excluir(Long id) {
        buscarPorId(id);
        ingredienteDAO.excluir(id);
    }
}
