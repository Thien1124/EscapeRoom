# EscapeRoom

## Run with PostgreSQL

### 1) Start application with PostgreSQL profile

```powershell
./run-postgresql.ps1
```

### 2) Initialize schema and seed data

```powershell
./run-postgresql.ps1 -InitDb
```

### 3) Optional environment overrides

- `POSTGRES_URL` (default: `jdbc:postgresql://localhost:5432/gamegiaido`)
- `POSTGRES_USER` (default: `postgres`)
- `POSTGRES_PASSWORD` (default: `postgres`)