# 🚀 Guia Completo - Interface Web MiniPar

## 📖 Índice

1. [Como Executar](#como-executar)
2. [Usando a Interface](#usando-a-interface)
3. [Input Interativo (readln e readNumber)](#input-interativo)
4. [Arquitetura Técnica](#arquitetura-técnica)
5. [Exemplos Práticos](#exemplos-práticos)
6. [Solução de Problemas](#solução-de-problemas)
7. [Personalização](#personalização)

---

## Como Executar

### 1️⃣ Compilar o projeto

Abra o terminal na pasta do projeto e execute:

**Windows (PowerShell/CMD):**
```cmd
mkdir build
javac -encoding UTF-8 -d build -sourcepath src src\server.WebServer.java src\Main.java src\lexer\*.java src\parser\*.java src\interpreter\*.java
```

**Linux/Mac/Git Bash:**
```bash
mkdir -p build
javac -encoding UTF-8 -d build -sourcepath src src/server.WebServer.java src/Main.java src/lexer/*.java src/parser/*.java src/interpreter/*.java
```

### 2️⃣ Iniciar o servidor

**IMPORTANTE:** Execute o servidor **a partir da raiz do projeto** (não de dentro da pasta `build/`).

**Windows:**
```cmd
java -cp build server.WebServer
```

**Linux/Mac/Git Bash:**
```bash
java -cp build server.WebServer
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

## Usando a Interface

### Editor de Código (Esquerda)
- Digite ou cole seu código MiniPar
- Use os **exemplos prontos** clicando em "📚 Exemplos"
- **Syntax highlighting** automático para facilitar a leitura

### Área de Saída (Direita)
- Mostra o resultado da execução
- Exibe erros com destaque em vermelho
- Saída bem-sucedida em verde
- **Campo de input interativo** quando `readln()` ou `readNumber()` são chamados

### Botões Disponíveis
- **▶️ Executar**: Roda o código (ou use `Ctrl+Enter`)
- **📚 Exemplos**: Abre galeria com vários exemplos
- **🗑️ Limpar**: Remove o código do editor
- **🗑️ Limpar Saída**: Limpa a área de resultados

### Atalhos de Teclado
- `Ctrl+Enter` (Windows/Linux) ou `Cmd+Enter` (Mac): Executar código
- `Ctrl+/` ou `Cmd+/`: Comentar/descomentar linha
- `ESC`: Fechar modal de exemplos

### 📚 Exemplos Incluídos

1. **👋 Hello World** - Primeiro programa
2. **📦 Variáveis e Operações** - Tipos básicos
3. **🔧 Funções** - Fatorial e Fibonacci
4. **🏛️ Classes e Objetos** - POO com herança
5. **🔁 Loops** - For, While, Do-While
6. **📋 Listas e Dicionários** - Estruturas de dados
7. **⚡ Blocos Paralelos** - Seq e Par
8. **⌨️ Entrada de Dados (Input)** - readln() e readNumber()

---

## Input Interativo

O MiniPar possui **suporte completo** a entrada interativa de dados:
- ✅ Funções `readln()` e `readNumber()` 
- ✅ Backend com polling HTTP (200ms)
- ✅ Frontend com campo de input visual
- ✅ Execução assíncrona em threads separadas
- ✅ Gerenciamento de sessões
- ✅ Interface web responsiva

### 🎮 Como Usar

#### Opção 1: Interface Web (RECOMENDADO)

1. Acesse http://localhost:8080
2. Clique em "📚 Exemplos"
3. Selecione "⌨️ Entrada de Dados (Input)"
4. Clique em "▶️ Executar"
5. Digite os valores quando solicitado
6. Pressione Enter para enviar

#### Opção 2: Terminal (Console)

```bash
cd build
java Main ../tests/teste_input_interativo.minipar

# Digite os valores quando solicitado
```

### � Funções Disponíveis

#### `readln()` - Lê uma string
```minipar
println("Digite seu nome:");
string nome = readln();
println("Olá, ", nome, "!");
```

#### `readNumber()` - Lê um número
```minipar
println("Digite sua idade:");
number idade = readNumber();
println("Você tem ", idade, " anos.");
```

### 🎨 Interface Visual do Input

```
┌────────────────────────────────────────────┐
│ Saída do Programa                          │
│                                            │
│ Digite seu nome:                           │
│ > João Silva          ← já enviado         │
│ Digite sua idade:                          │
├────────────────────────────────────────────┤
│ > [____________]      ← aguardando input   │
│   ↑                                        │
│   prompt verde                             │
└────────────────────────────────────────────┘
```

**Estados Visuais:**

- **Aguardando Input**: Campo visível com border verde ao focar
- **Após Enviar**: Valor exibido no histórico em dourado/amarelo
- **Validação**: `readNumber()` valida entrada automaticamente

### 🔍 Fluxo de Execução (Polling)

```
Usuário clica "Executar"
    ↓
Frontend → POST /session/start
    ↓
Backend cria interpreter.ExecutionSession (UUID)
    ↓
Frontend inicia polling (200ms)
    ↓
GET /session/status?sessionId=xxx
    ↓
Programa chama readln()
    ↓
waitingForInput = true
    ↓
Frontend detecta e exibe campo de input
    ↓
Usuário digita e pressiona Enter
    ↓
POST /session/input
    ↓
Backend desbloqueia thread
    ↓
readln() retorna o valor
    ↓
Programa continua...
```

---

## Arquitetura Técnica

### 📊 Backend - Servidor Web

#### Novos Arquivos

**`src/interpreter.ExecutionSession.java`**
- Gerencia execução assíncrona
- `sessionId` único (UUID)
- `BlockingQueue` para inputs
- Thread separada para execução
- Status: running, waitingForInput
- Captura de output/error

**`src/parser/InputCallback.java`**
```java
public interface InputCallback {
    String readLine() throws Exception;
    double readNumber() throws Exception;
}
```

#### Endpoints Implementados

**POST /session/start**
- Inicia sessão de execução
- Retorna sessionId
- Executa código em background

**GET /session/status?sessionId=xxx**
```json
{
  "running": true/false,
  "waitingForInput": true/false,
  "output": "saída atual",
  "error": "erros se houver"
}
```

**POST /session/input**
```json
{
  "sessionId": "xxx",
  "input": "valor digitado"
}
```

### 💻 Frontend - Interface Web

**`web/app.js`** - Implementações principais:
- `runCode()` - Inicia sessão via POST /session/start
- `startPolling()` - Poll a cada 200ms para verificar status
- `setupTerminalInput()` - Gerencia campo de input visual
- Detecção automática de `waitingForInput`
- Echo de inputs no histórico

### 🔄 Polling vs WebSocket

**Por que Polling?**
- ✅ Mais simples de implementar
- ✅ Não requer dependências externas
- ✅ Compatível com todos navegadores
- ✅ Funciona com HttpServer nativo do Java
- ✅ Suficiente para latência de 200ms (5 verificações/segundo)

### 🔐 Gerenciamento de Sessões

```java
import interpreter.ExecutionSession;

ConcurrentHashMap<String, ExecutionSession> sessions
```

- Thread-safe para múltiplos usuários
- Auto-limpeza quando execução termina
- SessionId único (UUID)
- Isolamento completo entre sessões

### 🧵 Bloqueio de Thread

```java
BlockingQueue<String> inputQueue
```

Quando `readln()` é chamado:
1. Thread de execução chama `inputQueue.take()`
2. Thread **bloqueia** até input chegar
3. Frontend detecta via polling
4. Usuário digita e envia
5. `inputQueue.offer(valor)` desbloqueia
6. Execução continua

---

## Exemplos Práticos

### 1. Cadastro Simples
```minipar
println("Nome:");
string nome = readln();
println("Olá, ", nome, "!");
```

### 2. Calculadora
```minipar
println("Digite o primeiro número:");
number a = readNumber();
println("Digite o segundo número:");
number b = readNumber();
println("Soma: ", a + b);
```

### 3. Loop com Input
```minipar
number soma = 0;
for (number i in [1, 2, 3]) {
    println("Digite número ", i, ":");
    number n = readNumber();
    soma = soma + n;
}
println("Total: ", soma);
```

### 4. Validação
```minipar
println("Digite sua idade:");
number idade = readNumber();
while (idade < 0) {
    println("Idade inválida! Digite novamente:");
    idade = readNumber();
}
println("Idade válida: ", idade);
```

---

## Solução de Problemas

### ❌ "404 - Arquivo não encontrado" no navegador
**Causa:** O servidor não está encontrando a pasta `web/`

**Solução:** Certifique-se de executar o servidor **da raiz do projeto**, NÃO de dentro da pasta `build/`.

✅ **CORRETO:**
```bash
# Na raiz do projeto (onde está a pasta web/)
java -cp build server.WebServer
```

❌ **ERRADO:**
```bash
# Dentro da pasta build/
cd build
java server.WebServer  # Não vai encontrar ../web/
```

### ❌ "Erro ao conectar com o servidor"
**Solução:** Certifique-se de que:
1. O servidor está rodando (execute o passo 2)
2. A URL está correta: `http://localhost:8080`
3. Não há firewall bloqueando a porta 8080

### ❌ Porta 8080 já está em uso
**Solução:** Altere a porta em `src/server.WebServer.java`:
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

### ❌ Input Inválido (readNumber)

```minipar
number idade = readNumber();
# Usuário digita "abc"
# Erro: "Entrada inválida: esperado um número"
```

### ❌ Sessão Expirada

```
Frontend detecta erro 400
Exibe: "Sessão não encontrada"
Permite nova execução
```

---

## Personalização

### Mudar o tema do editor

Edite `web/app.js` e altere:
```javascript
theme: 'monokai', // Outros: dracula, solarized, material
```

Temas disponíveis: https://codemirror.net/5/theme/

### Adicionar novos exemplos

Edite `web/examples.js` e adicione:
```javascript
meuExemplo: {
    title: "🎯 Meu Exemplo",
    code: `# Seu código aqui
println("Olá!");
string nome = readln();
println("Bem-vindo, ", nome, "!");`
}
```

---

## ✅ Status de Implementação

✅ Backend com polling HTTP
✅ Frontend com interface visual
✅ `readln()` e `readNumber()` funcionais
✅ Gerenciamento de sessões
✅ Execução assíncrona
✅ Tratamento de erros
✅ Documentação completa
✅ Exemplos práticos
✅ Servidor rodando em http://localhost:8080

---

## 🚀 Próximas Melhorias (Opcional)

- [ ] WebSocket para latência zero
- [ ] Histórico de comandos (↑/↓)
- [ ] Autocomplete
- [ ] Prompt customizável: `readln("Digite:")`
- [ ] `readPassword()` com asteriscos
- [ ] Timeout configurável

**Nota:** O sistema atual com polling funciona perfeitamente para casos de uso interativos normais. WebSocket só seria necessário para aplicações real-time extremas.
