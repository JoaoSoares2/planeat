package br.com.fiap.planeat.dao;

import br.com.fiap.planeat.model.CategoriaAlimento;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoriaDAO {

    public Long inserir(CategoriaAlimento categoria) {
        return null;
    }

    public Optional<CategoriaAlimento> buscarPorId(Long id) {
        return Optional.empty();
    }

    public List<CategoriaAlimento> listarTodos() {
        return new ArrayList<>();
    }
}
