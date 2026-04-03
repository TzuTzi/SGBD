package org.example.sgbd_lab1.DAO;

public class BatchPerformanceDTO {
    private long autoCommitMs;      // approach 1: one transaction per insert
    private long batchCommitMs;     // approach 2: commit every 100 inserts
    private long singleBatchMs;     // approach 3: one transaction, batch every 50

    public long getAutoCommitMs()   { return autoCommitMs;   }
    public long getBatchCommitMs()  { return batchCommitMs;  }
    public long getSingleBatchMs()  { return singleBatchMs;  }

    public void setAutoCommitMs(long autoCommitMs)     { this.autoCommitMs   = autoCommitMs;   }
    public void setBatchCommitMs(long batchCommitMs)   { this.batchCommitMs  = batchCommitMs;  }
    public void setSingleBatchMs(long singleBatchMs)   { this.singleBatchMs  = singleBatchMs;  }

    public String getFastest() {
        if (autoCommitMs <= batchCommitMs && autoCommitMs <= singleBatchMs) return "Auto-commit";
        if (batchCommitMs <= autoCommitMs && batchCommitMs <= singleBatchMs) return "Batch commit";
        return "Single batch";
    }

    @Override
    public String toString() {
        return String.format(
                "Auto-commit: %dms | Batch commit (every 100): %dms | Single batch (every 50): %dms | Fastest: %s",
                autoCommitMs, batchCommitMs, singleBatchMs, getFastest()
        );
    }
}