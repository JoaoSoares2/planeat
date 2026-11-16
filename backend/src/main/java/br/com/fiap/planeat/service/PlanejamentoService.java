package br.com.fiap.planeat.service;

import br.com.fiap.planeat.dao.PlanejamentoDAO;
import br.com.fiap.planeat.model.PlanejamentoSemanal;

import java.util.List;

public class PlanejamentoService {

    private final PlanejamentoDAO planejamentoDAO;

    public PlanejamentoService() {
        this.planejamentoDAO = new PlanejamentoDAO();
    }

    public PlanejamentoService(PlanejamentoDAO planejamentoDAO) {
        this.planejamentoDAO = planejamentoDAO;
    }

    public List<PlanejamentoSemanal> listarPlanejamentos() {
        return planejamentoDAO.listarTodos();
    }

    public PlanejamentoSemanal buscarPorId(Long id) {
        return planejamentoDAO.buscarPorId(id).orElse(null);
    }
}
