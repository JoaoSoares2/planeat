package br.com.fiap.planeat.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {
    @Test
    void hashesPasswordsWithUniqueSaltsAndChecksCredentials() {
        String firstHash = PasswordHasher.hash("senha-segura-123");
        String secondHash = PasswordHasher.hash("senha-segura-123");

        assertNotEquals(firstHash, secondHash);
        assertTrue(PasswordHasher.matches("senha-segura-123", firstHash));
        assertFalse(PasswordHasher.matches("senha-incorreta", firstHash));
    }
}
