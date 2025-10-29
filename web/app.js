// Aplicação principal do interpretador MiniPar Web
let editor;
const API_URL = 'http://localhost:8080';

// Inicialização
document.addEventListener('DOMContentLoaded', function () {
    initializeEditor();
    setupEventListeners();
    loadInitialExample();
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
        const response = await fetch(`${API_URL}/execute`, {
            method: 'POST',
            headers: {
                'Content-Type': 'text/plain; charset=utf-8'
            },
            body: code
        });

        if (!response.ok) {
            throw new Error(`Erro HTTP: ${response.status}`);
        }

        const result = await response.json();

        if (result.success) {
            showOutput(result.output || 'Execução concluída sem saída.', 'success');
        } else {
            const fullError = (result.output ? result.output + '\n\n' : '') + result.error;
            showOutput(fullError || 'Erro desconhecido durante a execução.', 'error');
        }

    } catch (error) {
        showOutput(
            `Erro ao conectar com o servidor:\n${error.message}\n\n` +
            `Verifique se o servidor está rodando em ${API_URL}`,
            'error'
        );
    } finally {
        // Reabilitar botão
        runButton.disabled = false;
        runButton.innerHTML = '▶️ Executar';
    }
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
