package parser;

import java.util.List;

/**
 * Pretty printer simplificado usando padrão Visitor.
 * Muito mais limpo e extensível que a versão anterior.
 */
public final class ASTPrinter implements ASTVisitor<String> {

    private ASTPrinter() {}

    public static String print(ASTNode root) {
        ASTPrinter printer = new ASTPrinter();
        return root.accept(printer, 0);
    }

    @Override
    public String visit(ASTNode node, int depth) {
        StringBuilder sb = new StringBuilder();

        switch (node) {
            case Program p -> {
                sb.append(indent(depth)).append("Program\n");
                for (ASTNode stmt : safeList(p.statements)) {
                    sb.append(stmt.accept(this, depth + 1));
                }
            }
            case ClassDecl c -> {
                sb.append(indent(depth)).append("ClassDecl ").append(c.name);
                if (c.superClass != null) sb.append(" extends ").append(c.superClass);
                sb.append("\n");
                for (VarDecl attr : safeList(c.attributes)) {
                    sb.append(attr.accept(this, depth + 1));
                }
                for (MethodDecl m : safeList(c.methods)) {
                    sb.append(m.accept(this, depth + 1));
                }
            }
            case MethodDecl m -> {
                sb.append(indent(depth)).append("MethodDecl ").append(m.returnType)
                  .append(" ").append(m.name).append("(").append(formatParams(m.parameters)).append(")\n");
                for (ASTNode stmt : safeList(m.body)) {
                    sb.append(stmt.accept(this, depth + 1));
                }
            }
            case FuncDecl f -> {
                sb.append(indent(depth)).append("FuncDecl ").append(f.returnType)
                  .append(" ").append(f.name).append("(").append(formatParams(f.parameters)).append(")\n");
                for (ASTNode stmt : safeList(f.body)) {
                    sb.append(stmt.accept(this, depth + 1));
                }
            }
            case VarDecl v -> {
                sb.append(indent(depth)).append("VarDecl ").append(v.type).append(" ").append(v.name);
                if (v.initializer != null) {
                    sb.append(" = ").append(formatInline(v.initializer));
                }
                sb.append("\n");
            }
            case Assignment a -> {
                sb.append(indent(depth)).append("Assignment ").append(a.varName)
                  .append(" = ").append(formatInline(a.value)).append("\n");
            }
            case ReturnStmt r -> {
                sb.append(indent(depth)).append("Return ").append(formatInline(r.value)).append("\n");
            }
            case IfStmt i -> {
                sb.append(indent(depth)).append("If\n");
                sb.append(indent(depth + 1)).append("Condition: ").append(formatInline(i.condition)).append("\n");
                sb.append(indent(depth + 1)).append("Then:\n");
                for (ASTNode stmt : safeList(i.thenBranch)) {
                    sb.append(stmt.accept(this, depth + 2));
                }
                if (i.elseBranch != null) {
                    sb.append(indent(depth + 1)).append("Else:\n");
                    for (ASTNode stmt : safeList(i.elseBranch)) {
                        sb.append(stmt.accept(this, depth + 2));
                    }
                }
            }
            case WhileStmt w -> {
                sb.append(indent(depth)).append("While\n");
                sb.append(indent(depth + 1)).append("Condition: ").append(formatInline(w.condition)).append("\n");
                sb.append(indent(depth + 1)).append("Body:\n");
                for (ASTNode stmt : safeList(w.body)) {
                    sb.append(stmt.accept(this, depth + 2));
                }
            }
            case SeqBlock s -> {
                sb.append(indent(depth)).append("SEQ\n");
                for (ASTNode stmt : safeList(s.statements)) {
                    sb.append(stmt.accept(this, depth + 1));
                }
            }
            case ParBlock p -> {
                sb.append(indent(depth)).append("PAR\n");
                for (ASTNode stmt : safeList(p.statements)) {
                    sb.append(stmt.accept(this, depth + 1));
                }
            }
            case NewInstance n -> {
                sb.append(indent(depth)).append("New ").append(n.className);
                if (n.arguments != null && !n.arguments.isEmpty()) {
                    sb.append("(").append(formatArgs(n.arguments)).append(")");
                }
                sb.append("\n");
            }
            case MethodCall mc -> {
                sb.append(indent(depth)).append("MethodCall ").append(formatInline(mc.object))
                  .append(".").append(mc.methodName).append("(").append(formatArgs(mc.arguments)).append(")\n");
            }
            case FunctionCall fc -> {
                sb.append(indent(depth)).append("Call ").append(fc.functionName)
                  .append("(").append(formatArgs(fc.arguments)).append(")\n");
            }
            case BinaryExpr b -> {
                sb.append(indent(depth)).append("BinaryExpr ").append(b.operator).append("\n");
                sb.append(b.left.accept(this, depth + 1));
                sb.append(b.right.accept(this, depth + 1));
            }
            case UnaryExpr u -> {
                sb.append(indent(depth)).append("UnaryExpr ").append(u.operator).append("\n");
                sb.append(u.operand.accept(this, depth + 1));
            }
            case Identifier id -> sb.append(indent(depth)).append("Identifier ").append(id.name).append("\n");
            case Literal lit -> sb.append(indent(depth)).append("Literal ").append(lit.value).append("\n");
            case BreakStmt ignored -> sb.append(indent(depth)).append("Break\n");
            case ContinueStmt ignored -> sb.append(indent(depth)).append("Continue\n");
            default -> sb.append(indent(depth)).append(node.toString()).append("\n");
        }

        return sb.toString();
    }

    private String formatInline(ASTNode node) {
        if (node instanceof Identifier id) return id.name;
        if (node instanceof Literal lit) return String.valueOf(lit.value);
        if (node instanceof BinaryExpr b && isSimple(b.left) && isSimple(b.right)) {
            return "(" + formatInline(b.left) + " " + b.operator + " " + formatInline(b.right) + ")";
        }
        return node.toString();
    }

    private boolean isSimple(ASTNode node) {
        return node instanceof Identifier || node instanceof Literal;
    }

    private String formatArgs(List<ASTNode> args) {
        if (args == null) return "";
        return args.stream()
                   .map(this::formatInline)
                   .reduce((a, b) -> a + ", " + b)
                   .orElse("");
    }

    private String formatParams(List<Parameter> params) {
        if (params == null) return "";
        return params.stream()
                     .map(p -> p.name + ":" + p.type)
                     .reduce((a, b) -> a + ", " + b)
                     .orElse("");
    }

    private <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }

    private String indent(int depth) {
        return "  ".repeat(Math.max(0, depth));
    }
}
