# Programas de Teste - MiniPar

Este diretório contém os programas de teste escritos na linguagem MiniPar conforme especificado no documento ESPECIFICACOES.md.

## Arquivos de Teste

### 1. teste1_calculadora.minipar
**Calculadora Cliente-Servidor**
- Demonstra comunicação entre processos usando canais (c_channel)
- Cliente solicita operação aritmética (+, -, *, /)
- Servidor executa o cálculo e retorna o resultado
- Usa blocos SEQ para execução sequencial

### 2. teste2_fatorial_fibonacci.minipar
**Execução Paralela: Fatorial e Fibonacci**
- Demonstra execução paralela usando blocos PAR
- Thread 1: Calcula o fatorial de um número
- Thread 2: Calcula a série de Fibonacci
- Ambas as threads executam simultaneamente

### 3. teste3_neuronio.minipar
**Neurônio Simples (Perceptron)**
- Implementa um neurônio artificial que aprende
- Usa algoritmo de perceptron
- Ajusta pesos com base no erro
- Demonstra classes, objetos e loops

### 4. teste4_rede_neural_xor.minipar
**Rede Neural para XOR**
- Implementa uma rede neural com camada oculta
- 3 neurônios na camada oculta
- Aprende a função lógica XOR
- Usa feedforward e aproximação de sigmoid

### 5. teste5_recomendacao.minipar
**Sistema de Recomendação E-commerce**
- Sistema de recomendação baseado em histórico de compras
- Usa rede neural para prever preferências
- Codifica histórico de compras em vetor binário
- Recomenda produtos com base em probabilidades

### 6. teste6_quicksort.minipar
**Ordenação QuickSort**
- Implementa o algoritmo QuickSort
- Demonstra recursão
- Ordena array de números (incluindo negativos)
- Interface interativa para entrada de dados

## Características da Sintaxe MiniPar

### Baseada em Java/C-like
- Declaração de tipos: `number`, `string`, `bool`, `list`
- Construtores explícitos (não `__init__` do Python)
- Blocos delimitados por chaves `{}`
- Ponto e vírgula `;` no final de statements
- Acesso a membros com `.` (dot notation)

### Orientação a Objetos
```minipar
class NomeClasse {
    # Atributos
    number atributo;
    
    # Construtor
    NomeClasse(number param) {
        this.atributo = param;
    }
    
    # Métodos
    number metodo() {
        return this.atributo;
    }
}
```

### Blocos de Controle
- `if (condicao) { } else { }`
- `while (condicao) { }`
- `for (tipo var in lista) { }`

### Blocos SEQ e PAR
```minipar
seq {
    # Execução sequencial
}

par {
    # Execução paralela (threads)
}
```

### Canais de Comunicação
```minipar
c_channel nome_canal computador1 computador2;
canal.send(dados);
canal.receive(dados);
```

## Diferenças em relação ao Python

1. **Tipos Explícitos**: Variáveis devem ter tipo declarado
2. **Construtores**: Não usa `__init__`, mas método com nome da classe
3. **Self vs This**: Usa `this` em vez de `self`
4. **Sintaxe de Blocos**: Chaves `{}` em vez de indentação
5. **Declaração de Listas**: `list nome = []` em vez de `nome = []`
6. **Boolean**: `bool` e valores `true`/`false` (minúsculos)

## Como Usar

Para testar o Parser e Lexer com estes arquivos:

```bash
# Compile o projeto
javac -d bin src/**/*.java

# Execute o interpretador com um arquivo de teste
java -cp bin Main tests/teste6_quicksort.minipar
```