#!/bin/bash

# Script para compilar e executar o servidor web do MiniPar

echo "=========================================="
echo "  MiniPar Web Server - Build & Run"
echo "=========================================="
echo ""

# Diretório do projeto
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_DIR"

# Criar diretório de build se não existir
mkdir -p build

echo "Compilando classes Java..."
javac -d build -sourcepath src src/server/WebServer.java src/Main.java \
    src/lexer/*.java \
    src/parser/*.java \
    src/interpreter/*.java

if [ $? -ne 0 ]; then
    echo "Erro na compilacao!"
    exit 1
fi

echo "Compilacao concluida com sucesso!"
echo ""
echo "Iniciando servidor web..."
echo ""

# Executar o servidor (permanecer na raiz do projeto)
java -cp build WebServer

# Aguardar o servidor encerrar
echo ""
echo "Servidor encerrado."
