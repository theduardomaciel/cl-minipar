# 🌐 MiniPar - Interface Web

Interface web interativa para o interpretador MiniPar com syntax highlighting e execução em tempo real.

## 🚀 Como Usar

### 1. Compilar e Executar o Servidor

#### No Windows:
```bash
./scripts/run_web_server.bat
```

#### No Linux/Mac:
```bash
chmod +x scripts/run_web_server.sh
./scripts/run_web_server.sh
```

### 2. Acessar a Interface

Abra seu navegador em: **http://localhost:8080**

## ✨ Funcionalidades

### Editor de Código
- ✅ **Syntax Highlighting** personalizado para MiniPar
- ✅ **Numeração de linhas**
- ✅ **Auto-indentação**
- ✅ **Fechamento automático de parênteses e chaves**
- ✅ **Tema escuro (Monokai)**

### Controles de Execução
- ✅ **Botão Executar** - Executa o código (vira "Executando..." durante execução)
- ✅ **Botão Parar** - Durante execução, o botão "Limpar" vira "Parar" (vermelho) para interromper
- ✅ **Botão Limpar** - Limpa o editor (quando não está executando)
- ✅ **Estado Desabilitado** - Botão "Executar" fica desabilitado durante execução

### Input Interativo
- ✅ **readln()** - Lê entrada de texto do usuário
- ✅ **readNumber()** - Lê entrada numérica com validação
- ✅ **Campo de input visual** - Aparece automaticamente quando necessário
- ✅ **Polling em tempo real** - Status atualizado a cada 200ms

### Atalhos de Teclado
- `Ctrl+Enter` / `Cmd+Enter`: Executar código
- `Ctrl+/` / `Cmd+/`: Comentar/descomentar linha

### Exemplos Incluídos
1. 👋 **Hello World** - Primeiro programa
2. 📦 **Variáveis e Operações** - Tipos básicos e operações
3. 🔧 **Funções** - Declaração e uso de funções (fatorial, fibonacci)
4. 🏛️ **Classes e Objetos** - POO com herança
5. 🔁 **Loops** - For, While, Do-While
6. 📋 **Listas e Dicionários** - Estruturas de dados
7. ⚡ **Blocos Paralelos** - Execução sequencial e paralela

## 📁 Estrutura de Arquivos

```
web/
├── index.html          # Interface principal
├── style.css           # Estilos da interface
├── app.js              # Lógica da aplicação
├── minipar-mode.js     # Syntax highlighting customizado
└── examples.js         # Exemplos de código

src/
└── WebServer.java      # Servidor HTTP
```

## 🔧 Tecnologias Utilizadas

### Backend
- **Java HTTP Server** (`com.sun.net.httpserver`)
- Não requer dependências externas

### Frontend
- **HTML5** e **CSS3**
- **JavaScript** (ES6+)
- **CodeMirror** 5.65.2 - Editor de código
  - Syntax highlighting customizado
  - Tema Monokai

## 🎨 Personalização

### Alterar o Tema do Editor

Edite `index.html` e troque o tema na linha:
```html
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.65.2/theme/NOME_DO_TEMA.min.css">
```

E em `app.js`:
```javascript
theme: 'NOME_DO_TEMA'
```

Temas disponíveis: monokai, dracula, material, solarized, etc.

### Alterar a Porta do Servidor

Edite `WebServer.java`:
```java
private static final int PORT = 8080; // Altere aqui
```

## 🔍 API Endpoints

### GET /
Retorna a interface HTML principal.

### POST /execute
Executa código MiniPar.

**Request:**
```
POST /execute
Content-Type: text/plain; charset=utf-8

<código MiniPar>
```

**Response:**
```json
{
    "success": true|false,
    "output": "saída do programa",
    "error": "mensagem de erro (se houver)"
}
```

## 🐛 Solução de Problemas

### Porta já em uso
Se a porta 8080 já estiver em uso, altere a constante `PORT` em `WebServer.java` e recompile.

### Erro ao conectar
Verifique se:
1. O servidor está rodando
2. Não há firewall bloqueando a porta
3. A URL está correta: `http://localhost:8080`

### Código não executa
1. Verifique os logs no terminal do servidor
2. Abra o console do navegador (F12) para ver erros JavaScript
3. Teste primeiro com os exemplos incluídos

## 📝 Adicionando Novos Exemplos

Edite `web/examples.js` e adicione um novo exemplo:

```javascript
const examples = {
    // ... exemplos existentes
    
    meuExemplo: `# Meu Exemplo
print("Olá, mundo!");
number x = 42;
print("O valor é: ", x);`
};
```

E adicione o botão em `index.html`:

```html
<button class="example-btn" data-example="meuExemplo">
    🎯 Meu Exemplo
</button>
```

## 🤝 Contribuindo

Sugestões de melhorias:
- [ ] Autocomplete de palavras-chave
- [ ] Validação em tempo real
- [ ] Histórico de execuções
- [ ] Salvar/carregar códigos
- [ ] Múltiplas abas de código
- [ ] Temas personalizáveis

## 📄 Licença

Este projeto faz parte da disciplina de Compiladores 2025.1 - UFAL.
