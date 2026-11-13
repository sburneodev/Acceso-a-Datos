@echo off
REM Comprueba que este proyecto y tu equipo estan listos para trabajar.
REM Uso: doble clic, o  comprobar.bat  desde una terminal en esta carpeta.
REM No compila nada ni deja ficheros .class: Java ejecuta el fuente directamente.
cd /d "%~dp0"
java Comprobador.java
echo.
pause
