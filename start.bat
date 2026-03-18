@echo off
title LineupReporter Launcher
echo Starting LineupReporter...
start "LineupReporter" cmd /k "mvn spring-boot:run"
echo Waiting for application to start...
timeout /t 15 /nobreak >nul
echo Opening browser to http://localhost:8080/usage
start http://localhost:8080/usage
echo Done. The application is running in the other window. Close that window to stop the server.
