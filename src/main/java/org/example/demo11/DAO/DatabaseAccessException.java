package org.example.demo11.DAO;

/**
 * Exceptie folosita pentru erori aparute in timpul accesului la baza de date.
 * Este o exceptie "unchecked" ca sa nu fie nevoie de try/catch peste tot.
 */
public class DatabaseAccessException extends RuntimeException {

    public DatabaseAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

