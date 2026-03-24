param(
    [string]$PostgresUser = "postgres",
    [string]$PostgresPassword = "postgres",
    [string]$PostgresUrl = "jdbc:postgresql://localhost:5432/escaperoom",
    [switch]$InitDb
)

$env:POSTGRES_USER = $PostgresUser
$env:POSTGRES_PASSWORD = $PostgresPassword
$env:POSTGRES_URL = $PostgresUrl

Write-Host "Starting app with PostgreSQL profile..." -ForegroundColor Cyan
Write-Host "POSTGRES_USER=$PostgresUser" -ForegroundColor Yellow
Write-Host "POSTGRES_URL=$PostgresUrl" -ForegroundColor Yellow

$profiles = "postgresql"
if ($InitDb) {
    $profiles = "postgresql,postgresql-init"
    Write-Host "InitDb enabled: schema/data scripts will run." -ForegroundColor Magenta
}

.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=$profiles"