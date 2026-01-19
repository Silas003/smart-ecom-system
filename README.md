# Smart E-Commerce System

This repository contains the Smart E-Commerce System — a JavaFX + JDBC application with a PostgreSQL relational database (core business data) and MongoDB for unstructured data (reviews/logs).

This README provides setup and run instructions, environment variables, database setup, running the application, testing, and links to additional project docs.

## Prerequisites
- Java 21
- Maven
- PostgreSQL (12+ recommended)
- MongoDB (for NoSQL features; optional for core relational flows)
- psql client (for applying SQL scripts)

## Environment variables
- `DB_URL` 
- `DB_USERNAME` 
- `DB_PASSWORD` 
- `MONGO_URL`
- `MONGO_DB_NAME`

## Quick setup (PowerShell)
1. Create the PostgreSQL database (run as a user with CREATE DATABASE privileges):

   ```bash
   # Create database and owner (adjust as needed)
   psql -U postgres -c "CREATE DATABASE \"${dbName}\";"
   ```

2. Apply the schema (production-ready SQL provided):

   ```bash
   # From repo root (adjust path if needed)
   psql -U $env:DB_USERNAME -d ${dbName} -f src/main/resources/sql/schema_postgres.sql
   ```

3. Create indexes (recommended after schema + sample data loaded):

   ```bash
   psql -U $env:DB_USERNAME -d ${dbName} -f src/main/resources/sql/create_indexes.sql
   ```

4. Start MongoDB (optional for reviews/logs):
   - Locally: run `mongod` or your MongoDB service.
   - Ensure `MONGO_URL` and `MONGO_DB_NAME` env vars are set.

## Using Docker Compose (optional)

A convenience `docker-compose.yml` is included to start a local PostgreSQL and MongoDB for development:

```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down
```

After starting Postgres, apply schema and seed data (PowerShell example):

```powershell
$env:DB_USERNAME = your db username
$env:DB_PASSWORD = your db password
# Apply schema
psql -U $env:DB_USERNAME -d ${dbName} -f src/main/resources/sql/schema_postgres.sql
# Seed sample data
psql -U $env:DB_USERNAME -d ${dbName} -f src/main/resources/sql/seed_sample_data.sql
# Create indexes
psql -U $env:DB_USERNAME -d ${dbName} -f src/main/resources/sql/create_indexes.sql
```

See `docker-compose.yml` for credentials and ports.

## Run the application
- In development, from repository root use Maven:

  ```bash
  mvn clean javafx:run
  ```

- Or build a jar (if configured) and run with Java.

## Running tests
- Unit and integration tests use Maven:

  ```bash
  mvn test
  ```

## Database migrations (Flyway)

This project includes Flyway migrations located at `src/main/resources/db/migration`:
- `V1__create_schema.sql` — creates tables and constraints
- `V2__create_indexes.sql` — creates indexes including unique lower(name)
- `V3__seed_sample_data.sql` — inserts minimal seed data

Run migrations with Maven (PowerShell):

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5432/${dbName}'
$env:DB_USERNAME = <your db username>
$env:DB_PASSWORD = <your db password>
# Run Flyway migrate
mvn flyway:migrate
```

Or use the Flyway CLI / Docker image if preferred.

## ERD
A rendered ERD is available at `docs/erd.svg` (open in your editor or browser).

## Notes & developer tips
- Tests use an H2 test schema (see `src/test/resources/sql/schema.sql`). The H2 schema is intentionally simplified for CI and test speed.
- The DAO implementation previously used H2 `MERGE` statements in places; production SQL uses PostgreSQL `INSERT ... ON CONFLICT` semantics where applicable. See `src/main/resources/sql/schema_postgres.sql`.
- To capture performance metrics and generate reports, run the app, open the Admin Dashboard, "Capture Baseline", perform operations, "Capture Optimized", then "Generate Report".

## Docs and useful files
- `src/main/resources/sql/schema_postgres.sql` — Production schema (PostgreSQL) with FK constraints.
- `src/main/resources/sql/create_indexes.sql` — Index creation script (run after schema + data).
- `NOSQL_DESIGN.md` — NoSQL design for reviews & logs (MongoDB).
- `PERFORMANCE_REPORT.md` — Performance report and methodology summary.
- `DATABASE_DESIGN.md` — Relational database design notes and ERD guidance.
- `OPERATIONS.md` — Step-by-step operational processes (apply schema, seed data, run app, capture metrics).

If anything is unclear or you'd like me to provision sample seed data or add a quick-run Docker Compose manifest for Postgres + MongoDB, tell me and I can add it.
