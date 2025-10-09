import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;
import parser.Parser;
import parser.ASTNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * Classe principal do interpretador MiniPar.
 * Responsável por iniciar o programa, gerenciar modos de execução
 * (arquivo ou interativo), e coordenar as fases de análise léxica e sintática.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("  INTERPRETADOR MINIPAR COM PROGRAMAÇÃO ORIENTADA A OBJETOS");
        System.out.println("  Tema 2 - Compiladores 2025.1");
        System.out.println("  Atividade 2: Analisador Léxico e Sintático");
        System.out.println("=".repeat(70));
        System.out.println();

        if (args.length > 0) {
            // Modo arquivo
            runFile(args[0]);
        } else {
            // Modo interativo
            runInteractive();
        }
    }

    /**
     * Executa a análise de um arquivo
     */
    private static void runFile(String path) {
        try {
            String source = new String(Files.readAllBytes(Paths.get(path)));
            run(source, path);
        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Modo interativo (REPL)
     */
    private static void runInteractive() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Modo Interativo - Digite 'exit' para sair");
        System.out.println("Digite o código MiniPar ou o caminho do arquivo:");
        System.out.println();

        while (true) {
            System.out.print(">>> ");
            String input = scanner.nextLine();

            if (input.trim().equalsIgnoreCase("exit")) {
                System.out.println("Encerrando...");
                break;
            }

            // Verifica se é um caminho de arquivo
            if (input.trim().endsWith(".minipar")) {
                runFile(input.trim());
            } else if (!input.trim().isEmpty()) {
                run(input, "interactive");
            }

            System.out.println();
        }

        scanner.close();
    }

    /**
     * Executa as fases de análise léxica e sintática
     */
    private static void run(String source, String sourceName) {
        System.out.println("\n" + "─".repeat(70));
        System.out.println("📄 Analisando: " + sourceName);
        System.out.println("─".repeat(70));

        // ===== FASE 1: ANÁLISE LÉXICA =====
        System.out.println("\n🔍 FASE 1: ANÁLISE LÉXICA (LEXER)");
        System.out.println("─".repeat(70));

        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        System.out.println("Tokens reconhecidos: " + (tokens.size() - 1)); // -1 para excluir EOF
        System.out.println("\nLista de Tokens:");

        for (Token token : tokens) {
            if (token.type() != TokenType.EOF) {
                System.out.printf("  %-20s %-15s (Linha: %d, Coluna: %d)%n",
                    token.type(),
                    "'" + token.lexeme() + "'",
                    token.line(),
                    token.column());
            }
        }

        // ===== FASE 2: ANÁLISE SINTÁTICA =====
        System.out.println("\n🌳 FASE 2: ANÁLISE SINTÁTICA (PARSER)");
        System.out.println("─".repeat(70));

        try {
            Parser parser = new Parser(tokens);
            ASTNode ast = parser.parse();

            System.out.println("✅ Análise sintática concluída com sucesso!");
            System.out.println("\n📊 Árvore Sintática Abstrata (AST):");
            System.out.println("─".repeat(70));
            printAST(ast, 0);

        } catch (Exception e) {
            System.err.println("❌ Erro na análise sintática:");
            System.err.println("   " + e.getMessage());
        }

        System.out.println("\n" + "═".repeat(70));
    }

    /**
     * Imprime a Árvore Sintática Abstrata (AST) de forma hierárquica.
     * @param node Nó raiz da AST a ser impresso.
     * @param depth Nível de profundidade para indentação visual.
     */
    private static void printAST(ASTNode node, int depth) {
        String indent = "  ".repeat(depth);
        String className = node.getClass().getSimpleName();

        try {
            switch (className) {
                case "Program" -> {
                    System.out.println(indent + "Program {");
                    @SuppressWarnings("unchecked")
                    List<ASTNode> statements = (List<ASTNode>) node.getClass().getField("statements").get(node);
                    for (ASTNode stmt : statements) {
                        printAST(stmt, depth + 1);
                    }
                    System.out.println(indent + "}");
                }
                case "ClassDecl" -> {
                    System.out.println(indent + "ClassDecl {");
                    String name = (String) node.getClass().getField("name").get(node);
                    String superClass = (String) node.getClass().getField("superClass").get(node);
                    System.out.println(indent + "  name: " + name);
                    if (superClass != null) {
                        System.out.println(indent + "  extends: " + superClass);
                    }
                    List<?> attributes = (List<?>) node.getClass().getField("attributes").get(node);
                    List<?> methods = (List<?>) node.getClass().getField("methods").get(node);
                    if (!attributes.isEmpty()) {
                        System.out.println(indent + "  attributes: [");
                        for (Object attr : attributes) {
                            printAST((ASTNode) attr, depth + 2);
                        }
                        System.out.println(indent + "  ]");
                    }
                    if (!methods.isEmpty()) {
                        System.out.println(indent + "  methods: [");
                        for (Object method : methods) {
                            printAST((ASTNode) method, depth + 2);
                        }
                        System.out.println(indent + "  ]");
                    }
                    System.out.println(indent + "}");
                }
                case "MethodDecl" -> {
                    String name = (String) node.getClass().getField("name").get(node);
                    String returnType = (String) node.getClass().getField("returnType").get(node);
                    System.out.println(indent + "MethodDecl { name: " + name +
                            ", returnType: " + returnType + " }");
                }
                case "FuncDecl" -> {
                    String name = (String) node.getClass().getField("name").get(node);
                    String returnType = (String) node.getClass().getField("returnType").get(node);
                    System.out.println(indent + "FuncDecl { name: " + name +
                            ", returnType: " + returnType + " }");
                }
                case "VarDecl" -> {
                    String name = (String) node.getClass().getField("name").get(node);
                    String type = (String) node.getClass().getField("type").get(node);
                    ASTNode initializer = (ASTNode) node.getClass().getField("initializer").get(node);
                    System.out.print(indent + "VarDecl { name: " + name +
                            ", type: " + type);
                    if (initializer != null) {
                        System.out.print(", init: " + initializer);
                    }
                    System.out.println(" }");
                }
                case "NewInstance" -> {
                    String clsName = (String) node.getClass().getField("className").get(node);
                    System.out.println(indent + "NewInstance { class: " + clsName + " }");
                }
                case "MethodCall" -> {
                    String methodName = (String) node.getClass().getField("methodName").get(node);
                    System.out.println(indent + "MethodCall { method: " + methodName + " }");
                }
                case "IfStmt" -> {
                    ASTNode condition = (ASTNode) node.getClass().getField("condition").get(node);
                    System.out.println(indent + "IfStmt { condition: " + condition + " }");
                }
                case "WhileStmt" -> {
                    ASTNode condition = (ASTNode) node.getClass().getField("condition").get(node);
                    System.out.println(indent + "WhileStmt { condition: " + condition + " }");
                }
                default -> System.out.println(indent + node);
            }
        } catch (Exception e) {
            // Se não conseguir acessar os campos, usa toString padrão
            System.out.println(indent + node);
        }
    }
}
