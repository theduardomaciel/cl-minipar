/**
 * Enumeração dos tipos de tokens da linguagem MiniPar OOP
 * Tema 2 - Compiladores 2025.1
 */
public enum TokenType {
    // Literais
    NUMBER, STRING, TRUE, FALSE, ID,

    // Palavras-chave
    VAR, FUNC, IF, ELSE, WHILE, FOR, RETURN, BREAK, CONTINUE,
    CLASS, EXTENDS, NEW, THIS, SUPER,

    // Tipos
    TYPE_NUMBER, TYPE_STRING, TYPE_BOOL, TYPE_VOID, TYPE_LIST, TYPE_DICT,

    // Operadores aritméticos
    PLUS, MINUS, STAR, SLASH, MOD,

    // Operadores relacionais
    EQUAL, EQUAL_EQUAL, NOT_EQUAL, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL,

    // Operadores lógicos
    AND, OR, BANG,

    // Delimitadores
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, LEFT_BRACKET, RIGHT_BRACKET,
    COMMA, DOT, COLON, SEMICOLON, ARROW,

    // Palavras-chave específicas do MiniPar
    SEQ, PAR, C_CHANNEL, S_CHANNEL, IN,

    // Controle
    NEWLINE, EOF
}