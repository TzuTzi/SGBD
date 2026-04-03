package org.example.sgbd_lab1.DAO;

public class UnrepeatableReadDTO {
    private String firstReadValue;  // what T2 read before T1 committed a change
    private String secondReadValue; // what T2 read after T1 committed — should differ

    public String getFirstReadValue()  { return firstReadValue;  }
    public String getSecondReadValue() { return secondReadValue; }

    public void setFirstReadValue(String firstReadValue)   { this.firstReadValue  = firstReadValue;  }
    public void setSecondReadValue(String secondReadValue) { this.secondReadValue = secondReadValue; }

    public boolean unrepeatableReadOccurred() {
        return firstReadValue != null && !firstReadValue.equals(secondReadValue);
    }

    @Override
    public String toString() {
        return String.format(
                "First read: '%s' | Second read: '%s' | Unrepeatable read: %b",
                firstReadValue, secondReadValue, unrepeatableReadOccurred()
        );
    }
}