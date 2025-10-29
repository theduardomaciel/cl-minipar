// Aplicação principal do interpretador MiniPar Web
let editor;
const API_URL = 'http://localhost:8080';
let inputQueue = [];
let waitingForInput = false;
let currentSessionId = null;
let pollingInterval = null;
let activeTab = 'tab-output';
// AST canvas state
let astCanvas, astCtx;
let astTree = null;
let astPanX = 0, astPanY = 0;
let astScale = 1;
let astDragging = false;
let astLastX = 0, astLastY = 0;
const AST_NODE_W = 120;
const AST_NODE_H = 36;
const AST_H_SPACING = 24;
const AST_V_SPACING = 80;

// Inicialização
document.addEventListener('DOMContentLoaded', function () {
    initializeEditor();
    setupEventListeners();
    loadInitialExample();
    setupTerminalInput();
    setupTabs();
    setupAstCanvas();
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
            if (activeTab === 'tab-ast') {
                // garantir tamanho correto ao mostrar
                resizeAstCanvas();
                redrawAst();
            }
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
    if (tokensEl) { tokensEl.textContent = ''; tokensEl.classList.add('empty'); }
    // Limpar AST canvas
    astTree = null;
    redrawAst();
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

    // Render Tokens (lista)
    renderTokens(data.tokens || [], data.error);
    // Construir árvore combinada (AST + Tokens) para o canvas
    const astPart = data.astTree || null;
    const tokensPart = buildTokensTree(data.tokens || []);
    const combined = buildCombinedTree(astPart, tokensPart);
    astTree = combined;
    fitAst();
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

// ===== AST Canvas Rendering =====
function setupAstCanvas() {
    astCanvas = document.getElementById('astCanvas');
    if (!astCanvas) return;
    astCtx = astCanvas.getContext('2d');
    resizeAstCanvas();
    window.addEventListener('resize', () => {
        resizeAstCanvas();
        redrawAst();
    });

    // Drag to pan
    astCanvas.addEventListener('mousedown', (e) => {
        astDragging = true;
        const rect = astCanvas.getBoundingClientRect();
        astLastX = e.clientX - rect.left;
        astLastY = e.clientY - rect.top;
    });
    window.addEventListener('mouseup', () => { astDragging = false; });
    window.addEventListener('mousemove', (e) => {
        if (!astDragging) return;
        const rect = astCanvas.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;
        const dx = x - astLastX;
        const dy = y - astLastY;
        astPanX += dx;
        astPanY += dy;
        astLastX = x;
        astLastY = y;
        redrawAst();
    });

    // Wheel to zoom
    astCanvas.addEventListener('wheel', (e) => {
        e.preventDefault();
        const delta = -Math.sign(e.deltaY) * 0.1;
        const newScale = Math.min(2.5, Math.max(0.3, astScale * (1 + delta)));
        // zoom to mouse position
        const rect = astCanvas.getBoundingClientRect();
        const mx = e.clientX - rect.left;
        const my = e.clientY - rect.top;
        // world coords before
        const wx = (mx - astPanX) / astScale;
        const wy = (my - astPanY) / astScale;
        astScale = newScale;
        // adjust pan so the point stays under the cursor
        astPanX = mx - wx * astScale;
        astPanY = my - wy * astScale;
        redrawAst();
    }, { passive: false });

    // Toolbar buttons
    document.getElementById('astFit')?.addEventListener('click', fitAst);
    document.getElementById('astZoomIn')?.addEventListener('click', () => { setAstScale(astScale * 1.2); });
    document.getElementById('astZoomOut')?.addEventListener('click', () => { setAstScale(astScale / 1.2); });
}

function resizeAstCanvas() {
    if (!astCanvas) return;
    const parent = astCanvas.parentElement;
    const dpr = window.devicePixelRatio || 1;
    const width = parent.clientWidth;
    const height = parent.clientHeight;
    astCanvas.width = Math.max(1, Math.floor(width * dpr));
    astCanvas.height = Math.max(1, Math.floor(height * dpr));
    astCanvas.style.width = width + 'px';
    astCanvas.style.height = height + 'px';
    astCtx.setTransform(dpr, 0, 0, dpr, 0, 0);
}

function setAstScale(newScale) {
    const rect = astCanvas.getBoundingClientRect();
    const cx = rect.width / 2;
    const cy = rect.height / 2;
    const wx = (cx - astPanX) / astScale;
    const wy = (cy - astPanY) / astScale;
    astScale = Math.min(2.5, Math.max(0.3, newScale));
    astPanX = cx - wx * astScale;
    astPanY = cy - wy * astScale;
    redrawAst();
}

function fitAst() {
    if (!astTree) { redrawAst(); return; }
    // compute layout bounds to fit
    const layout = layoutAst(astTree);
    const bounds = computeBounds(layout);
    const rect = astCanvas.getBoundingClientRect();
    const margin = 40;
    const scaleX = (rect.width - margin) / (bounds.maxX - bounds.minX || 1);
    const scaleY = (rect.height - margin) / (bounds.maxY - bounds.minY || 1);
    astScale = Math.min(1.8, Math.max(0.3, Math.min(scaleX, scaleY)));
    // center
    const cx = rect.width / 2;
    const cy = rect.height / 2;
    const centerX = (bounds.minX + bounds.maxX) / 2;
    const centerY = (bounds.minY + bounds.maxY) / 2;
    astPanX = cx - centerX * astScale;
    astPanY = cy - centerY * astScale;
    redrawAst(layout);
}

function redrawAst(precomputedLayout = null) {
    if (!astCanvas || !astCtx) return;
    const ctx = astCtx;
    const rect = astCanvas.getBoundingClientRect();
    // clear
    ctx.save();
    ctx.setTransform(1, 0, 0, 1, 0, 0);
    ctx.clearRect(0, 0, astCanvas.width, astCanvas.height);
    ctx.restore();

    if (!astTree) return; // nothing to draw

    const layout = precomputedLayout || layoutAst(astTree);
    // apply pan/zoom
    ctx.save();
    const dpr = window.devicePixelRatio || 1;
    ctx.translate(astPanX * dpr, astPanY * dpr);
    ctx.scale(astScale * dpr, astScale * dpr);

    // draw edges first
    ctx.strokeStyle = '#666';
    ctx.lineWidth = 1.5 / (window.devicePixelRatio || 1);
    drawEdges(ctx, layout);

    // draw nodes
    drawNodes(ctx, layout);

    ctx.restore();
}

function drawEdges(ctx, node) {
    if (!node.children) return;
    const children = node.children || [];

    if (children.length <= 3) {
        // Layout horizontal: linha reta do centro do pai para o centro do filho
        for (const child of children) {
            ctx.beginPath();
            ctx.moveTo(node.x, node.y + AST_NODE_H / 2);
            ctx.lineTo(child.x, child.y - AST_NODE_H / 2);
            ctx.stroke();
            drawEdges(ctx, child);
        }
    } else {
        // Layout vertical: linha em L (direita do pai -> linha horizontal -> topo do filho)
        for (const child of children) {
            ctx.beginPath();
            ctx.moveTo(node.x + AST_NODE_W / 2, node.y);
            ctx.lineTo(child.x - AST_NODE_W / 2 - 10, node.y);
            ctx.lineTo(child.x - AST_NODE_W / 2 - 10, child.y);
            ctx.lineTo(child.x - AST_NODE_W / 2, child.y);
            ctx.stroke();
            drawEdges(ctx, child);
        }
    }
}

function drawNodes(ctx, node) {
    // node box
    const x = node.x - AST_NODE_W / 2;
    const y = node.y - AST_NODE_H / 2;
    ctx.fillStyle = '#2d2d30';
    ctx.strokeStyle = '#3e3e42';
    ctx.lineWidth = 1;
    roundRect(ctx, x, y, AST_NODE_W, AST_NODE_H, 6);
    ctx.fill();
    ctx.stroke();

    // title (type)
    ctx.font = 'bold 12px Segoe UI, sans-serif';
    ctx.fillStyle = '#d4d4d4';
    const type = node.type || 'Node';
    ctx.fillText(truncate(type, 18), x + 8, y + 14);
    // subtitle (label)
    if (node.label && node.label !== type) {
        ctx.font = '11px Segoe UI, sans-serif';
        ctx.fillStyle = '#858585';
        ctx.fillText(truncate(node.label, 28), x + 8, y + 28);
    }

    if (node.children) {
        for (const child of node.children) {
            drawNodes(ctx, child);
        }
    }
}

function roundRect(ctx, x, y, w, h, r) {
    const rr = Math.min(r, w / 2, h / 2);
    ctx.beginPath();
    ctx.moveTo(x + rr, y);
    ctx.arcTo(x + w, y, x + w, y + h, rr);
    ctx.arcTo(x + w, y + h, x, y + h, rr);
    ctx.arcTo(x, y + h, x, y, rr);
    ctx.arcTo(x, y, x + w, y, rr);
    ctx.closePath();
}

function truncate(s, n) {
    s = String(s);
    return s.length > n ? s.slice(0, n - 1) + '…' : s;
}

// Compute tree layout (x,y for each node)
function layoutAst(tree) {
    // compute subtree widths first
    computeSize(tree);
    // assign positions
    const startX = 0;
    const startY = 0;
    position(tree, startX, startY);
    // shift so that minX is >= 0
    const bounds = computeBounds(tree);
    shift(tree, -bounds.minX + 10, -bounds.minY + 10);
    return tree;
}

function computeSize(node) {
    const children = node.children || [];
    for (const c of children) computeSize(c);
    if (children.length === 0) {
        node._w = AST_NODE_W;
        node._h = AST_NODE_H;
        return;
    }
    // Layout híbrido: até 3 filhos em linha, depois empilha verticalmente
    if (children.length <= 3) {
        // Horizontal
        const totalChildrenWidth = children.reduce((sum, c) => sum + (c._w || AST_NODE_W), 0) + (children.length - 1) * AST_H_SPACING;
        node._w = Math.max(AST_NODE_W, totalChildrenWidth);
        node._h = AST_NODE_H;
    } else {
        // Vertical para muitos filhos
        node._w = AST_NODE_W + 150; // espaço para indent
        const totalChildrenHeight = children.reduce((sum, c) => sum + (c._h || AST_NODE_H), 0) + (children.length - 1) * 20;
        node._h = totalChildrenHeight;
    }
}

function position(node, x, y) {
    node.x = x + AST_NODE_W / 2;
    node.y = y + AST_NODE_H / 2;
    const children = node.children || [];
    if (children.length === 0) return;

    if (children.length <= 3) {
        // Layout horizontal para poucos filhos
        let curX = x;
        for (const c of children) {
            const cw = c._w || AST_NODE_W;
            position(c, curX, y + AST_NODE_H + AST_V_SPACING);
            curX += cw + AST_H_SPACING;
        }
    } else {
        // Layout vertical para muitos filhos
        let curY = y + AST_NODE_H + AST_V_SPACING;
        for (const c of children) {
            const ch = c._h || AST_NODE_H;
            position(c, x + 150, curY);
            curY += ch + 20;
        }
    }
}

function shift(node, dx, dy) {
    node.x += dx; node.y += dy;
    if (node.children) node.children.forEach(c => shift(c, dx, dy));
}

function computeBounds(node, b = { minX: Infinity, minY: Infinity, maxX: -Infinity, maxY: -Infinity }) {
    b.minX = Math.min(b.minX, node.x - AST_NODE_W / 2);
    b.maxX = Math.max(b.maxX, node.x + AST_NODE_W / 2);
    b.minY = Math.min(b.minY, node.y - AST_NODE_H / 2);
    b.maxY = Math.max(b.maxY, node.y + AST_NODE_H / 2);
    if (node.children) node.children.forEach(c => computeBounds(c, b));
    return b;
}

// ===== Tokens tree helpers =====
function buildTokensTree(tokens) {
    if (!tokens || tokens.length === 0) return null;
    // Agrupar por linha
    const byLine = new Map();
    for (const t of tokens) {
        const line = t.line ?? 0;
        if (!byLine.has(line)) byLine.set(line, []);
        byLine.get(line).push(t);
    }
    const lineNodes = Array.from(byLine.entries())
        .sort((a, b) => a[0] - b[0])
        .map(([line, list]) => ({
            type: `Linha ${line}`,
            label: `tokens: ${list.length}`,
            children: list.map(tok => ({
                type: tok.type || 'TOKEN',
                label: `${String(tok.lexeme ?? '').replace(/\n/g, '\\n')} (${tok.line}:${tok.column})`,
                children: []
            }))
        }));
    return {
        type: 'Tokens',
        label: `linhas: ${lineNodes.length}`,
        children: lineNodes
    };
}

function buildCombinedTree(astPart, tokensPart) {
    const children = [];
    if (astPart) {
        children.push({ type: 'AST', label: 'Árvore Sintática', children: [astPart] });
    }
    if (tokensPart) {
        children.push(tokensPart);
    }
    if (children.length === 0) return null;
    return { type: 'Análise', label: '', children };
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
