$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$mavenHome = Join-Path $projectRoot ".tools\apache-maven-3.9.16"
$maven = Join-Path $mavenHome "bin\mvn.cmd"

if (-not (Test-Path $maven)) {
    throw "Maven не найден: $maven"
}

if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME "bin\java.exe"))) {
    $jbr = Get-ChildItem -Path "C:\Program Files\JetBrains" -Directory -Recurse -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -eq "jbr" -and (Test-Path (Join-Path $_.FullName "bin\java.exe")) } |
            Select-Object -First 1 -ExpandProperty FullName
    if (-not $jbr) {
        throw "JAVA_HOME не задан и JDK из IntelliJ не найден."
    }
    $env:JAVA_HOME = $jbr
}

$env:Path = "$env:JAVA_HOME\bin;$mavenHome\bin;$env:Path"
& $maven @args
