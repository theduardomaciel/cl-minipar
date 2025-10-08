<picture>
  <source media="(prefers-color-scheme: dark)" srcset="/.github/cover.png">
  <source media="(prefers-color-scheme: light)" srcset="/.github/cover_light.png">
  <img alt="Banner do projeto MiniPar 2025.1" src="/.github/cover_light.png">
</picture>

<br />

## 🚶 Sobre o Projeto

Um **interpretador orientado a objetos** para a linguagem educacional **MiniPar 2025.1**, desenvolvido como requisito parcial da disciplina de **Compiladores (UFAL, 2025.1)**, ministrada pelo professor **Arturo Hernández Domínguez**.  

O tema escolhido dentre as opções propostas foi o **Tema 2 – MiniPar Orientado a Objetos**.

> [!NOTE]
> Esta atual entrega corresponde à **Atividade 2 – Front-End**, abrangendo a implementação e demonstração do **Analisador Léxico (Lexer)** e do **Analisador Sintático (Parser)** do Tema 2.  
> Assim sendo, nesta fase, o foco está exclusivamente na **análise léxica e sintática** (sem semântica ou execução).  
> As próximas atividades abordarão a **interpretação orientada a objetos completa** da linguagem MiniPar 2025.1.

---

## ⭐ Features

- 🔤 **Analisador Léxico (Lexer)** baseado em expressões regulares (Regex)
- 🧱 **Analisador Sintático (Parser)** descendente recursivo
- 🌳 **Construção da Árvore Sintática Abstrata (AST)**
- ⚠️ **Tratamento de erros sintáticos** com exceções customizadas (`SyntaxError`)
- 📄 **Interface textual simples** para entrada e saída no console
- ✅ Estrutura modular e extensível para fases futuras (semântica e execução OO)

---

## ⚙️ Arquitetura Modular

A aplicação segue a estrutura clássica de um *front-end de compilador*, organizada em arquivos distintos para clareza e evolução incremental.

```
src/
├── Main.java
├── lexer/
│   ├── Token.java
│   └── Lexer.java
└── parser/
    ├── Parser.java
    ├── ASTNode.java

```

### Principais conceitos:

- **Lexer:** realiza a varredura do código-fonte e converte-o em uma lista de *tokens*.
- **Parser:** analisa a sequência de tokens e constrói a árvore sintática abstrata (AST).
- **AST (Abstract Syntax Tree):** representação hierárquica do código analisado.
- **Main:** ponto de entrada — integra Lexer, Parser e exibe resultados.

---

## 🧩 Exemplo de Execução

### Entrada (`test.minipar`)
```minipar
x = 3 + 5;
```

### Execução

```bash
javac src/*.java
java -cp src Main test.minipar
```

### Saída esperada

```
=== TOKENS ===
Token(ID, "x", line=1)
Token(OP, "=", line=1)
Token(NUMBER, "3", line=1)
Token(OP, "+", line=1)
Token(NUMBER, "5", line=1)
Token(PUNC, ";", line=1)
Token(EOF, "", line=1)

=== AST ===
(Assign x
  (Binary +
    (Literal 3)
    (Literal 5)))
```

---

## 🛠️ Setup & Execução

### 1. Clonar repositório

```bash
git clone https://github.com/theduardomaciel/cl-minipar.git
cd cl-minipar
```

### 2. Compilar o projeto

```bash
javac src/*.java
```

### 3. Executar com arquivo de teste

```bash
java -cp src Main test.minipar
```

> [!TIP]
> O projeto pode ser aberto diretamente no **IntelliJ IDEA** ou **VS Code** como um projeto Java.
> Certifique-se de que o JDK 17+ esteja configurado como SDK principal.

---

## 🧠 Princípios do Projeto

1. **Clareza:** manter o código simples, legível e modular.
2. **Coerência:** seguir a arquitetura clássica de compiladores (lexer → parser → AST).
3. **Evolução:** permitir expansão gradual para semântica, execução e OO.
4. **Orientação a Objetos:** modelar cada elemento da linguagem como uma entidade Java.
5. **Reprodutibilidade:** fácil de compilar e executar em qualquer ambiente com JDK 17+.

---

## 🧮 Estrutura da Linguagem MiniPar (versão simplificada)

Produções básicas suportadas nesta versão:

```
stmt → ID '=' expr ';'
expr → term (('+' | '-') term)*
term → NUMBER | ID
```

Esta gramática inicial será expandida nas próximas etapas para incluir **blocos, funções, classes e métodos**, conforme o Tema 2 – *MiniPar Orientado a Objetos*.

---

## 🎥 Demonstração em Vídeo

> 🎬 **Link para o vídeo da Atividade 2:**
> [pendente]

O vídeo mostra:

* Execução do Lexer e Parser;
* Impressão dos tokens reconhecidos;
* Construção e exibição da AST;
* Explicação breve da arquitetura do projeto.

---

## 🧱 Próximos Passos (Roadmap)

* [x] Implementação do Lexer
* [x] Implementação do Parser
* [x] Impressão da AST
* [ ] Implementar análise semântica (escopos e tipos)
* [ ] Implementar interpretador orientado a objetos
* [ ] Interface web com visualização da AST

## 👥 Equipe

Disciplina ministrada pelo professor **Arturo Hernández Domínguez (UFAL)**.
Atividade desenvolvida por:

* [Eduardo Maciel (@theduardomaciel)](https://github.com/theduardomaciel)
* [Lucas Maciel (@lucas7maciel)](https://github.com/lucas7maciel)
* [Josenilton Ferreira (@914joseph)](https://github.com/914joseph)
* [Maria Letícia (@letsventura)](https://github.com/letsventura)

---

