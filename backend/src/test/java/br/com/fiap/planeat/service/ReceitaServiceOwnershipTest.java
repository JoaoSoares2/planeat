package br.com.fiap.planeat.service;

import br.com.fiap.planeat.dao.IngredienteDAO;
import br.com.fiap.planeat.dao.ReceitaDAO;
import br.com.fiap.planeat.dao.UsuarioDAO;
import br.com.fiap.planeat.exception.EntidadeNaoEncontradaException;
import br.com.fiap.planeat.model.CategoriaAlimento;
import br.com.fiap.planeat.model.Ingrediente;
import br.com.fiap.planeat.model.ItemReceita;
import br.com.fiap.planeat.model.Receita;
import br.com.fiap.planeat.model.Usuario;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReceitaServiceOwnershipTest {
    @Test
    void creatingRecipeUsesAuthenticatedOwnerInsteadOfPayloadOwner() {
        CapturingReceitaDAO recipes = new CapturingReceitaDAO();
        ReceitaService service = new ReceitaService(
                recipes,
                new CatalogIngredienteDAO(),
                new ExistingUsuarioDAO());
        Receita recipe = new Receita("Sopa", "Cozinhar", 2, 20, "Almoço");
        recipe.setUsuarioId(999L);
        recipe.adicionarIngrediente(new ItemReceita(
                new Ingrediente(1L, "Tomate", new CategoriaAlimento(1L, "Hortifrúti"), "g"),
                100.0,
                "g"));

        service.cadastrarReceita(recipe, 10L);

        assertEquals(10L, recipes.insertedOwner);
    }

    @Test
    void recipeLookupIsScopedToTheAuthenticatedUser() {
        CapturingReceitaDAO recipes = new CapturingReceitaDAO();
        ReceitaService service = new ReceitaService(
                recipes,
                new CatalogIngredienteDAO(),
                new ExistingUsuarioDAO());

        assertThrows(EntidadeNaoEncontradaException.class, () -> service.buscarPorId(33L, 20L));
        assertEquals(20L, recipes.lastLookupOwner);
    }

    private static final class CapturingReceitaDAO extends ReceitaDAO {
        private Long insertedOwner;
        private Long lastLookupOwner;

        @Override
        public Long inserir(Receita receita) {
            insertedOwner = receita.getUsuarioId();
            return 1L;
        }

        @Override
        public Optional<Receita> buscarPorId(Long id, Long usuarioId) {
            lastLookupOwner = usuarioId;
            return usuarioId == 10L ? Optional.of(new Receita(id, usuarioId, "Prato", "Modo", 1, 1, "Almoço"))
                    : Optional.empty();
        }
    }

    private static final class CatalogIngredienteDAO extends IngredienteDAO {
        @Override
        public Optional<Ingrediente> buscarPorId(Long id) {
            return Optional.of(new Ingrediente(id, "Tomate", new CategoriaAlimento(1L, "Hortifrúti"), "g"));
        }
    }

    private static final class ExistingUsuarioDAO extends UsuarioDAO {
        @Override
        public Optional<Usuario> buscarPorId(Long id) {
            return Optional.of(new Usuario(id, "Usuário", "usuario@example.com", "irrelevante"));
        }
    }
}
