import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * Classe Principal - Demonstração do Lexer e Parser MiniPar OOP
 * Tema 2 - Compiladores 2025.1
 * Atividade 2 - Analisador Léxico e Sintático
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
            if (token.getType() != TokenType.EOF) {
                System.out.printf("  %-20s %-15s (Linha: %d, Coluna: %d)%n",
                    token.getType(),
                    "'" + token.getLexeme() + "'",
                    token.getLine(),
                    token.getColumn());
            }
        }

        // ===== FASE 2: ANÁLISE SINTÁTICA =====
        System.out.println("\n🌳 FASE 2: ANÁLISE SINTÁTICA (PARSER)");
        System.out.println("─".repeat(70));

        try {
            Parser parser = new Parser(tokens);
            Program ast = parser.parse();

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
     * Imprime a AST de forma hierárquica
     */
    private static void printAST(ASTNode node, int depth) {
        String indent = "  ".repeat(depth);

        if (node instanceof Program) {
            Program prog = (Program) node;
            System.out.println(indent + "Program {");
            for (ASTNode stmt : prog.statements) {
                printAST(stmt, depth + 1);
            }
            System.out.println(indent + "}");
        } else if (node instanceof ClassDecl) {
            ClassDecl classDecl = (ClassDecl) node;
            System.out.println(indent + "ClassDecl {");
            System.out.println(indent + "  name: " + classDecl.name);
            if (classDecl.superClass != null) {
                System.out.println(indent + "  extends: " + classDecl.superClass);
            }
            if (!classDecl.attributes.isEmpty()) {
                System.out.println(indent + "  attributes: [");
                for (VarDecl attr : classDecl.attributes) {
                    printAST(attr, depth + 2);
                }
                System.out.println(indent + "  ]");
            }
            if (!classDecl.methods.isEmpty()) {
                System.out.println(indent + "  methods: [");
                for (MethodDecl method : classDecl.methods) {
                    printAST(method, depth + 2);
                }
                System.out.println(indent + "  ]");
            }
            System.out.println(indent + "}");
        } else if (node instanceof MethodDecl) {
            MethodDecl method = (MethodDecl) node;
            System.out.println(indent + "MethodDecl { name: " + method.name +
                             ", returnType: " + method.returnType + " }");
        } else if (node instanceof FuncDecl) {
            FuncDecl func = (FuncDecl) node;
            System.out.println(indent + "FuncDecl { name: " + func.name +
                             ", returnType: " + func.returnType + " }");
        } else if (node instanceof VarDecl) {
            VarDecl var = (VarDecl) node;
            System.out.print(indent + "VarDecl { name: " + var.name +
                           ", type: " + var.type);
            if (var.initializer != null) {
                System.out.print(", init: " + var.initializer);
            }
            System.out.println(" }");
        } else if (node instanceof NewInstance) {
            NewInstance newInst = (NewInstance) node;
            System.out.println(indent + "NewInstance { class: " + newInst.className + " }");
        } else if (node instanceof MethodCall) {
            MethodCall call = (MethodCall) node;
            System.out.println(indent + "MethodCall { method: " + call.methodName + " }");
        } else if (node instanceof IfStmt) {
            IfStmt ifStmt = (IfStmt) node;
            System.out.println(indent + "IfStmt { condition: " + ifStmt.condition + " }");
        } else if (node instanceof WhileStmt) {
            WhileStmt whileStmt = (WhileStmt) node;
            System.out.println(indent + "WhileStmt { condition: " + whileStmt.condition + " }");
        } else {
            System.out.println(indent + node.toString());
        }
    }

    /**
     * Exemplos de teste
     */
    public static void runExamples() {
        System.out.println("\n📚 EXECUTANDO EXEMPLOS DE TESTE");
        System.out.println("═".repeat(70));

        // Exemplo 1: Declaração de variáveis e funções
        String example1 = """
            var x: number = 10
            var nome: string = "MiniPar"
            
            func soma(a: number, b: number) -> number {
                return a + b
            }
            """;

        System.out.println("\n📝 Exemplo 1: Variáveis e Funções");
        run(example1, "Exemplo 1");

        // Exemplo 2: Classe com OOP
        String example2 = """
            class Pessoa {
                var nome: string
                var idade: number
                
                void inicializar(n: string, i: number) {
                    nome = n
                    idade = i
                }
                
                string getNome() {
                    return nome
                }
            }
            
            var p: Pessoa = new Pessoa()
            """;

        System.out.println("\n📝 Exemplo 2: Programação Orientada a Objetos");
        run(example2, "Exemplo 2");

        // Exemplo 3: Herança
        String example3 = """
            class Animal {
                var nome: string
                
                void emitirSom() {
                    print("Som genérico")
                }
            }
            
            class Cachorro extends Animal {
                void emitirSom() {
                    print("Au au!")
                }
            }
            """;

        System.out.println("\n📝 Exemplo 3: Herança");
        run(example3, "Exemplo 3");
    }
}

