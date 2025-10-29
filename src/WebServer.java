import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import lexer.Lexer;
import lexer.Token;
import parser.Parser;
import parser.Program;
import parser.Interpreter;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Servidor HTTP para a interface web do interpretador MiniPar.
 * Fornece endpoints para executar código MiniPar e servir a interface HTML.
 */
public class WebServer {
    private static final int PORT = 8080;
    private static final Map<String, ExecutionSession> sessions = new ConcurrentHashMap<>();

    /**
     * Encontra o diretório web, verificando se estamos em build/ ou na raiz
     */
    private static String findWebDirectory() {
        // Tentar web/ no diretório atual
        File webDir = new File("web");
        if (webDir.exists() && webDir.isDirectory()) {
            return "web";
        }

        // Tentar ../web (caso estejamos em build/)
        webDir = new File("../web");
        if (webDir.exists() && webDir.isDirectory()) {
            return "../web";
        }

        // Fallback para web/
        return "web";
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Endpoint para servir a página HTML
        server.createContext("/", new StaticFileHandler());

        // Endpoint para executar código MiniPar
        server.createContext("/execute", new ExecuteHandler());

        // Endpoints para execução com input interativo
        server.createContext("/session/start", new StartSessionHandler());
        server.createContext("/session/status", new SessionStatusHandler());
        server.createContext("/session/input", new ProvideInputHandler());

        // Endpoint para análise (tokens e AST)
        server.createContext("/analyze", new AnalyzeHandler());

        server.setExecutor(null); // usa executor padrão
        server.start();

        System.out.println("=".repeat(70));
        System.out.println("  SERVIDOR WEB MINIPAR");
        System.out.println("=".repeat(70));
        System.out.println("\n🌐 Servidor rodando em: http://localhost:" + PORT);
        System.out.println("📝 Acesse a interface web no navegador");
        System.out.println("\nPressione Ctrl+C para encerrar o servidor\n");
    }

    /**
     * Handler para retornar Tokens e AST (sem executar o programa)
     */
    static class AnalyzeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            // Ler o código do corpo da requisição
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String code = br.lines().collect(Collectors.joining("\n"));

            String jsonResponse;
            try {
                Lexer lexer = new Lexer(code);
                java.util.List<Token> tokens = lexer.scanTokens();

                Parser parser = new Parser(tokens);
                Program program = parser.parse();

                // Montar JSON
                String tokensJson = tokensToJson(tokens);
                String astStr = program != null ? program.toString() : "";
                String astTree = program != null ? astToJson(program) : "null";
                jsonResponse = String.format(
                        "{\"success\": true, \"tokens\": %s, \"ast\": %s, \"astTree\": %s, \"error\": \"\"}",
                        tokensJson, escapeJson(astStr), astTree);
            } catch (Exception e) {
                // Em caso de erro, ainda podemos tentar retornar os tokens (se possível)
                String tokensJson = "[]";
                try {
                    Lexer lexer = new Lexer(code);
                    java.util.List<Token> partialTokens = lexer.scanTokens();
                    tokensJson = tokensToJson(partialTokens);
                } catch (Exception ignored) {
                }

                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String err = e.getMessage() != null ? ("Erro: " + e.getMessage() + "\n\n" + sw) : sw.toString();
                jsonResponse = String.format(
                        "{\"success\": false, \"tokens\": %s, \"ast\": \"\", \"astTree\": null, \"error\": %s}",
                        tokensJson, escapeJson(err));
            }

            byte[] response = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }

        private String astToJson(parser.ASTNode node) {
            if (node == null)
                return "null";
            StringBuilder sb = new StringBuilder();
            sb.append('{');
            String type = node.getClass().getSimpleName();
            sb.append("\"type\":\"").append(escapeForJsonRaw(type)).append("\",");
            // label básico: inclui até 2 campos String/number/bool
            String label = buildLabel(node);
            sb.append("\"label\":").append(escapeJson(label)).append(",");
            // children
            sb.append("\"children\":");
            sb.append('[');
            boolean first = true;
            try {
                java.lang.reflect.Field[] fields = node.getClass().getFields();
                for (java.lang.reflect.Field f : fields) {
                    Object v = f.get(node);
                    if (v == null)
                        continue;
                    if (v instanceof parser.ASTNode) {
                        if (!first)
                            sb.append(',');
                        sb.append(astToJson((parser.ASTNode) v));
                        first = false;
                    } else if (v instanceof java.util.List<?>) {
                        java.util.List<?> list = (java.util.List<?>) v;
                        for (Object item : list) {
                            if (item instanceof parser.ASTNode) {
                                if (!first)
                                    sb.append(',');
                                sb.append(astToJson((parser.ASTNode) item));
                                first = false;
                            } else if (item != null && item.getClass().getSimpleName().equals("DictEntry")) {
                                // DictEntry: tem campos key e value (ASTNode)
                                try {
                                    java.lang.reflect.Field keyF = item.getClass().getField("key");
                                    java.lang.reflect.Field valF = item.getClass().getField("value");
                                    Object key = keyF.get(item);
                                    Object val = valF.get(item);
                                    if (!first)
                                        sb.append(',');
                                    sb.append('{')
                                            .append("\"type\":\"DictEntry\",")
                                            .append("\"label\":\"entry\",")
                                            .append("\"children\":[")
                                            .append(key instanceof parser.ASTNode ? astToJson((parser.ASTNode) key)
                                                    : "null")
                                            .append(',')
                                            .append(val instanceof parser.ASTNode ? astToJson((parser.ASTNode) val)
                                                    : "null")
                                            .append("]}");
                                    first = false;
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
            sb.append(']');
            sb.append('}');
            return sb.toString();
        }

        private String buildLabel(Object node) {
            try {
                java.lang.reflect.Field[] fields = node.getClass().getFields();
                java.util.List<String> parts = new java.util.ArrayList<>();
                for (java.lang.reflect.Field f : fields) {
                    Class<?> t = f.getType();
                    if (t == String.class || Number.class.isAssignableFrom(t) || t == boolean.class
                            || t == Boolean.class) {
                        Object v = f.get(node);
                        if (v != null) {
                            parts.add(f.getName() + "=" + v.toString());
                            if (parts.size() >= 2)
                                break;
                        }
                    }
                }
                String base = node.getClass().getSimpleName();
                if (parts.isEmpty())
                    return base;
                return base + "(" + String.join(", ", parts) + ")";
            } catch (Exception e) {
                return node.getClass().getSimpleName();
            }
        }

        private String escapeForJsonRaw(String s) {
            if (s == null)
                return "";
            StringBuilder sb = new StringBuilder();
            for (char c : s.toCharArray()) {
                switch (c) {
                    case '"':
                        sb.append("\\\"");
                        break;
                    case '\\':
                        sb.append("\\\\");
                        break;
                    default:
                        sb.append(c);
                }
            }
            return sb.toString();
        }

        private String tokensToJson(java.util.List<Token> tokens) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < tokens.size(); i++) {
                Token t = tokens.get(i);
                sb.append("{\"type\":\"").append(t.type()).append("\",")
                        .append("\"lexeme\":").append(escapeJson(t.lexeme())).append(",")
                        .append("\"line\":").append(t.line()).append(",")
                        .append("\"column\":").append(t.column()).append("}");
                if (i < tokens.size() - 1)
                    sb.append(",");
            }
            sb.append("]");
            return sb.toString();
        }

        private String escapeJson(String str) {
            if (str == null || str.isEmpty())
                return "\"\"";
            StringBuilder sb = new StringBuilder();
            sb.append('"');
            for (char c : str.toCharArray()) {
                switch (c) {
                    case '"':
                        sb.append("\\\"");
                        break;
                    case '\\':
                        sb.append("\\\\");
                        break;
                    case '\b':
                        sb.append("\\b");
                        break;
                    case '\f':
                        sb.append("\\f");
                        break;
                    case '\n':
                        sb.append("\\n");
                        break;
                    case '\r':
                        sb.append("\\r");
                        break;
                    case '\t':
                        sb.append("\\t");
                        break;
                    default:
                        if (c < ' ')
                            sb.append(String.format("\\u%04x", (int) c));
                        else
                            sb.append(c);
                }
            }
            sb.append('"');
            return sb.toString();
        }
    }

    /**
     * Handler para servir arquivos estáticos (HTML, CSS, JS)
     */
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();

            if (path.equals("/")) {
                path = "/index.html";
            }

            // Mapear para o diretório web (relativo ao diretório de execução ou absoluto)
            String filePath = findWebDirectory() + path;
            File file = new File(filePath);

            if (file.exists() && file.isFile()) {
                // Determinar Content-Type
                String contentType = getContentType(filePath);

                byte[] content = Files.readAllBytes(file.toPath());

                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, content.length);

                OutputStream os = exchange.getResponseBody();
                os.write(content);
                os.close();
            } else {
                String response = "404 - Arquivo não encontrado";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }

        private String getContentType(String filePath) {
            if (filePath.endsWith(".html"))
                return "text/html; charset=utf-8";
            if (filePath.endsWith(".css"))
                return "text/css; charset=utf-8";
            if (filePath.endsWith(".js"))
                return "application/javascript; charset=utf-8";
            if (filePath.endsWith(".json"))
                return "application/json; charset=utf-8";
            return "text/plain; charset=utf-8";
        }
    }

    /**
     * Handler para executar código MiniPar e retornar o resultado
     */
    static class ExecuteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            // Ler o código do corpo da requisição
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String code = br.lines().collect(Collectors.joining("\n"));

            // Executar o código e capturar a saída
            ExecutionResult result = executeCode(code);

            // Construir resposta JSON
            String jsonResponse = String.format(
                    "{\"success\": %s, \"output\": %s, \"error\": %s}",
                    result.success,
                    escapeJson(result.output),
                    escapeJson(result.error));

            byte[] response = jsonResponse.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, response.length);

            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }

        private ExecutionResult executeCode(String code) {
            // Capturar System.out e System.err
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            PrintStream originalErr = System.err;

            try {
                System.setOut(new PrintStream(outContent, true, StandardCharsets.UTF_8));
                System.setErr(new PrintStream(errContent, true, StandardCharsets.UTF_8));

                // Executar análise léxica
                Lexer lexer = new Lexer(code);
                List<Token> tokens = lexer.scanTokens();

                // Executar análise sintática
                Parser parser = new Parser(tokens);
                Program program = parser.parse();

                // Executar o programa
                Interpreter interpreter = new Interpreter();
                interpreter.execute(program);

                // Restaurar streams
                System.setOut(originalOut);
                System.setErr(originalErr);

                String output = outContent.toString(StandardCharsets.UTF_8);
                String error = errContent.toString(StandardCharsets.UTF_8);

                if (!error.isEmpty()) {
                    return new ExecutionResult(false, output, error);
                }

                return new ExecutionResult(true, output, "");

            } catch (Exception e) {
                System.setOut(originalOut);
                System.setErr(originalErr);

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);

                String output = outContent.toString(StandardCharsets.UTF_8);
                String errorMessage = "Erro: " + e.getMessage() + "\n\n" + sw.toString();

                return new ExecutionResult(false, output, errorMessage);
            }
        }

        private String escapeJson(String str) {
            if (str == null || str.isEmpty()) {
                return "\"\"";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("\"");

            for (char c : str.toCharArray()) {
                switch (c) {
                    case '"':
                        sb.append("\\\"");
                        break;
                    case '\\':
                        sb.append("\\\\");
                        break;
                    case '\b':
                        sb.append("\\b");
                        break;
                    case '\f':
                        sb.append("\\f");
                        break;
                    case '\n':
                        sb.append("\\n");
                        break;
                    case '\r':
                        sb.append("\\r");
                        break;
                    case '\t':
                        sb.append("\\t");
                        break;
                    default:
                        if (c < ' ') {
                            sb.append(String.format("\\u%04x", (int) c));
                        } else {
                            sb.append(c);
                        }
                }
            }

            sb.append("\"");
            return sb.toString();
        }
    }

    static class ExecutionResult {
        boolean success;
        String output;
        String error;

        ExecutionResult(boolean success, String output, String error) {
            this.success = success;
            this.output = output;
            this.error = error;
        }
    }

    /**
     * Handler para iniciar uma sessão de execução interativa
     */
    static class StartSessionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            // Ler o código do corpo da requisição
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String code = br.lines().collect(Collectors.joining("\n"));

            // Criar e iniciar sessão
            ExecutionSession session = new ExecutionSession(code);
            sessions.put(session.getSessionId(), session);
            session.start();

            // Retornar ID da sessão
            String jsonResponse = String.format(
                    "{\"sessionId\": \"%s\"}",
                    session.getSessionId());

            byte[] response = jsonResponse.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, response.length);

            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    /**
     * Handler para verificar o status de uma sessão
     */
    static class SessionStatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            // Obter sessionId da query string
            String query = exchange.getRequestURI().getQuery();
            String sessionId = null;
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2 && pair[0].equals("sessionId")) {
                        sessionId = pair[1];
                        break;
                    }
                }
            }

            if (sessionId == null) {
                sendError(exchange, "sessionId não fornecido");
                return;
            }

            ExecutionSession session = sessions.get(sessionId);
            if (session == null) {
                sendError(exchange, "Sessão não encontrada");
                return;
            }

            // Construir resposta com status da sessão
            String jsonResponse = String.format(
                    "{\"running\": %s, \"waitingForInput\": %s, \"output\": %s, \"error\": %s}",
                    session.isRunning(),
                    session.isWaitingForInput(),
                    escapeJson(session.getOutput()),
                    escapeJson(session.getError()));

            byte[] response = jsonResponse.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, response.length);

            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();

            // Limpar sessão se não está mais rodando
            if (!session.isRunning()) {
                sessions.remove(sessionId);
            }
        }

        private String escapeJson(String str) {
            if (str == null || str.isEmpty()) {
                return "\"\"";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("\"");

            for (char c : str.toCharArray()) {
                switch (c) {
                    case '"':
                        sb.append("\\\"");
                        break;
                    case '\\':
                        sb.append("\\\\");
                        break;
                    case '\b':
                        sb.append("\\b");
                        break;
                    case '\f':
                        sb.append("\\f");
                        break;
                    case '\n':
                        sb.append("\\n");
                        break;
                    case '\r':
                        sb.append("\\r");
                        break;
                    case '\t':
                        sb.append("\\t");
                        break;
                    default:
                        if (c < ' ') {
                            sb.append(String.format("\\u%04x", (int) c));
                        } else {
                            sb.append(c);
                        }
                }
            }

            sb.append("\"");
            return sb.toString();
        }

        private void sendError(HttpExchange exchange, String message) throws IOException {
            String jsonResponse = String.format("{\"error\": \"%s\"}", message);
            byte[] response = jsonResponse.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(400, response.length);

            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    /**
     * Handler para fornecer input para uma sessão em execução
     */
    static class ProvideInputHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            // Ler JSON do corpo da requisição
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String body = br.lines().collect(Collectors.joining("\n"));

            // Parse simples do JSON
            String sessionId = extractJsonField(body, "sessionId");
            String input = extractJsonField(body, "input");

            if (sessionId == null || input == null) {
                sendError(exchange, "sessionId e input são obrigatórios");
                return;
            }

            ExecutionSession session = sessions.get(sessionId);
            if (session == null) {
                sendError(exchange, "Sessão não encontrada");
                return;
            }

            // Fornecer input para a sessão
            session.provideInput(input);

            // Retornar sucesso
            String jsonResponse = "{\"success\": true}";
            byte[] response = jsonResponse.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, response.length);

            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }

        private String extractJsonField(String json, String field) {
            String pattern = "\"" + field + "\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
            return null;
        }

        private void sendError(HttpExchange exchange, String message) throws IOException {
            String jsonResponse = String.format("{\"error\": \"%s\"}", message);
            byte[] response = jsonResponse.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(400, response.length);

            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }
}
