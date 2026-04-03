package org.example.sgbd_lab1.DAO;

public class PhantomReadDTO {
    private int firstCount;  // number of rows T2 saw before T1 inserted
    private int secondCount; // number of rows T2 saw after T1 inserted

    public int getFirstCount()  { return firstCount;  }
    public int getSecondCount() { return secondCount; }

    public void setFirstCount(int firstCount)   { this.firstCount  = firstCount;  }
    public void setSecondCount(int secondCount) { this.secondCount = secondCount; }

    public boolean phantomReadOccurred() {
        return firstCount != secondCount;
    }

    @Override
    public String toString() {
        return String.format(
                "First count: %d | Second count: %d | Phantom read: %b",
                firstCount, secondCount, phantomReadOccurred()
        );
    }
}