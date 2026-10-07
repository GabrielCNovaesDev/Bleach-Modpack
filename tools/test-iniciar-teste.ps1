param([string]$ReportPath)
$ErrorActionPreference = 'Stop'
$sourceScript = Join-Path $PSScriptRoot 'iniciar-teste.ps1'
$tempBase = [IO.Path]::GetFullPath($env:TEMP)
$fixtureRoot = [IO.Path]::GetFullPath((Join-Path $tempBase ('Bleach launcher audit [dados] & teste ! ' + [Guid]::NewGuid().ToString('N'))))
if (!$fixtureRoot.StartsWith($tempBase + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) { throw 'Fixture fora do TEMP.' }
$results = New-Object 'System.Collections.Generic.List[object]'
$processes = New-Object 'System.Collections.Generic.List[System.Diagnostics.Process]'
$caseNumber = 0
$utf8 = New-Object Text.UTF8Encoding($true)
function Write-Text([string]$path, [string]$value) {
    [IO.Directory]::CreateDirectory((Split-Path -Parent $path)) | Out-Null
    [IO.File]::WriteAllText($path, $value, $utf8)
}
function Assert-True($condition, [string]$message) { if (!$condition) { throw $message } }
function New-Fixture {
    $script:caseNumber++
    $root = Join-Path $fixtureRoot "case-$script:caseNumber"
    [IO.Directory]::CreateDirectory((Join-Path $root 'tools')) | Out-Null
    Copy-Item -LiteralPath $sourceScript -Destination (Join-Path $root 'tools/iniciar-teste.ps1')
    Copy-Item -LiteralPath (Join-Path $PSScriptRoot '../Iniciar-Teste.bat') -Destination (Join-Path $root 'Iniciar-Teste.bat')
    Write-Text (Join-Path $root 'build.gradle') '// Mock project: NEVER launches Gradle or Minecraft.'
    Write-Text (Join-Path $root 'gradlew.bat') @'
@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0stub.ps1" %*
exit /b %ERRORLEVEL%
'@
    Write-Text (Join-Path $root 'stub.ps1') @'
$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
Add-Content -LiteralPath (Join-Path $root 'calls.txt') -Value ($args -join ' ')
[IO.Directory]::CreateDirectory((Join-Path $root '.test-launcher-cache')) | Out-Null
[IO.File]::WriteAllText((Join-Path $root '.test-launcher-cache/new.cache'), 'CACHE')
[IO.Directory]::CreateDirectory((Join-Path $root 'build')) | Out-Null
[IO.File]::WriteAllText((Join-Path $root 'build/new.jar'), 'NEW')
$mode = [IO.File]::ReadAllText((Join-Path $root 'mode.txt')).Trim()
if ($mode -eq 'build-fail' -and $args -contains 'build') { exit 7 }
if ($mode -eq 'client-fail' -and $args -contains 'runClient') { exit 9 }
if ($mode -eq 'hold-build') {
    [IO.File]::WriteAllText((Join-Path $root 'build-ready'), 'READY')
    $deadline = [DateTime]::UtcNow.AddSeconds(25)
    while (!(Test-Path -LiteralPath (Join-Path $root 'release-build'))) {
        if ([DateTime]::UtcNow -gt $deadline) { exit 8 }; Start-Sleep -Milliseconds 100
    }
}
if ($mode -eq 'cleanup-fail') {
    $helper = Start-Process powershell.exe -WindowStyle Hidden -PassThru -ArgumentList @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', ('"' + (Join-Path $root 'hold-cache.ps1') + '"'))
    [IO.File]::WriteAllText((Join-Path $root 'helper.pid'), [string]$helper.Id)
    $deadline = [DateTime]::UtcNow.AddSeconds(10)
    while (!(Test-Path -LiteralPath (Join-Path $root 'cache-ready'))) {
        if ([DateTime]::UtcNow -gt $deadline) { exit 8 }; Start-Sleep -Milliseconds 100
    }
}
exit 0
'@
    Write-Text (Join-Path $root 'hold-cache.ps1') @'
$handle = [IO.File]::Open((Join-Path $PSScriptRoot '.test-launcher-cache/new.cache'), 'Open', 'ReadWrite', 'None')
try {
    [IO.File]::WriteAllText((Join-Path $PSScriptRoot 'cache-ready'), 'READY')
    $deadline = [DateTime]::UtcNow.AddSeconds(25)
    while (!(Test-Path -LiteralPath (Join-Path $PSScriptRoot 'release-cache')) -and [DateTime]::UtcNow -lt $deadline) { Start-Sleep -Milliseconds 100 }
} finally { $handle.Dispose() }
'@
    Write-Text (Join-Path $root 'mode.txt') 'success'
    foreach ($path in @('build/old.bin', '.test-launcher-cache/stale.cache', 'run/logs/old.log', 'run/crash-reports/old.txt')) {
        Write-Text (Join-Path $root $path) 'OLD'
    }
    foreach ($path in @('run/saves/world/level.dat', 'run/options.txt', 'run/config/settings.toml', 'run/screenshots/keep.png', 'src/keep.java', '.git/keep', '.gradle/keep', 'outside/keep.txt')) {
        Write-Text (Join-Path $root $path) 'PRESERVAR'
    }
    return $root
}
function Invoke-Launcher([string]$root, [string[]]$options = @()) {
    $savedPreference = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue' # Child stderr is expected in negative scenarios.
        $output = & powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $root 'tools/iniciar-teste.ps1') @options 2>&1
        $code = $LASTEXITCODE
    } finally { $ErrorActionPreference = $savedPreference }
    return @{ Code = $code; Output = ($output -join "`n") }
}
function Assert-Preserved([string]$root) {
    foreach ($path in @('run/saves/world/level.dat', 'run/options.txt', 'run/config/settings.toml', 'run/screenshots/keep.png', 'src/keep.java', '.git/keep', '.gradle/keep', 'outside/keep.txt')) {
        Assert-True ([IO.File]::ReadAllText((Join-Path $root $path)) -eq 'PRESERVAR') "Arquivo protegido alterado: $path"
    }
}
function Run-Case([string]$name, [scriptblock]$action) {
    try { & $action; $results.Add(@{ Name = $name; Passed = $true }); Write-Host "PASS $name" }
    catch { $results.Add(@{ Name = $name; Passed = $false; Error = $_.Exception.Message }); Write-Host "FAIL $name`: $($_.Exception.Message)" }
}
function Wait-File([string]$path) {
    $deadline = [DateTime]::UtcNow.AddSeconds(15)
    while (!(Test-Path -LiteralPath $path)) { if ([DateTime]::UtcNow -gt $deadline) { throw 'Timeout de fixture' }; Start-Sleep -Milliseconds 100 }
}
function Remove-Fixture([string]$path) {
    $absolute = [IO.Path]::GetFullPath($path)
    if ($absolute -ne $fixtureRoot -and !$absolute.StartsWith($fixtureRoot + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) { throw 'Limpeza fora da fixture' }
    $item = Get-Item -LiteralPath $absolute -Force
    if ($item.Attributes -band [IO.FileAttributes]::ReparsePoint) {
        if ($item.PSIsContainer) { [IO.Directory]::Delete($absolute) } else { [IO.File]::Delete($absolute) }
    } elseif ($item.PSIsContainer) {
        foreach ($child in Get-ChildItem -LiteralPath $absolute -Force) { Remove-Fixture $child.FullName }
        Remove-Item -LiteralPath $absolute -Force
    } else { Remove-Item -LiteralPath $absolute -Force }
}
try {
    Run-Case 'DryRun validates without mutation' {
        $root = New-Fixture; $result = Invoke-Launcher $root @('-DryRun')
        Assert-True ($result.Code -eq 0) $result.Output
        Assert-True (Test-Path -LiteralPath (Join-Path $root 'build/old.bin')) 'DryRun removed build'
        Assert-True (Test-Path -LiteralPath (Join-Path $root '.test-launcher-cache/stale.cache')) 'DryRun removed cache'
        Assert-True (!(Test-Path -LiteralPath (Join-Path $root 'calls.txt'))) 'DryRun ran wrapper'
        Assert-True (!(Test-Path -LiteralPath (Join-Path $root '.test-launcher.lock'))) 'DryRun created lock'
        Assert-Preserved $root
    }
    Run-Case 'BAT entry point preserves literal paths and exit code without interactive input' {
        $root = New-Fixture
        $savedPreference = $ErrorActionPreference
        Push-Location -LiteralPath $root
        try {
            $ErrorActionPreference = 'Continue'
            $output = & cmd.exe /d /c 'Iniciar-Teste.bat -DryRun <NUL' 2>&1
            $code = $LASTEXITCODE
        } finally { $ErrorActionPreference = $savedPreference; Pop-Location }
        Assert-True ($code -eq 0) ($output -join "`n")
        Assert-True (Test-Path -LiteralPath (Join-Path $root 'build/old.bin')) 'BAT DryRun removed build'
        Assert-True (!(Test-Path -LiteralPath (Join-Path $root 'calls.txt'))) 'BAT DryRun ran wrapper'
        Assert-Preserved $root
    }
    Run-Case 'BuildOnly cleanup, cache recovery, protected files and paths with special characters' {
        $root = New-Fixture; $result = Invoke-Launcher $root @('-BuildOnly', '-Offline')
        Assert-True ($result.Code -eq 0) $result.Output
        foreach ($path in @('build/old.bin', '.test-launcher-cache', 'run/logs', 'run/crash-reports')) {
            Assert-True (!(Test-Path -LiteralPath (Join-Path $root $path))) "Residuos: $path"
        }
        Assert-True (Test-Path -LiteralPath (Join-Path $root 'build/new.jar')) 'New build absent'
        $calls = @(Get-Content -LiteralPath (Join-Path $root 'calls.txt'))
        Assert-True ($calls.Count -eq 1 -and $calls[0] -match '--offline' -and $calls[0] -match '--no-daemon' -and $calls[0] -match '--no-build-cache') 'Wrong Gradle arguments'
        Assert-True ((Get-Item -LiteralPath (Join-Path $root '.test-launcher.lock')).Length -eq 0) 'Lock accumulated data'
        Assert-Preserved $root
    }
    Run-Case 'Normal client flow and repeated starts' {
        $root = New-Fixture
        for ($i = 0; $i -lt 2; $i++) {
            $result = Invoke-Launcher $root
            Assert-True ($result.Code -eq 0) $result.Output
            Assert-True (!(Test-Path -LiteralPath (Join-Path $root '.test-launcher-cache'))) 'Cache remained'
        }
        $calls = @(Get-Content -LiteralPath (Join-Path $root 'calls.txt'))
        Assert-True ($calls.Count -eq 4 -and $calls[1] -match 'runClient' -and $calls[3] -match 'runClient') 'Client not sequenced after builds'
        Assert-Preserved $root
    }
    foreach ($mode in @('build-fail', 'client-fail')) {
        Run-Case "Failure handling: $mode" {
            $root = New-Fixture; Write-Text (Join-Path $root 'mode.txt') $mode
            $result = Invoke-Launcher $root
            Assert-True ($result.Code -ne 0) 'Failure returned success'
            Assert-True (!(Test-Path -LiteralPath (Join-Path $root '.test-launcher-cache'))) 'Failure left cache'
            if ($mode -eq 'build-fail') { Assert-True (@(Get-Content -LiteralPath (Join-Path $root 'calls.txt')).Count -eq 1) 'Client ran after failed build' }
            Assert-Preserved $root
        }
    }
    foreach ($kind in @('build', '.test-launcher-cache', 'run', 'nested', 'cycle', 'broken', 'lock')) {
        Run-Case "Junction refused before cleanup: $kind" {
            $root = New-Fixture
            $destination = Join-Path $root 'outside'
            $link = Join-Path $root $kind
            if ($kind -eq 'nested') { $link = Join-Path $root 'build/nested/link' }
            if ($kind -eq 'cycle') { $link = Join-Path $root 'build/loop'; $destination = Join-Path $root 'build' }
            if ($kind -eq 'broken') { $link = Join-Path $root 'build/broken'; $destination = Join-Path $root 'broken-destination'; [IO.Directory]::CreateDirectory($destination) | Out-Null }
            if ($kind -eq 'lock') { $link = Join-Path $root '.test-launcher.lock' }
            if ($kind -eq 'run') {
                foreach ($path in @('saves/world/level.dat', 'options.txt', 'config/settings.toml', 'screenshots/keep.png')) {
                    Write-Text (Join-Path $destination $path) 'PRESERVAR'
                }
            }
            if (Test-Path -LiteralPath $link) { Remove-Fixture $link }
            [IO.Directory]::CreateDirectory((Split-Path -Parent $link)) | Out-Null
            # Creation only: cmd's mklink accepts literal [] paths, unlike PS5 New-Item -Target.
            & cmd.exe /d /c "mklink /J `"$link`" `"$destination`"" | Out-Null
            Assert-True ($LASTEXITCODE -eq 0) 'Junction fixture creation failed'
            if ($kind -eq 'broken') { [IO.Directory]::Delete($destination) }
            if ($kind -ne 'lock') {
                $dry = Invoke-Launcher $root @('-DryRun')
                Assert-True ($dry.Code -ne 0) 'DryRun accepted unsafe link'
            }
            $result = Invoke-Launcher $root @('-BuildOnly')
            Assert-True ($result.Code -ne 0) 'Execution accepted unsafe link'
            Assert-True (!(Test-Path -LiteralPath (Join-Path $root 'calls.txt'))) 'Wrapper ran with unsafe links'
            if ($kind -ne 'build') { Assert-True (Test-Path -LiteralPath (Join-Path $root 'build/old.bin')) 'Validation partially deleted build' }
            Assert-Preserved $root
        }
    }
    Run-Case 'A regular file cannot be treated as a generated directory' {
        $root = New-Fixture; Remove-Fixture (Join-Path $root 'build'); Write-Text (Join-Path $root 'build') 'PRESERVAR'
        $result = Invoke-Launcher $root @('-BuildOnly')
        Assert-True ($result.Code -ne 0 -and [IO.File]::ReadAllText((Join-Path $root 'build')) -eq 'PRESERVAR') 'Unexpected file removed'
        Assert-Preserved $root
    }
    Run-Case 'Missing wrapper prevents all cleanup' {
        $root = New-Fixture; Remove-Fixture (Join-Path $root 'gradlew.bat')
        $result = Invoke-Launcher $root @('-BuildOnly')
        Assert-True ($result.Code -ne 0 -and (Test-Path -LiteralPath (Join-Path $root 'build/old.bin'))) 'Incomplete project cleaned'
        Assert-Preserved $root
    }
    Run-Case 'Exclusive lock prevents a second launcher from cleaning an active build' {
        $root = New-Fixture; Write-Text (Join-Path $root 'mode.txt') 'hold-build'
        # .NET avoids PS5 wildcard resolution of redirection filenames containing [].
        $start = New-Object System.Diagnostics.ProcessStartInfo
        $start.FileName = (Get-Command powershell.exe).Source
        $start.Arguments = '-NoProfile -ExecutionPolicy Bypass -File "' + (Join-Path $root 'tools/iniciar-teste.ps1') + '" -BuildOnly'
        $start.WorkingDirectory = $root
        $start.UseShellExecute = $false
        $start.CreateNoWindow = $true
        $start.RedirectStandardOutput = $true
        $start.RedirectStandardError = $true
        $process = New-Object System.Diagnostics.Process
        $process.StartInfo = $start
        Assert-True ($process.Start()) 'Could not start concurrency fixture'
        $processes.Add($process)
        try {
            Wait-File (Join-Path $root 'build-ready')
            $result = Invoke-Launcher $root @('-BuildOnly')
            Assert-True ($result.Code -ne 0 -and $result.Output -match 'Outro inicializador') 'Second launcher not rejected'
            Assert-True (Test-Path -LiteralPath (Join-Path $root 'build/new.jar')) 'Second launcher erased active build'
            Assert-True (@(Get-Content -LiteralPath (Join-Path $root 'calls.txt')).Count -eq 1) 'Second launcher ran wrapper'
        } finally {
            Write-Text (Join-Path $root 'release-build') 'RELEASE'
            Assert-True ($process.WaitForExit(15000)) 'First launcher did not exit'
        }
        Assert-True ($process.ExitCode -eq 0) ('First launcher failed: ' + $process.StandardError.ReadToEnd())
        $process.StandardOutput.ReadToEnd() | Out-Null
        Assert-Preserved $root
    }
    Run-Case 'A locked output aborts safely' {
        $root = New-Fixture
        $handle = [IO.File]::Open((Join-Path $root 'build/old.bin'), 'Open', 'ReadWrite', 'None')
        try {
            $result = Invoke-Launcher $root @('-BuildOnly')
            Assert-True ($result.Code -ne 0) 'Locked output returned success'
            Assert-True (!(Test-Path -LiteralPath (Join-Path $root 'calls.txt'))) 'Wrapper ran after cleanup failure'
        } finally { $handle.Dispose() }
        Assert-Preserved $root
    }
    Run-Case 'Final cleanup failure returns error; a later start recovers the cache' {
        $root = New-Fixture; Write-Text (Join-Path $root 'mode.txt') 'cleanup-fail'
        try {
            $result = Invoke-Launcher $root @('-BuildOnly')
            Assert-True ($result.Code -ne 0 -and $result.Output -match 'Cache temporario nao removido') 'Final cleanup falsely reported success'
            Assert-True (Test-Path -LiteralPath (Join-Path $root '.test-launcher-cache/new.cache')) 'Expected locked cache missing'
        } finally {
            Write-Text (Join-Path $root 'release-cache') 'RELEASE'
            $helper = Get-Process -Id ([int][IO.File]::ReadAllText((Join-Path $root 'helper.pid'))) -ErrorAction SilentlyContinue
            if ($helper) { Assert-True ($helper.WaitForExit(10000)) 'Cache helper did not exit' }
        }
        Write-Text (Join-Path $root 'mode.txt') 'success'
        $next = Invoke-Launcher $root @('-BuildOnly')
        Assert-True ($next.Code -eq 0 -and !(Test-Path -LiteralPath (Join-Path $root '.test-launcher-cache'))) 'Next start failed to recover cache'
        Assert-Preserved $root
    }
} finally {
    foreach ($process in $processes) { if (!$process.HasExited) { $process.WaitForExit(30000) | Out-Null } }
    if (Test-Path -LiteralPath $fixtureRoot) { Remove-Fixture $fixtureRoot }
}
$report = @{ Scenarios = $results.Count; Failed = @($results | Where-Object { !$_.Passed }).Count; FixtureRemoved = !(Test-Path -LiteralPath $fixtureRoot); Results = @($results.ToArray()) }
if ($ReportPath) { Write-Text ([IO.Path]::GetFullPath($ReportPath)) ($report | ConvertTo-Json -Depth 5) }
Write-Host "Scenarios: $($report.Scenarios); Failed: $($report.Failed); temporary fixtures removed: $($report.FixtureRemoved)"
if ($report.Failed -gt 0 -or !$report.FixtureRemoved) { exit 1 }
exit 0
