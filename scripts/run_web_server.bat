@echo off
chcp 65001 >nul
REM Script para compilar e executar o servidor web do MiniPar no Windows

echo ==========================================
echo   MiniPar Web Server - Build e Run
echo ==========================================
echo.

REM Diretorio do projeto
cd /d "%~dp0\.."

REM Criar diretorio de build se nao existir
if not exist build mkdir build

echo [1/2] Compilando classes Java...
javac -encoding UTF-8 -d build -sourcepath src src/server/WebServer.java src/Main.java src/lexer/*.java src/parser/*.java src/interpreter/*.java

if %ERRORLEVEL% neq 0 (
    echo [ERRO] Falha na compilacao!
    pause
    exit /b 1
)

echo [OK] Compilacao concluida com sucesso!
echo.
echo [2/2] Iniciando servidor web...
echo.
echo Diretorio de trabalho: %CD%
echo.

REM Executar o servidor (manter na raiz do projeto)
cd /d "%~dp0\.."
java -cp build server.WebServer

echo.
echo Servidor encerrado.
pause
