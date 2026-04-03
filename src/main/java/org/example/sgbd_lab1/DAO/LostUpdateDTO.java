package org.example.sgbd_lab1.DAO;

public class LostUpdateDTO {
    private int initialValue;   // what both T1 and T2 read at the start
    private int t1Addition;     // how much T1 added
    private int t2Addition;     // how much T2 added
    private int expectedValue;  // what the value should be if both updates applied
    private int actualValue;    // what the value actually is in the DB after both commits

    public int getInitialValue()  { return initialValue;  }
    public int getT1Addition()    { return t1Addition;    }
    public int getT2Addition()    { return t2Addition;    }
    public int getExpectedValue() { return expectedValue; }
    public int getActualValue()   { return actualValue;   }

    public void setInitialValue(int initialValue)   { this.initialValue  = initialValue;  }
    public void setT1Addition(int t1Addition)       { this.t1Addition    = t1Addition;    }
    public void setT2Addition(int t2Addition)       { this.t2Addition    = t2Addition;    }
    public void setExpectedValue(int expectedValue) { this.expectedValue = expectedValue; }
    public void setActualValue(int actualValue)     { this.actualValue   = actualValue;   }

    public boolean lostUpdateOccurred() {
        return actualValue != expectedValue;
    }

    @Override
    public String toString() {
        return String.format(
                "Initial: %d | T1 added: %d | T2 added: %d | Expected: %d | Actual: %d | Lost update: %b",
                initialValue, t1Addition, t2Addition, expectedValue, actualValue, lostUpdateOccurred()
        );
    }
}