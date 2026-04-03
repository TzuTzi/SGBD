package org.example.sgbd_lab1.DAO;

import org.example.sgbd_lab1.DbException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

public class Transactions {

    private static final int INSERT_COUNT      = 5000;
    private static final int BATCH_COMMIT_SIZE = 100;
    private static final int BATCH_EXECUTE_SIZE = 50;
    private static final int RUNS              = 3;

    // Ensures shows(s_id=1) exists with a known title before each test that needs it.
    private void ensureShowsRow() {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(true);
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO shows (s_id, s_title) VALUES (1, 'Original Title') " +
                    "ON CONFLICT (s_id) DO UPDATE SET s_title = 'Original Title'")) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DbException("Failed to seed shows data", e);
        }
    }

    // Ensures sums(id=1) exists with value=0 before each lost-update test.
    private void resetSumsRow() {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(true);
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO sums (id, value) VALUES (1, 0) " +
                    "ON CONFLICT (id) DO UPDATE SET value = 0")) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DbException("Failed to seed sums data", e);
        }
    }

    // -------------------------------------------------------------------------
    // 1. DIRTY READ
    // T1 commits a change, T2 reads it, then T1 restores the original value —
    // simulating a rollback. T2 acted on a value that no longer exists in the DB.
    // PostgreSQL does not support true READ UNCOMMITTED so we simulate it this way.
    // -------------------------------------------------------------------------
    public TransactionDTO dirtyRead() {
        ensureShowsRow();
        Semaphore t1HasWritten = new Semaphore(0);
        Semaphore t2HasRead    = new Semaphore(0);
        CountDownLatch done    = new CountDownLatch(2);

        TransactionDTO result = new TransactionDTO();
        AtomicReference<Exception> exception = new AtomicReference<>();

        Thread transaction1 = new Thread(() -> {
            Connection connection = null;
            try {
                connection = Database.getConnection();
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                connection.setAutoCommit(false);

                // Save original value
                try (PreparedStatement select = connection.prepareStatement(
                        "SELECT s_title FROM shows WHERE s_id = 1")) {
                    ResultSet rs = select.executeQuery();
                    if (rs.next()) {
                        result.setOriginalValue(rs.getString("s_title"));
                        System.out.println("[T1] Original value saved: '" + result.getOriginalValue() + "'");
                    }
                }

                // Write and commit dirty value
                try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE shows SET s_title = 'Dirty Title' WHERE s_id = 1")) {
                    update.executeUpdate();
                }
                connection.commit();
                System.out.println("[T1] Committed 'Dirty Title' to DB");

                t1HasWritten.release();
                t2HasRead.acquire();

                // Simulate rollback by restoring original
                connection.setAutoCommit(false);
                try (PreparedStatement restore = connection.prepareStatement(
                        "UPDATE shows SET s_title = ? WHERE s_id = 1")) {
                    restore.setString(1, result.getOriginalValue());
                    restore.executeUpdate();
                }
                connection.commit();
                System.out.println("[T1] Simulated rollback — restored to '" + result.getOriginalValue() + "'");

                // Confirm final DB value
                try (PreparedStatement verify = connection.prepareStatement(
                        "SELECT s_title FROM shows WHERE s_id = 1")) {
                    ResultSet rs = verify.executeQuery();
                    if (rs.next()) {
                        result.setFinalValue(rs.getString("s_title"));
                    }
                }

            } catch (SQLException | InterruptedException e) {
                exception.set(e);
                try { if (connection != null) connection.rollback(); } catch (SQLException ex) { /* ignore */ }
                t1HasWritten.release();
            } finally {
                try { if (connection != null) connection.close(); } catch (SQLException e) { /* ignore */ }
                done.countDown();
            }
        });

        Thread transaction2 = new Thread(() -> {
            Connection connection = null;
            try {
                t1HasWritten.acquire();

                connection = Database.getConnection();
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                connection.setAutoCommit(true);

                try (PreparedStatement select = connection.prepareStatement(
                        "SELECT s_title FROM shows WHERE s_id = 1")) {
                    ResultSet rs = select.executeQuery();
                    if (rs.next()) {
                        result.setDirtyReadValue(rs.getString("s_title"));
                        System.out.println("[T2] Read value from DB = '" + result.getDirtyReadValue() + "'");
                    }
                }

                t2HasRead.release();

            } catch (SQLException | InterruptedException e) {
                exception.set(e);
                t2HasRead.release();
            } finally {
                try { if (connection != null) connection.close(); } catch (SQLException e) { /* ignore */ }
                done.countDown();
            }
        });

        transaction1.start();
        transaction2.start();

        try {
            done.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DbException("Threads interrupted", e);
        }

        if (exception.get() != null) {
            throw new DbException("Transaction failed", exception.get());
        }

        System.out.println(result);
        return result;
    }

    // -------------------------------------------------------------------------
    // 2. UNREPEATABLE READ
    // T2 reads the same row twice under READ COMMITTED.
    // T1 commits an update between the two reads — T2 gets different values.
    // -------------------------------------------------------------------------
    public UnrepeatableReadDTO unrepeatableRead() {
        ensureShowsRow();
        Semaphore t2HasFirstRead = new Semaphore(0);
        Semaphore t1HasCommitted = new Semaphore(0);
        CountDownLatch done      = new CountDownLatch(2);

        UnrepeatableReadDTO result = new UnrepeatableReadDTO();
        AtomicReference<Exception> exception = new AtomicReference<>();

        Thread transaction1 = new Thread(() -> {
            Connection connection = null;
            try {
                connection = Database.getConnection();
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                connection.setAutoCommit(false);

                t2HasFirstRead.acquire();

                try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE shows SET s_title = 'Updated Title' WHERE s_id = 1")) {
                    update.executeUpdate();
                }
                connection.commit();
                System.out.println("[T1] Committed 'Updated Title' to DB");

                t1HasCommitted.release();

            } catch (SQLException | InterruptedException e) {
                exception.set(e);
                try { if (connection != null) connection.rollback(); } catch (SQLException ex) { /* ignore */ }
                t1HasCommitted.release();
            } finally {
                try { if (connection != null) connection.close(); } catch (SQLException e) { /* ignore */ }
                done.countDown();
            }
        });

        Thread transaction2 = new Thread(() -> {
            Connection connection = null;
            try {
                connection = Database.getConnection();
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                connection.setAutoCommit(false);

                // First read
                try (PreparedStatement select = connection.prepareStatement(
                        "SELECT s_title FROM shows WHERE s_id = 1")) {
                    ResultSet rs = select.executeQuery();
                    if (rs.next()) {
                        result.setFirstReadValue(rs.getString("s_title"));
                        System.out.println("[T2] First read: '" + result.getFirstReadValue() + "'");
                    }
                }

                t2HasFirstRead.release();
                t1HasCommitted.acquire();

                // Second read — T1 has committed in between
                try (PreparedStatement select = connection.prepareStatement(
                        "SELECT s_title FROM shows WHERE s_id = 1")) {
                    ResultSet rs = select.executeQuery();
                    if (rs.next()) {
                        result.setSecondReadValue(rs.getString("s_title"));
                        System.out.println("[T2] Second read: '" + result.getSecondReadValue() + "'");
                    }
                }

                connection.commit();

            } catch (SQLException | InterruptedException e) {
                exception.set(e);
                try { if (connection != null) connection.rollback(); } catch (SQLException ex) { /* ignore */ }
                t2HasFirstRead.release();
            } finally {
                try { if (connection != null) connection.close(); } catch (SQLException e) { /* ignore */ }
                done.countDown();
            }
        });

        transaction1.start();
        transaction2.start();

        try {
            done.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DbException("Threads interrupted", e);
        }

        if (exception.get() != null) {
            throw new DbException("Transaction failed", exception.get());
        }

        System.out.println(result);
        return result;
    }

    // -------------------------------------------------------------------------
    // 3. PHANTOM READ
    // T2 counts rows twice under READ COMMITTED.
    // T1 inserts and commits a new episode between the two counts — T2 sees more rows.
    // -------------------------------------------------------------------------
    public PhantomReadDTO phantomRead() {
        Semaphore t2HasFirstRead = new Semaphore(0);
        Semaphore t1HasCommitted = new Semaphore(0);
        CountDownLatch done      = new CountDownLatch(2);

        PhantomReadDTO result = new PhantomReadDTO();
        AtomicReference<Exception> exception = new AtomicReference<>();

        Thread transaction1 = new Thread(() -> {
            Connection connection = null;
            try {
                connection = Database.getConnection();
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                connection.setAutoCommit(false);

                t2HasFirstRead.acquire();

                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO episodes (e_title, s_id) VALUES ('Phantom Episode', 1)")) {
                    insert.executeUpdate();
                }
                connection.commit();
                System.out.println("[T1] Inserted new episode and committed");

                t1HasCommitted.release();

            } catch (SQLException | InterruptedException e) {
                exception.set(e);
                try { if (connection != null) connection.rollback(); } catch (SQLException ex) { /* ignore */ }
                t1HasCommitted.release();
            } finally {
                try { if (connection != null) connection.close(); } catch (SQLException e) { /* ignore */ }
                done.countDown();
            }
        });

        Thread transaction2 = new Thread(() -> {
            Connection connection = null;
            try {
                connection = Database.getConnection();
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                connection.setAutoCommit(false);

                // First count
                try (PreparedStatement select = connection.prepareStatement(
                        "SELECT COUNT(*) FROM episodes WHERE s_id = 1")) {
                    ResultSet rs = select.executeQuery();
                    if (rs.next()) {
                        result.setFirstCount(rs.getInt(1));
                        System.out.println("[T2] First count: " + result.getFirstCount());
                    }
                }

                t2HasFirstRead.release();
                t1HasCommitted.acquire();

                // Second count — phantom row now included
                try (PreparedStatement select = connection.prepareStatement(
                        "SELECT COUNT(*) FROM episodes WHERE s_id = 1")) {
                    ResultSet rs = select.executeQuery();
                    if (rs.next()) {
                        result.setSecondCount(rs.getInt(1));
                        System.out.println("[T2] Second count: " + result.getSecondCount());
                    }
                }

                connection.commit();

            } catch (SQLException | InterruptedException e) {
                exception.set(e);
                try { if (connection != null) connection.rollback(); } catch (SQLException ex) { /* ignore */ }
                t2HasFirstRead.release();
            } finally {
                try { if (connection != null) connection.close(); } catch (SQLException e) { /* ignore */ }
                done.countDown();
            }
        });

        transaction1.start();
        transaction2.start();

        try {
            done.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DbException("Threads interrupted", e);
        }

        if (exception.get() != null) {
            throw new DbException("Transaction failed", exception.get());
        }

        System.out.println(result);
        return result;
    }

    // -------------------------------------------------------------------------
    // 4. LOST UPDATE
    // Both T1 and T2 read the same value, each adds to it, and writes back.
    // T2 commits last using the stale value — T1's update is lost.
    // -------------------------------------------------------------------------
    public LostUpdateDTO lostUpdate() {
        resetSumsRow();
        Semaphore bothHaveRead   = new Semaphore(0);
        Semaphore t1HasCommitted = new Semaphore(0);
        CountDownLatch done      = new CountDownLatch(2);

        LostUpdateDTO result = new LostUpdateDTO();
        result.setT1Addition(50);
        result.setT2Addition(30);

        AtomicReference<Exception> exception = new AtomicReference<>();

        Thread transaction1 = new Thread(() -> {
            Connection connection = null;
            try {
                connection = Database.getConnection();
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                connection.setAutoCommit(false);

                int initial;
                try (PreparedStatement select = connection.prepareStatement(
                        "SELECT value FROM Sums WHERE id = 1")) {
                    ResultSet rs = select.executeQuery();
                    rs.next();
                    initial = rs.getInt("value");
                    result.setInitialValue(initial);
                    System.out.println("[T1] Read value: " + initial);
                }

                bothHaveRead.release();
                bothHaveRead.acquire();

                try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE Sums SET value = ? WHERE id = 1")) {
                    update.setInt(1, initial + result.getT1Addition());
                    update.executeUpdate();
                }
                connection.commit();
                System.out.println("[T1] Committed value: " + (initial + result.getT1Addition()));

                t1HasCommitted.release();

            } catch (SQLException | InterruptedException e) {
                exception.set(e);
                try { if (connection != null) connection.rollback(); } catch (SQLException ex) { /* ignore */ }
                bothHaveRead.release();
                t1HasCommitted.release();
            } finally {
                try { if (connection != null) connection.close(); } catch (SQLException e) { /* ignore */ }
                done.countDown();
            }
        });

        Thread transaction2 = new Thread(() -> {
            Connection connection = null;
            try {
                connection = Database.getConnection();
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                connection.setAutoCommit(false);

                int initial;
                try (PreparedStatement select = connection.prepareStatement(
                        "SELECT value FROM Sums WHERE id = 1")) {
                    ResultSet rs = select.executeQuery();
                    rs.next();
                    initial = rs.getInt("value");
                    System.out.println("[T2] Read value: " + initial);
                }

                bothHaveRead.release();
                bothHaveRead.acquire();

                t1HasCommitted.acquire();

                // Write using stale initial value — stomps T1's update
                try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE Sums SET value = ? WHERE id = 1")) {
                    update.setInt(1, initial + result.getT2Addition());
                    update.executeUpdate();
                }
                connection.commit();
                System.out.println("[T2] Committed value: " + (initial + result.getT2Addition()));

                try (PreparedStatement select = connection.prepareStatement(
                        "SELECT value FROM Sums WHERE id = 1")) {
                    ResultSet rs = select.executeQuery();
                    if (rs.next()) {
                        result.setActualValue(rs.getInt("value"));
                    }
                }
                result.setExpectedValue(result.getInitialValue() + result.getT1Addition() + result.getT2Addition());

            } catch (SQLException | InterruptedException e) {
                exception.set(e);
                try { if (connection != null) connection.rollback(); } catch (SQLException ex) { /* ignore */ }
                bothHaveRead.release();
                t1HasCommitted.release();
            } finally {
                try { if (connection != null) connection.close(); } catch (SQLException e) { /* ignore */ }
                done.countDown();
            }
        });

        transaction1.start();
        transaction2.start();

        try {
            done.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DbException("Threads interrupted", e);
        }

        if (exception.get() != null) {
            throw new DbException("Transaction failed", exception.get());
        }

        System.out.println(result);
        return result;
    }

    // -------------------------------------------------------------------------
    // 5. LOST UPDATE PREVENTED — Pessimistic locking (SELECT ... FOR UPDATE)
    // T1 locks the row before reading. T2 blocks until T1 commits, then reads
    // the already-updated value — both additions are applied correctly.
    // -------------------------------------------------------------------------
    public LostUpdateDTO lostUpdateWithPessimisticLock() {
        resetSumsRow();

        // Read initial value here, before threads start, so it is always the
        // known reset value regardless of which thread acquires the lock first.
        int initialValue;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT value FROM sums WHERE id = 1");
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            initialValue = rs.getInt("value");
        } catch (SQLException e) {
            throw new DbException("Failed to read initial sums value", e);
        }

        CountDownLatch done = new CountDownLatch(2);

        LostUpdateDTO result = new LostUpdateDTO();
        result.setT1Addition(50);
        result.setT2Addition(30);
        result.setInitialValue(initialValue);

        AtomicReference<Exception> exception = new AtomicReference<>();

        Thread transaction1 = new Thread(() -> {
            Connection connection = null;
            try {
                connection = Database.getConnection();
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                connection.setAutoCommit(false);

                try (PreparedStatement select = connection.prepareStatement(
                        "SELECT value FROM Sums WHERE id = 1 FOR UPDATE")) {
                    ResultSet rs = select.executeQuery();
                    rs.next();
                    int initial = rs.getInt("value");
                    System.out.println("[T1] Acquired lock — value = " + initial);

                    try (PreparedStatement update = connection.prepareStatement(
                            "UPDATE Sums SET value = ? WHERE id = 1")) {
                        update.setInt(1, initial + result.getT1Addition());
                        update.executeUpdate();
                        System.out.println("[T1] Updated to " + (initial + result.getT1Addition()));
                    }
                }

                connection.commit();
                System.out.println("[T1] Committed — lock released");

            } catch (SQLException e) {
                exception.set(e);
                try { if (connection != null) connection.rollback(); } catch (SQLException ex) { /* ignore */ }
            } finally {
                try { if (connection != null) connection.close(); } catch (SQLException e) { /* ignore */ }
                done.countDown();
            }
        });

        Thread transaction2 = new Thread(() -> {
            Connection connection = null;
            try {
                connection = Database.getConnection();
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                connection.setAutoCommit(false);

                System.out.println("[T2] Waiting to acquire lock...");
                try (PreparedStatement select = connection.prepareStatement(
                        "SELECT value FROM Sums WHERE id = 1 FOR UPDATE")) {
                    ResultSet rs = select.executeQuery(); // blocks until T1 releases lock
                    rs.next();
                    int current = rs.getInt("value");
                    System.out.println("[T2] Acquired lock — value = " + current);

                    try (PreparedStatement update = connection.prepareStatement(
                            "UPDATE Sums SET value = ? WHERE id = 1")) {
                        update.setInt(1, current + result.getT2Addition());
                        update.executeUpdate();
                        System.out.println("[T2] Updated to " + (current + result.getT2Addition()));
                    }
                }

                connection.commit();
                System.out.println("[T2] Committed");

            } catch (SQLException e) {
                exception.set(e);
                try { if (connection != null) connection.rollback(); } catch (SQLException ex) { /* ignore */ }
            } finally {
                try { if (connection != null) connection.close(); } catch (SQLException e) { /* ignore */ }
                done.countDown();
            }
        });

        transaction1.start();
        transaction2.start();

        try {
            done.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DbException("Threads interrupted", e);
        }

        if (exception.get() != null) {
            throw new DbException("Transaction failed", exception.get());
        }

        try (Connection conn = Database.getConnection();
             PreparedStatement select = conn.prepareStatement(
                     "SELECT value FROM Sums WHERE id = 1");
             ResultSet rs = select.executeQuery()) {
            if (rs.next()) {
                int actual = rs.getInt("value");
                result.setActualValue(actual);
                result.setExpectedValue(result.getInitialValue() + result.getT1Addition() + result.getT2Addition());
                System.out.println("[VERIFY] Final value = " + actual + " (both additions applied)");
            }
        } catch (SQLException e) {
            System.err.println("Failed to verify: " + e.getMessage());
        }

        System.out.println(result);
        return result;
    }

    // -------------------------------------------------------------------------
    // 6. DEADLOCK
    // T1 locks row s_id=1, T2 locks row s_id=2.
    // T1 then tries to lock s_id=2, T2 tries to lock s_id=1.
    // Circular dependency — PostgreSQL detects it and rolls back one transaction.
    // -------------------------------------------------------------------------
    public DeadlockDTO deadlock() {
        Semaphore t1LockedFirst  = new Semaphore(0);
        Semaphore t2LockedSecond = new Semaphore(0);
        CountDownLatch done      = new CountDownLatch(2);

        DeadlockDTO result = new DeadlockDTO();
        AtomicReference<Exception> exception = new AtomicReference<>();

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(true);
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO shows (s_id, s_title) VALUES (1, 'Original Title') ON CONFLICT (s_id) DO NOTHING")) {
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO shows (s_id, s_title) VALUES (2, 'Deadlock Show') ON CONFLICT (s_id) DO NOTHING")) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DbException("Failed to prepare rows for deadlock demo", e);
        }

        Thread transaction1 = new Thread(() -> {
            Connection connection = null;
            try {
                connection = Database.getConnection();
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                connection.setAutoCommit(false);

                System.out.println("[T1] Locking show s_id=1...");
                try (PreparedStatement stmt = connection.prepareStatement(
                        "SELECT * FROM shows WHERE s_id = 1 FOR UPDATE")) {
                    stmt.executeQuery();
                }
                System.out.println("[T1] Locked show s_id=1");

                t1LockedFirst.release();
                t2LockedSecond.acquire();

                System.out.println("[T1] Now trying to lock show s_id=2 (will block — T2 holds it)...");
                try (PreparedStatement stmt = connection.prepareStatement(
                        "SELECT * FROM shows WHERE s_id = 2 FOR UPDATE")) {
                    stmt.executeQuery();
                }

                System.out.println("[T1] Acquired lock on s_id=2 — T1 was NOT the deadlock victim");
                connection.commit();
                System.out.println("[T1] Committed");

            } catch (SQLException e) {
                if (e.getSQLState() != null && e.getSQLState().startsWith("40")) {
                    result.setDeadlockOccurred(true);
                    result.setDeadlockVictim("T1");
                    System.out.println("[T1] DEADLOCK VICTIM — rolled back by PostgreSQL");
                } else {
                    exception.set(e);
                }
                try { if (connection != null) connection.rollback(); } catch (SQLException ex) { /* ignore */ }
                t1LockedFirst.release();
            } catch (InterruptedException e) {
                exception.set(e);
            } finally {
                try { if (connection != null) connection.close(); } catch (SQLException e) { /* ignore */ }
                done.countDown();
            }
        });

        Thread transaction2 = new Thread(() -> {
            Connection connection = null;
            try {
                connection = Database.getConnection();
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                connection.setAutoCommit(false);

                t1LockedFirst.acquire();

                System.out.println("[T2] Locking show s_id=2...");
                try (PreparedStatement stmt = connection.prepareStatement(
                        "SELECT * FROM shows WHERE s_id = 2 FOR UPDATE")) {
                    stmt.executeQuery();
                }
                System.out.println("[T2] Locked show s_id=2");

                t2LockedSecond.release();

                System.out.println("[T2] Now trying to lock show s_id=1 (will block — T1 holds it)...");
                try (PreparedStatement stmt = connection.prepareStatement(
                        "SELECT * FROM shows WHERE s_id = 1 FOR UPDATE")) {
                    stmt.executeQuery();
                }

                System.out.println("[T2] Acquired lock on s_id=1 — T2 was NOT the deadlock victim");
                connection.commit();
                System.out.println("[T2] Committed");

            } catch (SQLException e) {
                if (e.getSQLState() != null && e.getSQLState().startsWith("40")) {
                    result.setDeadlockOccurred(true);
                    result.setDeadlockVictim("T2");
                    System.out.println("[T2] DEADLOCK VICTIM — rolled back by PostgreSQL");
                } else {
                    exception.set(e);
                }
                try { if (connection != null) connection.rollback(); } catch (SQLException ex) { /* ignore */ }
                t2LockedSecond.release();
            } catch (InterruptedException e) {
                exception.set(e);
                t2LockedSecond.release();
            } finally {
                try { if (connection != null) connection.close(); } catch (SQLException e) { /* ignore */ }
                done.countDown();
            }
        });

        transaction1.start();
        transaction2.start();

        try {
            done.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DbException("Threads interrupted", e);
        }

        if (exception.get() != null) {
            throw new DbException("Transaction failed", exception.get());
        }

        if (result.getDeadlockVictim() == null) {
            result.setDeadlockVictim("none");
        }

        System.out.println(result);
        return result;
    }

    // -------------------------------------------------------------------------
    // 7. BATCH PERFORMANCE
    // Compares three insert strategies over 5000 rows, averaged across 3 runs.
    // Approach 1: auto-commit per insert
    // Approach 2: manual commit every 100 inserts
    // Approach 3: addBatch + executeBatch every 50, single commit
    // -------------------------------------------------------------------------
    public BatchPerformanceDTO batchPerformance() {
        BatchPerformanceDTO result = new BatchPerformanceDTO();

        long autoTotal = 0, batchTotal = 0, singleTotal = 0;
        for (int i = 1; i <= RUNS; i++) {
            long ms;

            ms = runAutoCommit();
            System.out.printf("[Auto-commit] Run %d: %dms%n", i, ms);
            autoTotal += ms;
            cleanup();

            ms = runBatchCommit();
            System.out.printf("[Batch commit] Run %d: %dms%n", i, ms);
            batchTotal += ms;
            cleanup();

            ms = runSingleBatch();
            System.out.printf("[Single batch] Run %d: %dms%n", i, ms);
            singleTotal += ms;
            cleanup();
        }

        result.setAutoCommitMs(autoTotal / RUNS);
        result.setBatchCommitMs(batchTotal / RUNS);
        result.setSingleBatchMs(singleTotal / RUNS);

        System.out.printf("[Averages] Auto-commit: %dms | Batch commit: %dms | Single batch: %dms%n",
                result.getAutoCommitMs(), result.getBatchCommitMs(), result.getSingleBatchMs());

        System.out.println(result);
        return result;
    }

    private long runAutoCommit() {
        System.out.println("[Approach 1] Starting auto-commit inserts...");
        long start = System.currentTimeMillis();

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(true);
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO employees (e_name, e_salary) VALUES (?, ?)")) {
                for (int i = 0; i < INSERT_COUNT; i++) {
                    stmt.setString(1, "Employee_" + i);
                    stmt.setInt(2, 1000 + i);
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DbException("Auto-commit approach failed", e);
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("[Approach 1] Done in " + elapsed + "ms");
        return elapsed;
    }

    private long runBatchCommit() {
        System.out.println("[Approach 2] Starting batch-commit inserts...");
        long start = System.currentTimeMillis();

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO employees (e_name, e_salary) VALUES (?, ?)")) {
                for (int i = 0; i < INSERT_COUNT; i++) {
                    stmt.setString(1, "Employee_" + i);
                    stmt.setInt(2, 1000 + i);
                    stmt.executeUpdate();
                    if ((i + 1) % BATCH_COMMIT_SIZE == 0) {
                        conn.commit();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DbException("Batch-commit approach failed", e);
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("[Approach 2] Done in " + elapsed + "ms");
        return elapsed;
    }

    private long runSingleBatch() {
        System.out.println("[Approach 3] Starting single-batch inserts...");
        long start = System.currentTimeMillis();

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO employees (e_name, e_salary) VALUES (?, ?)")) {
                for (int i = 0; i < INSERT_COUNT; i++) {
                    stmt.setString(1, "Employee_" + i);
                    stmt.setInt(2, 1000 + i);
                    stmt.addBatch();
                    if ((i + 1) % BATCH_EXECUTE_SIZE == 0) {
                        stmt.executeBatch();
                    }
                }
                stmt.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DbException("Single-batch approach failed", e);
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("[Approach 3] Done in " + elapsed + "ms");
        return elapsed;
    }

    private void cleanup() {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM employees")) {
            stmt.executeUpdate();
            System.out.println("[Cleanup] employees table cleared");
        } catch (SQLException e) {
            throw new DbException("Cleanup failed", e);
        }
    }
}