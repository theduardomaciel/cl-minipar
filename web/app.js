// Aplicação principal do interpretador MiniPar Web
let editor;
const API_URL = 'http://localhost:8080';
let inputQueue = [];
let waitingForInput = false;
let currentSessionId = null;
let pollingInterval = null;
let activeTab = 'tab-output';

// Inicialização
document.addEventListener('DOMContentLoaded', function () {
    initializeEditor();
    setupEventListeners();
    loadInitialExample();
    setupTerminalInput();
    setupTabs();
});

/**
 * Inicializa o editor CodeMirror com syntax highlighting
 */
function initializeEditor() {
    const textarea = document.getElementById('codeEditor');

    editor = CodeMirror.fromTextArea(textarea, {
        mode: 'minipar',
        theme: 'monokai',
        lineNumbers: true,
        lineWrapping: true,
        indentUnit: 4,
        tabSize: 4,
        indentWithTabs: false,
        matchBrackets: true,
        autoCloseBrackets: true,
        extraKeys: {
            "Ctrl-Enter": runCode,
            "Cmd-Enter": runCode,
            "Ctrl-/": "toggleComment",
            "Cmd-/": "toggleComment"
        }
    });

    // Ajustar altura do editor
    editor.setSize(null, "100%");
}

/**
 * Configura event listeners para os botões
 */
function setupEventListeners() {
    document.getElementById('runCode').addEventListener('click', runCode);
    document.getElementById('clearCode').addEventListener('click', handleClearOrStop);
    document.getElementById('clearOutput').addEventListener('click', clearOutput);
    document.getElementById('loadExample').addEventListener('click', openExamplesModal);
    document.getElementById('closeModal').addEventListener('click', closeExamplesModal);

    // Event listeners para os botões de exemplo
    const exampleButtons = document.querySelectorAll('.example-btn');
    exampleButtons.forEach(btn => {
        btn.addEventListener('click', function () {
            const exampleName = this.getAttribute('data-example');
            loadExample(exampleName);
            closeExamplesModal();
        });
    });

    // Fechar modal clicando fora
    document.getElementById('examplesModal').addEventListener('click', function (e) {
        if (e.target === this) {
            closeExamplesModal();
        }
    });
}

/**
 * Tabs: alterna entre Saída, Tokens e AST
 */
function setupTabs() {
    const buttons = document.querySelectorAll('.tab-btn');
    buttons.forEach(btn => {
        btn.addEventListener('click', () => {
            const tab = btn.getAttribute('data-tab');

            // Toggle btns
            buttons.forEach(b => b.classList.toggle('active', b === btn));
            // Toggle content
            document.querySelectorAll('.tab-content').forEach(el => {
                el.classList.toggle('active', el.id === tab);
            });
            activeTab = tab;
        });
    });
}

/**
 * Carrega exemplo inicial
 */
function loadInitialExample() {
    loadExample('hello');
}

/**
 * Carrega um exemplo específico no editor
 */
function loadExample(name) {
    const code = getExample(name);
    editor.setValue(code);
    clearOutput();
}

/**
 * Abre o modal de exemplos
 */
function openExamplesModal() {
    document.getElementById('examplesModal').classList.add('active');
}

/**
 * Fecha o modal de exemplos
 */
function closeExamplesModal() {
    document.getElementById('examplesModal').classList.remove('active');
}

/**
 * Manipula o botão Limpar/Parar
 */
function handleClearOrStop() {
    // Se estiver executando, para a execução
    if (pollingInterval) {
        stopExecution();
    } else {
        // Caso contrário, limpa o código
        clearCode();
    }
}

/**
 * Para a execução atual
 */
function stopExecution() {
    if (confirm('Deseja realmente parar a execução?')) {
        stopPolling();
        hideInputField();
        waitingForInput = false;
        appendToOutput('\n⚠️ Execução interrompida pelo usuário', 'output-warning');
        resetUIAfterExecution();
    }
}

/**
 * Atualiza UI para estado de execução
 */
function setExecutingUI() {
    const runButton = document.getElementById('runCode');
    const clearButton = document.getElementById('clearCode');

    // Desabilitar botão executar
    runButton.disabled = true;
    runButton.innerHTML = '<span class="loading"></span> Executando...';

    // Transformar botão limpar em parar
    clearButton.innerHTML = '⏹️ Parar';
    clearButton.classList.remove('btn-secondary');
    clearButton.classList.add('btn-danger');
}

/**
 * Restaura UI para estado normal
 */
function resetUIAfterExecution() {
    const runButton = document.getElementById('runCode');
    const clearButton = document.getElementById('clearCode');

    // Reabilitar botão executar
    runButton.disabled = false;
    runButton.innerHTML = '▶️ Executar';

    // Restaurar botão limpar
    clearButton.innerHTML = '🗑️ Limpar';
    clearButton.classList.remove('btn-danger');
    clearButton.classList.add('btn-secondary');
}

/**
 * Limpa o editor de código
 */
function clearCode() {
    if (confirm('Deseja realmente limpar o código?')) {
        editor.setValue('');
        editor.focus();
    }
}

/**
 * Limpa a área de saída
 */
function clearOutput() {
    const outputDiv = document.getElementById('output');
    outputDiv.textContent = '';
    outputDiv.classList.add('empty');
    hideInputField();
    inputQueue = [];
    waitingForInput = false;

    // Limpar Tokens e AST
    const tokensEl = document.getElementById('tokens');
    const astEl = document.getElementById('ast');
    if (tokensEl) { tokensEl.textContent = ''; tokensEl.classList.add('empty'); }
    if (astEl) { astEl.textContent = ''; astEl.classList.add('empty'); }
}

/**
 * Configura o campo de input do terminal
 */
function setupTerminalInput() {
    const userInput = document.getElementById('userInput');
    const inputContainer = document.getElementById('inputContainer');

    userInput.addEventListener('keypress', async function (e) {
        if (e.key === 'Enter') {
            const value = userInput.value;

            // Adiciona à saída visual
            appendToOutput(`> ${value}`, 'user-input-echo');

            // Limpa o campo
            userInput.value = '';

            // Esconde o campo de input
            hideInputField();
            waitingForInput = false;

            // Envia input para o servidor
            if (currentSessionId) {
                try {
                    await fetch(`${API_URL}/session/input`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json; charset=utf-8'
                        },
                        body: JSON.stringify({
                            sessionId: currentSessionId,
                            input: value
                        })
                    });
                } catch (error) {
                    console.error('Erro ao enviar input:', error);
                    appendToOutput('Erro ao enviar input: ' + error.message, 'output-error');
                }
            }
        }
    });
}

/**
 * Mostra o campo de input
 */
function showInputField(prompt = '') {
    const inputContainer = document.getElementById('inputContainer');
    const userInput = document.getElementById('userInput');

    if (prompt) {
        appendToOutput(prompt, 'input-line');
    }

    inputContainer.classList.remove('hidden');
    userInput.focus();
    waitingForInput = true;
}

/**
 * Esconde o campo de input
 */
function hideInputField() {
    const inputContainer = document.getElementById('inputContainer');
    const userInput = document.getElementById('userInput');
    
    inputContainer.classList.add('hidden');
    userInput.value = ''; // Limpa o campo ao esconder
}

/**
 * Adiciona texto à saída
 */
function appendToOutput(text, className = '') {
    const outputDiv = document.getElementById('output');
    outputDiv.classList.remove('empty');

    const line = document.createElement('div');
    line.className = className;
    line.textContent = text;
    outputDiv.appendChild(line);

    // Auto-scroll para o final
    outputDiv.scrollTop = outputDiv.scrollHeight;
}

/**
 * Aguarda por input do usuário
 */
async function waitForUserInput(prompt = '') {
    return new Promise((resolve) => {
        showInputField(prompt);

        const checkInput = setInterval(() => {
            if (inputQueue.length > 0) {
                clearInterval(checkInput);
                resolve(inputQueue.shift());
            }
        }, 100);
    });
}

/**
 * Executa o código MiniPar
 */
async function runCode() {
    const code = editor.getValue().trim();

    if (!code) {
        showOutput('Erro: Nenhum código para executar!', 'error');
        return;
    }

    const outputDiv = document.getElementById('output');
    const runButton = document.getElementById('runCode');

    // Atualizar UI para estado de execução
    setExecutingUI();

    // Limpar saída anterior
    outputDiv.textContent = '';
    outputDiv.classList.remove('empty');

    try {
        // Atualizar Tokens e AST (análise léxica e sintática)
        analyzeAndRender(code).catch(err => {
            console.warn('Falha na análise (tokens/AST):', err);
        });

        // Iniciar sessão de execução interativa
        const startResponse = await fetch(`${API_URL}/session/start`, {
            method: 'POST',
            headers: {
                'Content-Type': 'text/plain; charset=utf-8'
            },
            body: code
        });

        if (!startResponse.ok) {
            throw new Error(`Erro HTTP: ${startResponse.status}`);
        }

        const startResult = await startResponse.json();
        currentSessionId = startResult.sessionId;

        // Iniciar polling para verificar status
        startPolling();

    } catch (error) {
        showOutput(
            `Erro ao conectar com o servidor:\n${error.message}\n\n` +
            `Verifique se o servidor está rodando em ${API_URL}`,
            'error'
        );
        resetUIAfterExecution();
    }
}

/**
 * Chama o backend para obter tokens e AST e renderiza nas respectivas abas
 */
async function analyzeAndRender(code) {
    const res = await fetch(`${API_URL}/analyze`, {
        method: 'POST',
        headers: { 'Content-Type': 'text/plain; charset=utf-8' },
        body: code
    });
    if (!res.ok) throw new Error(`Erro HTTP: ${res.status}`);
    const data = await res.json();

    // Render Tokens
    renderTokens(data.tokens || [], data.error);
    // Render AST
    renderAST(data.ast || '', data.error);
}

function renderTokens(tokens, error) {
    const el = document.getElementById('tokens');
    if (!el) return;
    el.classList.remove('empty');
    if (error) {
        el.textContent = `Erro na análise léxica/sintática:\n${error}`;
        return;
    }
    if (!tokens || tokens.length === 0) {
        el.classList.add('empty');
        return;
    }
    const lines = tokens.map(t => {
        const type = t.type || '';
        const lex = (t.lexeme ?? '').replace(/\n/g, '\\n');
        const pos = `${t.line}:${t.column}`;
        return `${pos.padEnd(7)} ${String(type).padEnd(18)} ${lex}`;
    });
    el.textContent = lines.join('\n');
    el.scrollTop = 0;
}

function renderAST(ast, error) {
    const el = document.getElementById('ast');
    if (!el) return;
    el.classList.remove('empty');
    if (error) {
        el.textContent = `Erro na análise:\n${error}`;
        return;
    }
    if (!ast) {
        el.classList.add('empty');
        return;
    }
    el.textContent = ast;
    el.scrollTop = 0;
}

/**
 * Inicia o polling para verificar status da execução
 */
function startPolling() {
    if (pollingInterval) {
        clearInterval(pollingInterval);
    }

    pollingInterval = setInterval(async () => {
        try {
            const response = await fetch(`${API_URL}/session/status?sessionId=${currentSessionId}`);

            if (!response.ok) {
                throw new Error(`Erro HTTP: ${response.status}`);
            }

            const status = await response.json();

            // Atualizar saída
            if (status.output) {
                showOutput(status.output, status.error ? 'error' : 'normal');
            }

            // Verificar se está aguardando input
            if (status.waitingForInput && !waitingForInput) {
                showInputField();
            }

            // Verificar se terminou
            if (!status.running) {
                stopPolling();
                resetUIAfterExecution();

                if (status.error) {
                    appendToOutput('\n' + status.error, 'output-error');
                }
            }

        } catch (error) {
            console.error('Erro no polling:', error);
            stopPolling();
            resetUIAfterExecution();
        }
    }, 200); // Poll a cada 200ms
}

/**
 * Para o polling
 */
function stopPolling() {
    if (pollingInterval) {
        clearInterval(pollingInterval);
        pollingInterval = null;
    }
    currentSessionId = null;
}

/**
 * Exibe a saída na área de output
 */
function showOutput(text, type = 'normal') {
    const outputDiv = document.getElementById('output');
    outputDiv.classList.remove('empty');

    // Limpar classes anteriores
    outputDiv.classList.remove('output-success', 'output-error');

    // Adicionar classe apropriada
    if (type === 'success') {
        outputDiv.classList.add('output-success');
    } else if (type === 'error') {
        outputDiv.classList.add('output-error');
    }

    // Processar o texto para melhor visualização
    const formattedText = text
        .replace(/\r\n/g, '\n')
        .replace(/\r/g, '\n');

    outputDiv.textContent = formattedText;

    // Auto-scroll para o topo
    outputDiv.scrollTop = 0;
}

/**
 * Adiciona suporte para atalhos de teclado globais
 */
document.addEventListener('keydown', function (e) {
    // ESC para fechar modal
    if (e.key === 'Escape') {
        closeExamplesModal();
    }
});

// Feedback visual quando o código está sendo digitado
editor.on('change', function () {
    // Pode adicionar aqui validação em tempo real ou outras funcionalidades
});

console.log('🚀 MiniPar Web Interface carregada com sucesso!');
console.log('Atalhos disponíveis:');
console.log('  Ctrl+Enter / Cmd+Enter: Executar código');
console.log('  Ctrl+/ / Cmd+/: Comentar/descomentar linha');
