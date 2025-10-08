import java.util.ArrayList;
import java.util.List;

/**
 * Analisador Sintático (Parser) para a linguagem MiniPar OOP
 * Tema 2 - Compiladores 2025.1
 *
 * Parser Descendente Recursivo que constrói uma Árvore Sintática Abstrata (AST)
 * com suporte a Programação Orientada a Objetos
 */
public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Ponto de entrada do parser
     * programa ::= { declaracao }
     */
    public Program parse() {
        List<ASTNode> statements = new ArrayList<>();

        while (!isAtEnd()) {
            try {
                ASTNode decl = declaration();
                if (decl != null) {
                    statements.add(decl);
                }
            } catch (ParseException e) {
                System.err.println(e.getMessage());
                synchronize();
            }
        }

        return new Program(statements);
    }

    /**
     * declaracao ::= classDecl | funcDecl | varDecl | statement
     */
    private ASTNode declaration() {
        try {
            if (match(TokenType.CLASS)) {
                return classDeclaration();
            }
            if (match(TokenType.FUNC)) {
                return functionDeclaration();
            }
            if (match(TokenType.VAR)) {
                return varDeclaration();
            }

            return statement();
        } catch (ParseException e) {
            throw e;
        }
    }

    /**
     * classDecl ::= "class" ID [ "extends" ID ] "{" { varDecl | methodDecl } "}"
     */
    private ClassDecl classDeclaration() {
        Token nameToken = consume(TokenType.ID, "Esperado nome da classe");
        String className = nameToken.getLexeme();

        String superClass = null;
        if (match(TokenType.EXTENDS)) {
            Token superToken = consume(TokenType.ID, "Esperado nome da superclasse");
            superClass = superToken.getLexeme();
        }

        consume(TokenType.LEFT_BRACE, "Esperado '{' após declaração da classe");

        List<VarDecl> attributes = new ArrayList<>();
        List<MethodDecl> methods = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            // Verifica se é um método ou atributo
            if (checkSequence(TokenType.ID, TokenType.ID)) {
                // É um método: tipo id (...)
                methods.add(methodDeclaration());
            } else if (match(TokenType.VAR)) {
                // É um atributo
                attributes.add((VarDecl) varDeclaration());
            } else {
                throw error(peek(), "Esperado declaração de método ou atributo");
            }
        }

        consume(TokenType.RIGHT_BRACE, "Esperado '}' após corpo da classe");

        return new ClassDecl(className, superClass, attributes, methods);
    }

    /**
     * methodDecl ::= tipo ID "(" parametros ")" "{" { statement } "}"
     */
    private MethodDecl methodDeclaration() {
        Token returnTypeToken = advance();
        String returnType = returnTypeToken.getLexeme();

        Token nameToken = consume(TokenType.ID, "Esperado nome do método");
        String methodName = nameToken.getLexeme();

        consume(TokenType.LEFT_PAREN, "Esperado '(' após nome do método");
        List<Parameter> parameters = parameters();
        consume(TokenType.RIGHT_PAREN, "Esperado ')' após parâmetros");

        consume(TokenType.LEFT_BRACE, "Esperado '{' antes do corpo do método");
        List<ASTNode> body = block();
        consume(TokenType.RIGHT_BRACE, "Esperado '}' após corpo do método");

        return new MethodDecl(returnType, methodName, parameters, body);
    }

    /**
     * funcDecl ::= "func" ID "(" parametros ")" "->" tipo "{" { statement } "}"
     */
    private FuncDecl functionDeclaration() {
        Token nameToken = consume(TokenType.ID, "Esperado nome da função");
        String funcName = nameToken.getLexeme();

        consume(TokenType.LEFT_PAREN, "Esperado '(' após nome da função");
        List<Parameter> parameters = parameters();
        consume(TokenType.RIGHT_PAREN, "Esperado ')' após parâmetros");

        consume(TokenType.ARROW, "Esperado '->' após parâmetros da função");
        Token returnTypeToken = advance();
        String returnType = returnTypeToken.getLexeme();

        consume(TokenType.LEFT_BRACE, "Esperado '{' antes do corpo da função");
        List<ASTNode> body = block();
        consume(TokenType.RIGHT_BRACE, "Esperado '}' após corpo da função");

        return new FuncDecl(funcName, returnType, parameters, body);
    }

    /**
     * parametros ::= [ parametro { "," parametro } ]
     */
    private List<Parameter> parameters() {
        List<Parameter> params = new ArrayList<>();

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                Token paramName = consume(TokenType.ID, "Esperado nome do parâmetro");
                consume(TokenType.COLON, "Esperado ':' após nome do parâmetro");
                Token paramType = advance();

                params.add(new Parameter(paramName.getLexeme(), paramType.getLexeme()));
            } while (match(TokenType.COMMA));
        }

        return params;
    }

    /**
     * varDecl ::= "var" ID ":" tipo [ "=" expression ]
     */
    private VarDecl varDeclaration() {
        Token nameToken = consume(TokenType.ID, "Esperado nome da variável");
        String varName = nameToken.getLexeme();

        consume(TokenType.COLON, "Esperado ':' após nome da variável");
        Token typeToken = advance();
        String type = typeToken.getLexeme();

        ASTNode initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }

        return new VarDecl(varName, type, initializer);
    }

    /**
     * statement ::= exprStmt | ifStmt | whileStmt | returnStmt | breakStmt |
     *               continueStmt | seqBlock | parBlock | block
     */
    private ASTNode statement() {
        if (match(TokenType.IF)) return ifStatement();
        if (match(TokenType.WHILE)) return whileStatement();
        if (match(TokenType.RETURN)) return returnStatement();
        if (match(TokenType.BREAK)) return new BreakStmt();
        if (match(TokenType.CONTINUE)) return new ContinueStmt();
        if (match(TokenType.SEQ)) return seqBlock();
        if (match(TokenType.PAR)) return parBlock();
        if (match(TokenType.LEFT_BRACE)) {
            List<ASTNode> stmts = block();
            consume(TokenType.RIGHT_BRACE, "Esperado '}' após bloco");
            return new Program(stmts); // Bloco anônimo
        }

        return expressionStatement();
    }

    /**
     * ifStmt ::= "if" "(" expression ")" statement [ "else" statement ]
     */
    private IfStmt ifStatement() {
        consume(TokenType.LEFT_PAREN, "Esperado '(' após 'if'");
        ASTNode condition = expression();
        consume(TokenType.RIGHT_PAREN, "Esperado ')' após condição");

        consume(TokenType.LEFT_BRACE, "Esperado '{' após condição do if");
        List<ASTNode> thenBranch = block();
        consume(TokenType.RIGHT_BRACE, "Esperado '}' após bloco then");

        List<ASTNode> elseBranch = null;
        if (match(TokenType.ELSE)) {
            consume(TokenType.LEFT_BRACE, "Esperado '{' após else");
            elseBranch = block();
            consume(TokenType.RIGHT_BRACE, "Esperado '}' após bloco else");
        }

        return new IfStmt(condition, thenBranch, elseBranch);
    }

    /**
     * whileStmt ::= "while" "(" expression ")" statement
     */
    private WhileStmt whileStatement() {
        consume(TokenType.LEFT_PAREN, "Esperado '(' após 'while'");
        ASTNode condition = expression();
        consume(TokenType.RIGHT_PAREN, "Esperado ')' após condição");

        consume(TokenType.LEFT_BRACE, "Esperado '{' após condição do while");
        List<ASTNode> body = block();
        consume(TokenType.RIGHT_BRACE, "Esperado '}' após corpo do while");

        return new WhileStmt(condition, body);
    }

    /**
     * returnStmt ::= "return" expression
     */
    private ReturnStmt returnStatement() {
        ASTNode value = null;
        if (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            value = expression();
        }
        return new ReturnStmt(value);
    }

    /**
     * seqBlock ::= "seq" "{" { statement } "}"
     */
    private SeqBlock seqBlock() {
        consume(TokenType.LEFT_BRACE, "Esperado '{' após 'seq'");
        List<ASTNode> statements = block();
        consume(TokenType.RIGHT_BRACE, "Esperado '}' após bloco seq");
        return new SeqBlock(statements);
    }

    /**
     * parBlock ::= "par" "{" { statement } "}"
     */
    private ParBlock parBlock() {
        consume(TokenType.LEFT_BRACE, "Esperado '{' após 'par'");
        List<ASTNode> statements = block();
        consume(TokenType.RIGHT_BRACE, "Esperado '}' após bloco par");
        return new ParBlock(statements);
    }

    /**
     * block ::= { statement }
     */
    private List<ASTNode> block() {
        List<ASTNode> statements = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            ASTNode decl = declaration();
            if (decl != null) {
                statements.add(decl);
            }
        }

        return statements;
    }

    /**
     * exprStmt ::= expression
     */
    private ASTNode expressionStatement() {
        return expression();
    }

    /**
     * expression ::= assignment
     */
    private ASTNode expression() {
        return assignment();
    }

    /**
     * assignment ::= ID "=" assignment | logicOr
     */
    private ASTNode assignment() {
        ASTNode expr = logicOr();

        if (match(TokenType.EQUAL)) {
            ASTNode value = assignment();

            if (expr instanceof Identifier) {
                String name = ((Identifier) expr).name;
                return new Assignment(name, value);
            }

            throw error(previous(), "Alvo de atribuição inválido");
        }

        return expr;
    }

    /**
     * logicOr ::= logicAnd { "||" logicAnd }
     */
    private ASTNode logicOr() {
        ASTNode expr = logicAnd();

        while (match(TokenType.OR)) {
            String operator = previous().getLexeme();
            ASTNode right = logicAnd();
            expr = new BinaryExpr(expr, operator, right);
        }

        return expr;
    }

    /**
     * logicAnd ::= equality { "&&" equality }
     */
    private ASTNode logicAnd() {
        ASTNode expr = equality();

        while (match(TokenType.AND)) {
            String operator = previous().getLexeme();
            ASTNode right = equality();
            expr = new BinaryExpr(expr, operator, right);
        }

        return expr;
    }

    /**
     * equality ::= comparison { ( "==" | "!=" ) comparison }
     */
    private ASTNode equality() {
        ASTNode expr = comparison();

        while (match(TokenType.EQUAL_EQUAL, TokenType.NOT_EQUAL)) {
            String operator = previous().getLexeme();
            ASTNode right = comparison();
            expr = new BinaryExpr(expr, operator, right);
        }

        return expr;
    }

    /**
     * comparison ::= term { ( ">" | ">=" | "<" | "<=" ) term }
     */
    private ASTNode comparison() {
        ASTNode expr = term();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            String operator = previous().getLexeme();
            ASTNode right = term();
            expr = new BinaryExpr(expr, operator, right);
        }

        return expr;
    }

    /**
     * term ::= factor { ( "+" | "-" ) factor }
     */
    private ASTNode term() {
        ASTNode expr = factor();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            String operator = previous().getLexeme();
            ASTNode right = factor();
            expr = new BinaryExpr(expr, operator, right);
        }

        return expr;
    }

    /**
     * factor ::= unary { ( "*" | "/" | "%" ) unary }
     */
    private ASTNode factor() {
        ASTNode expr = unary();

        while (match(TokenType.STAR, TokenType.SLASH, TokenType.MOD)) {
            String operator = previous().getLexeme();
            ASTNode right = unary();
            expr = new BinaryExpr(expr, operator, right);
        }

        return expr;
    }

    /**
     * unary ::= ( "!" | "-" ) unary | call
     */
    private ASTNode unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            String operator = previous().getLexeme();
            ASTNode right = unary();
            return new UnaryExpr(operator, right);
        }

        return call();
    }

    /**
     * call ::= primary { "(" arguments ")" | "." ID }
     */
    private ASTNode call() {
        ASTNode expr = primary();

        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(TokenType.DOT)) {
                Token name = consume(TokenType.ID, "Esperado nome do método/propriedade após '.'");

                // Verifica se é uma chamada de método
                if (match(TokenType.LEFT_PAREN)) {
                    List<ASTNode> arguments = arguments();
                    consume(TokenType.RIGHT_PAREN, "Esperado ')' após argumentos");
                    expr = new MethodCall(expr, name.getLexeme(), arguments);
                } else {
                    // É acesso a propriedade - por enquanto tratamos como identifier
                    expr = new Identifier(name.getLexeme());
                }
            } else {
                break;
            }
        }

        return expr;
    }

    /**
     * Finaliza uma chamada de função
     */
    private ASTNode finishCall(ASTNode callee) {
        List<ASTNode> arguments = arguments();
        consume(TokenType.RIGHT_PAREN, "Esperado ')' após argumentos");

        if (callee instanceof Identifier) {
            return new FunctionCall(((Identifier) callee).name, arguments);
        }

        throw error(previous(), "Chamada inválida");
    }

    /**
     * arguments ::= [ expression { "," expression } ]
     */
    private List<ASTNode> arguments() {
        List<ASTNode> args = new ArrayList<>();

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                args.add(expression());
            } while (match(TokenType.COMMA));
        }

        return args;
    }

    /**
     * primary ::= NUMBER | STRING | "true" | "false" | ID |
     *             "new" ID "(" arguments ")" | "(" expression ")"
     */
    private ASTNode primary() {
        if (match(TokenType.TRUE)) {
            return new Literal(true);
        }
        if (match(TokenType.FALSE)) {
            return new Literal(false);
        }

        if (match(TokenType.NUMBER)) {
            String lexeme = previous().getLexeme();
            if (lexeme.contains(".")) {
                return new Literal(Double.parseDouble(lexeme));
            } else {
                return new Literal(Integer.parseInt(lexeme));
            }
        }

        if (match(TokenType.STRING)) {
            return new Literal(previous().getLexeme());
        }

        if (match(TokenType.NEW)) {
            Token className = consume(TokenType.ID, "Esperado nome da classe após 'new'");
            consume(TokenType.LEFT_PAREN, "Esperado '(' após nome da classe");
            List<ASTNode> arguments = arguments();
            consume(TokenType.RIGHT_PAREN, "Esperado ')' após argumentos do construtor");
            return new NewInstance(className.getLexeme(), arguments);
        }

        if (match(TokenType.ID)) {
            return new Identifier(previous().getLexeme());
        }

        if (match(TokenType.LEFT_PAREN)) {
            ASTNode expr = expression();
            consume(TokenType.RIGHT_PAREN, "Esperado ')' após expressão");
            return expr;
        }

        throw error(peek(), "Esperado expressão");
    }

    // ===== Métodos auxiliares =====

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().getType() == type;
    }

    private boolean checkSequence(TokenType type1, TokenType type2) {
        if (current + 1 >= tokens.size()) return false;
        return tokens.get(current).getType() == type1 &&
               tokens.get(current + 1).getType() == type2;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().getType() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private ParseException error(Token token, String message) {
        String error = "[Erro Sintático] Linha " + token.getLine() +
                      ", Coluna " + token.getColumn() + ": " + message +
                      " (encontrado: '" + token.getLexeme() + "')";
        return new ParseException(error);
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().getType() == TokenType.NEWLINE) return;

            switch (peek().getType()) {
                case CLASS:
                case FUNC:
                case VAR:
                case IF:
                case WHILE:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}

/**
 * Exceção customizada para erros de parsing
 */
class ParseException extends RuntimeException {
    public ParseException(String message) {
        super(message);
    }
}

