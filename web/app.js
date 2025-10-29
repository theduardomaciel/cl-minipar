// Aplicação principal do interpretador MiniPar Web
let editor;
const API_URL = 'http://localhost:8080';
let inputQueue = [];
let waitingForInput = false;
let waitingForTCPConfig = false;
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
            
            // Verificar se está aguardando configuração TCP
            if (status.waitingForTCPConfig && !waitingForTCPConfig) {
                const configInfo = parseTCPConfigInfo(status.tcpConfigInfo);
                showTCPConfigDialog(configInfo);
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

/**
 * Parseia a string de informações de configuração TCP
 */
function parseTCPConfigInfo(infoStr) {
    if (!infoStr) return null;
    
    const parts = {};
    infoStr.split(',').forEach(part => {
        const [key, value] = part.split('=');
        parts[key] = value;
    });
    
    return parts;
}

/**
 * Mostra dialog para configuração de canal TCP
 */
function showTCPConfigDialog(configInfo) {
    if (!configInfo) return;
    
    waitingForTCPConfig = true;
    
    const message = `\n🔌 Configuração do Canal TCP '${configInfo.canal}'\n` +
                   `   Componentes: ${configInfo.comp1} ⟷ ${configInfo.comp2}\n\n` +
                   `   Este processo será Servidor ou Cliente?\n`;
    
    appendToOutput(message, 'output-info');
    
    // Criar dialog de configuração
    const dialog = document.createElement('div');
    dialog.className = 'tcp-config-dialog';
    dialog.innerHTML = `
        <div class="tcp-config-content">
            <h3>Configuração de Canal TCP</h3>
            <p><strong>Canal:</strong> ${configInfo.canal}</p>
            <p><strong>Componentes:</strong> ${configInfo.comp1} ⟷ ${configInfo.comp2}</p>
            
            <div class="form-group">
                <label>
                    <input type="radio" name="tcpRole" value="server" checked>
                    Servidor (espera conexões)
                </label>
                <label>
                    <input type="radio" name="tcpRole" value="client">
                    Cliente (conecta ao servidor)
                </label>
            </div>
            
            <div id="serverConfig" class="config-section">
                <div class="form-group">
                    <label for="tcpPort">Porta:</label>
                    <input type="number" id="tcpPort" value="8081" min="1024" max="65535">
                </div>
            </div>
            
            <div id="clientConfig" class="config-section" style="display: none;">
                <div class="form-group">
                    <label for="tcpHost">Host do Servidor:</label>
                    <input type="text" id="tcpHost" value="localhost" placeholder="localhost ou IP">
                </div>
                <div class="form-group">
                    <label for="tcpPortClient">Porta:</label>
                    <input type="number" id="tcpPortClient" value="8081" min="1024" max="65535">
                </div>
            </div>
            
            <div class="dialog-buttons">
                <button id="confirmTCPConfig" class="btn btn-primary">Confirmar</button>
                <button id="cancelTCPConfig" class="btn btn-secondary">Cancelar</button>
            </div>
        </div>
    `;
    
    document.body.appendChild(dialog);
    
    // Event listeners para alternar entre servidor/cliente
    const roleRadios = dialog.querySelectorAll('input[name="tcpRole"]');
    roleRadios.forEach(radio => {
        radio.addEventListener('change', function() {
            const serverConfig = dialog.querySelector('#serverConfig');
            const clientConfig = dialog.querySelector('#clientConfig');
            
            if (this.value === 'server') {
                serverConfig.style.display = 'block';
                clientConfig.style.display = 'none';
            } else {
                serverConfig.style.display = 'none';
                clientConfig.style.display = 'block';
            }
        });
    });
    
    // Confirmar configuração
    dialog.querySelector('#confirmTCPConfig').addEventListener('click', async function() {
        const role = dialog.querySelector('input[name="tcpRole"]:checked').value;
        const isServer = role === 'server';
        
        let host = 'localhost';
        let port = 8081;
        
        if (isServer) {
            port = parseInt(dialog.querySelector('#tcpPort').value);
        } else {
            host = dialog.querySelector('#tcpHost').value;
            port = parseInt(dialog.querySelector('#tcpPortClient').value);
        }
        
        // Enviar configuração para o servidor
        try {
            await fetch(`${API_URL}/session/tcpconfig`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json; charset=utf-8'
                },
                body: JSON.stringify({
                    sessionId: currentSessionId,
                    isServer: isServer,
                    host: host,
                    port: port
                })
            });
            
            const configType = isServer ? 'Servidor' : 'Cliente';
            appendToOutput(`✓ Configurado como ${configType} (${host}:${port})\n`, 'output-success');
        } catch (error) {
            console.error('Erro ao enviar configuração TCP:', error);
            appendToOutput('✗ Erro ao enviar configuração TCP: ' + error.message, 'output-error');
        }
        
        dialog.remove();
        waitingForTCPConfig = false;
    });
    
    // Cancelar configuração
    dialog.querySelector('#cancelTCPConfig').addEventListener('click', function() {
        dialog.remove();
        waitingForTCPConfig = false;
        stopExecution();
    });
}

console.log('🚀 MiniPar Web Interface carregada com sucesso!');
console.log('Atalhos disponíveis:');
console.log('  Ctrl+Enter / Cmd+Enter: Executar código');
console.log('  Ctrl+/ / Cmd+/: Comentar/descomentar linha');
