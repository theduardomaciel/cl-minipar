package parser;

import interpreter.Environment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Interpretador da AST do MiniPar OOP.
 * Implementa execução com suporte a: variáveis, funções, classes/objetos, listas/dicionários,
 * controle de fluxo (if/while/do-while/for), blocos seq/par (threads), canais (c_channel),
 * entrada/saída, recursão e operadores.
 */
public class Interpreter {
    private final Environment globals = new Environment();
    private Environment env = globals;
    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    // Built-ins de função: nome -> invocador
    private final Map<String, Builtin> builtins = new HashMap<>();

    public Interpreter() {
        // Registrar built-ins simples
        builtins.put("random", (args) -> Math.random());
        builtins.put("sqrt", (args) -> checkArityAndNumber("sqrt", args, 1, true));
        builtins.put("abs", (args) -> checkArityAndNumber("abs", args, 1, false));
        builtins.put("pow", (args) -> {
            checkArity("pow", args, 2);
            double a = toNumber(args.get(0));
            double b = toNumber(args.get(1));
            return Math.pow(a, b);
        });
    }

    // ===== API =====

    public void execute(Program program) {
        exec(program);
    }

    // ===== Execução de nós =====

    private Object exec(ASTNode node) {
        if (node == null) return null;

        try {
            // Declarações e statements de topo
            if (node instanceof Program p) return execProgram(p);
            if (node instanceof VarDecl v) return execVarDecl(v);
            if (node instanceof FuncDecl f) return execFuncDecl(f);
            if (node instanceof ClassDecl c) return execClassDecl(c);
            if (node instanceof CanalDecl c) return execCanalDecl(c);
            if (node instanceof SeqBlock s) return execSeq(s.statements);
            if (node instanceof ParBlock p) return execPar(p.statements);
            if (node instanceof IfStmt i) return execIf(i);
            if (node instanceof WhileStmt w) return execWhile(w);
            if (node instanceof DoWhileStmt d) return execDoWhile(d);
            if (node instanceof ForStmt f) return execFor(f);
            if (node instanceof PrintStmt p) return execPrint(p);
            if (node instanceof ReturnStmt r) throw new ReturnSignal(eval(r.value));
            if (node instanceof BreakStmt) throw new BreakSignal();
            if (node instanceof ContinueStmt) throw new ContinueSignal();

            // Expressões
            if (node instanceof Assignment a) return execAssignment(a);
            if (node instanceof IndexAssign ia) return execIndexAssign(ia);
            if (node instanceof PropertyAssign pa) return execPropertyAssign(pa);

            return eval(node);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    private Object execProgram(Program program) {
        for (ASTNode s : program.statements) {
            exec(s);
        }
        return null;
    }

    private Object execVarDecl(VarDecl v) {
        Object init = (v.initializer != null) ? eval(v.initializer) : null;
        env.define(v.name, init);
        return null;
    }

    private Object execFuncDecl(FuncDecl f) {
        MiniFunction fn = new MiniFunction(f.name, f.parameters, f.body, env);
        env.define(f.name, fn);
        return null;
    }

    private Object execClassDecl(ClassDecl c) {
        // Mapear métodos por nome
        Map<String, MethodDecl> methods = new HashMap<>();
        for (MethodDecl m : c.methods) methods.put(m.name, m);
        MiniClass klass = new MiniClass(c.name, c.superClass, c.attributes, methods);
        env.define(c.name, klass);
        return null;
    }

    private Object execCanalDecl(CanalDecl c) {
        // Se for forma com parênteses, criar um canal para cada nome; caso contrário, primeiro nome é o canal
        if (c.nomes == null || c.nomes.isEmpty()) return null;
        if (c.nomes.size() >= 1) {
            // Na sintaxe "c_channel canal comp1 comp2", o primeiro é o canal
            String first = c.nomes.get(0);
            env.define(first, new Channel(first));
            // Nomes extras não usados na semântica mínima
        }
        return null;
    }

    private Object execSeq(List<ASTNode> statements) {
        for (ASTNode s : statements) exec(s);
        return null;
    }

    private Object execPar(List<ASTNode> statements) {
        // Executa cada statement em uma thread com cópia do ambiente atual (sem compartilhamento)
        List<Thread> threads = new ArrayList<>();
        for (ASTNode s : statements) {
            Environment child = new Environment(env); // herda leitura do pai
            Thread t = new Thread(() -> {
                Environment prev = this.env;
                try {
                    this.env = child;
                    exec(s);
                } finally {
                    this.env = prev;
                }
            });
            threads.add(t);
            t.start();
        }
        for (Thread t : threads) {
            try { t.join(); } catch (InterruptedException ignored) {}
        }
        return null;
    }

    private Object execIf(IfStmt i) {
        if (isTruthy(eval(i.condition))) {
            return execSeq(i.thenBranch);
        } else if (i.elseBranch != null) {
            return execSeq(i.elseBranch);
        }
        return null;
    }

    private Object execWhile(WhileStmt w) {
        while (isTruthy(eval(w.condition))) {
            try {
                execSeq(w.body);
            } catch (BreakSignal b) {
                break;
            } catch (ContinueSignal c) {
                continue;
            }
        }
        return null;
    }

    private Object execDoWhile(DoWhileStmt d) {
        do {
            try {
                execSeq(d.body);
            } catch (BreakSignal b) {
                break;
            } catch (ContinueSignal c) {
                // segue
            }
        } while (isTruthy(eval(d.condition)));
        return null;
    }

    private Object execFor(ForStmt f) {
        Object iterable = eval(f.iterable);
        if (iterable instanceof List<?> list) {
            for (Object item : list) {
                Environment loopEnv = new Environment(env);
                loopEnv.define(f.variable.name, item);
                Environment prev = env;
                env = loopEnv;
                try {
                    execSeq(f.body);
                } catch (BreakSignal b) {
                    break;
                } catch (ContinueSignal c) {
                    // ignora
                } finally {
                    env = prev;
                }
            }
            return null;
        }
        throw new RuntimeException("for-in suporta apenas listas");
    }

    private Object execPrint(PrintStmt p) {
        List<Object> vals = new ArrayList<>();
        for (ASTNode a : p.arguments) vals.add(eval(a));
        if (vals.size() == 1) {
            System.out.println(stringify(vals.get(0)));
        } else {
            // imprime com espaço entre argumentos
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < vals.size(); i++) {
                if (i > 0) sb.append(" ");
                sb.append(stringify(vals.get(i)));
            }
            System.out.println(sb);
        }
        return null;
    }

    private Object execAssignment(Assignment a) {
        Object value = eval(a.value);
        env.assign(a.varName, value);
        return value;
    }

    private Object execIndexAssign(IndexAssign ia) {
        Object target = eval(ia.target);
        Object index = eval(ia.index);
        Object value = eval(ia.value);
        if (target instanceof List<?> lst) {
            List<Object> list = castList(lst);
            int i = (int) toNumber(index);
            setListIndex(list, i, value);
            return value;
        }
        if (target instanceof Map<?,?> m) {
            @SuppressWarnings("unchecked") Map<Object,Object> map = (Map<Object,Object>) m;
            map.put(index, value);
            return value;
        }
        throw new RuntimeException("Indexação suportada apenas para listas e dicionários");
    }

    private Object execPropertyAssign(PropertyAssign pa) {
        Object obj = eval(pa.object);
        Object value = eval(pa.value);
        if (obj instanceof MiniInstance inst) {
            inst.set(pa.propertyName, value);
            return value;
        }
        throw new RuntimeException("Atribuição de propriedade somente em instâncias");
    }

    // ===== Avaliação de expressões =====

    private Object eval(ASTNode node) {
        if (node == null) return null;
        if (node instanceof Literal l) return l.value;
        if (node instanceof Identifier id) return env.get(id.name);
        if (node instanceof UnaryExpr u) return evalUnary(u);
        if (node instanceof BinaryExpr b) return evalBinary(b);
        if (node instanceof ListLiteral ll) return evalListLiteral(ll);
        if (node instanceof DictLiteral dl) return evalDictLiteral(dl);
        if (node instanceof IndexExpr ie) return evalIndexExpr(ie);
        if (node instanceof PropertyAccess pa) return evalPropertyAccess(pa);
        if (node instanceof InputExpr in) return evalInput(in);
        if (node instanceof NewInstance ni) return evalNewInstance(ni);
        if (node instanceof MethodCall mc) return evalMethodCall(mc);
        if (node instanceof FunctionCall fc) return evalFunctionCall(fc);
        if (node instanceof ThisExpr) return env.get("this");
        if (node instanceof SendStmt s) { execSend(s); return null; }
        if (node instanceof ReceiveStmt r) { execReceive(r); return null; }
        if (node instanceof Program p) { execProgram(p); return null; }
        // Outras formas já cobertas em exec(...)
        throw new RuntimeException("Nós de expressão não suportados: " + node.getClass().getSimpleName());
    }

    private Object evalUnary(UnaryExpr u) {
        Object right = eval(u.operand);
        return switch (u.operator) {
            case "-" -> -toNumber(right);
            case "!" -> !isTruthy(right);
            default -> throw new RuntimeException("Operador unário desconhecido: " + u.operator);
        };
    }

    private Object evalBinary(BinaryExpr b) {
        Object left = eval(b.left);
        Object right = eval(b.right);
        String op = b.operator;

        // Lógicos curtos
        if (Objects.equals(op, "&&")) return isTruthy(left) && isTruthy(right);
        if (Objects.equals(op, "||")) return isTruthy(left) || isTruthy(right);

        // Comparações e igualdade
        switch (op) {
            case "==": return equals(left, right);
            case "!=": return !equals(left, right);
            case ">": return toNumber(left) > toNumber(right);
            case ">=": return toNumber(left) >= toNumber(right);
            case "<": return toNumber(left) < toNumber(right);
            case "<=": return toNumber(left) <= toNumber(right);
        }

        // Aritmética ou concatenação
        switch (op) {
            case "+":
                if (left instanceof String || right instanceof String) {
                    return stringify(left) + stringify(right);
                }
                return toNumber(left) + toNumber(right);
            case "-": return toNumber(left) - toNumber(right);
            case "*": return toNumber(left) * toNumber(right);
            case "/": return toNumber(left) / toNumber(right);
            case "%": return toNumber(left) % toNumber(right);
        }
        throw new RuntimeException("Operador desconhecido: " + op);
    }

    private Object evalListLiteral(ListLiteral ll) {
        List<Object> list = new ArrayList<>();
        for (ASTNode e : ll.elements) list.add(eval(e));
        return list;
    }

    private Object evalDictLiteral(DictLiteral dl) {
        Map<Object,Object> map = new HashMap<>();
        for (DictEntry e : dl.entries) map.put(eval(e.key), eval(e.value));
        return map;
    }

    private Object evalIndexExpr(IndexExpr ie) {
        Object target = eval(ie.target);
        Object index = eval(ie.index);
        if (target instanceof List<?> lst) {
            int i = (int) toNumber(index);
            List<Object> l = castList(lst);
            if (i < 0 || i >= l.size()) return null;
            return l.get(i);
        }
        if (target instanceof Map<?,?> m) {
            @SuppressWarnings("unchecked") Map<Object,Object> map = (Map<Object,Object>) m;
            return map.get(index);
        }
        throw new RuntimeException("Indexação suportada apenas para listas e dicionários");
    }

    private Object evalPropertyAccess(PropertyAccess pa) {
        Object obj = eval(pa.object);
        if (obj instanceof MiniInstance inst) {
            return inst.get(pa.propertyName);
        }
        if (obj instanceof List<?> lst) {
            if (pa.propertyName.equals("length")) return ((List<?>) lst).size();
        }
        if (obj instanceof String s) {
            if (pa.propertyName.equals("length")) return s.length();
        }
        if (obj instanceof Map<?,?> m) {
            if (pa.propertyName.equals("length")) return ((Map<?,?>) m).size();
        }
        throw new RuntimeException("Propriedade desconhecida: " + pa.propertyName);
    }

    private Object evalInput(InputExpr in) {
        if (in.prompt != null) System.out.print(stringify(eval(in.prompt)));
        try {
            String line = reader.readLine();
            if (line == null) return null;
            // tenta número
            try {
                if (line.contains(".")) return Double.parseDouble(line.trim());
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                return line;
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro de leitura de input: " + e.getMessage());
        }
    }

    private Object evalNewInstance(NewInstance ni) {
        Object found = env.get(ni.className);
        if (!(found instanceof MiniClass klass)) {
            throw new RuntimeException("Classe não definida: " + ni.className);
        }
        List<Object> args = new ArrayList<>();
        for (ASTNode a : ni.arguments) args.add(eval(a));
        return klass.instantiate(args);
    }

    private Object evalMethodCall(MethodCall mc) {
        Object obj = eval(mc.object);
        List<Object> args = new ArrayList<>();
        for (ASTNode a : mc.arguments) args.add(eval(a));
        if (obj instanceof MiniInstance inst) {
            return inst.call(mc.methodName, args);
        }
        throw new RuntimeException("Chamada de método em não-instância");
    }

    private Object evalFunctionCall(FunctionCall fc) {
        List<Object> args = new ArrayList<>();
        for (ASTNode a : fc.arguments) args.add(eval(a));
        // built-in
        if (builtins.containsKey(fc.functionName)) {
            return builtins.get(fc.functionName).call(args);
        }
        // função definida pelo usuário
        Object callee = env.get(fc.functionName);
        if (callee instanceof MiniFunction fn) {
            return fn.call(args);
        }
        throw new RuntimeException("Função não definida: " + fc.functionName);
    }

    private void execSend(SendStmt s) {
        Object ch = eval(s.channel);
        if (!(ch instanceof Channel c)) throw new RuntimeException("Objeto não é canal");
        List<Object> args = new ArrayList<>();
        for (ASTNode a : s.arguments) args.add(eval(a));
        c.send(args);
    }

    private void execReceive(ReceiveStmt r) {
        Object ch = eval(r.channel);
        if (!(ch instanceof Channel c)) throw new RuntimeException("Objeto não é canal");
        List<Object> msg = c.receive();
        int n = Math.min(msg.size(), r.arguments.size());
        for (int i = 0; i < n; i++) {
            ASTNode target = r.arguments.get(i);
            Object value = msg.get(i);
            assignToTarget(target, value);
        }
    }

    private void assignToTarget(ASTNode target, Object value) {
        if (target instanceof Identifier id) {
            env.assign(id.name, value);
            return;
        }
        if (target instanceof PropertyAccess pa) {
            Object obj = eval(pa.object);
            if (obj instanceof MiniInstance inst) {
                inst.set(pa.propertyName, value);
                return;
            }
        }
        if (target instanceof IndexExpr ie) {
            Object t = eval(ie.target);
            Object idx = eval(ie.index);
            if (t instanceof List<?> lst) {
                List<Object> l = castList(lst);
                int i = (int) toNumber(idx);
                setListIndex(l, i, value);
                return;
            }
            if (t instanceof Map<?,?> m) {
                @SuppressWarnings("unchecked") Map<Object,Object> map = (Map<Object,Object>) m;
                map.put(idx, value);
                return;
            }
        }
        throw new RuntimeException("Alvo de receive inválido");
    }

    // ===== Utilitários =====

    private boolean isTruthy(Object v) {
        if (v == null) return false;
        if (v instanceof Boolean b) return b;
        if (v instanceof Number n) return n.doubleValue() != 0.0;
        if (v instanceof String s) return !s.isEmpty();
        if (v instanceof List<?> l) return !l.isEmpty();
        if (v instanceof Map<?,?> m) return !m.isEmpty();
        return true;
    }

    private String stringify(Object v) {
        if (v == null) return "null";
        if (v instanceof Double d) {
            if (d % 1 == 0) return String.valueOf(d.longValue());
            return d.toString();
        }
        if (v instanceof List<?> l) return l.toString();
        if (v instanceof Map<?,?> m) return m.toString();
        return String.valueOf(v);
    }

    private boolean equals(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        if (a instanceof Number && b instanceof Number) {
            return Double.compare(((Number) a).doubleValue(), ((Number) b).doubleValue()) == 0;
        }
        return Objects.equals(a, b);
    }

    private double toNumber(Object v) {
        if (v instanceof Integer i) return i.doubleValue();
        if (v instanceof Double d) return d;
        if (v instanceof Number n) return n.doubleValue();
        if (v instanceof String s) {
            try {
                if (s.contains(".")) return Double.parseDouble(s);
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Esperado número, obtido: '" + s + "'");
            }
        }
        throw new RuntimeException("Esperado número, obtido tipo: " + (v == null ? "null" : v.getClass().getSimpleName()));
    }

    @SuppressWarnings("unchecked")
    private List<Object> castList(List<?> l) {
        return (List<Object>) l;
    }

    private void setListIndex(List<Object> l, int i, Object v) {
        while (l.size() <= i) l.add(null);
        l.set(i, v);
    }

    private Object checkArityAndNumber(String name, List<Object> args, int arity, boolean sqrt) {
        checkArity(name, args, arity);
        double n = toNumber(args.get(0));
        return sqrt ? Math.sqrt(n) : Math.abs(n);
    }

    private void checkArity(String name, List<Object> args, int arity) {
        if (args.size() != arity) {
            throw new RuntimeException("Função " + name + " espera " + arity + " argumentos, recebeu " + args.size());
        }
    }

    // ===== Tipos de runtime =====

    private interface Builtin {
        Object call(List<Object> args);
    }

    private static class ReturnSignal extends RuntimeException {
        final Object value;
        ReturnSignal(Object value) { this.value = value; }
    }

    private static class BreakSignal extends RuntimeException {}
    private static class ContinueSignal extends RuntimeException {}

    private class MiniFunction {
        final String name;
        final List<Parameter> params; // name:type
        final List<ASTNode> body;
        final Environment closure;

        MiniFunction(String name, List<Parameter> params, List<ASTNode> body, Environment closure) {
            this.name = name; this.params = params; this.body = body; this.closure = closure;
        }

        Object call(List<Object> args) {
            Environment local = new Environment(closure);
            for (int i = 0; i < params.size(); i++) {
                String pname = params.get(i).name;
                Object pval = i < args.size() ? args.get(i) : null;
                local.define(pname, pval);
            }
            Environment prev = env;
            env = local;
            try {
                for (ASTNode s : body) exec(s);
            } catch (ReturnSignal rs) {
                return rs.value;
            } finally {
                env = prev;
            }
            return null; // void
        }
    }

    private class MiniClass {
        final String name;
        final String superName; // não usado neste MVP
        final List<VarDecl> attrs;
        final Map<String, MethodDecl> methods;

        MiniClass(String name, String superName, List<VarDecl> attrs, Map<String, MethodDecl> methods) {
            this.name = name; this.superName = superName; this.attrs = attrs; this.methods = methods;
        }

        MiniInstance instantiate(List<Object> args) {
            MiniInstance inst = new MiniInstance(this);
            // inicializar atributos (null ou initializer avaliado no contexto da instância)
            for (VarDecl v : attrs) {
                Object init = null;
                if (v.initializer != null) {
                    // criar ambiente com this
                    Environment prev = env;
                    Environment local = new Environment(prev);
                    local.define("this", inst);
                    env = local;
                    try { init = eval(v.initializer); }
                    finally { env = prev; }
                }
                inst.fields.put(v.name, init);
            }
            // construtor opcional: nome igual à classe
            MethodDecl ctor = methods.get(name);
            if (ctor != null) {
                inst.call(ctor.name, args);
            }
            return inst;
        }
    }

    private class MiniInstance {
        final MiniClass klass;
        final Map<String, Object> fields = new HashMap<>();

        MiniInstance(MiniClass k) { this.klass = k; }

        Object get(String name) {
            if (fields.containsKey(name)) return fields.get(name);
            MethodDecl m = klass.methods.get(name);
            if (m != null) {
                // retorna um callable que quando chamado executa o método com this
                return new BoundMethod(this, m);
            }
            if (name.equals("length")) return fields.size();
            throw new RuntimeException("Propriedade/método não encontrado: " + name);
        }

        void set(String name, Object value) {
            fields.put(name, value);
        }

        Object call(String methodName, List<Object> args) {
            MethodDecl m = klass.methods.get(methodName);
            if (m == null) throw new RuntimeException("Método não encontrado: " + methodName);
            Environment local = new Environment(env);
            local.define("this", this);
            for (int i = 0; i < m.parameters.size(); i++) {
                String pname = m.parameters.get(i).name;
                Object pval = i < args.size() ? args.get(i) : null;
                local.define(pname, pval);
            }
            Environment prev = env;
            env = local;
            try {
                for (ASTNode s : m.body) exec(s);
            } catch (ReturnSignal rs) {
                return rs.value;
            } finally {
                env = prev;
            }
            return null;
        }
    }

    private class BoundMethod {
        final MiniInstance receiver;
        final MethodDecl decl;
        BoundMethod(MiniInstance r, MethodDecl d) { this.receiver = r; this.decl = d; }
        Object call(List<Object> args) { return receiver.call(decl.name, args); }
        @Override public String toString() { return "<method " + decl.name + ">"; }
    }

    private static class Channel {
        final String name;
        final BlockingQueue<List<Object>> queue = new ArrayBlockingQueue<>(1024);
        Channel(String name) { this.name = name; }
        void send(List<Object> msg) { try { queue.put(msg); } catch (InterruptedException ignored) {} }
        List<Object> receive() { try { return queue.take(); } catch (InterruptedException e) { return List.of(); } }
    }
}
