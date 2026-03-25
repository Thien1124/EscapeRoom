# EscapeRoom

## Run with MySQL (Laragon)

### 1) Start application with MySQL profile

```powershell
./run-mysql.ps1
```

### 2) Initialize schema and seed data

```powershell
./run-mysql.ps1 -InitDb
```

### 3) Optional environment overrides

- `MYSQL_URL` (default: `jdbc:mysql://localhost:3306/gamegiaido?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh`)
- `MYSQL_USER` (default: `root`)
- `MYSQL_PASSWORD` (default: empty)