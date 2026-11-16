package br.com.fiap.planeat.dao;

import br.com.fiap.planeat.model.ItemListaCompras;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ListaComprasDAO {

    public Long salvarItemLista(Long listaId, ItemListaCompras item) {
        return null;
    }

    public Optional<ItemListaCompras> buscarPorId(Long id) {
        return Optional.empty();
    }

    public List<ItemListaCompras> listarPorListaId(Long listaId) {
        return new ArrayList<>();
    }

    public void atualizarStatusComprado(Long itemListaId, boolean comprado) {
    }
}
