package br.com.fiap.planeat.exception;

/**
 * Exceção de infraestrutura lançada em caso de falhas de comunicação, restrições relacionais
 * ou erros de execução de comandos SQL no banco de dados.
 */
public class DatabasePersistenceException extends RuntimeException {

    public DatabasePersistenceException(String message) {
        super(message);
    }

    public DatabasePersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabasePersistenceException(Throwable cause) {
        super(cause);
    }
}
