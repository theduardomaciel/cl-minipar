package parser;

import lexer.Token;
import lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser Descendente Recursivo para construção de Árvore Sintática Abstrata
 * (AST)
 * com suporte a Programação Orientada a Objetos.
 * <p>
 * Responsável por analisar uma lista de tokens e construir a estrutura
 * sintática
 * correspondente ao código-fonte, incluindo suporte a classes, funções,
 * variáveis,
 * blocos sequenciais e paralelos, expressões e instruções de controle de fluxo.
 */
public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    /**
     * Construtor do Parser.
     *
     * @param tokens Lista de tokens a serem analisados.
     */
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Ponto de entrada do parser.
     * Realiza o parsing do programa, retornando a AST principal.
     *
     * @return Program ASTNode representando o programa.
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
     * Realiza o parsing de uma declaração.
     * Pode ser uma declaração de instância, classe, função, variável ou uma
     * instrução.
     *
     * @return ASTNode representando a declaração encontrada.
     */
    private ASTNode declaration() {
        if (match(TokenType.CLASS)) {
            return classDeclaration();
        }
        if (match(TokenType.FUNC)) {
            return functionDeclaration();
        }
        if (match(TokenType.C_CHANNEL)) {
            return channelDeclaration();
        }

        if (isVarDeclStart()) {
            return varDeclaration();
        }

        return statement();
    }

    /**
     * Realiza o parsing de uma declaração de instância
     *
     * @return VarDecl representando a declaração de instância.
     * @throws ParseException se a sintaxe estiver incorreta.
     */
    private VarDecl instanceDeclaration() {
        Token typeToken = advance(); // Tipo
        String type = typeToken.lexeme();
        Token nameToken = consume(TokenType.ID, "Esperado nome da variável após tipo");
        String varName = nameToken.lexeme();
        consume(TokenType.EQUAL, "Esperado '=' após nome da variável");
        consume(TokenType.NEW, "Esperado 'new' para instanciar objeto");
        Token classToken = consume(TokenType.ID, "Esperado nome da classe após 'new'");
        String className = classToken.lexeme();
        consume(TokenType.LEFT_PAREN, "Esperado '(' após nome da classe");
        List<ASTNode> arguments = arguments();
        consume(TokenType.RIGHT_PAREN, "Esperado ')' após argumentos do construtor");
        consume(TokenType.SEMICOLON, "Esperado ';' ao final da declaração de instância");
        // Usa VarDecl para representar a instância
        ASTNode initializer = new NewInstance(className, arguments);
        return new VarDecl(varName, type, initializer);
    }

    /**
     * Realiza o parsing de uma declaração de classe.
     *
     * @return ClassDecl representando a classe.
     */
    private ClassDecl classDeclaration() {
        Token nameToken = consume(TokenType.ID, "Esperado nome da classe");
        String className = nameToken.lexeme();

        String superClass = null;
        if (match(TokenType.EXTENDS)) {
            Token superToken = consume(TokenType.ID, "Esperado nome da superclasse");
            superClass = superToken.lexeme();
        }

        consume(TokenType.LEFT_BRACE, "Esperado '{' após declaração da classe");

        List<VarDecl> attributes = new ArrayList<>();
        List<MethodDecl> methods = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            // Verifica primeiro atributos, depois construtor e métodos
            if (isVarDeclStart()) {
                // Atributo (declaração de variável)
                attributes.add(varDeclaration());
            } else if (isConstructorStart(className)) {
                methods.add(constructorDeclaration(className));
            } else if (isMethodStart()) {
                // Método: <tipo> <id>(...)
                methods.add(methodDeclaration());
            } else {
                throw error(peek(), "Esperado declaração de método ou atributo");
            }
        }

        consume(TokenType.RIGHT_BRACE, "Esperado '}' após corpo da classe");

        return new ClassDecl(className, superClass, attributes, methods);
    }

    /**
     * Verifica se a posição atual inicia um construtor da classe.
     * Forma aceita: <NomeDaClasse> ( ... ) { ... }
     */
    private boolean isConstructorStart(String className) {
        if (isAtEnd())
            return false;
        Token t0 = peek();
        if (t0.type() != TokenType.ID)
            return false;
        if (!t0.lexeme().equals(className))
            return false;
        if (current + 1 >= tokens.size())
            return false;
        return tokens.get(current + 1).type() == TokenType.LEFT_PAREN;
    }

    /**
     * Realiza o parsing de um construtor: <NomeClasse>(params) { corpo }
     * Representado como MethodDecl com returnType = <NomeClasse> e name =
     * <NomeClasse>
     */
    private MethodDecl constructorDeclaration(String className) {
        // Consome o nome da classe já verificado
        advance(); // nome da classe (como "nome do método")
        consume(TokenType.LEFT_PAREN, "Esperado '(' após nome do construtor");
        List<Parameter> params = parameters();
        consume(TokenType.RIGHT_PAREN, "Esperado ')' após parâmetros do construtor");
        consume(TokenType.LEFT_BRACE, "Esperado '{' antes do corpo do construtor");
        List<ASTNode> body = block();
        consume(TokenType.RIGHT_BRACE, "Esperado '}' após corpo do construtor");
        return new MethodDecl(className, className, params, body);
    }

    /**
     * Realiza o parsing de uma declaração de método.
     *
     * @return MethodDecl representando o método.
     */
    private MethodDecl methodDeclaration() {
        Token returnTypeToken = advance();
        String returnType = returnTypeToken.lexeme();

        Token nameToken = consume(TokenType.ID, "Esperado nome do método");
        String methodName = nameToken.lexeme();

        consume(TokenType.LEFT_PAREN, "Esperado '(' após nome do método");
        List<Parameter> parameters = parameters();
        consume(TokenType.RIGHT_PAREN, "Esperado ')' após parâmetros");

        consume(TokenType.LEFT_BRACE, "Esperado '{' antes do corpo do método");
        List<ASTNode> body = block();
        consume(TokenType.RIGHT_BRACE, "Esperado '}' após corpo do método");

        return new MethodDecl(returnType, methodName, parameters, body);
    }

    /**
     * Realiza o parsing de uma declaração de função.
     *
     * @return FuncDecl representando a função.
     */
    private FuncDecl functionDeclaration() {
        Token nameToken = consume(TokenType.ID, "Esperado nome da função");
        String funcName = nameToken.lexeme();

        consume(TokenType.LEFT_PAREN, "Esperado '(' após nome da função");
        List<Parameter> parameters = parameters();
        consume(TokenType.RIGHT_PAREN, "Esperado ')' após parâmetros");

        consume(TokenType.ARROW, "Esperado '->' após parâmetros da função");
        Token returnTypeToken = advance();
        String returnType = returnTypeToken.lexeme();

        consume(TokenType.LEFT_BRACE, "Esperado '{' antes do corpo da função");
        List<ASTNode> body = block();
        consume(TokenType.RIGHT_BRACE, "Esperado '}' após corpo da função");

        return new FuncDecl(funcName, returnType, parameters, body);
    }

    /**
     * Realiza o parsing da lista de parâmetros.
     * Agora no formato: <tipo> <nome>, separados por vírgula.
     * Ex.: (number x, string nome, MinhaClasse obj)
     *
     * @return Lista de parâmetros.
     */
    private List<Parameter> parameters() {
        List<Parameter> params = new ArrayList<>();

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                Token typeToken = consumeTypeTokenOrId("Esperado tipo do parâmetro");
                Token nameToken = consume(TokenType.ID, "Esperado nome do parâmetro");
                params.add(new Parameter(nameToken.lexeme(), typeToken.lexeme()));
            } while (match(TokenType.COMMA));
        }

        return params;
    }

    /** Consome um token de tipo embutido ou um identificador (tipo customizado). */
    private Token consumeTypeTokenOrId(String message) {
        if (isAtEnd())
            throw error(peek(), message);
        Token t = peek();
        if (isTypeTokenOrId(t.type())) {
            return advance();
        }
        throw error(t, message);
    }

    /**
     * Realiza o parsing de uma declaração de variável.
     *
     * @return VarDecl representando a variável.
     */
    private VarDecl varDeclaration() {
        // <tipo> <identificador> [= <expressao>] ;
        Token typeToken = advance();
        String type = typeToken.lexeme();
        Token nameToken = consume(TokenType.ID, "Esperado nome da variável");
        String varName = nameToken.lexeme();

        ASTNode initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }
        consume(TokenType.SEMICOLON, "Esperado ';' ao final da declaração de variável");
        return new VarDecl(varName, type, initializer);
    }

    /**
     * Realiza o parsing de uma declaração de canal.
     *
     * @return CanalDecl representando a declaração de canal.
     */
    private CanalDecl channelDeclaration() {
        List<String> nomes = new ArrayList<>();
        if (match(TokenType.LEFT_PAREN)) {
            if (!check(TokenType.RIGHT_PAREN)) {
                do {
                    Token nameToken = consume(TokenType.ID, "Esperado nome do canal");
                    nomes.add(nameToken.lexeme());
                } while (match(TokenType.COMMA));
            }
            consume(TokenType.RIGHT_PAREN, "Esperado ')' após nomes dos canais");
        } else {
            // Sintaxe alternativa: c_channel canal comp1 comp2
            Token canal = consume(TokenType.ID, "Esperado nome do canal");
            Token comp1 = consume(TokenType.ID, "Esperado identificador do primeiro componente");
            Token comp2 = consume(TokenType.ID, "Esperado identificador do segundo componente");
            nomes.add(canal.lexeme());
            nomes.add(comp1.lexeme());
            nomes.add(comp2.lexeme());
        }
        consume(TokenType.SEMICOLON, "Esperado ';' ao final da declaração de canal");
        return new CanalDecl(nomes);
    }

    /**
     * Realiza o parsing de uma instrução.
     *
     * @return ASTNode da instrução.
     */
    private ASTNode statement() {
        if (match(TokenType.IF))
            return ifStatement();
        if (match(TokenType.WHILE))
            return whileStatement();
        if (match(TokenType.DO))
            return doWhileStatement();
        if (match(TokenType.FOR))
            return forStatement();
        if (match(TokenType.PRINT))
            return printStatement(false);
        if (match(TokenType.PRINTLN))
            return printStatement(true);
        if (match(TokenType.RETURN))
            return returnStatement();
        if (match(TokenType.BREAK))
            return new BreakStmt();
        if (match(TokenType.CONTINUE))
            return new ContinueStmt();
        if (match(TokenType.SEQ))
            return seqBlock();
        if (match(TokenType.PAR))
            return parBlock();
        if (match(TokenType.LEFT_BRACE)) {
            List<ASTNode> stmts = block();
            consume(TokenType.RIGHT_BRACE, "Esperado '}' após bloco");
            return new Program(stmts); // Bloco anônimo
        }

        return expressionStatement();
    }

    /**
     * Realiza o parsing de uma instrução condicional if.
     *
     * @return IfStmt representando o if.
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
            if (match(TokenType.IF)) {
                // else if: encadeia outro IfStmt
                elseBranch = List.of(ifStatement()); // Encapsula IfStmt em uma lista
            } else {
                consume(TokenType.LEFT_BRACE, "Esperado '{' após else");
                elseBranch = block();
                consume(TokenType.RIGHT_BRACE, "Esperado '}' após bloco else");
            }
        }

        return new IfStmt(condition, thenBranch, elseBranch);
    }

    /**
     * Realiza o parsing de uma instrução de repetição while.
     *
     * @return WhileStmt representando o while.
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
     * Realiza o parsing de uma instrução do-while.
     */
    private DoWhileStmt doWhileStatement() {
        consume(TokenType.LEFT_BRACE, "Esperado '{' após 'do'");
        List<ASTNode> body = block();
        consume(TokenType.RIGHT_BRACE, "Esperado '}' após corpo do 'do'");
        consume(TokenType.WHILE, "Esperado 'while' após bloco do 'do'");
        consume(TokenType.LEFT_PAREN, "Esperado '(' após 'while'");
        ASTNode condition = expression();
        consume(TokenType.RIGHT_PAREN, "Esperado ')' após condição do 'do-while'");
        consume(TokenType.SEMICOLON, "Esperado ';' após do-while");
        return new DoWhileStmt(body, condition);
    }

    /**
     * Realiza o parsing de uma instrução for-in.
     * Forma: for (tipo nome in expressao) { ... }
     */
    private ForStmt forStatement() {
        consume(TokenType.LEFT_PAREN, "Esperado '(' após 'for'");
        // Forma: <tipo> <id> in expr
        Token typeToken = consumeTypeTokenOrId("Esperado tipo no for");
        Token nameToken = consume(TokenType.ID, "Esperado nome da variável do for");
        VarDecl variable = new VarDecl(nameToken.lexeme(), typeToken.lexeme(), null);
        consume(TokenType.IN, "Esperado 'in' no for");
        ASTNode iterable = expression();
        consume(TokenType.RIGHT_PAREN, "Esperado ')' após cláusula do for");
        consume(TokenType.LEFT_BRACE, "Esperado '{' após for");
        List<ASTNode> body = block();
        consume(TokenType.RIGHT_BRACE, "Esperado '}' após corpo do for");
        return new ForStmt(variable, iterable, body);
    }

    /**
     * Realiza o parsing de um comando de impressão.
     */
    private PrintStmt printStatement(boolean newline) {
        consume(TokenType.LEFT_PAREN, "Esperado '(' após 'print' ou 'println'");
        List<ASTNode> args = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                args.add(expression());
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_PAREN, "Esperado ')' após argumentos de print");
        consume(TokenType.SEMICOLON, "Esperado ';' ao final do print");
        return new PrintStmt(args, newline);
    }

    /**
     * Realiza o parsing de uma instrução de retorno.
     * Aceita 'return;' (sem valor) para funções void ou 'return expr;'
     *
     * @return ReturnStmt representando o retorno.
     */
    private ReturnStmt returnStatement() {
        ASTNode value = null;
        // Só tenta parsear expressão se o próximo token não for ';'
        if (!check(TokenType.SEMICOLON)) {
            value = expression();
        }
        consume(TokenType.SEMICOLON, "Esperado ';' ao final da instrução de retorno");
        return new ReturnStmt(value);
    }

    /**
     * Realiza o parsing de um bloco sequencial.
     *
     * @return SeqBlock representando o bloco seq.
     */
    private SeqBlock seqBlock() {
        consume(TokenType.LEFT_BRACE, "Esperado '{' após 'seq'");
        List<ASTNode> statements = block();
        consume(TokenType.RIGHT_BRACE, "Esperado '}' após bloco seq");
        return new SeqBlock(statements);
    }

    /**
     * Realiza o parsing de um bloco paralelo.
     *
     * @return ParBlock representando o bloco par.
     */
    private ParBlock parBlock() {
        consume(TokenType.LEFT_BRACE, "Esperado '{' após 'par'");
        List<ASTNode> statements = block();
        consume(TokenType.RIGHT_BRACE, "Esperado '}' após bloco par");
        return new ParBlock(statements);
    }

    /**
     * Realiza o parsing de um bloco de instruções.
     *
     * @return Lista de ASTNode representando o bloco.
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
     * Realiza o parsing de uma expressão como instrução.
     *
     * @return ASTNode da expressão.
     */
    private ASTNode expressionStatement() {
        ASTNode expr = expression();
        consume(TokenType.SEMICOLON, "Esperado ';' ao final da instrução");
        return expr;
    }

    /**
     * Realiza o parsing de uma expressão.
     *
     * @return ASTNode da expressão.
     */
    private ASTNode expression() {
        return assignment();
    }

    /**
     * Realiza o parsing de uma expressão de atribuição.
     *
     * @return ASTNode da atribuição.
     */
    private ASTNode assignment() {
        ASTNode expr = logicOr();

        if (match(TokenType.EQUAL)) {
            ASTNode value = assignment();

            if (expr instanceof Identifier) {
                String name = ((Identifier) expr).name;
                return new Assignment(name, value);
            }

            if (expr instanceof PropertyAccess) {
                PropertyAccess pa = (PropertyAccess) expr;
                return new PropertyAssign(pa.object, pa.propertyName, value);
            }

            if (expr instanceof IndexExpr) {
                IndexExpr ie = (IndexExpr) expr;
                return new IndexAssign(ie.target, ie.index, value);
            }

            throw error(previous(), "Alvo de atribuição inválido");
        }

        return expr;
    }

    /**
     * Realiza o parsing de uma expressão lógica OR.
     *
     * @return ASTNode da expressão lógica OR.
     */
    private ASTNode logicOr() {
        ASTNode expr = logicAnd();

        while (match(TokenType.OR)) {
            String operator = previous().lexeme();
            ASTNode right = logicAnd();
            expr = new BinaryExpr(expr, operator, right);
        }

        return expr;
    }

    /**
     * Realiza o parsing de uma expressão lógica AND.
     *
     * @return ASTNode da expressão lógica AND.
     */
    private ASTNode logicAnd() {
        ASTNode expr = equality();

        while (match(TokenType.AND)) {
            String operator = previous().lexeme();
            ASTNode right = equality();
            expr = new BinaryExpr(expr, operator, right);
        }

        return expr;
    }

    /**
     * Realiza o parsing de uma expressão de igualdade.
     *
     * @return ASTNode da expressão de igualdade.
     */
    private ASTNode equality() {
        ASTNode expr = comparison();

        while (match(TokenType.EQUAL_EQUAL, TokenType.NOT_EQUAL)) {
            String operator = previous().lexeme();
            ASTNode right = comparison();
            expr = new BinaryExpr(expr, operator, right);
        }

        return expr;
    }

    /**
     * Realiza o parsing de uma expressão de comparação.
     *
     * @return ASTNode da expressão de comparação.
     */
    private ASTNode comparison() {
        ASTNode expr = term();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            String operator = previous().lexeme();
            ASTNode right = term();
            expr = new BinaryExpr(expr, operator, right);
        }

        return expr;
    }

    /**
     * Realiza o parsing de uma expressão de termo (adição/subtração).
     *
     * @return ASTNode da expressão de termo.
     */
    private ASTNode term() {
        ASTNode expr = factor();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            String operator = previous().lexeme();
            ASTNode right = factor();
            expr = new BinaryExpr(expr, operator, right);
        }

        return expr;
    }

    /**
     * Realiza o parsing de uma expressão de fator (multiplicação/divisão/módulo).
     *
     * @return ASTNode da expressão de fator.
     */
    private ASTNode factor() {
        ASTNode expr = unary();

        while (match(TokenType.STAR, TokenType.SLASH, TokenType.MOD)) {
            String operator = previous().lexeme();
            ASTNode right = unary();
            expr = new BinaryExpr(expr, operator, right);
        }

        return expr;
    }

    /**
     * Realiza o parsing de uma expressão unária.
     *
     * @return ASTNode da expressão unária.
     */
    private ASTNode unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            String operator = previous().lexeme();
            ASTNode right = unary();
            return new UnaryExpr(operator, right);
        }

        return call();
    }

    /**
     * Realiza o parsing de uma chamada de função ou acesso de propriedade.
     *
     * @return ASTNode da chamada ou acesso.
     */
    private ASTNode call() {
        ASTNode expr = primary();

        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(TokenType.DOT)) {
                Token name;
                // Aceita ID, SEND ou RECEIVE como nome do método/propriedade
                if (match(TokenType.ID, TokenType.SEND, TokenType.RECEIVE)) {
                    name = previous();
                } else {
                    throw error(peek(),
                            "Esperado nome do método/propriedade após '.' (encontrado: '" + peek().lexeme() + "')");
                }
                // send/receive como nós específicos
                if (name.lexeme().equals("send") || name.lexeme().equals("receive")) {
                    consume(TokenType.LEFT_PAREN, "Esperado '(' após '" + name.lexeme() + "'");
                    List<ASTNode> args = arguments();
                    consume(TokenType.RIGHT_PAREN, "Esperado ')' após argumentos");
                    if (name.lexeme().equals("send")) {
                        expr = new SendStmt(expr, args);
                    } else {
                        expr = new ReceiveStmt(expr, args);
                    }
                } else if (match(TokenType.LEFT_PAREN)) {
                    // chamada de método comum
                    List<ASTNode> arguments = arguments();
                    consume(TokenType.RIGHT_PAREN, "Esperado ')' após argumentos");
                    expr = new MethodCall(expr, name.lexeme(), arguments);
                } else {
                    // acesso a propriedade
                    expr = new PropertyAccess(expr, name.lexeme());
                }
            } else if (match(TokenType.LEFT_BRACKET)) {
                ASTNode indexExpr = expression();
                consume(TokenType.RIGHT_BRACKET, "Esperado ']' após índice");
                expr = new IndexExpr(expr, indexExpr);
            } else {
                break;
            }
        }

        return expr;
    }

    /**
     * Finaliza uma chamada de função.
     *
     * @param callee ASTNode representando o alvo da chamada.
     * @return ASTNode da chamada de função.
     * @throws ParseException se a chamada for inválida.
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
     * Realiza o parsing da lista de argumentos de chamada.
     *
     * @return Lista de ASTNode representando os argumentos.
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
     * Realiza o parsing de uma expressão primária.
     *
     * @return ASTNode da expressão primária.
     * @throws ParseException se não encontrar expressão válida.
     */
    private ASTNode primary() {
        if (match(TokenType.TRUE)) {
            return new Literal(true);
        }
        if (match(TokenType.FALSE)) {
            return new Literal(false);
        }

        if (match(TokenType.THIS)) {
            return new ThisExpr();
        }

        if (match(TokenType.SUPER)) {
            // Suporta apenas chamada de construtor da superclasse: super(args)
            consume(TokenType.LEFT_PAREN, "Esperado '(' após 'super'");
            List<ASTNode> args = arguments();
            consume(TokenType.RIGHT_PAREN, "Esperado ')' após argumentos de 'super'");
            return new SuperCall(args);
        }

        if (match(TokenType.NUMBER)) {
            String lexeme = previous().lexeme();
            if (lexeme.contains(".")) {
                return new Literal(Double.parseDouble(lexeme));
            } else {
                return new Literal(Integer.parseInt(lexeme));
            }
        }

        if (match(TokenType.STRING)) {
            return new Literal(previous().lexeme());
        }

        if (match(TokenType.INPUT)) {
            // input([prompt])
            ASTNode prompt = null;
            if (match(TokenType.LEFT_PAREN)) {
                if (!check(TokenType.RIGHT_PAREN)) {
                    prompt = expression();
                }
                consume(TokenType.RIGHT_PAREN, "Esperado ')' após input");
            }
            return new InputExpr(prompt);
        }

        if (match(TokenType.READLN)) {
            // readln()
            consume(TokenType.LEFT_PAREN, "Esperado '(' após 'readln'");
            consume(TokenType.RIGHT_PAREN, "Esperado ')' após 'readln'");
            return new ReadlnExpr();
        }

        if (match(TokenType.READNUMBER)) {
            // readNumber()
            consume(TokenType.LEFT_PAREN, "Esperado '(' após 'readNumber'");
            consume(TokenType.RIGHT_PAREN, "Esperado ')' após 'readNumber'");
            return new ReadNumberExpr();
        }

        if (match(TokenType.NEW)) {
            Token className = consume(TokenType.ID, "Esperado nome da classe após 'new'");
            consume(TokenType.LEFT_PAREN, "Esperado '(' após nome da classe");
            List<ASTNode> arguments = arguments();
            consume(TokenType.RIGHT_PAREN, "Esperado ')' após argumentos do construtor");
            return new NewInstance(className.lexeme(), arguments);
        }

        if (match(TokenType.LEFT_BRACKET)) {
            // lista literal
            List<ASTNode> elems = new ArrayList<>();
            if (!check(TokenType.RIGHT_BRACKET)) {
                do {
                    elems.add(expression());
                } while (match(TokenType.COMMA));
            }
            consume(TokenType.RIGHT_BRACKET, "Esperado ']' após literal de lista");
            return new ListLiteral(elems);
        }

        if (match(TokenType.LEFT_BRACE)) {
            // dicionário literal { key: value, ... }
            List<DictEntry> entries = new ArrayList<>();
            if (!check(TokenType.RIGHT_BRACE)) {
                do {
                    ASTNode key = expression();
                    consume(TokenType.COLON, "Esperado ':' entre chave e valor no dicionário");
                    ASTNode value = expression();
                    entries.add(new DictEntry(key, value));
                } while (match(TokenType.COMMA));
            }
            consume(TokenType.RIGHT_BRACE, "Esperado '}' após literal de dicionário");
            return new DictLiteral(entries);
        }

        if (match(TokenType.ID)) {
            return new Identifier(previous().lexeme());
        }

        if (match(TokenType.LEFT_PAREN)) {
            ASTNode expr = expression();
            consume(TokenType.RIGHT_PAREN, "Esperado ')' após expressão");
            return expr;
        }

        throw error(peek(), "Esperado expressão");
    }

    // ===== Métodos auxiliares =====

    /**
     * Verifica se o próximo token corresponde a algum dos tipos informados e
     * avança.
     *
     * @param types Tipos de token a verificar.
     * @return true se algum tipo corresponde, false caso contrário.
     */
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica se o próximo token corresponde ao tipo informado.
     *
     * @param type Tipo de token a verificar.
     * @return true se corresponde, false caso contrário.
     */
    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return peek().type() == type;
    }

    /**
     * Verifica se os próximos dois tokens correspondem à sequência informada.
     *
     * @param type1 Tipo do primeiro token.
     * @param type2 Tipo do segundo token.
     * @return true se ambos correspondem, false caso contrário.
     */
    private boolean checkSequence(TokenType type1, TokenType type2) {
        // Simplificado para refletir o comportamento esperado
        return current + 1 < tokens.size() &&
                tokens.get(current).type() == TokenType.ID &&
                tokens.get(current + 1).type() == TokenType.ID;
    }

    /**
     * Verifica se o token atual inicia uma declaração de método.
     * Um método começa com um tipo de retorno seguido de um identificador e '('.
     *
     * @return true se é início de método, false caso contrário.
     */
    private boolean isMethodStart() {
        if (isAtEnd())
            return false;

        TokenType currentType = peek().type();

        // Verifica se é um tipo de retorno válido (void, string, number, bool ou ID
        // customizado)
        boolean isReturnType = currentType == TokenType.TYPE_VOID ||
                currentType == TokenType.TYPE_STRING ||
                currentType == TokenType.TYPE_NUMBER ||
                currentType == TokenType.TYPE_BOOL ||
                currentType == TokenType.TYPE_LIST ||
                currentType == TokenType.TYPE_DICT ||
                currentType == TokenType.ID;

        // Verifica se o próximo token é um ID (nome do método) e o seguinte é '('
        if (isReturnType && current + 1 < tokens.size()) {
            if (tokens.get(current + 1).type() != TokenType.ID)
                return false;
            if (current + 2 < tokens.size()) {
                return tokens.get(current + 2).type() == TokenType.LEFT_PAREN;
            }
        }

        return false;
    }

    /**
     * Verifica se a posição atual inicia uma declaração de variável no formato
     * "<tipo> <identificador> [= ...] ;".
     */
    private boolean isVarDeclStart() {
        if (isAtEnd())
            return false;
        TokenType t0 = peek().type();
        if (!isTypeTokenOrId(t0))
            return false;
        if (current + 1 >= tokens.size())
            return false;
        TokenType t1 = tokens.get(current + 1).type();
        if (t1 != TokenType.ID)
            return false;
        // Heurística: se depois do nome vier '=' ou ';', tratamos como declaração
        if (current + 2 < tokens.size()) {
            TokenType t2 = tokens.get(current + 2).type();
            if (t2 == TokenType.EQUAL || t2 == TokenType.SEMICOLON)
                return true;
        }
        return false;
    }

    /**
     * Verifica se é um token de tipo embutido ou um identificador (tipo
     * customizado).
     */
    private boolean isTypeTokenOrId(TokenType tt) {
        return tt == TokenType.TYPE_NUMBER || tt == TokenType.TYPE_STRING ||
                tt == TokenType.TYPE_BOOL || tt == TokenType.TYPE_VOID ||
                tt == TokenType.TYPE_LIST || tt == TokenType.TYPE_DICT ||
                tt == TokenType.ID;
    }

    /**
     * Avança para o próximo token.
     *
     * @return Token anterior ao avanço.
     */
    private Token advance() {
        if (!isAtEnd())
            current++;
        return previous();
    }

    /**
     * Verifica se chegou ao final da lista de tokens.
     *
     * @return true se está no final, false caso contrário.
     */
    private boolean isAtEnd() {
        return peek().type() == TokenType.EOF;
    }

    /**
     * Retorna o token atual.
     *
     * @return Token atual.
     */
    private Token peek() {
        return tokens.get(current);
    }

    /**
     * Retorna o token anterior.
     *
     * @return Token anterior.
     */
    private Token previous() {
        return tokens.get(current - 1);
    }

    /**
     * Consome o próximo token se corresponder ao tipo informado.
     *
     * @param type    Tipo esperado.
     * @param message Mensagem de erro caso não corresponda.
     * @return Token consumido.
     * @throws ParseException se o tipo não corresponder.
     */
    private Token consume(TokenType type, String message) {
        if (check(type))
            return advance();
        throw error(peek(), message);
    }

    /**
     * Cria uma exceção de erro sintático.
     *
     * @param token   Token onde ocorreu o erro.
     * @param message Mensagem de erro.
     * @return ParseException criada.
     */
    private ParseException error(Token token, String message) {
        String error = "[Erro Sintático] Linha " + token.line() +
                ", Coluna " + token.column() + ": " + message +
                " (encontrado: '" + token.lexeme() + "')";
        return new ParseException(error);
    }

    /**
     * Sincroniza o parser após um erro, avançando até um ponto seguro.
     */
    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type() == TokenType.NEWLINE)
                return;

            switch (peek().type()) {
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
 * Exceção customizada para erros de parsing.
 */
class ParseException extends RuntimeException {
    /**
     * Construtor da exceção de parsing.
     *
     * @param message Mensagem de erro.
     */
    public ParseException(String message) {
        super(message);
    }
}

// Classe para representar declaração de canal
class CanalDecl extends ASTNode {
    public final List<String> nomes;

    public CanalDecl(List<String> nomes) {
        this.nomes = nomes;
    }

    @Override
    public String toString() {
        return "";
    }
}
