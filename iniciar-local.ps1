param([switch]$SomenteBanco)

$ErrorActionPreference = 'Stop'
$localData = Join-Path ([Environment]::GetFolderPath('LocalApplicationData')) 'AlertaEnchentes'
$cluster = Join-Path $localData 'PostgreSQL16'
$pgBin = Join-Path $env:ProgramFiles 'PostgreSQL\16\bin'
$pgCtl = Join-Path $pgBin 'pg_ctl.exe'
$pgReady = Join-Path $pgBin 'pg_isready.exe'
$port = 5433

if (!(Test-Path -LiteralPath $pgCtl) -or !(Test-Path -LiteralPath (Join-Path $cluster 'PG_VERSION'))) {
    throw 'O PostgreSQL local do Alerta Enchentes não foi configurado nesta conta.'
}

& $pgCtl -D $cluster status *> $null
if ($LASTEXITCODE -ne 0) {
    & $pgReady -q -h 127.0.0.1 -p $port -t 2
    if ($LASTEXITCODE -ne 2) {
        throw "A porta $port está sendo usada por outro banco."
    }
    $pgLog = Join-Path $localData 'postgresql.log'
    & $pgCtl -D $cluster -l $pgLog -o "-h 127.0.0.1 -p $port" -w -t 30 start
    if ($LASTEXITCODE -ne 0) {
        throw "Não foi possível iniciar o banco. Consulte $pgLog."
    }
}

if ($SomenteBanco) {
    return
}

$userJava = [Environment]::GetEnvironmentVariable('JAVA_HOME', 'User')
if ($userJava -and (Test-Path -LiteralPath (Join-Path $userJava 'bin\javac.exe'))) {
    $env:JAVA_HOME = $userJava
}
Push-Location -LiteralPath $PSScriptRoot
try {
    & .\mvnw.cmd spring-boot:run
    $result = $LASTEXITCODE
} finally {
    Pop-Location
}
exit $result
