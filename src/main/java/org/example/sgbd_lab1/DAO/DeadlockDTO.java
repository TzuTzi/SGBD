package org.example.sgbd_lab1.DAO;

public class DeadlockDTO {
    private boolean deadlockOccurred; // whether PostgreSQL detected a deadlock
    private String deadlockVictim;    // which transaction was rolled back ("T1", "T2", or "none")

    public boolean isDeadlockOccurred() { return deadlockOccurred; }
    public String getDeadlockVictim()   { return deadlockVictim;   }

    public void setDeadlockOccurred(boolean deadlockOccurred) { this.deadlockOccurred = deadlockOccurred; }
    public void setDeadlockVictim(String deadlockVictim)      { this.deadlockVictim   = deadlockVictim;   }

    @Override
    public String toString() {
        return String.format(
                "Deadlock occurred: %b | Victim: %s",
                deadlockOccurred, deadlockVictim
        );
    }
}