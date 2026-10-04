param([switch]$BuildOnly, [switch]$Offline, [switch]$DryRun)
$ErrorActionPreference = 'Stop'
$repoDir = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$wrapper = Join-Path $repoDir 'gradlew.bat'
if (!(Test-Path -LiteralPath $wrapper) -or !(Test-Path -LiteralPath (Join-Path $repoDir 'build.gradle'))) {
    throw 'Extraia o ZIP inteiro antes de iniciar o teste.'
}
# Exact allowlist. Neither worlds/settings nor the shared Gradle downloads are included.
$cleanupPaths = @('build', '.test-launcher-cache', 'run\logs', 'run\crash-reports')
function Remove-TestOutput([string]$relative) {
    if ($cleanupPaths -notcontains $relative) { throw 'Destino fora da lista de limpeza.' }
    $absolute = [IO.Path]::GetFullPath((Join-Path $repoDir $relative))
    if (!$absolute.StartsWith($repoDir + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) {
        throw 'Destino fora do projeto.'
    }
    # Reject junctions/symlinks on the target or intermediate directories.
    $part = $absolute
    while ($part -ne $repoDir) {
        if (Test-Path -LiteralPath $part) {
            if ((Get-Item -LiteralPath $part -Force).Attributes -band [IO.FileAttributes]::ReparsePoint) {
                throw "Limpeza recusada: link de diretorio em $part"
            }
        }
        $part = Split-Path -Parent $part
    }
    if ($DryRun) { Write-Host "Limparia: $absolute"; return }
    if (Test-Path -LiteralPath $absolute) {
        # Also prevent traversal through links inside an allowed directory.
        if (Get-ChildItem -LiteralPath $absolute -Recurse -Force | Where-Object { $_.Attributes -band [IO.FileAttributes]::ReparsePoint }) {
            throw "Limpeza recusada: link dentro de $absolute"
        }
        Remove-Item -LiteralPath $absolute -Recurse -Force
    }
}
if ($DryRun) {
    foreach ($path in $cleanupPaths) { Remove-TestOutput $path }
    Write-Host 'Executaria: Gradle sem daemon/cache -> check build -> runClient; limparia cache temporario ao sair.'
    exit 0
}
$candidates = @($env:JAVA_HOME)
$javaCommand = Get-Command java.exe -ErrorAction SilentlyContinue
if ($javaCommand) { $candidates += Split-Path -Parent (Split-Path -Parent $javaCommand.Source) }
foreach ($pattern in @("$env:USERPROFILE\.gradle\jdks\*\*", 'C:\Program Files\Eclipse Adoptium\jdk-17*', 'C:\Program Files\Microsoft\jdk-17*', 'C:\Program Files\Java\jdk-17*')) {
    $candidates += Get-ChildItem -Path $pattern -Directory -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName
}
$javaDir = $null
foreach ($candidate in $candidates) {
    if (!$candidate) { continue }
    $javaExe = Join-Path $candidate 'bin\java.exe'
    $release = Join-Path $candidate 'release'
    if ((Test-Path -LiteralPath $javaExe) -and (Test-Path -LiteralPath $release) -and
        ([IO.File]::ReadAllText($release) -match 'JAVA_VERSION="17[.\"]')) { $javaDir = $candidate; break }
}
if (!$javaDir) { throw 'Instale um JDK 17 (Temurin) ou configure JAVA_HOME para ele.' }
$env:JAVA_HOME = $javaDir
$env:Path = "$javaDir\bin;$env:Path"
$lockPath = Join-Path $repoDir '.test-launcher.lock'
$lock = $null
$exitCode = 1
Push-Location $repoDir
try {
    try { $lock = [IO.File]::Open($lockPath, 'OpenOrCreate', 'ReadWrite', 'None') }
    catch { throw 'Outro inicializador esta aberto. Feche o jogo e tente novamente.' }
    foreach ($path in $cleanupPaths) { Remove-TestOutput $path }
    $gradleArgs = @('--no-daemon', '--no-build-cache', '--rerun-tasks', '--project-cache-dir', (Join-Path $repoDir '.test-launcher-cache'))
    if ($Offline) { $gradleArgs += '--offline' }
    Write-Host 'Compilando e verificando o mod. A primeira execucao precisa de internet para baixar Forge/Gradle.'
    & $wrapper @gradleArgs check build
    if ($LASTEXITCODE -ne 0) { throw 'A build falhou. O jogo nao foi iniciado.' }
    if (!$BuildOnly) {
        Write-Host 'Abrindo Minecraft. Feche o jogo para concluir e liberar o cache temporario.'
        & $wrapper @gradleArgs runClient
        if ($LASTEXITCODE -ne 0) { throw 'O cliente encerrou com erro.' }
    }
    $exitCode = 0
} catch { Write-Host $_.Exception.Message -ForegroundColor Red }
finally {
    if ($lock) {
        try { Remove-TestOutput '.test-launcher-cache' } catch { Write-Warning $_.Exception.Message }
        $lock.Dispose()
    }
    Pop-Location
}
exit $exitCode
