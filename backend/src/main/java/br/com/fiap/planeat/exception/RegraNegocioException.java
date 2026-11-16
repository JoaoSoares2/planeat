package br.com.fiap.planeat.exception;

/**
 * Exceção lançada quando uma regra de negócio ou validação de integridade do domínio é violada.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String message) {
        super(message);
    }

    public RegraNegocioException(String message, Throwable cause) {
        super(message, cause);
    }
}
