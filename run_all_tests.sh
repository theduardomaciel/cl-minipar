#!/bin/bash

# Script de Testes Automatizados - Interpretador MiniPar
# Executa todos os testes com entradas padrão e valida as saídas

set -e

WORKSPACE="$(cd "$(dirname "$0")" && pwd)"
cd "$WORKSPACE"

echo "======================================================================"
echo "  TESTES AUTOMATIZADOS - INTERPRETADOR MINIPAR"
echo "======================================================================"
echo ""

# Compilar o projeto
echo "[1/8] Compilando o projeto..."
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

# Teste 1: Calculadora Simples
if run_test "2" "Calculadora Cliente-Servidor" "tests/teste1_calculadora_corrigido.minipar" "+\n10\n5\n" "Resultado:"; then
    ((PASSED++))
else
    ((FAILED++))
fi
echo ""

# Teste 2: Fatorial e Fibonacci
if run_test "3" "Fatorial e Fibonacci (PAR)" "tests/teste2_fatorial_fibonacci.minipar" "5\n10\n" "Fatorial"; then
    ((PASSED++))
else
    ((FAILED++))
fi
echo ""

# Teste 3: Neurônio
if run_test "4" "Neurônio com Ativação" "tests/teste3_neuronio.minipar" "" "Saída do neurônio"; then
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
if run_test "6" "Sistema de Recomendação" "tests/teste5_recomendacao.minipar" "" "Recomendações para"; then
    ((PASSED++))
else
    ((FAILED++))
fi
echo ""

# Teste 6: QuickSort
if run_test "7" "QuickSort (Recursão)" "tests/teste6_quicksort.minipar" "" "Array ordenado"; then
    ((PASSED++))
else
    ((FAILED++))
fi
echo ""

# Teste 7: Calculadora Simples (versão simples)
if run_test "8" "Calculadora (tests_simple)" "tests_simple/teste1_calculadora.minipar" "+\n15\n3\n" "Resultado:"; then
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
echo "  Total:              $((PASSED + FAILED))"
echo "======================================================================"

if [ $FAILED -eq 0 ]; then
    echo "  🎉 TODOS OS TESTES PASSARAM!"
    exit 0
else
    echo "  ⚠️  ALGUNS TESTES FALHARAM"
    exit 1
fi
