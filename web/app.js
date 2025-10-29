// Aplicação principal do interpretador MiniPar Web
let editor;
const API_URL = 'http://localhost:8080';
let inputQueue = [];
let waitingForInput = false;
let currentSessionId = null;
let pollingInterval = null;

// Inicialização
document.addEventListener('DOMContentLoaded', function () {
    initializeEditor();
    setupEventListeners();
    loadInitialExample();
    setupTerminalInput();
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
    document.getElementById('clearCode').addEventListener('click', clearCode);
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
    inputContainer.classList.add('hidden');
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

    // Desabilitar botão e mostrar loading
    runButton.disabled = true;
    runButton.innerHTML = '<span class="loading"></span> Executando...';

    // Limpar saída anterior
    outputDiv.textContent = '';
    outputDiv.classList.remove('empty');

    try {
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
        runButton.disabled = false;
        runButton.innerHTML = '▶️ Executar';
    }
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

                const runButton = document.getElementById('runCode');
                runButton.disabled = false;
                runButton.innerHTML = '▶️ Executar';

                if (status.error) {
                    appendToOutput('\n' + status.error, 'output-error');
                }
            }

        } catch (error) {
            console.error('Erro no polling:', error);
            stopPolling();

            const runButton = document.getElementById('runCode');
            runButton.disabled = false;
            runButton.innerHTML = '▶️ Executar';
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
