# PowerShell build & package script for Coffee ni Dawgz POS into .exe

$ErrorActionPreference = "Stop"

# Stop any running process instance
Get-Process -Name CoffeeNiDawgz -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Milliseconds 500

$jdkPath = "C:\Users\User\.jdks\graalvm-jdk-21.0.7\bin"
$javac = "$jdkPath\javac.exe"
$java = "$jdkPath\java.exe"
$jar = "$jdkPath\jar.exe"
$jpackage = "$jdkPath\jpackage.exe"
$csc = "C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe"

Write-Host "========================================" -ForegroundColor Yellow
Write-Host "   Building Coffee ni Dawgz POS .EXE   " -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow

# 1. Compile Java Source Files
Write-Host "[1/5] Compiling Java source files..." -ForegroundColor Cyan
if (-not (Test-Path "bin")) { New-Item -ItemType Directory -Path "bin" | Out-Null }
$javaFiles = Get-ChildItem -Path "src" -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName
& $javac -cp "lib/*;src" -d bin $javaFiles

if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed!" -ForegroundColor Red
    exit 1
}
Write-Host "-> Java compilation successful." -ForegroundColor Green

# 2. Generate Application Icon (.ico)
Write-Host "[2/5] Generating Golden Retriever application icon (app.ico)..." -ForegroundColor Cyan
& $java -cp "bin;lib/*" com.coffeenidawgz.utils.IconGenerator app.ico
if (Test-Path "app.ico") {
    Write-Host "-> Icon generated: app.ico" -ForegroundColor Green
} else {
    Write-Host "-> Icon generation warning, proceeding without icon." -ForegroundColor Yellow
}

# 3. Create Fat Executable JAR (CoffeeNiDawgz.jar) including ALL lib dependencies & resources
Write-Host "[3/5] Creating self-contained CoffeeNiDawgz.jar..." -ForegroundColor Cyan
$tempDir = "build_temp"
if (Test-Path $tempDir) { Remove-Item -Recurse -Force $tempDir }
New-Item -ItemType Directory -Path $tempDir | Out-Null

# Copy bin classes
Copy-Item -Recurse -Force "bin\*" $tempDir

# Copy logo resources & images directory
if (-not (Test-Path "$tempDir\resources")) { New-Item -ItemType Directory -Path "$tempDir\resources" | Out-Null }
if (Test-Path "logo.jpg") {
    Copy-Item "logo.jpg" "$tempDir\resources\logo.jpg" -Force
    Copy-Item "logo.jpg" "$tempDir\logo.jpg" -Force
}
if (Test-Path "images") {
    if (-not (Test-Path "$tempDir\images")) { New-Item -ItemType Directory -Path "$tempDir\images" | Out-Null }
    Copy-Item -Recurse -Force "images\*" "$tempDir\images"
}

# Extract all JAR files in lib/ into tempDir
$libJars = Get-ChildItem -Path "lib" -Filter "*.jar"
foreach ($lj in $libJars) {
    Push-Location $tempDir
    & $jar xf "$($lj.FullName)"
    Pop-Location
}

# Clean META-INF signatures
Get-ChildItem -Path "$tempDir\META-INF" -Filter "*.SF" -ErrorAction SilentlyContinue | Remove-Item -Force
Get-ChildItem -Path "$tempDir\META-INF" -Filter "*.DSA" -ErrorAction SilentlyContinue | Remove-Item -Force
Get-ChildItem -Path "$tempDir\META-INF" -Filter "*.RSA" -ErrorAction SilentlyContinue | Remove-Item -Force

# Manifest file
$manifestContent = "Manifest-Version: 1.0`r`nMain-Class: com.coffeenidawgz.Main`r`n"
Set-Content -Path "Manifest.txt" -Value $manifestContent -Encoding ASCII

# Create JAR
& $jar cfm "CoffeeNiDawgz.jar" "Manifest.txt" -C $tempDir .
Remove-Item -Recurse -Force $tempDir
Remove-Item -Force "Manifest.txt"
Write-Host "-> Executable JAR created: CoffeeNiDawgz.jar" -ForegroundColor Green

# 4. Compile Native Windows .EXE Launcher with embedded Icon (CoffeeNiDawgz.exe)
Write-Host "[4/5] Compiling native Windows executable launcher (CoffeeNiDawgz.exe)..." -ForegroundColor Cyan

$csharpCode = @"
using System;
using System.Diagnostics;
using System.IO;
using System.Windows.Forms;

class Program {
    [STAThread]
    static void Main() {
        string appDir = AppDomain.CurrentDomain.BaseDirectory;
        string jarPath = Path.Combine(appDir, "CoffeeNiDawgz.jar");
        
        string javaExe = FindJavaPath();
        if (string.IsNullOrEmpty(javaExe)) {
            MessageBox.Show("Java 21 (JRE/JDK) was not found on this system.\n\nPlease install Java 21 or run dist/CoffeeNiDawgz/CoffeeNiDawgz.exe.", "Coffee ni Dawgz POS", MessageBoxButtons.OK, MessageBoxIcon.Error);
            return;
        }

        ProcessStartInfo psi = new ProcessStartInfo();
        psi.FileName = javaExe;
        if (File.Exists(jarPath)) {
            psi.Arguments = "-jar \"" + jarPath + "\"";
        } else {
            psi.Arguments = "-cp \"bin;lib/*\" com.coffeenidawgz.Main";
        }
        psi.WorkingDirectory = appDir;
        psi.UseShellExecute = false;
        psi.CreateNoWindow = true;

        try {
            Process.Start(psi);
        } catch (Exception ex) {
            MessageBox.Show("Failed to launch application:\n" + ex.Message, "Coffee ni Dawgz POS Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
        }
    }

    static string FindJavaPath() {
        // 1. Check relative runtime/bin or jre/bin
        string[] localPaths = new string[] {
            Path.Combine(AppDomain.CurrentDomain.BaseDirectory, @"runtime\bin\javaw.exe"),
            Path.Combine(AppDomain.CurrentDomain.BaseDirectory, @"jre\bin\javaw.exe"),
            @"C:\Users\User\.jdks\graalvm-jdk-21.0.7\bin\javaw.exe",
            @"C:\Users\User\.jdks\graalvm-jdk-21.0.7\bin\java.exe"
        };
        foreach (var path in localPaths) {
            if (File.Exists(path)) return path;
        }

        // 2. Check JAVA_HOME
        string javaHome = Environment.GetEnvironmentVariable("JAVA_HOME");
        if (!string.IsNullOrEmpty(javaHome)) {
            string javaw = Path.Combine(javaHome, @"bin\javaw.exe");
            if (File.Exists(javaw)) return javaw;
            string java = Path.Combine(javaHome, @"bin\java.exe");
            if (File.Exists(java)) return java;
        }

        // 3. Fallback to system javaw
        return "javaw.exe";
    }
}
"@

$csFile = "Launcher.cs"
Set-Content -Path $csFile -Value $csharpCode -Encoding UTF8

$win32IconOpt = if (Test-Path "app.ico") { "/win32icon:app.ico" } else { "" }
& $csc /target:winexe $win32IconOpt /out:CoffeeNiDawgz.exe /r:System.Windows.Forms.dll $csFile
Remove-Item -Force $csFile
Write-Host "-> Native Windows executable with logo created: CoffeeNiDawgz.exe" -ForegroundColor Green

# 5. Generate jpackage Portable Bundle with Icon (dist/CoffeeNiDawgz/)
Write-Host "[5/5] Generating standalone app-image using jpackage..." -ForegroundColor Cyan
if (Test-Path "dist") { Remove-Item -Recurse -Force "dist" }
New-Item -ItemType Directory -Path "dist" | Out-Null

$inputDir = "dist_input"
if (Test-Path $inputDir) { Remove-Item -Recurse -Force $inputDir }
New-Item -ItemType Directory -Path $inputDir | Out-Null
Copy-Item "CoffeeNiDawgz.jar" $inputDir
if (Test-Path "logo.jpg") { Copy-Item "logo.jpg" $inputDir }
if (Test-Path "images") { Copy-Item -Recurse -Force "images" $inputDir }

$iconParam = @()
if (Test-Path "app.ico") {
    $iconParam = @("--icon", "app.ico")
}

& $jpackage `
    --type app-image `
    --name "CoffeeNiDawgz" `
    --input $inputDir `
    --main-jar "CoffeeNiDawgz.jar" `
    --dest "dist" `
    --vendor "Coffee ni Dawgz" `
    --description "Offline Coffee Shop POS System" `
    @iconParam

Remove-Item -Recurse -Force $inputDir
Write-Host "-> Standalone package with embedded JRE generated at: dist/CoffeeNiDawgz/" -ForegroundColor Green

# 6. Create Desktop Shortcut with Official Emblem Logo
Write-Host "[6/6] Creating Desktop shortcut icon..." -ForegroundColor Cyan
try {
    $wsh = New-Object -ComObject WScript.Shell
    $desktopPath = [System.Environment]::GetFolderPath('Desktop')
    $shortcutFile = Join-Path $desktopPath "Coffee ni Dawgz POS.lnk"
    $shortcut = $wsh.CreateShortcut($shortcutFile)
    $shortcut.TargetPath = (Get-Item "CoffeeNiDawgz.exe").FullName
    $shortcut.WorkingDirectory = (Get-Item ".").FullName
    if (Test-Path "app.ico") {
        $shortcut.IconLocation = (Get-Item "app.ico").FullName
    }
    $shortcut.Description = "Coffee ni Dawgz POS System"
    $shortcut.Save()
    Write-Host "-> Desktop shortcut created with logo icon at: $shortcutFile" -ForegroundColor Green
} catch {
    Write-Host "-> Desktop shortcut creation skipped." -ForegroundColor Yellow
}

Write-Host "========================================" -ForegroundColor Yellow
Write-Host " Packaging Complete!" -ForegroundColor Green
Write-Host " Created Executables & Shortcuts:" -ForegroundColor Yellow
Write-Host " 1. CoffeeNiDawgz.exe (Root executable launcher with custom mascot icon)" -ForegroundColor White
Write-Host " 2. Desktop Shortcut: 'Coffee ni Dawgz POS' on Desktop" -ForegroundColor White
Write-Host " 3. CoffeeNiDawgz.jar (Self-contained Runnable JAR with SQLite & SLF4J)" -ForegroundColor White
Write-Host " 4. dist/CoffeeNiDawgz/CoffeeNiDawgz.exe (Bundled app with logo & embedded Java)" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Yellow
