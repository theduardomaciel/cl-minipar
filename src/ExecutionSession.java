import parser.*;
import lexer.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Sessão de execução com suporte a input interativo.
 * Gerencia a execução assíncrona do programa com callbacks para entrada de dados.
 */
public class ExecutionSession {
    private final String sessionId;
    private final String code;
    private final BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean running = false;
    private volatile boolean waitingForInput = false;
    private volatile String currentPrompt = "";
    private Future<?> executionFuture;
    
    public ExecutionSession(String code) {
        this.sessionId = UUID.randomUUID().toString();
        this.code = code;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public boolean isWaitingForInput() {
        return waitingForInput;
    }
    
    public String getCurrentPrompt() {
        return currentPrompt;
    }
    
    public String getOutput() {
        return outContent.toString(StandardCharsets.UTF_8);
    }
    
    public String getError() {
        return errContent.toString(StandardCharsets.UTF_8);
    }
    
    /**
     * Inicia a execução do código em uma thread separada
     */
    public void start() {
        if (running) {
            throw new IllegalStateException("Sessão já está em execução");
        }
        
        running = true;
        executionFuture = executor.submit(() -> {
            PrintStream originalOut = System.out;
            PrintStream originalErr = System.err;
            
            try {
                System.setOut(new PrintStream(outContent, true, StandardCharsets.UTF_8));
                System.setErr(new PrintStream(errContent, true, StandardCharsets.UTF_8));
                
                // Análise léxica
                Lexer lexer = new Lexer(code);
                List<Token> tokens = lexer.scanTokens();
                
                // Análise sintática
                Parser parser = new Parser(tokens);
                Program program = parser.parse();
                
                // Interpretação com callback de input
                Interpreter interpreter = new Interpreter();
                interpreter.setInputCallback(new InputCallback() {
                    @Override
                    public String readLine() throws Exception {
                        waitingForInput = true;
                        String input = inputQueue.take(); // Bloqueia até receber input
                        waitingForInput = false;
                        return input;
                    }
                    
                    @Override
                    public double readNumber() throws Exception {
                        waitingForInput = true;
                        String input = inputQueue.take(); // Bloqueia até receber input
                        waitingForInput = false;
                        try {
                            return Double.parseDouble(input.trim());
                        } catch (NumberFormatException e) {
                            throw new Exception("Entrada inválida: esperado um número");
                        }
                    }
                });
                
                interpreter.execute(program);
                
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                
                try {
                    errContent.write(("Erro: " + e.getMessage() + "\n" + sw.toString()).getBytes(StandardCharsets.UTF_8));
                } catch (IOException ignored) {}
            } finally {
                System.setOut(originalOut);
                System.setErr(originalErr);
                running = false;
                waitingForInput = false;
            }
        });
    }
    
    /**
     * Fornece input para o programa em execução
     */
    public void provideInput(String input) {
        inputQueue.offer(input);
    }
    
    /**
     * Cancela a execução
     */
    public void cancel() {
        if (executionFuture != null) {
            executionFuture.cancel(true);
        }
        running = false;
        waitingForInput = false;
        executor.shutdownNow();
    }
    
    /**
     * Aguarda o término da execução
     */
    public void waitForCompletion(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (executionFuture != null) {
            executionFuture.get(timeout, unit);
        }
    }
}
