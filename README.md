<picture>
  <source media="(prefers-color-scheme: dark)" srcset="/.github/cover.png">
  <source media="(prefers-color-scheme: light)" srcset="/.github/cover_light.png">
  <img alt="Banner do projeto MiniPar 2025.1" src="/.github/cover_light.png">
</picture>

<br />

## 🚶 Sobre o Projeto

Um **interpretador orientado a objetos** para a linguagem educacional **MiniPar 2025.1**, desenvolvido como requisito parcial da disciplina de **Compiladores (UFAL, 2025.1)**, ministrada pelo professor **Arturo Hernández Domínguez**.  

O tema escolhido dentre as opções propostas foi o **Tema 2 – MiniPar Orientado a Objetos**.

---

## ⭐ Features

- 🔤 **Analisador Léxico (Lexer)** baseado em expressões regulares (Regex)
- 🧱 **Analisador Sintático (Parser)** descendente recursivo
- 🌳 **Construção da Árvore Sintática Abstrata (AST)**
- ⚠️ **Tratamento de erros sintáticos** com exceções customizadas (`SyntaxError`)
- 📄 **Interface textual simples** para entrada e saída no console
- 🌐 **Interface Web** com syntax highlighting e execução em tempo real

---

## ⚙️ Arquitetura Modular

A aplicação segue a estrutura clássica de um *front-end de compilador*, organizada em arquivos distintos para clareza e evolução incremental.

```
src/
├── Main.java                  # Interface CLI
├── server/
│   └── WebServer.java         # Servidor HTTP para interface web
├── lexer/  
│   ├── Lexer.java
│   ├── Token.java
│   └── TokenType.java
├── parser/
│   ├── ASTNode.java
│   ├── Parser.java
│   ├── Program.java
└── interpreter/
    ├── Environment.java
    └── Interpreter.java

web/                       # Interface Web
├── index.html            # Interface principal
├── style.css             # Estilos
├── app.js                # Lógica da aplicação
├── minipar-mode.js       # Syntax highlighting
└── examples.js           # Exemplos de código
```

### Principais conceitos

- **Lexer:** realiza a varredura do código-fonte e converte-o em uma lista de *tokens*.
- **Parser:** analisa a sequência de tokens e constrói a árvore sintática abstrata (AST).
- **AST (Abstract Syntax Tree):** representação hierárquica do código analisado.
- **Interpreter:** percorre a AST para executar o código.
- **Main:** ponto de entrada — integra Lexer, Parser e exibe resultados.

---

## 🧩 Exemplo de Execução

### 🌐 Interface Web (Recomendado)

**Método 1: Script automático**

Windows:
```cmd
scripts\run_web_server.bat
```

Linux/Mac/Git Bash:
```bash
chmod +x scripts/run_web_server.sh
./scripts/run_web_server.sh
```

**Método 2: Manual**

Compile e execute (certifique-se de estar na raiz do projeto):
```bash
# Compilar
javac -encoding UTF-8 -d build -sourcepath src src/server.WebServer.java src/Main.java src/lexer/*.java src/parser/*.java src/interpreter/*.java

# Executar (DA RAIZ DO PROJETO)
java -cp build server.WebServer
```

Acesse no navegador: **http://localhost:8080**

> [!IMPORTANT]   
Execute o comando `java -cp build server.WebServer` **da raiz do projeto**, onde está a pasta `web/`. Não execute de dentro da pasta `build/`.

A interface web oferece:
- Syntax highlighting para MiniPar
- Exemplos prontos para executar
- Atalhos de teclado (Ctrl+Enter para executar)

### 💻 Interface CLI

> Observação: os exemplos abaixo usam o shell Bash no Windows (Git Bash). Se preferir PowerShell/CMD, adapte as barras e aspas conforme necessário.

Compile as fontes (gera as classes em `out/`):

```bash
mkdir -p out
javac -encoding UTF-8 -d out src/Main.java src/lexer/*.java src/parser/*.java src/interpreter/*.java
```

Execute em modo interativo (REPL):

```bash
java -cp out Main
```

Ou execute passando um arquivo `.minipar` (exemplos no diretório `tests/`):

```bash
java -cp out Main tests/teste6_quicksort.minipar
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

## 🧮 BNF da Linguagem MiniPar

A gramática BNF da linguagem MiniPar que desenvolvemos nesse projeto está disponível no arquivo [GRAMATICA_BNF.txt](GRAMATICA_BNF.txt).

---

## 🎥 Demonstrações em Vídeo

> 🎬 **Link para o vídeo da Atividade 2:**
> [pendente]

O vídeo mostra:

* Execução do Lexer e Parser;
* Impressão dos tokens reconhecidos;
* Construção e exibição da AST;
* Explicação breve da arquitetura do projeto.

> 🎬 **Link para o vídeo da Versão Final:**
> [pendente]

O vídeo mostra:

* Execução do interpretador orientado a objetos;
* Demonstração da interface web com syntax highlighting;
* Visualização gráfica da AST na interface web.

---

## 🧱 Roadmap

* [x] Implementação do Lexer
* [x] Implementação do Parser
* [x] Impressão da AST
* [x] Implementar análise semântica (escopos e tipos)
* [x] Implementar interpretador orientado a objetos
* [x] Interface web com syntax highlighting
* [x] Visualização gráfica da AST na interface web

## 👥 Equipe

Disciplina ministrada pelo professor **Arturo Hernández Domínguez (UFAL)**.
Atividade desenvolvida por:

* [Eduardo Maciel (@theduardomaciel)](https://github.com/theduardomaciel)
* [Lucas Maciel (@lucas7maciel)](https://github.com/lucas7maciel)
* [Josenilton Ferreira (@914joseph)](https://github.com/914joseph)
* [Maria Letícia (@letsventura)](https://github.com/letsventura)

---