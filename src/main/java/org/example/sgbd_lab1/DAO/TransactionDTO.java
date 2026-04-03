package org.example.sgbd_lab1.DAO;

public class TransactionDTO {
    private String originalValue;   // what was in DB before T1 touched anything
    private String dirtyReadValue;  // what T2 read while T1 had written but not yet "rolled back"
    private String finalValue;      // what DB contains after T1's simulated rollback
    public TransactionDTO() {}

    public TransactionDTO(String originalValue, String dirtyReadValue, String finalValue) {
        this.originalValue  = originalValue;
        this.dirtyReadValue = dirtyReadValue;
        this.finalValue     = finalValue;
    }
    public void setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
    }
    public void setDirtyReadValue(String originalValue) {
        this.dirtyReadValue= originalValue;
    }
    public void setFinalValue(String finalValue) {
        this.finalValue = finalValue;
    }

    public String getOriginalValue()  { return originalValue;  }
    public String getDirtyReadValue() { return dirtyReadValue; }
    public String getFinalValue()     { return finalValue;     }

    // T2 read something different from what ended up in the DB — dirty read occurred
    public boolean dirtyReadOccurred() {
        return dirtyReadValue != null && !dirtyReadValue.equals(finalValue);
    }

    // sanity check — DB was properly restored after simulated rollback
    public boolean wasRestoredCorrectly() {
        return originalValue != null && originalValue.equals(finalValue);
    }

    @Override
    public String toString() {
        return String.format(
                "Original: '%s' | T2 Read (dirty): '%s' | Final (after rollback): '%s' | Dirty read: %b | Restored: %b",
                originalValue, dirtyReadValue, finalValue, dirtyReadOccurred(), wasRestoredCorrectly()
        );
    }
}