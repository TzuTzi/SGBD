# SGBD Lab 2 — Transactions & Isolation Levels (JavaFX + JDBC, PostgreSQL)

Demonstrates transaction isolation problems, deadlock detection, and batch insert
performance using two concurrent JDBC connections per scenario.

---

## Database schema

```sql
shows       (s_id, s_title)
episodes    (e_id, e_title, s_id → shows)
actors      (a_id, a_name)
showsactors (sa_show_id → shows, sa_actor_id → actors)   -- m-n
sums        (id, value)        -- used by Lost Update demos
employees   (e_id, e_name, e_salary)  -- used by Batch Performance demo
```

Run `sql/init.sql` against your PostgreSQL database to create all tables.

---

## Setup

### 1. Configure the connection

Copy the example config and fill in your credentials:

```bash
cp src/main/resources/config.properties.example src/main/resources/config.properties
```

Edit `src/main/resources/config.properties`:

```properties
db.url=jdbc:postgresql://localhost:5432/SGBD
db.username=postgres
db.password=your_password
```

> `config.properties` is gitignored and will never be committed.

### 2. Run

```bash
# Linux / macOS
./gradlew run

# Windows
gradlew.bat run
```

---

## Demonstrations

### 1. Dirty Read

| Step | What happens |
|------|-------------|
| T1 | Reads original `s_title` from `shows` where `s_id = 1` |
| T1 | Commits `'Dirty Title'` to the DB |
| T2 | Reads `s_title` — sees the committed dirty value |
| T1 | Restores original title (simulates rollback) |

**Result shown:** original value, dirty value T2 read, final value, whether dirty
read occurred, whether the DB was correctly restored.

> PostgreSQL does not support true `READ UNCOMMITTED`, so the dirty value is
> committed briefly before T1 "rolls back" by restoring the original.

---

### 2. Unrepeatable Read

| Step | What happens |
|------|-------------|
| T2 | First read of `s_title` (READ COMMITTED) |
| T1 | Commits `'Updated Title'` |
| T2 | Second read of the same row — gets a different value |

**Result shown:** first read value, second read value, whether an unrepeatable
read occurred.

---

### 3. Phantom Read

| Step | What happens |
|------|-------------|
| T2 | `COUNT(*)` of episodes for `s_id = 1` (READ COMMITTED) |
| T1 | Inserts a new episode and commits |
| T2 | Same `COUNT(*)` — returns a higher number |

**Result shown:** first count, second count, whether a phantom read occurred.

---

### 4. Lost Update

| Step | What happens |
|------|-------------|
| T1 & T2 | Both read `value` from `sums` where `id = 1` |
| T1 | Writes `value + 50`, commits |
| T2 | Writes its stale `value + 30`, commits — overwrites T1 |

**Result shown:** initial value, T1 addition (+50), T2 addition (+30), expected
value (initial + 80), actual DB value, whether the update was lost.

---

### 5. Lost Update — Pessimistic Lock

Same scenario as Lost Update but T1 uses `SELECT … FOR UPDATE`.  
T2 blocks until T1 releases the lock, then reads the already-updated value —
both additions are applied and no update is lost.

**Result shown:** same fields as Lost Update; `lostUpdateOccurred` should always
be `false`.

---

### 6. Deadlock

| Step | What happens |
|------|-------------|
| T1 | Locks `shows` row `s_id = 1` |
| T2 | Locks `shows` row `s_id = 2` |
| T1 | Tries to lock `s_id = 2` — blocks |
| T2 | Tries to lock `s_id = 1` — circular dependency |
| PostgreSQL | Detects deadlock, rolls back one transaction (victim) |

**Result shown:** whether a deadlock occurred, which transaction was the victim
(`T1`, `T2`, or `none`).

---

### 7. Batch Performance

Inserts 5 000 rows into `employees`, averaged over 3 runs, using three strategies:

| Approach | Strategy |
|----------|----------|
| Auto-commit | One transaction per insert |
| Batch commit | Manual commit every 100 inserts |
| Single batch | `addBatch` + `executeBatch` every 50 rows, one final commit |

**Result shown:** average time in ms for each approach, fastest approach.

---

## Grading criteria (100 pts)

| Area | Points |
|------|--------|
| Concurrency demonstrations (all 4 problems correct) | 20 |
| Clear step-by-step logging | 10 |
| Correct isolation level solutions | 10 |
| Batch approaches implemented correctly | 15 |
| Precise measurements, multiple runs | 10 |
| Clear results presentation | 5 |
| Correct transaction management | 10 |
| Clean concurrency implementation | 5 |
| Concurrency problem analysis in report | 8 |
| Performance analysis & conclusions | 7 |
| **Bonus** — visual comparison chart | +10 |

---

## Project structure

```
src/main/java/org/example/sgbd_lab1/
├── DAO/
│   ├── Database.java            # connection factory (reads config.properties)
│   ├── Transactions.java        # all 7 demonstrations
│   ├── TransactionDTO.java      # dirty read result
│   ├── UnrepeatableReadDTO.java
│   ├── PhantomReadDTO.java
│   ├── LostUpdateDTO.java
│   ├── DeadlockDTO.java
│   └── BatchPerformanceDTO.java
├── model/                       # Actor, Show, Episode
├── service/Service.java
├── MainController.java          # JavaFX controller — wires buttons to DAO
├── HelloApplication.java
├── Launcher.java
└── DbException.java             # translates SQLState codes to friendly messages
sql/
└── init.sql                     # DDL — run this first
src/main/resources/
├── config.properties            # gitignored — copy from .example
├── config.properties.example
└── org/example/sgbd_lab1/transaction-view.fxml
```
