@echo off
title ChaProde - Testing Tabla de Posiciones
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0test_leaderboard.ps1"
pause
