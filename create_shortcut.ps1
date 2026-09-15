$WshShell = New-Object -ComObject WScript.Shell
$DesktopPath = [System.Environment]::GetFolderPath('Desktop')
$ShortcutPath = Join-Path $DesktopPath "Coffee ni Dawgz POS.lnk"

$ExeFile = Get-Item "dist/CoffeeNiDawgz/CoffeeNiDawgz.exe"
$IconFile = Get-Item "app.ico"

$Shortcut = $WshShell.CreateShortcut($ShortcutPath)
$Shortcut.TargetPath = $ExeFile.FullName
$Shortcut.WorkingDirectory = $ExeFile.DirectoryName
$Shortcut.IconLocation = $IconFile.FullName
$Shortcut.Description = "Coffee ni Dawgz POS System"
$Shortcut.Save()

Write-Host "Desktop shortcut created successfully at: $ShortcutPath"
