@echo off
title COFFEE NI DAWGZ POS System
echo Launching Coffee ni Dawgz POS...
if exist "%~dp0dist\CoffeeNiDawgz\CoffeeNiDawgzV2.exe" (
    start "" "%~dp0dist\CoffeeNiDawgz\CoffeeNiDawgzV2.exe"
) else if exist "%~dp0dist\CoffeeNiDawgz\CoffeeNiDawgz.exe" (
    start "" "%~dp0dist\CoffeeNiDawgz\CoffeeNiDawgz.exe"
) else (
    start "" javaw -jar "%~dp0CoffeeNiDawgz.jar"
)
