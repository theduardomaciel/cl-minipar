package parser;

import java.util.List;

/**
 * Classe abstrata base para todos os nós da AST.
 */
public abstract class ASTNode {
    /**
     * Retorna uma representação em string do nó da AST.
     * @return String representando o nó.
     */
    public abstract String toString();
}


/**
 * Declaração de classe
 */
class ClassDecl extends ASTNode {
    public String name;
    public String superClass;
    public List<VarDecl> attributes;
    public List<MethodDecl> methods;

    /**
     * Construtor do nó ClassDecl.
     * @param name Nome da classe.
     * @param superClass Nome da superclasse (pode ser null).
     * @param attributes Lista de atributos da classe.
     * @param methods Lista de métodos da classe.
     */
    public ClassDecl(String name, String superClass, List<VarDecl> attributes, List<MethodDecl> methods) {
        this.name = name;
        this.superClass = superClass;
        this.attributes = attributes;
        this.methods = methods;
    }

    /**
     * Retorna uma representação em string do nó ClassDecl.
     * @return String representando a declaração de classe.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ClassDecl(name=").append(name);
        if (superClass != null) {
            sb.append(", extends=").append(superClass);
        }
        sb.append(", attributes=").append(attributes);
        sb.append(", methods=").append(methods);
        sb.append(")");
        return sb.toString();
    }
}

/**
 * Declaração de método
 */
class MethodDecl extends ASTNode {
    public String returnType;
    public String name;
    public List<Parameter> parameters;
    public List<ASTNode> body;

    /**
     * Construtor do nó MethodDecl.
     * @param returnType Tipo de retorno do método.
     * @param name Nome do método.
     * @param parameters Lista de parâmetros do método.
     * @param body Corpo do método (lista de nós AST).
     */
    public MethodDecl(String returnType, String name, List<Parameter> parameters, List<ASTNode> body) {
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    /**
     * Retorna uma representação em string do nó MethodDecl.
     * @return String representando a declaração de método.
     */
    @Override
    public String toString() {
        return "MethodDecl(name=" + name + ", returnType=" + returnType +
               ", params=" + parameters + ", body=" + body + ")";
    }
}

/**
 * Parâmetro de método/função
 */
class Parameter {
    public String name;
    public String type;

    /**
     * Construtor do nó Parameter.
     * @param name Nome do parâmetro.
     * @param type Tipo do parâmetro.
     */
    public Parameter(String name, String type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Retorna uma representação em string do parâmetro.
     * @return String representando o parâmetro.
     */
    @Override
    public String toString() {
        return name + ":" + type;
    }
}

/**
 * Declaração de variável
 */
class VarDecl extends ASTNode {
    public String name;
    public String type;
    public ASTNode initializer;

    /**
     * Construtor do nó VarDecl.
     * @param name Nome da variável.
     * @param type Tipo da variável.
     * @param initializer Inicializador da variável (pode ser null).
     */
    public VarDecl(String name, String type, ASTNode initializer) {
        this.name = name;
        this.type = type;
        this.initializer = initializer;
    }

    /**
     * Retorna uma representação em string do nó VarDecl.
     * @return String representando a declaração de variável.
     */
    @Override
    public String toString() {
        return "VarDecl(name=" + name + ", type=" + type +
               (initializer != null ? ", init=" + initializer : "") + ")";
    }
}

/**
 * Declaração de função
 */
class FuncDecl extends ASTNode {
    public String name;
    public String returnType;
    public List<Parameter> parameters;
    public List<ASTNode> body;

    /**
     * Construtor do nó FuncDecl.
     * @param name Nome da função.
     * @param returnType Tipo de retorno da função.
     * @param parameters Lista de parâmetros da função.
     * @param body Corpo da função (lista de nós AST).
     */
    public FuncDecl(String name, String returnType, List<Parameter> parameters, List<ASTNode> body) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
        this.body = body;
    }

    /**
     * Retorna uma representação em string do nó FuncDecl.
     * @return String representando a declaração de função.
     */
    @Override
    public String toString() {
        return "FuncDecl(name=" + name + ", returnType=" + returnType +
               ", params=" + parameters + ")";
    }
}

/**
 * Instanciação de objeto
 */
class NewInstance extends ASTNode {
    public String className;
    public List<ASTNode> arguments;

    /**
     * Construtor do nó NewInstance.
     * @param className Nome da classe a ser instanciada.
     * @param arguments Lista de argumentos para o construtor.
     */
    public NewInstance(String className, List<ASTNode> arguments) {
        this.className = className;
        this.arguments = arguments;
    }

    /**
     * Retorna uma representação em string do nó NewInstance.
     * @return String representando a instanciação de objeto.
     */
    @Override
    public String toString() {
        return "NewInstance(class=" + className + ", args=" + arguments + ")";
    }
}

/**
 * Chamada de método
 */
class MethodCall extends ASTNode {
    public ASTNode object;
    public String methodName;
    public List<ASTNode> arguments;

    /**
     * Construtor do nó MethodCall.
     * @param object Objeto alvo da chamada de método.
     * @param methodName Nome do método.
     * @param arguments Lista de argumentos do método.
     */
    public MethodCall(ASTNode object, String methodName, List<ASTNode> arguments) {
        this.object = object;
        this.methodName = methodName;
        this.arguments = arguments;
    }

    /**
     * Retorna uma representação em string do nó MethodCall.
     * @return String representando a chamada de método.
     */
    @Override
    public String toString() {
        return "MethodCall(object=" + object + ", method=" + methodName + ", args=" + arguments + ")";
    }
}

/**
 * Atribuição
 */
class Assignment extends ASTNode {
    public String varName;
    public ASTNode value;

    /**
     * Construtor do nó Assignment.
     * @param varName Nome da variável.
     * @param value Valor a ser atribuído.
     */
    public Assignment(String varName, ASTNode value) {
        this.varName = varName;
        this.value = value;
    }

    /**
     * Retorna uma representação em string do nó Assignment.
     * @return String representando a atribuição.
     */
    @Override
    public String toString() {
        return "Assignment(" + varName + " = " + value + ")";
    }
}

/**
 * If statement
 */
class IfStmt extends ASTNode {
    public ASTNode condition;
    public List<ASTNode> thenBranch;
    public List<ASTNode> elseBranch;

    /**
     * Construtor do nó IfStmt.
     * @param condition Condição do if.
     * @param thenBranch Lista de nós AST do bloco then.
     * @param elseBranch Lista de nós AST do bloco else (pode ser null).
     */
    public IfStmt(ASTNode condition, List<ASTNode> thenBranch, List<ASTNode> elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    /**
     * Retorna uma representação em string do nó IfStmt.
     * @return String representando o if.
     */
    @Override
    public String toString() {
        return "If(condition=" + condition + ", then=" + thenBranch +
               (elseBranch != null ? ", else=" + elseBranch : "") + ")";
    }
}

/**
 * While statement
 */
class WhileStmt extends ASTNode {
    public ASTNode condition;
    public List<ASTNode> body;

    /**
     * Construtor do nó WhileStmt.
     * @param condition Condição do while.
     * @param body Corpo do while (lista de nós AST).
     */
    public WhileStmt(ASTNode condition, List<ASTNode> body) {
        this.condition = condition;
        this.body = body;
    }

    /**
     * Retorna uma representação em string do nó WhileStmt.
     * @return String representando o while.
     */
    @Override
    public String toString() {
        return "While(condition=" + condition + ", body=" + body + ")";
    }
}

/**
 * Return statement
 */
class ReturnStmt extends ASTNode {
    public ASTNode value;

    /**
     * Construtor do nó ReturnStmt.
     * @param value Valor de retorno.
     */
    public ReturnStmt(ASTNode value) {
        this.value = value;
    }

    /**
     * Retorna uma representação em string do nó ReturnStmt.
     * @return String representando o retorno.
     */
    @Override
    public String toString() {
        return "Return(" + value + ")";
    }
}

/**
 * Expressões binárias
 */
class BinaryExpr extends ASTNode {
    public ASTNode left;
    public String operator;
    public ASTNode right;

    /**
     * Construtor do nó BinaryExpr.
     * @param left Expressão à esquerda.
     * @param operator Operador binário.
     * @param right Expressão à direita.
     */
    public BinaryExpr(ASTNode left, String operator, ASTNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    /**
     * Retorna uma representação em string do nó BinaryExpr.
     * @return String representando a expressão binária.
     */
    @Override
    public String toString() {
        return "BinaryExpr(" + left + " " + operator + " " + right + ")";
    }
}

/**
 * Expressões unárias
 */
class UnaryExpr extends ASTNode {
    public String operator;
    public ASTNode operand;

    /**
     * Construtor do nó UnaryExpr.
     * @param operator Operador unário.
     * @param operand Operando da expressão.
     */
    public UnaryExpr(String operator, ASTNode operand) {
        this.operator = operator;
        this.operand = operand;
    }

    /**
     * Retorna uma representação em string do nó UnaryExpr.
     * @return String representando a expressão unária.
     */
    @Override
    public String toString() {
        return "UnaryExpr(" + operator + operand + ")";
    }
}

/**
 * Literal (número, string, boolean)
 */
class Literal extends ASTNode {
    public Object value;

    /**
     * Construtor do nó Literal.
     * @param value Valor literal (número, string, boolean).
     */
    public Literal(Object value) {
        this.value = value;
    }

    /**
     * Retorna uma representação em string do nó Literal.
     * @return String representando o valor literal.
     */
    @Override
    public String toString() {
        return "Literal(" + value + ")";
    }
}

/**
 * Identificador (variável)
 */
class Identifier extends ASTNode {
    public String name;

    /**
     * Construtor do nó Identifier.
     * @param name Nome do identificador.
     */
    public Identifier(String name) {
        this.name = name;
    }

    /**
     * Retorna uma representação em string do nó Identifier.
     * @return String representando o identificador.
     */
    @Override
    public String toString() {
        return "Identifier(" + name + ")";
    }
}

/**
 * Chamada de função
 */
class FunctionCall extends ASTNode {
    public String functionName;
    public List<ASTNode> arguments;

    /**
     * Construtor do nó FunctionCall.
     * @param functionName Nome da função.
     * @param arguments Lista de argumentos da função.
     */
    public FunctionCall(String functionName, List<ASTNode> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }

    /**
     * Retorna uma representação em string do nó FunctionCall.
     * @return String representando a chamada de função.
     */
    @Override
    public String toString() {
        return "FunctionCall(" + functionName + ", args=" + arguments + ")";
    }
}

/**
 * Bloco SEQ
 */
class SeqBlock extends ASTNode {
    public List<ASTNode> statements;

    /**
     * Construtor do nó SeqBlock.
     * @param statements Lista de instruções do bloco sequencial.
     */
    public SeqBlock(List<ASTNode> statements) {
        this.statements = statements;
    }

    /**
     * Retorna uma representação em string do nó SeqBlock.
     * @return String representando o bloco SEQ.
     */
    @Override
    public String toString() {
        return "SEQ" + statements;
    }
}

/**
 * Bloco PAR
 */
class ParBlock extends ASTNode {
    public List<ASTNode> statements;

    /**
     * Construtor do nó ParBlock.
     * @param statements Lista de instruções do bloco paralelo.
     */
    public ParBlock(List<ASTNode> statements) {
        this.statements = statements;
    }

    /**
     * Retorna uma representação em string do nó ParBlock.
     * @return String representando o bloco PAR.
     */
    @Override
    public String toString() {
        return "PAR" + statements;
    }
}

/**
 * Break statement
 */
class BreakStmt extends ASTNode {
    /**
     * Retorna uma representação em string do nó BreakStmt.
     * @return String representando o break.
     */
    @Override
    public String toString() {
        return "Break";
    }
}

/**
 * Continue statement
 */
class ContinueStmt extends ASTNode {
    /**
     * Retorna uma representação em string do nó ContinueStmt.
     * @return String representando o continue.
     */
    @Override
    public String toString() {
        return "Continue";
    }
}

/**
 * Do-While statement
 */
class DoWhileStmt extends ASTNode {
    public List<ASTNode> body;
    public ASTNode condition;

    public DoWhileStmt(List<ASTNode> body, ASTNode condition) {
        this.body = body;
        this.condition = condition;
    }

    @Override
    public String toString() {
        return "DoWhile(condition=" + condition + ", body=" + body + ")";
    }
}

/**
 * For-in statement
 */
class ForStmt extends ASTNode {
    public VarDecl variable; // variável de iteração (nome e tipo)
    public ASTNode iterable; // expressão iterável
    public List<ASTNode> body;

    public ForStmt(VarDecl variable, ASTNode iterable, List<ASTNode> body) {
        this.variable = variable;
        this.iterable = iterable;
        this.body = body;
    }

    @Override
    public String toString() {
        return "For(var=" + variable + ", in=" + iterable + ", body=" + body + ")";
    }
}

/**
 * Literal de lista: [expr, expr, ...]
 */
class ListLiteral extends ASTNode {
    public List<ASTNode> elements;

    public ListLiteral(List<ASTNode> elements) {
        this.elements = elements;
    }

    @Override
    public String toString() {
        return "List" + elements;
    }
}

/**
 * Par chave:valor para dicionário
 */
class DictEntry {
    public ASTNode key;
    public ASTNode value;

    public DictEntry(ASTNode key, ASTNode value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return key + ":" + value;
    }
}

/**
 * Literal de dicionário: { key: value, ... }
 */
class DictLiteral extends ASTNode {
    public List<DictEntry> entries;

    public DictLiteral(List<DictEntry> entries) {
        this.entries = entries;
    }

    @Override
    public String toString() {
        return "Dict" + entries;
    }
}

/**
 * Indexação: alvo[índice]
 */
class IndexExpr extends ASTNode {
    public ASTNode target;
    public ASTNode index;

    public IndexExpr(ASTNode target, ASTNode index) {
        this.target = target;
        this.index = index;
    }

    @Override
    public String toString() {
        return "Index(" + target + "[" + index + "])";
    }
}

/**
 * Atribuição em índice: alvo[índice] = valor
 */
class IndexAssign extends ASTNode {
    public ASTNode target;
    public ASTNode index;
    public ASTNode value;

    public IndexAssign(ASTNode target, ASTNode index, ASTNode value) {
        this.target = target;
        this.index = index;
        this.value = value;
    }

    @Override
    public String toString() {
        return "IndexAssign(" + target + "[" + index + "] = " + value + ")";
    }
}

/**
 * Comando de impressão: print(arg1, arg2, ...)
 */
class PrintStmt extends ASTNode {
    public List<ASTNode> arguments;

    public PrintStmt(List<ASTNode> arguments) {
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return "Print" + arguments;
    }
}

/**
 * Expressão de entrada: input([prompt])
 */
class InputExpr extends ASTNode {
    public ASTNode prompt; // pode ser null

    public InputExpr(ASTNode prompt) {
        this.prompt = prompt;
    }

    @Override
    public String toString() {
        return "Input(" + (prompt != null ? prompt : "") + ")";
    }
}

/**
 * Envio via canal: canal.send(...)
 */
class SendStmt extends ASTNode {
    public ASTNode channel;
    public List<ASTNode> arguments;

    public SendStmt(ASTNode channel, List<ASTNode> arguments) {
        this.channel = channel;
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return "Send(channel=" + channel + ", args=" + arguments + ")";
    }
}

/**
 * Recepção via canal: canal.receive(...)
 */
class ReceiveStmt extends ASTNode {
    public ASTNode channel;
    public List<ASTNode> arguments;

    public ReceiveStmt(ASTNode channel, List<ASTNode> arguments) {
        this.channel = channel;
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return "Receive(channel=" + channel + ", args=" + arguments + ")";
    }
}

    /**
     * Acesso a propriedade: alvo.propriedade
     */
    class PropertyAccess extends ASTNode {
        public final ASTNode object;
        public final String propertyName;

        public PropertyAccess(ASTNode object, String propertyName) {
            this.object = object;
            this.propertyName = propertyName;
        }

        @Override
        public String toString() {
            return String.format("(get %s.%s)", object, propertyName);
        }
    }

    /**
     * Atribuição a propriedade: alvo.propriedade = valor
     */
    class PropertyAssign extends ASTNode {
        public final ASTNode object;
        public final String propertyName;
        public final ASTNode value;

        public PropertyAssign(ASTNode object, String propertyName, ASTNode value) {
            this.object = object;
            this.propertyName = propertyName;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("(set %s.%s %s)", object, propertyName, value);
        }
    }

    /**
     * Referência ao objeto atual (this)
     */
    class ThisExpr extends ASTNode {
        @Override
        public String toString() {
            return "this";
        }
    }