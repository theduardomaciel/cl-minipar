package lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Analisador Léxico (Lexer) para a linguagem MiniPar OOP
 * Tema 2 - Compiladores 2025.1
 *
 * Responsável por transformar o código-fonte em uma sequência de tokens
 */
public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int column = 1;

    private static final Map<String, TokenType> keywords = new HashMap<>();

    static {
        // Palavras-chave de controle
        keywords.put("var", TokenType.VAR);
        keywords.put("func", TokenType.FUNC);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("while", TokenType.WHILE);
        keywords.put("for", TokenType.FOR);
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
    }

    public Lexer(String source) {
        this.source = source;
    }

    /**
     * Realiza a análise léxica completa do código fonte
     * @return Lista de tokens reconhecidos
     */
    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", line, column));
        return tokens;
    }

    /**
     * Reconhece um único token
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
                    addToken(TokenType.ARROW);
                } else {
                    addToken(TokenType.MINUS);
                }
                break;
            case '/':
                if (match('/')) {
                    // Comentário de linha - ignora até o fim da linha
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
                    addToken(TokenType.AND);
                }
                break;
            case '|':
                if (match('|')) {
                    addToken(TokenType.OR);
                }
                break;

            // Comentário Python-style
            case '#':
                while (peek() != '\n' && !isAtEnd()) advance();
                break;

            // Whitespace
            case ' ':
            case '\r':
            case '\t':
                break;

            case '\n':
                line++;
                column = 0;
                break;

            // String literal
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
     * Reconhece comentários de bloco
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
     * Reconhece strings
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
     * Reconhece números (inteiros e decimais)
     */
    private void number() {
        while (isDigit(peek())) advance();

        // Verifica se é decimal
        if (peek() == '.' && isDigit(peekNext())) {
            advance(); // consome o '.'
            while (isDigit(peek())) advance();
        }

        addToken(TokenType.NUMBER);
    }

    /**
     * Reconhece identificadores e palavras-chave
     */
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.getOrDefault(text, TokenType.ID);
        addToken(type);
    }

    // Métodos auxiliares

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        column++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
               c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        column++;
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, String literal) {
        String text = source.substring(start, current);
        if (literal == null) literal = text;
        tokens.add(new Token(type, literal, line, column - text.length()));
    }

    private void error(String message) {
        System.err.println("[Erro Léxico] Linha " + line + ", Coluna " + column + ": " + message);
    }
}