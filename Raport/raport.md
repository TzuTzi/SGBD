## Raport scurt (Lab 1) - Aplicatie Desktop Parent-Child (1-n)

### 1. Decizii de design
- Am folosit arhitectura in 3 straturi:
  - `DAO` pentru acces direct la baza de date folosind JDBC (fara ORM).
  - `service.Service` pentru intermedierea apelurilor dintre UI si DAO.
  - UI JavaFX (`HelloController` + `hello-view.fxml`) pentru implementarea master-detail.
- Modelul relatiei parinte-copil este:
  - `shows` (parinte)
  - `episodes` (copil), cu `episodes.s_id` ca referinta externa la `shows.s_id`.
- In UI:
  - TableView pentru parinte (shows).
  - TableView pentru copil (episodes) care se reincarca automat la schimbarea selectiei parintelui.

### 2. Provocari intalnite si solutii
- Gestionarea resurselor JDBC:
  - Am folosit `try-with-resources` in toate operatiile SQL pentru a inchide automat `Connection`, `PreparedStatement` si `ResultSet`.
- Chei generate la INSERT:
  - Pentru inserarea episoadelor sau a show-urilor am folosit `PreparedStatement(..., Statement.RETURN_GENERATED_KEYS)` si `getGeneratedKeys()` pentru a popula `id` in obiectele din model.
- Operatii CRUD parametrizate (SQL injection):
  - Toate interogarile care primesc valori din UI folosesc `PreparedStatement` cu `?` si `setInt/setString`.
- Mesaje de eroare:
  - La validarea din UI, se foloseste `statusLabel`.
  - Pentru erori SQL, mesajele sunt afisate in GUI prin `Alert` de tip `ERROR` (nu doar in consola).

### 3. Ce am invatat
- Cum se realizeaza conexiunea la PostgreSQL prin JDBC folosind `DriverManager`.
- Cum se scriu interogari SQL cu `PreparedStatement` si cum se gestioneaza `ResultSet`.
- Cum se construieste un UI master-detail in JavaFX folosind `TableView`, `ObservableList` si listener-e pe selectie.
- Cum se mentine separarea responsabilitatilor intre UI si logica de acces la date.

### 4. Functionalitati implementate (CRUD + relatia 1-n)
- CRUD pentru parinte (`shows`):
  - `Add show`
  - `Update show`
  - `Delete show` (cu confirmare)
- CRUD pentru copil (`episodes`) conditionat de show selectat:
  - `Add episode` pentru show-ul selectat
  - `Update episode`
  - `Delete episode` (cu confirmare)
- Refresh:
  - buton `Refresh` pentru re-incarcarea datelor din baza de date.

### 5. Cum se ruleaza aplicatia
- Se configureaza `src/main/resources/config.properties` cu valorile reale ale DB-ului.
- Se pornesc serverul PostgreSQL si conexiunea este disponibila (ex: `localhost:5432` sau IP pentru retea).
- Rulare:
  - `gradlew.bat clean run`

