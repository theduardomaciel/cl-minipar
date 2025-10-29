package lexer;

/**
 * Enumeração que representa os tipos de tokens reconhecidos pelo lexer.
 * Cada valor corresponde a um tipo específico de token na linguagem MiniPar.
 */
public enum TokenType {
    // Literais
    NUMBER,      // Número literal
    STRING,      // String literal
    TRUE,        // Literal booleano verdadeiro
    FALSE,       // Literal booleano falso
    ID,          // Identificador

    // Palavras-chave
    VAR,         // Declaração de variável
    FUNC,        // Declaração de função
    PRINT,       // Saída (sem quebra de linha)
    PRINTLN,     // Saída (com quebra de linha)
    INPUT,       // Entrada
    IF,          // Estrutura condicional
    ELSE,        // Alternativa condicional
    WHILE,       // Estrutura de repetição
    FOR,         // Estrutura de repetição
    DO,          // Estrutura de repetição do-while
    RETURN,      // Retorno de função
    BREAK,       // Interrupção de laço
    CONTINUE,    // Continuação de laço
    CLASS,       // Declaração de classe
    EXTENDS,     // Herança de classe
    NEW,         // Instanciação de objeto
    THIS,        // Referência ao objeto atual
    SUPER,       // Referência à superclasse

    // Tipos
    TYPE_NUMBER, // Tipo numérico
    TYPE_STRING, // Tipo string
    TYPE_BOOL,   // Tipo booleano
    TYPE_VOID,   // Tipo void
    TYPE_LIST,   // Tipo lista
    TYPE_DICT,   // Tipo dicionário

    // Operadores aritméticos
    PLUS,        // Soma
    MINUS,       // Subtração
    STAR,        // Multiplicação
    SLASH,       // Divisão
    MOD,         // Módulo

    // Operadores relacionais
    EQUAL,           // Atribuição
    EQUAL_EQUAL,     // Igualdade
    NOT_EQUAL,       // Diferença
    LESS,            // Menor que
    LESS_EQUAL,      // Menor ou igual
    GREATER,         // Maior que
    GREATER_EQUAL,   // Maior ou igual

    // Operadores lógicos
    AND,         // E lógico
    OR,          // Ou lógico
    BANG,        // Negação lógica

    // Delimitadores
    LEFT_PAREN,      // Parêntese esquerdo
    RIGHT_PAREN,     // Parêntese direito
    LEFT_BRACE,      // Chave esquerda
    RIGHT_BRACE,     // Chave direita
    LEFT_BRACKET,    // Colchete esquerdo
    RIGHT_BRACKET,   // Colchete direito
    COMMA,           // Vírgula
    DOT,             // Ponto
    COLON,           // Dois pontos
    SEMICOLON,       // Ponto e vírgula
    ARROW,           // Seta (->)

    // Palavras-chave específicas do MiniPar
    SEQ,         // Sequencial
    PAR,         // Paralelo
    C_CHANNEL,   // Canal de comunicação
    S_CHANNEL,   // Canal de sincronização
    SEND,        // Envio de mensagem
    RECEIVE,     // Recebimento de mensagem
    IN,          // Palavra-chave 'in'

    // Controle
    NEWLINE,     // Nova linha
    EOF          // Fim de arquivo
}