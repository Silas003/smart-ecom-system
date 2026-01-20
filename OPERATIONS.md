# Operations & Processes

This file documents routine operational processes for developers and operators working with the Smart E-Commerce System.

1) Provision a local development environment (Postgres + MongoDB)

- PostgreSQL (sample commands):
  - Create DB user (optional):
    psql -U postgres -c "CREATE USER appuser WITH PASSWORD 'appPass';"
  - Create DB:
    psql -U postgres -c "CREATE DATABASE \"smartEcom\" OWNER appuser;"
  - Grant privileges (if needed):
    psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE \"smartEcom\" TO appuser;"

- MongoDB:
  - Start `mongod` locally or use a Docker container.
  - Default connection URL: `mongodb://localhost:27017`.

2) Apply schema and indexes

- Apply schema:
  psql -U $env:DB_USERNAME -d smartEcom -f src/main/resources/sql/schema_postgres.sql

- Apply indexes (after data load):
  psql -U $env:DB_USERNAME -d smartEcom -f src/main/resources/sql/create_indexes.sql

3) Seed sample data (optional)
- Provide a SQL seed script or call service layer to insert sample products, categories, users.

4) Running the application (dev)
- Set environment variables:
  $env:DB_URL = 'jdbc:postgresql://localhost:5432/smartEcom'
  $env:DB_USERNAME = 'postgres'
  $env:DB_PASSWORD = 'Drake@7890'
  $env:MONGO_URL = 'mongodb://localhost:27017'
  $env:MONGO_DB_NAME = 'smartEcom'

- Run:
  mvn clean javafx:run

5) Capturing performance metrics
- Open Admin Dashboard → Performance Monitoring.
- Click "Capture Baseline" before making changes/optimizations.
- Perform representative operations (searches, order retrievals, product listing).
- Click "Capture Optimized".
- Click "Generate Report" and optionally save the file.

6) Cache management
- Admin Dashboard has "Clear Cache" to reset product caches.
- ProductService.clearCache() can also be invoked programmatically.

7) Backups and restore (Postgres)
- Backup DB:
  pg_dump -U $env:DB_USERNAME -Fc smartEcom > smartEcom.dump
- Restore DB:
  pg_restore -U $env:DB_USERNAME -d smartEcom smartEcom.dump

8) Notes on compatibility
- Tests use H2; DAO implementations may contain H2-specific SQL (MERGE). For production, prefer Postgres upserts.

For any operational runbooks, create an issue in the repository so we can expand this doc with specific scripts and Docker Compose manifests.
