package lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe responsável pela análise léxica do código-fonte da linguagem MiniPar OOP.
 * Converte o texto de entrada em uma lista de tokens, identificando palavras-chave,
 * operadores, delimitadores, literais e identificadores.
 * <p>
 * Utilizada como etapa inicial do processo de compilação/interpretação.
 */
public class Lexer {
    /** Código-fonte a ser analisado */
    private final String source;
    /** Lista de tokens reconhecidos */
    private final List<Token> tokens = new ArrayList<>();
    /** Índice do início do token atual */
    private int start = 0;
    /** Índice do caractere atual */
    private int current = 0;
    /** Número da linha atual (inicia em 1) */
    private int line = 1;
    /** Número da coluna atual (inicia em 1) */
    private int column = 1;

    /**
     * Mapeamento de palavras-chave para seus respectivos tipos de token.
     * Inclui comandos de controle, tipos, operadores e palavras reservadas da linguagem.
     */
    private static final Map<String, TokenType> keywords = new HashMap<>();

    static {
        // Palavras-chave de controle
        keywords.put("var", TokenType.VAR);
        keywords.put("func", TokenType.FUNC);
    keywords.put("print", TokenType.PRINT);
    keywords.put("input", TokenType.INPUT);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("while", TokenType.WHILE);
        keywords.put("for", TokenType.FOR);
    keywords.put("do", TokenType.DO);
        keywords.put("return", TokenType.RETURN);
        keywords.put("break", TokenType.BREAK);
        keywords.put("continue", TokenType.CONTINUE);
        keywords.put("in", TokenType.IN);

        // Palavras-chave OOP
        keywords.put("class", TokenType.CLASS);
        keywords.put("extends", TokenType.EXTENDS);
        keywords.put("new", TokenType.NEW);
        keywords.put("this", TokenType.THIS);
        keywords.put("super", TokenType.SUPER);

        // Tipos
        keywords.put("number", TokenType.TYPE_NUMBER);
        keywords.put("string", TokenType.TYPE_STRING);
        keywords.put("bool", TokenType.TYPE_BOOL);
        keywords.put("void", TokenType.TYPE_VOID);
        keywords.put("list", TokenType.TYPE_LIST);
        keywords.put("dict", TokenType.TYPE_DICT);

        // Literais booleanos
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);

        // Palavras-chave específicas do MiniPar
        keywords.put("seq", TokenType.SEQ);
        keywords.put("par", TokenType.PAR);
        keywords.put("c_channel", TokenType.C_CHANNEL);
        keywords.put("s_channel", TokenType.S_CHANNEL);
        keywords.put("send", TokenType.SEND);
        keywords.put("receive", TokenType.RECEIVE);
    }

    /**
     * Construtor do analisador léxico.
     * @param source Código-fonte a ser analisado
     */
    public Lexer(String source) {
        this.source = source;
    }

    /**
     * Realiza a análise léxica completa do código fonte.
     * Percorre todo o texto, reconhecendo e armazenando tokens.
     * @return Lista de tokens reconhecidos
     */
    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        // Adiciona token de fim de arquivo
        tokens.add(new Token(TokenType.EOF, "", line, column));
        return tokens;
    }

    /**
     * Reconhece e adiciona um único token à lista, de acordo com o caractere atual.
     * Utiliza switch para identificar delimitadores, operadores, literais, comentários e identificadores.
     */
    private void scanToken() {
        char c = advance();

        switch (c) {
            // Delimitadores simples
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case '[': addToken(TokenType.LEFT_BRACKET); break;
            case ']': addToken(TokenType.RIGHT_BRACKET); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case ':': addToken(TokenType.COLON); break;
            case ';': addToken(TokenType.SEMICOLON); break;

            // Operadores aritméticos
            case '+': addToken(TokenType.PLUS); break;
            case '%': addToken(TokenType.MOD); break;
            case '*': addToken(TokenType.STAR); break;

            // Operadores que podem ser compostos
            case '-':
                if (match('>')) {
                    addToken(TokenType.ARROW); // Operador de seta
                } else {
                    addToken(TokenType.MINUS);
                }
                break;
            case '/':
                if (match('/')) {
                    // Comentário de linha: ignora até o fim da linha
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    // Comentário de bloco
                    blockComment();
                } else {
                    addToken(TokenType.SLASH);
                }
                break;
            case '!':
                addToken(match('=') ? TokenType.NOT_EQUAL : TokenType.BANG);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '&':
                if (match('&')) {
                    addToken(TokenType.AND); // Operador lógico AND
                }
                break;
            case '|':
                if (match('|')) {
                    addToken(TokenType.OR); // Operador lógico OR
                }
                break;

            // Comentário estilo Python
            case '#':
                while (peek() != '\n' && !isAtEnd()) advance();
                break;

            // Espaços em branco (whitespace)
            case ' ':
            case '\r':
            case '\t':
                break;

            case '\n':
                line++;
                column = 0;
                break;

            // Literal de string (string literal)
            case '"':
                string();
                break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    error("Caractere inesperado: " + c);
                }
                break;
        }
    }

    /**
     * Reconhece comentários de bloco (/* ... *&#47;).
     * Avança até encontrar o fechamento ou o fim do arquivo.
     * Emite erro caso o comentário não seja fechado.
     */
    private void blockComment() {
        while (!isAtEnd()) {
            if (peek() == '*' && peekNext() == '/') {
                advance(); // consome '*'
                advance(); // consome '/'
                return;
            }
            if (peek() == '\n') {
                line++;
                column = 0;
            }
            advance();
        }
        error("Comentário de bloco não fechado");
    }

    /**
     * Reconhece literais de string delimitados por aspas duplas.
     * Permite quebra de linha dentro da string.
     * Emite erro caso a string não seja fechada.
     */
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
                column = 0;
            }
            advance();
        }

        if (isAtEnd()) {
            error("String não fechada");
            return;
        }

        // Consome o " final
        advance();

        // Remove as aspas
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    /**
     * Reconhece números inteiros e decimais.
     * Aceita ponto para números decimais.
     */
    private void number() {
        while (isDigit(peek())) advance();

        // Verifica se é decimal
        if (peek() == '.' && isDigit(peekNext())) {
            do advance(); // consome o '.'
            while (isDigit(peek()));
        }

        addToken(TokenType.NUMBER);
    }

    /**
     * Reconhece identificadores e palavras-chave.
     * Identificadores podem conter letras, dígitos e underline.
     * Se o texto corresponder a uma palavra-chave, atribui o tipo correspondente.
     */
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.getOrDefault(text, TokenType.ID);
        addToken(type);
    }

    // Métodos auxiliares

    /**
     * Verifica se o próximo caractere corresponde ao esperado e avança se verdadeiro.
     * @param expected Caractere esperado
     * @return true se corresponde, false caso contrário
     */
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        column++;
        return true;
    }

    /**
     * Retorna o caractere atual sem avançar o índice.
     * @return Caractere atual ou '\0' se fim do texto
     */
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    /**
     * Retorna o próximo caractere sem avançar o índice.
     * @return Próximo caractere ou '\0' se fim do texto
     */
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    /**
     * Verifica se o caractere é um dígito (0-9).
     * @param c Caractere a verificar
     * @return true se for dígito
     */
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Verifica se o caractere é uma letra (a-z, A-Z) ou underline.
     * @param c Caractere a verificar
     * @return true se for letra ou underline
     */
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
               c == '_';
    }

    /**
     * Verifica se o caractere é alfanumérico (letra ou dígito).
     * @param c Caractere a verificar
     * @return true se for alfanumérico
     */
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    /**
     * Verifica se chegou ao fim do texto.
     * @return true se fim do texto
     */
    private boolean isAtEnd() {
        return current >= source.length();
    }

    /**
     * Avança para o próximo caractere e retorna o atual.
     * Atualiza a coluna.
     * @return Caractere atual
     */
    private char advance() {
        column++;
        return source.charAt(current++);
    }

    /**
     * Adiciona um token à lista, sem literal.
     * @param type Tipo do token
     */
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    /**
     * Adiciona um token à lista, com literal opcional.
     * @param type Tipo do token
     * @param literal Valor literal do token (pode ser null)
     */
    private void addToken(TokenType type, String literal) {
        String text = source.substring(start, current);
        if (literal == null) literal = text;
        tokens.add(new Token(type, literal, line, column - text.length()));
    }

    /**
     * Exibe mensagem de erro léxico no console, indicando linha e coluna.
     * @param message Mensagem de erro
     */
    private void error(String message) {
        System.err.println("[Erro Léxico] Linha " + line + ", Coluna " + column + ": " + message);
    }
}