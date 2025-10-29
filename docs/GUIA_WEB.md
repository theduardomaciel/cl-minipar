# 🚀 Guia Rápido - Interface Web MiniPar

## Como executar em 3 passos:

### 1️⃣ Compilar o projeto

Abra o terminal na pasta do projeto e execute:

**Windows (PowerShell/CMD):**
```cmd
mkdir build
javac -encoding UTF-8 -d build -sourcepath src src\WebServer.java src\Main.java src\lexer\*.java src\parser\*.java src\interpreter\*.java
```

**Linux/Mac/Git Bash:**
```bash
mkdir -p build
javac -encoding UTF-8 -d build -sourcepath src src/WebServer.java src/Main.java src/lexer/*.java src/parser/*.java src/interpreter/*.java
```

### 2️⃣ Iniciar o servidor

**IMPORTANTE:** Execute o servidor **a partir da raiz do projeto** (não de dentro da pasta `build/`).

**Windows:**
```cmd
java -cp build WebServer
```

**Linux/Mac/Git Bash:**
```bash
java -cp build WebServer
```

**Ou use o script automático:**

Windows:
```cmd
scripts\run_web_server.bat
```

Linux/Mac/Git Bash:
```bash
chmod +x scripts/run_web_server.sh
./scripts/run_web_server.sh
```

Você verá:
```
======================================================================
  SERVIDOR WEB MINIPAR
======================================================================

🌐 Servidor rodando em: http://localhost:8080
📝 Acesse a interface web no navegador

Pressione Ctrl+C para encerrar o servidor
```

### 3️⃣ Acessar a interface

Abra seu navegador em: **http://localhost:8080**

---

## 🎯 Usando a Interface

### Editor de Código (Esquerda)
- Digite ou cole seu código MiniPar
- Use os **exemplos prontos** clicando em "📚 Carregar Exemplo"
- **Syntax highlighting** automático para facilitar a leitura

### Área de Saída (Direita)
- Mostra o resultado da execução
- Exibe erros com destaque em vermelho
- Saída bem-sucedida em verde

### Botões Disponíveis
- **▶️ Executar**: Roda o código (ou use `Ctrl+Enter`)
- **📚 Carregar Exemplo**: Abre galeria com 7 exemplos
- **🗑️ Limpar**: Remove o código do editor
- **🗑️ Limpar Saída**: Limpa a área de resultados

### Atalhos de Teclado
- `Ctrl+Enter` (Windows/Linux) ou `Cmd+Enter` (Mac): Executar código
- `Ctrl+/` ou `Cmd+/`: Comentar/descomentar linha
- `ESC`: Fechar modal de exemplos

---

## 📚 Exemplos Incluídos

1. **👋 Hello World** - Primeiro programa
2. **📦 Variáveis e Operações** - Tipos básicos
3. **🔧 Funções** - Fatorial e Fibonacci
4. **🏛️ Classes e Objetos** - POO com herança
5. **🔁 Loops** - For, While, Do-While
6. **📋 Listas e Dicionários** - Estruturas de dados
7. **⚡ Blocos Paralelos** - Seq e Par

---

## 🔧 Solução de Problemas

### ❌ "404 - Arquivo não encontrado" no navegador
**Causa:** O servidor não está encontrando a pasta `web/`

**Solução:** Certifique-se de executar o servidor **da raiz do projeto**, NÃO de dentro da pasta `build/`.

✅ **CORRETO:**
```bash
# Na raiz do projeto (onde está a pasta web/)
java -cp build WebServer
```

❌ **ERRADO:**
```bash
# Dentro da pasta build/
cd build
java WebServer  # Não vai encontrar ../web/
```

### ❌ "Erro ao conectar com o servidor"
**Solução:** Certifique-se de que:
1. O servidor está rodando (execute o passo 2)
2. A URL está correta: `http://localhost:8080`
3. Não há firewall bloqueando a porta 8080

### ❌ Porta 8080 já está em uso
**Solução:** Altere a porta em `src/WebServer.java`:
```java
private static final int PORT = 8080; // Mude para 8081, 8082, etc.
```
E recompile o projeto.

### ❌ Erro de compilação
**Solução:** Verifique se você tem o JDK instalado:
```bash
java -version
javac -version
```
Versão mínima recomendada: JDK 11 ou superior.

---

## 🎨 Personalização

### Mudar o tema do editor

Edite `web/app.js` e altere:
```javascript
theme: 'monokai', // Outros: dracula, solarized, material
```

Temas disponíveis: https://codemirror.net/5/theme/

### Adicionar novos exemplos

Edite `web/examples.js` e adicione:
```javascript
meuExemplo: `# Seu código aqui
print("Olá!");`
```

---

## 📞 Ajuda

Se encontrar problemas, verifique:
1. ✅ Compilação sem erros
2. ✅ Servidor rodando
3. ✅ Console do navegador (F12) para erros JavaScript
4. ✅ Terminal do servidor para logs de execução

---

**Feito com ❤️ para a disciplina de Compiladores 2025.1 - UFAL**
