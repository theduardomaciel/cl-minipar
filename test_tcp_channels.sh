#!/bin/bash

# Script para testar TCPChannel
# Compila e executa servidor e cliente em terminais separados

WORKSPACE="$(cd "$(dirname "$0")" && pwd)"
cd "$WORKSPACE"

echo "======================================================================"
echo "  TESTE DE CANAIS TCP - TCPChannel"
echo "======================================================================"
echo ""

# Compilar
echo "[1/3] Compilando TCPChannel e exemplos..."
javac -d out src/parser/TCPChannel.java 2>&1
if [ $? -ne 0 ]; then
    echo "✗ Erro ao compilar TCPChannel"
    exit 1
fi

javac -d out -cp out examples/TCPServerExample.java examples/TCPClientExample.java 2>&1
if [ $? -ne 0 ]; then
    echo "✗ Erro ao compilar exemplos"
    exit 1
fi

echo "✓ Compilação concluída"
echo ""

# Instruções
echo "[2/3] Instruções de uso:"
echo ""
echo "  Para testar os canais TCP, abra DOIS terminais:"
echo ""
echo "  Terminal 1 (Servidor):"
echo "  $ java -cp out examples.TCPServerExample"
echo ""
echo "  Terminal 2 (Cliente):"
echo "  $ java -cp out examples.TCPClientExample"
echo ""
echo "  O servidor aguardará conexão. Execute o cliente no segundo terminal."
echo "======================================================================"
echo ""

# Opção: executar automaticamente em background
read -p "Deseja executar o teste automaticamente? (s/n): " resposta

if [ "$resposta" = "s" ] || [ "$resposta" = "S" ]; then
    echo ""
    echo "[3/3] Executando teste automático..."
    echo ""
    
    # Inicia servidor em background
    echo "→ Iniciando servidor..."
    java -cp out examples.TCPServerExample &
    SERVER_PID=$!
    
    # Aguarda servidor inicializar
    sleep 2
    
    # Inicia cliente
    echo "→ Iniciando cliente..."
    java -cp out examples.TCPClientExample
    
    # Aguarda servidor finalizar
    wait $SERVER_PID
    
    echo ""
    echo "✓ Teste concluído!"
else
    echo "[3/3] Execute manualmente conforme as instruções acima."
fi
