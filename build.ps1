# PowerShell build script for Coffee ni Dawgz POS

$ErrorActionPreference = "Stop"

# Terminate any running instances to prevent file lock errors
Get-Process -Name CoffeeNiDawgz -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Get-Process -Name java -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Milliseconds 500

$jdkPath = "C:\Users\User\.jdks\graalvm-jdk-21.0.7\bin"
$javac = "$jdkPath\javac.exe"
$jar = "$jdkPath\jar.exe"

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

Write-Host "Compiling Java source files..." -ForegroundColor Cyan

$javaFiles = Get-ChildItem -Path "src" -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName
$classpath = "lib/*;src"

& $javac -cp $classpath -d bin $javaFiles

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful!" -ForegroundColor Green

    # Re-package CoffeeNiDawgz.jar & sync executable bundle
    Write-Host "Updating CoffeeNiDawgz.jar & executable bundle..." -ForegroundColor Cyan
    $tempDir = "build_temp"
    if (Test-Path $tempDir) { Remove-Item -Recurse -Force $tempDir }
    New-Item -ItemType Directory -Path $tempDir | Out-Null

    Copy-Item -Recurse -Force "bin\*" $tempDir
    if (-not (Test-Path "$tempDir\resources")) { New-Item -ItemType Directory -Path "$tempDir\resources" | Out-Null }
    if (Test-Path "logo.jpg") {
        Copy-Item "logo.jpg" "$tempDir\resources\logo.jpg" -Force
        Copy-Item "logo.jpg" "$tempDir\logo.jpg" -Force
    }
    if (Test-Path "images") {
        if (-not (Test-Path "$tempDir\images")) { New-Item -ItemType Directory -Path "$tempDir\images" | Out-Null }
        Copy-Item -Recurse -Force "images\*" "$tempDir\images"
    }

    $libJars = Get-ChildItem -Path "lib" -Filter "*.jar"
    foreach ($lj in $libJars) {
        Push-Location $tempDir
        & $jar xf "$($lj.FullName)"
        Pop-Location
    }

    Get-ChildItem -Path "$tempDir\META-INF" -Filter "*.SF" -ErrorAction SilentlyContinue | Remove-Item -Force
    Get-ChildItem -Path "$tempDir\META-INF" -Filter "*.DSA" -ErrorAction SilentlyContinue | Remove-Item -Force
    Get-ChildItem -Path "$tempDir\META-INF" -Filter "*.RSA" -ErrorAction SilentlyContinue | Remove-Item -Force

    $manifestContent = "Manifest-Version: 1.0`r`nMain-Class: com.coffeenidawgz.Main`r`n"
    Set-Content -Path "Manifest.txt" -Value $manifestContent -Encoding ASCII

    & $jar cfm "CoffeeNiDawgz.jar" "Manifest.txt" -C $tempDir .
    Remove-Item -Recurse -Force $tempDir
    Remove-Item -Force "Manifest.txt"

    # Sync to dist/CoffeeNiDawgz/app/ if dist exists
    if (Test-Path "dist\CoffeeNiDawgz\app") {
        Copy-Item "CoffeeNiDawgz.jar" "dist\CoffeeNiDawgz\app\CoffeeNiDawgz.jar" -Force
    }

    # Refresh Desktop Shortcut
    if (Test-Path "create_shortcut.ps1") {
        & powershell -ExecutionPolicy Bypass -File .\create_shortcut.ps1
    }

    Write-Host "Build & desktop shortcut refresh completed successfully!" -ForegroundColor Green
} else {
    Write-Host "Compilation failed with code $LASTEXITCODE" -ForegroundColor Red
}
