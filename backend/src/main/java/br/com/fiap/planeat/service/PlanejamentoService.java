package br.com.fiap.planeat.service;

import br.com.fiap.planeat.dao.PlanejamentoDAO;
import br.com.fiap.planeat.dao.ReceitaDAO;
import br.com.fiap.planeat.dao.UsuarioDAO;
import br.com.fiap.planeat.exception.EntidadeNaoEncontradaException;
import br.com.fiap.planeat.exception.RegraNegocioException;
import br.com.fiap.planeat.model.ItemPlanejamento;
import br.com.fiap.planeat.model.PlanejamentoSemanal;
import br.com.fiap.planeat.model.Receita;

import java.util.List;

/**
 * Camada de serviço e regras de negócio para a gestão de planejamentos semanais e cardápios conscientes.
 */
public class PlanejamentoService {

    private final PlanejamentoDAO planejamentoDAO;
    private final ReceitaDAO receitaDAO;
    private final UsuarioDAO usuarioDAO;

    public PlanejamentoService() {
        this.planejamentoDAO = new PlanejamentoDAO();
        this.receitaDAO = new ReceitaDAO();
        this.usuarioDAO = new UsuarioDAO();
    }

    public PlanejamentoService(PlanejamentoDAO planejamentoDAO, ReceitaDAO receitaDAO, UsuarioDAO usuarioDAO) {
        this.planejamentoDAO = planejamentoDAO;
        this.receitaDAO = receitaDAO;
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * Cadastra um novo planejamento semanal com validações de período e integridade referencial de receitas.
     *
     * @param plano Instância do planejamento semanal.
     * @return ID gerado pelo banco.
     */
    public Long cadastrar(PlanejamentoSemanal plano) {
        if (plano == null) {
            throw new RegraNegocioException("Dados do planejamento não podem ser nulos.");
        }

        plano.validarConsistencia();

        if (plano.getUsuarioId() != null && plano.getUsuarioId() > 0) {
            usuarioDAO.buscarPorId(plano.getUsuarioId()).ifPresentOrElse(
                    plano::setUsuario,
                    () -> plano.setUsuarioId(1L)
            );
        } else {
            plano.setUsuarioId(1L);
        }

        if (plano.getRefeicoes() != null) {
            for (ItemPlanejamento item : plano.getRefeicoes()) {
                if (item.getReceita() == null || item.getReceita().getId() == null) {
                    throw new RegraNegocioException("Cada refeição alocada deve referenciar uma receita com ID válido.");
                }
                Long receitaId = item.getReceita().getId();
                Receita receitaCadastrada = receitaDAO.buscarPorId(receitaId)
                        .orElseThrow(() -> new RegraNegocioException("Receita com ID " + receitaId + " não encontrada para alocação."));
                item.setReceita(receitaCadastrada);
            }
        }

        return planejamentoDAO.inserir(plano);
    }

    /**
     * Recupera um planejamento semanal pelo ID.
     */
    public PlanejamentoSemanal buscarPorId(Long id) {
        if (id == null || id <= 0) {
            throw new RegraNegocioException("ID de planejamento inválido: " + id);
        }
        return planejamentoDAO.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(PlanejamentoSemanal.class, id));
    }

    /**
     * Retorna todos os planejamentos semanais cadastrados.
     */
    public List<PlanejamentoSemanal> listarPlanejamentos() {
        return planejamentoDAO.listarTodos();
    }

    /**
     * Atualiza um planejamento semanal existente.
     */
    public void atualizar(PlanejamentoSemanal plano) {
        if (plano == null || plano.getId() == null || plano.getId() <= 0) {
            throw new RegraNegocioException("ID do planejamento é obrigatório para atualização.");
        }
        buscarPorId(plano.getId());
        plano.validarConsistencia();
        planejamentoDAO.atualizar(plano);
    }

    /**
     * Remove um planejamento semanal pelo ID.
     */
    public void excluir(Long id) {
        buscarPorId(id);
        planejamentoDAO.excluir(id);
    }
}
