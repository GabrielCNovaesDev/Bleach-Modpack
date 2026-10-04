param([switch]$BuildOnly, [switch]$Offline, [switch]$DryRun)
$ErrorActionPreference = 'Stop'
$repoDir = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$wrapper = Join-Path $repoDir 'gradlew.bat'
if (!(Test-Path -LiteralPath $wrapper) -or !(Test-Path -LiteralPath (Join-Path $repoDir 'build.gradle'))) {
    throw 'Extraia o ZIP inteiro antes de iniciar o teste.'
}
# Exact allowlist. Neither worlds/settings nor the shared Gradle downloads are included.
$cleanupPaths = @('build', '.test-launcher-cache', 'run\logs', 'run\crash-reports')
function Get-TestPathItem([string]$path) {
    try { return Get-Item -LiteralPath $path -Force -ErrorAction Stop }
    catch [System.Management.Automation.ItemNotFoundException] { return $null }
}
function Assert-TestOutput([string]$relative) {
    if ($cleanupPaths -notcontains $relative) { throw 'Destino fora da lista de limpeza.' }
    $absolute = [IO.Path]::GetFullPath((Join-Path $repoDir $relative))
    if (!$absolute.StartsWith($repoDir + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) {
        throw 'Destino fora do projeto.'
    }
    # Reject junctions/symlinks on the target or intermediate directories.
    $part = $absolute
    while ($part -ne $repoDir) {
        $item = Get-TestPathItem $part
        if ($item) {
            if ($item.Attributes -band [IO.FileAttributes]::ReparsePoint) {
                throw "Limpeza recusada: link de diretorio em $part"
            }
        }
        $part = Split-Path -Parent $part
    }
    $target = Get-TestPathItem $absolute
    if ($target) {
        if (!$target.PSIsContainer) { throw "Limpeza recusada: esperado diretorio em $absolute" }
        # Inspect one directory at a time: reject links BEFORE descending into them.
        $pending = New-Object 'System.Collections.Generic.Stack[string]'
        $pending.Push($absolute)
        while ($pending.Count -gt 0) {
            foreach ($child in Get-ChildItem -LiteralPath $pending.Pop() -Force) {
                if ($child.Attributes -band [IO.FileAttributes]::ReparsePoint) {
                    throw "Limpeza recusada: link dentro de $absolute"
                }
                if ($child.PSIsContainer) { $pending.Push($child.FullName) }
            }
        }
    }
}
function Remove-TestOutput([string]$relative) {
    Assert-TestOutput $relative
    $absolute = [IO.Path]::GetFullPath((Join-Path $repoDir $relative))
    if (Test-Path -LiteralPath $absolute) {
        Remove-Item -LiteralPath $absolute -Recurse -Force
    }
}
if ($DryRun) {
    foreach ($path in $cleanupPaths) { Assert-TestOutput $path }
    foreach ($path in $cleanupPaths) { Write-Host "Limparia: $(Join-Path $repoDir $path)" }
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
$cleanupStarted = $false
Push-Location -LiteralPath $repoDir
try {
    $lockItem = Get-TestPathItem $lockPath
    if ($lockItem -and (($lockItem.Attributes -band [IO.FileAttributes]::ReparsePoint) -or $lockItem.PSIsContainer)) {
        throw 'Lock recusado: esperado arquivo comum, sem links.'
    }
    try { $lock = [IO.File]::Open($lockPath, 'OpenOrCreate', 'ReadWrite', 'None') }
    catch { throw 'Outro inicializador esta aberto. Feche o jogo e tente novamente.' }
    # Validate ALL destinations before deleting anything; repeat checks at each removal.
    foreach ($path in $cleanupPaths) { Assert-TestOutput $path }
    $cleanupStarted = $true
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
        if ($cleanupStarted) {
            try { Remove-TestOutput '.test-launcher-cache' }
            catch {
                $exitCode = 1
                Write-Warning ('Cache temporario nao removido. Feche os processos de teste e tente novamente. ' + $_.Exception.Message)
            }
        }
        $lock.Dispose()
    }
    Pop-Location
}
exit $exitCode
