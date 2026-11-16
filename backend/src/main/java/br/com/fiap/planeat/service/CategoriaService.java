package br.com.fiap.planeat.service;

import br.com.fiap.planeat.dao.CategoriaDAO;
import br.com.fiap.planeat.exception.EntidadeNaoEncontradaException;
import br.com.fiap.planeat.exception.RegraNegocioException;
import br.com.fiap.planeat.model.CategoriaAlimento;

import java.util.List;

/**
 * Camada de serviço e regras de negócio para categorias de alimentos.
 */
public class CategoriaService {

    private final CategoriaDAO categoriaDAO;

    public CategoriaService() {
        this.categoriaDAO = new CategoriaDAO();
    }

    public CategoriaService(CategoriaDAO categoriaDAO) {
        this.categoriaDAO = categoriaDAO;
    }

    public Long cadastrar(CategoriaAlimento categoria) {
        if (categoria == null) {
            throw new RegraNegocioException("Dados da categoria não podem ser nulos.");
        }
        categoria.validarConsistencia();

        if (categoriaDAO.buscarPorNome(categoria.getNome()).isPresent()) {
            throw new RegraNegocioException("Já existe uma categoria cadastrada com o nome: " + categoria.getNome());
        }

        return categoriaDAO.inserir(categoria);
    }

    public CategoriaAlimento buscarPorId(Long id) {
        if (id == null || id <= 0) {
            throw new RegraNegocioException("ID da categoria inválido: " + id);
        }
        return categoriaDAO.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(CategoriaAlimento.class, id));
    }

    public List<CategoriaAlimento> listarTodas() {
        return categoriaDAO.listarTodos();
    }

    public void atualizar(CategoriaAlimento categoria) {
        if (categoria == null || categoria.getId() == null) {
            throw new RegraNegocioException("ID da categoria é obrigatório para atualização.");
        }
        buscarPorId(categoria.getId());
        categoria.validarConsistencia();
        categoriaDAO.atualizar(categoria);
    }

    public void excluir(Long id) {
        buscarPorId(id);
        categoriaDAO.excluir(id);
    }
}
