# SGBD Lab 1 (JavaFX + JDBC, PostgreSQL)

## Requirements covered

- Parent table: `shows` (view + selection)
- Child table: `episodes` (auto-refresh on parent selection)
- CRUD on child rows (Add / Update / Delete + validation)
- JDBC only (no ORM), parameterized queries, try-with-resources
- DB errors shown as friendly dialogs (traducere din SQLState PostgreSQL)
- DB schema includes **1-n** (`shows` → `episodes`) and **m-n** (`shows` ↔ `actors`)

## 1) Configure the database

1. Create a PostgreSQL database named `SGBD`.
2. Run the script `sql/init.sql` in that database.

## 2) Configure the connection

Edit `src/main/resources/config.properties`:

- `db.url` (example: `jdbc:postgresql://localhost:5432/SGBD`)
- `db.username`
- `db.password`

## 3) Run the application

From the project root:

```bash
./gradlew run
```

Or on Windows:

```bash
gradlew.bat run
```

## 4) GUI behavior (what to expect)

- Tables
  - Left table (`shows`) is the parent; selecting a row automatically loads child rows in the episodes table.
  - Right table (`episodes`) shows episodes for the currently selected show.
- Search / filter
  - `Search shows` filters by `ID` or `Title` (client-side).
  - `Search episodes` filters by `ID` or `Title` (client-side) within the currently loaded episodes.
- Refresh
  - `Refresh` (shows) reloads the parent list from the database.
  - `Refresh` (episodes) reloads the episodes for the currently selected show.
- Sorting
  - Table headers are sortable; sorting works with filtering.
- Episode CRUD + validation
  - `Add`: requires a selected show and a non-empty title (2–200 chars).
  - `Update`: updates the selected episode using the current form.
  - `Delete`: asks for confirmation before removing the selected episode.

## 5) Exception messages

- `DbException` translates common PostgreSQL errors using SQLState codes (example: FK violation, unique constraint violation).
- The GUI displays a friendly message to the user and shows technical details in the expandable part of the error dialog.
