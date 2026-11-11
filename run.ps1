# run.ps1 - Compiles and runs the HR Management System Swing UI

$ErrorActionPreference = "Stop"

Write-Host "Compiling Java files..."
# Create a bin directory for compiled classes
if (-not (Test-Path -Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

# Find all java files
$javaFiles = Get-ChildItem -Path "src" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName

# Classpath with FlatLaf and MySQL Connector J
$libCp = "lib/flatlaf-3.4.jar;lib/mysql-connector-j-8.3.0.jar"

# Compile
javac -cp $libCp -d bin $javaFiles

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful. Launching..."
    $cp = "bin;$libCp"
    java -cp $cp hrms.Main
} else {
    Write-Host "Compilation failed." -ForegroundColor Red
}
