package org.example.sgbd_lab1;

import java.sql.SQLException;

public class DbException extends RuntimeException {
    private final String userMessage;
    private final String sqlState;

    public DbException(String userMessage, Throwable cause) {
        super(userMessage, cause);
        this.userMessage = userMessage;
        this.sqlState = (cause instanceof SQLException sqlEx) ? sqlEx.getSQLState() : null;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public String getSqlState() {
        return sqlState;
    }

    /**
     * Translates common PostgreSQL SQLState codes into friendly messages for the user.
     */
    public static DbException fromSQLException(String context, SQLException ex) {
        String state = ex.getSQLState();
        String msg;

        if ("23503".equals(state)) { // foreign_key_violation
            msg = "Operația nu poate fi finalizată deoarece există dependențe înregistrate (cheie străină).";
        } else if ("23505".equals(state)) { // unique_violation
            msg = "Nu se poate efectua operația: există deja o înregistrare cu aceeași valoare unică.";
        } else if ("22P02".equals(state)) { // invalid_text_representation
            msg = "Date introduse invalide (format greșit). Verificați câmpurile.";
        } else if ("22001".equals(state)) { // string_data_right_truncation
            msg = "Textul introdus este prea lung pentru câmpul din baza de date.";
        } else if ("42601".equals(state)) { // syntax_error / access rule
            msg = "Eroare SQL. Verificați schema și interogările.";
        } else {
            msg = "Eroare de bază de date în timpul: " + context + ".";
        }

        return new DbException(msg, ex);
    }
}

