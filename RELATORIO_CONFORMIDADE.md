# Relatório de Conformidade - Interpretador MiniPar 2025.1

**Data:** 29 de outubro de 2025  
**Linguagem Escolhida:** Java  
**Versão das Especificações:** Tema 2 - ESPECIFICACOES_v2.md

---

## 📋 Sumário Executivo

Este relatório analisa a conformidade da implementação atual do interpretador MiniPar 2025.1 em relação às especificações fornecidas. A análise cobre os componentes de Analisador Léxico, Analisador Sintático e Gramática BNF.

### ✅ Status Geral

| Componente | Status | Conformidade |
|------------|--------|--------------|
| Analisador Léxico | 🟡 Parcial | ~85% |
| Analisador Sintático | 🟡 Parcial | ~80% |
| Gramática BNF | ✅ Completa | ~95% |
| AST (Árvore Sintática) | ✅ Implementada | ~90% |

---

## 1. Analisador Léxico (Lexer)

### ✅ Pontos Positivos

#### 1.1 Tokens Implementados Corretamente

**Palavras-chave OOP:**
- ✅ `class`, `extends`, `new`, `this`, `super`

**Palavras-chave de Controle:**
- ✅ `var`, `func`, `if`, `else`, `while`, `for`, `return`, `break`, `continue`, `in`

**Tipos de Dados:**
- ✅ `number`, `string`, `bool`, `void`, `list`, `dict`
- ✅ Literais booleanos: `true`, `false`

**Palavras-chave Específicas do MiniPar:**
- ✅ `seq`, `par`, `c_channel`, `s_channel`

**Operadores:**
- ✅ Aritméticos: `+`, `-`, `*`, `/`, `%`
- ✅ Relacionais: `==`, `!=`, `<`, `<=`, `>`, `>=`
- ✅ Lógicos: `&&`, `||`, `!`
- ✅ Atribuição: `=`
- ✅ Seta: `->`

**Delimitadores:**
- ✅ Parênteses: `(`, `)`
- ✅ Chaves: `{`, `}`
- ✅ Colchetes: `[`, `]`
- ✅ Outros: `,`, `.`, `:`, `;`

**Comentários:**
- ✅ Comentário de linha: `#` (estilo Python)
- ✅ Comentário de linha: `//` (estilo Java/C++)
- ✅ Comentário de bloco: `/* ... */`

**Literais:**
- ✅ Números (inteiros e decimais)
- ✅ Strings (entre aspas duplas)
- ✅ Identificadores (letras, dígitos, underscore)

### 🔴 Problemas Identificados

#### 1.2 Tokens Ausentes

**Palavras-chave de I/O:**
- ❌ `input` - Entrada de teclado (mencionada nas especificações)
- ❌ `output` ou `print` - Saída na tela (mencionada nas especificações)

**Operações de Comunicação:**
- ❌ `send` - Envio de dados via canal
- ❌ `receive` - Recepção de dados via canal

**Comandos Adicionais:**
- ❌ `do` - Loop do-while (mencionado como possível extensão)

**Observação:** Embora `send` e `receive` possam ser tratados como chamadas de método (ex: `calculadora.send(...)`), é recomendável adicionar tokens específicos para facilitar o parsing e a semântica de canais de comunicação.

### 📝 Recomendações para o Lexer

1. **Adicionar tokens de I/O:**
   ```java
   keywords.put("input", TokenType.INPUT);
   keywords.put("print", TokenType.PRINT);
   keywords.put("output", TokenType.OUTPUT);
   ```

2. **Adicionar tokens de comunicação:**
   ```java
   keywords.put("send", TokenType.SEND);
   keywords.put("receive", TokenType.RECEIVE);
   ```

3. **Adicionar token do:**
   ```java
   keywords.put("do", TokenType.DO);
   ```

---

## 2. Analisador Sintático (Parser)

### ✅ Pontos Positivos

#### 2.1 Produções Implementadas

**Declarações:**
- ✅ Declaração de classe com herança (`class X extends Y`)
- ✅ Declaração de método com parâmetros
- ✅ Declaração de função com tipo de retorno
- ✅ Declaração de variável com tipo e inicialização
- ✅ Declaração de canal (`c_channel`)

**Comandos:**
- ✅ If-else
- ✅ While
- ✅ Return
- ✅ Break
- ✅ Continue
- ✅ Blocos SEQ
- ✅ Blocos PAR

**Expressões:**
- ✅ Precedência de operadores correta:
  - Lógicos: OR (`||`) → AND (`&&`)
  - Relacionais: `==`, `!=`, `<`, `<=`, `>`, `>=`
  - Aritméticos: `+`, `-` → `*`, `/`, `%`
  - Unários: `!`, `-`
- ✅ Expressões binárias e unárias
- ✅ Chamadas de função e método
- ✅ Acesso a propriedades (`.`)
- ✅ Instanciação com `new`

**AST (Nós Implementados):**
- ✅ `ClassDecl`, `MethodDecl`, `FuncDecl`, `VarDecl`
- ✅ `Assignment`, `IfStmt`, `WhileStmt`, `ReturnStmt`
- ✅ `BinaryExpr`, `UnaryExpr`, `Literal`, `Identifier`
- ✅ `MethodCall`, `FunctionCall`, `NewInstance`
- ✅ `SeqBlock`, `ParBlock`, `BreakStmt`, `ContinueStmt`

### 🔴 Problemas Identificados

#### 2.2 Produções Ausentes ou Incompletas

**Comandos:**
- ❌ **Loop `for`**: Mencionado nas especificações mas não há implementação no parser
  - Produção esperada: `for (var x in lista) { ... }`
  
- ❌ **Loop `do-while`**: Mencionado como possível extensão

- ❌ **Print/Output**: Não há nó AST específico para comandos de saída
  - Atualmente pode ser tratado como `FunctionCall`, mas deveria ter semântica própria

- ❌ **Input**: Não há nó AST específico para comandos de entrada

**Operações de Canal:**
- ⚠️ **Send/Receive**: 
  - O parser reconhece `c_channel` para declaração
  - Mas não há parsing específico para `canal.send(...)` e `canal.receive(...)`
  - Atualmente são tratados como chamadas de método genéricas
  - **Problema:** Isso dificulta a análise semântica e geração de código para comunicação entre processos

**Declaração de Canal:**
- ⚠️ **Sintaxe atual vs. Especificação:**
  ```
  Especificação: c_channel calculadora computador_1 computador_2
  Implementação: c_channel(id1, id2, id3);
  ```
  - A sintaxe implementada usa parênteses, mas a especificação não mostra isso claramente
  - **Recomendação:** Verificar qual sintaxe será usada nos programas de teste

**Atributos de Classe:**
- ⚠️ **Parsing de atributos**: 
  - O parser lê atributos como `VarDecl` dentro de classes
  - Mas não há distinção semântica entre atributos e variáveis locais
  - **Observação da especificação:** "deve ser acrescentado um não terminal para <declaração de atributos> na produção <classe>"

**Construtores:**
- ❌ **Construtores de classe**: Não implementados
  - Programas de teste (Neurônio, RedeNeural) usam `__init__` em Python
  - Em Java seria necessário suportar construtores com parâmetros

**Expressões Complexas:**
- ⚠️ **Listas e Dicionários**: Tokens existem, mas não há parsing para literais de lista/dict
  - Exemplo: `[1, 2, 3]` ou `{"chave": "valor"}`

### 📝 Recomendações para o Parser

#### 2.3 Implementações Necessárias

**1. Adicionar suporte ao loop `for`:**

```java
// No método statement()
if (match(TokenType.FOR)) return forStatement();

// Novo método
private ForStmt forStatement() {
    consume(TokenType.LEFT_PAREN, "Esperado '(' após 'for'");
    VarDecl variable = varDeclaration();
    consume(TokenType.IN, "Esperado 'in' no loop for");
    ASTNode iterable = expression();
    consume(TokenType.RIGHT_PAREN, "Esperado ')' após iterável");
    List<ASTNode> body = block();
    return new ForStmt(variable, iterable, body);
}
```

**2. Adicionar nós AST para I/O:**

```java
// Em ASTNode.java
class PrintStmt extends ASTNode {
    public List<ASTNode> arguments;
    
    public PrintStmt(List<ASTNode> arguments) {
        this.arguments = arguments;
    }
}

class InputExpr extends ASTNode {
    public ASTNode prompt; // opcional
    
    public InputExpr(ASTNode prompt) {
        this.prompt = prompt;
    }
}
```

**3. Melhorar parsing de canais:**

```java
// Ajustar sintaxe conforme programas de teste
// Opção 1: c_channel nome comp1 comp2
private CanalDecl channelDeclaration() {
    Token canalName = consume(TokenType.ID, "Esperado nome do canal");
    Token comp1 = consume(TokenType.ID, "Esperado nome do computador 1");
    Token comp2 = consume(TokenType.ID, "Esperado nome do computador 2");
    return new CanalDecl(canalName.lexeme(), comp1.lexeme(), comp2.lexeme());
}
```

**4. Adicionar nós para Send/Receive:**

```java
class SendStmt extends ASTNode {
    public String channelName;
    public List<ASTNode> arguments;
}

class ReceiveStmt extends ASTNode {
    public String channelName;
    public List<ASTNode> parameters;
}
```

**5. Adicionar suporte a construtores:**

```java
class ConstructorDecl extends ASTNode {
    public String className;
    public List<Parameter> parameters;
    public List<ASTNode> body;
}
```

**6. Adicionar ForStmt ao ASTNode.java:**

```java
class ForStmt extends ASTNode {
    public VarDecl variable;
    public ASTNode iterable;
    public List<ASTNode> body;
    
    public ForStmt(VarDecl variable, ASTNode iterable, List<ASTNode> body) {
        this.variable = variable;
        this.iterable = iterable;
        this.body = body;
    }
}
```

---

## 3. Gramática BNF

### ✅ Pontos Positivos

A gramática em `GRAMATICA_BNF.txt` está **bem estruturada e completa**, cobrindo:

- ✅ Estrutura de programa
- ✅ Orientação a Objetos (classes, métodos, herança)
- ✅ Funções com parâmetros e tipos de retorno
- ✅ Variáveis tipadas
- ✅ Comandos de controle (if, while, for, return, break, continue)
- ✅ Blocos SEQ e PAR
- ✅ Expressões com precedência correta
- ✅ Tipos (primitivos e personalizados)
- ✅ Comentários (linha e bloco)
- ✅ Palavras reservadas listadas

### 🔴 Pontos de Atenção

#### 3.1 Produções que Precisam de Esclarecimento

**1. Canal de Comunicação:**

A gramática atual não especifica a sintaxe para:
- Declaração de canal: `c_channel calculadora computador_1 computador_2`
- Send: `calculadora.send(op, val1, val2, resultado)`
- Receive: `calculadora.receive(op, val1, val2, resultado)`

**Recomendação:** Adicionar à gramática:

```bnf
<declaracao_canal> ::= "c_channel" <identificador> <identificador> <identificador>

<comando_send> ::= <identificador> "." "send" "(" <argumentos> ")"

<comando_receive> ::= <identificador> "." "receive" "(" <parametros> ")"
```

**2. Comandos de I/O:**

```bnf
<comando_input> ::= "input" "(" [ <expressao> ] ")"

<comando_print> ::= "print" "(" <argumentos> ")"
```

**3. Construtores:**

```bnf
<declaracao_construtor> ::= <identificador> "(" <parametros> ")" "{" { <comando> } "}"

<membro_classe> ::= <declaracao_variavel>
                  | <declaracao_metodo>
                  | <declaracao_construtor>
```

**4. Literais de Lista e Dicionário:**

```bnf
<literal_lista> ::= "[" [ <expressao> { "," <expressao> } ] "]"

<literal_dict> ::= "{" [ <par_chave_valor> { "," <par_chave_valor> } ] "}"

<par_chave_valor> ::= <expressao> ":" <expressao>

<expr_primaria> ::= <numero>
                  | <string>
                  | "true"
                  | "false"
                  | <identificador>
                  | <instanciacao>
                  | <literal_lista>
                  | <literal_dict>
                  | "(" <expressao> ")"
```

**5. Indexação e Acesso a Elementos:**

```bnf
<expr_chamada> ::= <expr_primaria> 
                   { "(" [ <argumentos> ] ")" 
                   | "." <identificador>
                   | "[" <expressao> "]" }
```

---

## 4. Compatibilidade com Programas de Teste

### 📊 Análise por Programa

#### Programa 1: Cliente-Servidor Calculadora

**Requisitos:**
- Declaração de canal: `c_channel calculadora computador_1 computador_2`
- Bloco SEQ
- Print de opções
- Input de dados
- Send/Receive via canal

**Status Atual:**
- ✅ c_channel reconhecido
- ✅ SEQ implementado
- ❌ Print não tem token específico
- ❌ Input não implementado
- ⚠️ Send/Receive tratados como métodos genéricos

**Conformidade:** 🟡 60% - Precisa de I/O e operações de canal

---

#### Programa 2: Execução Paralela (Fatorial + Fibonacci)

**Requisitos:**
- Bloco PAR
- Funções para cálculo
- Execução com threads (implementação do interpretador)

**Status Atual:**
- ✅ PAR implementado
- ✅ Funções implementadas
- ⚠️ Threads (implementação pendente no interpretador)

**Conformidade:** ✅ 90% - Estrutura pronta, falta execução

---

#### Programa 3: Neurônio (Classe e Objetos)

**Requisitos (traduzido de Python para MiniPar):**
- Classe com atributos
- Construtor com parâmetros
- Métodos
- Instanciação
- Print formatado
- Loops while
- Condicionais

**Status Atual:**
- ✅ Classes implementadas
- ❌ Construtores com parâmetros (apenas `new Classe()`)
- ✅ Métodos implementados
- ✅ While implementado
- ✅ If implementado
- ❌ Print formatado

**Conformidade:** 🟡 70% - Falta construtor e I/O

---

#### Programa 4: Rede Neural XOR

**Requisitos adicionais:**
- Listas (para pesos, entradas)
- Funções matemáticas
- Múltiplas classes
- Iteração sobre listas (for...in)

**Status Atual:**
- ✅ Classes múltiplas
- ⚠️ Listas (token existe, parsing não)
- ❌ For...in não implementado
- ✅ Funções implementadas

**Conformidade:** 🟡 65% - Falta for e listas

---

## 5. Resumo de Ações Necessárias

### 🔴 Alta Prioridade (Essencial)

1. **Implementar loop `for`** - Usado em múltiplos testes
2. **Adicionar tokens e parsing para `print`/`input`** - I/O básico
3. **Implementar construtores com parâmetros** - Programas 3 e 4
4. **Definir e implementar sintaxe de Send/Receive** - Programa 1
5. **Parsing de literais de lista `[1, 2, 3]`** - Programa 4

### 🟡 Média Prioridade (Importante)

6. **Adicionar nós AST específicos para Send/Receive**
7. **Implementar indexação de listas `lista[i]`**
8. **Adicionar suporte a dicionários**
9. **Melhorar declaração de canal** (sintaxe conforme especificação)
10. **Atualizar gramática BNF** com todas as produções faltantes

### 🟢 Baixa Prioridade (Opcional)

11. **Implementar loop `do-while`**
12. **Adicionar funções matemáticas built-in** (exp, max, min, etc.)
13. **Suporte a strings formatadas** (f-strings ou equivalente)

---

## 6. Conclusão

### Pontos Fortes da Implementação

1. ✅ **Estrutura sólida de OOP** - Classes, herança, métodos bem implementados
2. ✅ **Precedência de operadores correta** - Expressões complexas funcionam
3. ✅ **Blocos SEQ/PAR reconhecidos** - Base para paralelismo
4. ✅ **Gramática BNF bem documentada** - Facilita desenvolvimento
5. ✅ **AST bem estruturada** - Boa base para semântica e interpretação

### Principais Lacunas

1. ❌ **I/O (input/print)** - Essencial para programas de teste
2. ❌ **Loop for** - Presente nas especificações e necessário
3. ❌ **Construtores parametrizados** - Classes não podem ser inicializadas corretamente
4. ⚠️ **Send/Receive** - Comunicação entre processos mal definida
5. ⚠️ **Listas e Dicionários** - Tipos declarados mas não utilizáveis

### Recomendação Final

A implementação está em **bom caminho** (≈80% de conformidade), mas precisa de:

1. **Curto prazo (1-2 semanas):**
   - Adicionar I/O (print/input)
   - Implementar loop for
   - Construtores com parâmetros

2. **Médio prazo (2-4 semanas):**
   - Parsing de listas e indexação
   - Operações Send/Receive específicas
   - Testes com programas 3 e 4

3. **Longo prazo (após funcionalidades básicas):**
   - Implementação de threads para PAR
   - Sockets para canais de comunicação
   - Análise semântica completa
   - Tabela de símbolos

---

**Observação Final:** Como a equipe escolheu Java, deve-se adaptar os exemplos em Python para a sintaxe Java quando apropriado (ex: construtores ao invés de `__init__`, tipos explícitos, etc.). A gramática já permite isso, mas a implementação precisa ser completada.
