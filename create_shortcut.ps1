$WshShell = New-Object -ComObject WScript.Shell

# Identify desktop directories (both default and OneDrive redirected)
$desktopDirs = @()
$envDesktop = [System.Environment]::GetFolderPath('Desktop')
if ($envDesktop -and (Test-Path $envDesktop)) { $desktopDirs += $envDesktop }

$userProfile = [System.Environment]::GetFolderPath('UserProfile')
$localDesktop = Join-Path $userProfile "Desktop"
if ($localDesktop -and (Test-Path $localDesktop) -and ($desktopDirs -notcontains $localDesktop)) {
    $desktopDirs += $localDesktop
}

# Locate target executable
$exeCandidates = @(
    "dist\CoffeeNiDawgz\CoffeeNiDawgzV2.exe",
    "dist\CoffeeNiDawgz\CoffeeNiDawgz.exe",
    "CoffeeNiDawgzV2.exe",
    "CoffeeNiDawgz.exe"
)

$targetExe = $null
foreach ($cand in $exeCandidates) {
    if (Test-Path $cand) {
        $targetExe = (Get-Item $cand).FullName
        break
    }
}

if (-not $targetExe) {
    Write-Error "Could not locate application executable in dist\CoffeeNiDawgz or root."
    exit 1
}

$workingDir = (Split-Path -Path $targetExe -Parent)
$iconPath = if (Test-Path "app.ico") { (Get-Item "app.ico").FullName } else { $targetExe }

# Shortcut names to register/update
$shortcutNames = @(
    "Coffee ni Dawgz V2.lnk",
    "Coffee ni Dawgz POS.lnk"
)

foreach ($dir in $desktopDirs) {
    foreach ($scName in $shortcutNames) {
        $scPath = Join-Path $dir $scName
        $shortcut = $WshShell.CreateShortcut($scPath)
        $shortcut.TargetPath = $targetExe
        $shortcut.WorkingDirectory = $workingDir
        $shortcut.IconLocation = "$iconPath,0"
        $shortcut.Description = "Coffee ni Dawgz V2 POS System"
        $shortcut.Save()
        Write-Host "Desktop shortcut created/updated at: $scPath" -ForegroundColor Green
    }
}
