#!/bin/bash

# Script de Testes Automatizados - Interpretador MiniPar
# Executa todos os testes com entradas padrão e valida as saídas

set -e

# Navegar para o diretório raiz do projeto (um nível acima de scripts)
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
WORKSPACE="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$WORKSPACE"

echo "======================================================================"
echo "  TESTES AUTOMATIZADOS - INTERPRETADOR MINIPAR"
echo "======================================================================"
echo ""

# Compilar o projeto
echo "[1/7] Compilando o projeto..."
javac -d out src/Main.java src/lexer/*.java src/parser/*.java src/interpreter/*.java 2>&1 > /dev/null
if [ $? -eq 0 ]; then
    echo "✓ Compilação concluída com sucesso"
else
    echo "✗ Erro na compilação"
    exit 1
fi
echo ""

# Função auxiliar para executar um teste
run_test() {
    local test_num=$1
    local test_name=$2
    local test_file=$3
    local input_data=$4
    local expected_pattern=$5
    
    echo "[${test_num}/8] Teste: ${test_name}"
    echo "  Arquivo: ${test_file}"
    
    # Executar o teste com entrada padrão
    local output
    if [ -n "$input_data" ]; then
        output=$(printf "%s" "$input_data" | java -cp out Main "$test_file" 2>&1)
    else
        output=$(java -cp out Main "$test_file" 2>&1)
    fi
    
    # Verificar se o padrão esperado está na saída
    if echo "$output" | grep -q "$expected_pattern"; then
        echo "  ✓ PASSOU (padrão encontrado: $expected_pattern)"
        return 0
    else
        echo "  ✗ FALHOU (padrão não encontrado: $expected_pattern)"
        echo "  Output (últimas 20 linhas):"
        echo "$output" | tail -20 | sed 's/^/    /'
        return 1
    fi
}

# Contador de testes
PASSED=0
FAILED=0

# Teste 1: Calculadora Cliente-Servidor (TCP)
echo "[2/7] Teste: Calculadora Cliente-Servidor (TCP)"
echo "  Arquivos: teste1_servidor.minipar + teste1_cliente.minipar"
echo "  ⚠️  PULADO - Requer execução manual em terminais separados"
echo "  Use: ./scripts/test_tcp_channels.sh para testar manualmente"
echo ""

# Teste 2: Fatorial e Fibonacci
if run_test "3" "Fatorial e Fibonacci (PAR)" "tests/teste2_fatorial_fibonacci.minipar" "" "Fatorial"; then
    ((PASSED++))
else
    ((FAILED++))
fi
echo ""

# Teste 3: Neurônio
if run_test "4" "Neurônio com Ativação" "tests/teste3_neuronio.minipar" "" "aprendeu"; then
    ((PASSED++))
else
    ((FAILED++))
fi
echo ""

# Teste 4: Rede Neural XOR
if run_test "5" "Rede Neural (XOR)" "tests/teste4_rede_neural_xor.minipar" "" "Treinamento concluído"; then
    ((PASSED++))
else
    ((FAILED++))
fi
echo ""

# Teste 5: Sistema de Recomendação
if run_test "6" "Sistema de Recomendação" "tests/teste5_recomendacao.minipar" "" "Produtos recomendados"; then
    ((PASSED++))
else
    ((FAILED++))
fi
echo ""

# Teste 6: QuickSort
if run_test "7" "QuickSort (Recursão)" "tests/teste6_quicksort.minipar" "" "Vetor ordenado"; then
    ((PASSED++))
else
    ((FAILED++))
fi
echo ""

# Resumo
echo "======================================================================"
echo "  RESUMO DOS TESTES"
echo "======================================================================"
echo "  ✓ Testes aprovados: $PASSED"
echo "  ✗ Testes falhados:  $FAILED"
echo "  ⚠️  Testes pulados:  1 (teste1 - TCP Cliente-Servidor)"
echo "  Total executados:   $((PASSED + FAILED))"
echo "======================================================================"

if [ $FAILED -eq 0 ]; then
    echo "  🎉 TODOS OS TESTES EXECUTADOS PASSARAM!"
    echo "  💡 Para testar TCP Cliente-Servidor: ./scripts/test_tcp_channels.sh"
    exit 0
else
    echo "  ⚠️  ALGUNS TESTES FALHARAM"
    exit 1
fi
