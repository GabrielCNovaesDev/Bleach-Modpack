@echo off
setlocal
cd /d "%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0tools\iniciar-teste.ps1" %*
set "result=%ERRORLEVEL%"
if not "%result%"=="0" echo Falha ao iniciar. Consulte a mensagem acima.
pause
exit /b %result%
