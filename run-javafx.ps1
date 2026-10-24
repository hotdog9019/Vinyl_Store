$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$mavenHome = Join-Path $projectRoot ".tools\apache-maven-3.9.16"
$maven = Join-Path $mavenHome "bin\mvn.cmd"

$jetBrainsJdks = @()
foreach ($dir in @("C:\Program Files\JetBrains", "$env:LOCALAPPDATA\Programs")) {
    if (Test-Path $dir) {
        $jetBrainsJdks += Get-ChildItem -Path $dir -Directory -Recurse -ErrorAction SilentlyContinue |
                Where-Object { $_.Name -eq "jbr" -and (Test-Path (Join-Path $_.FullName "bin\java.exe")) } |
                Select-Object -ExpandProperty FullName
    }
}

$javaHomeCandidates = @($env:JAVA_HOME) + $jetBrainsJdks |
        Where-Object { $_ -and (Test-Path (Join-Path $_ "bin\java.exe")) } |
        Select-Object -Unique

if (-not (Test-Path $maven)) {
    throw "Maven не найден: $maven"
}

if (-not $javaHomeCandidates) {
    throw "JDK не найден. Установите Java 21 или укажите JAVA_HOME."
}

$env:JAVA_HOME = $javaHomeCandidates[0]
$env:Path = "$env:JAVA_HOME\bin;$mavenHome\bin;$env:Path"

& $maven javafx:run
