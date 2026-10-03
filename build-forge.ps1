$ErrorActionPreference = 'Stop'

$repoDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$preferredJavaHomes = @(
    "$env:USERPROFILE\.gradle\jdks\eclipse_adoptium-17-amd64-windows\jdk-17.0.20.1+1",
    'C:\Program Files\Eclipse Adoptium\jdk-17*',
    'C:\Program Files\Microsoft\jdk-17*',
    'C:\Program Files\Java\jdk-17*'
)

$javaHome = $null
foreach ($candidate in $preferredJavaHomes) {
    if ((Test-Path $candidate) -and (Test-Path (Join-Path $candidate 'bin\java.exe'))) {
        $javaHome = $candidate
        break
    }

    $match = Get-ChildItem -Path $candidate -Directory -ErrorAction SilentlyContinue |
        Sort-Object FullName -Descending |
        Select-Object -First 1

    if ($match -and (Test-Path (Join-Path $match.FullName 'bin\java.exe'))) {
        $javaHome = $match.FullName
        break
    }
}

if (-not $javaHome) {
    throw 'No Java 17 installation was found. Install Eclipse Temurin JDK 17, then run this script again.'
}

$env:JAVA_HOME = $javaHome
$env:Path = "$javaHome\bin;$env:Path"

Write-Host "Using JAVA_HOME=$javaHome"
& (Join-Path $repoDir 'gradlew.bat') --no-daemon build
exit $LASTEXITCODE

# .\build-forge.ps1 para rodar
# ./gradlew runClient