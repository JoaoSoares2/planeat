package br.com.fiap.planeat.exception;

/**
 * Exceção lançada quando um registro solicitado por ID não é localizado no banco de dados.
 */
public class EntidadeNaoEncontradaException extends RuntimeException {

    public EntidadeNaoEncontradaException(String message) {
        super(message);
    }

    public EntidadeNaoEncontradaException(String entidade, Long id) {
        super(String.format("%s com ID %d não foi encontrado(a).", entidade, id));
    }

    public EntidadeNaoEncontradaException(Class<?> entidadeClasse, Long id) {
        super(String.format("%s com ID %d não foi encontrado(a).", entidadeClasse.getSimpleName(), id));
    }
}
