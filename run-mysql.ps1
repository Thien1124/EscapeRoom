param(
    [string]$MysqlUser = "root",
    [string]$MysqlPassword = "",
    [string]$MysqlUrl = "jdbc:mysql://localhost:3306/gamegiaido?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh",
    [switch]$InitDb
)

$env:MYSQL_USER = $MysqlUser
$env:MYSQL_PASSWORD = $MysqlPassword
$env:MYSQL_URL = $MysqlUrl

Write-Host "Starting app with MySQL profile..." -ForegroundColor Cyan
Write-Host "MYSQL_USER=$MysqlUser" -ForegroundColor Yellow
Write-Host "MYSQL_URL=$MysqlUrl" -ForegroundColor Yellow

$profiles = "mysql"
if ($InitDb) {
    $profiles = "mysql,mysql-init"
    Write-Host "InitDb enabled: schema/data scripts will run." -ForegroundColor Magenta
}

.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=$profiles"
