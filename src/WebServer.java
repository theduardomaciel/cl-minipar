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
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servidor HTTP para a interface web do interpretador MiniPar.
 * Fornece endpoints para executar código MiniPar e servir a interface HTML.
 */
public class WebServer {
    private static final int PORT = 8080;
    
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
            if (filePath.endsWith(".html")) return "text/html; charset=utf-8";
            if (filePath.endsWith(".css")) return "text/css; charset=utf-8";
            if (filePath.endsWith(".js")) return "application/javascript; charset=utf-8";
            if (filePath.endsWith(".json")) return "application/json; charset=utf-8";
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
                escapeJson(result.error)
            );
            
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
}
