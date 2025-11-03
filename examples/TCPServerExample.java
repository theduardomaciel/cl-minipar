package examples;

import io.TCPChannel;
import java.util.*;

/**
 * Exemplo de uso de TCPChannel - Processo Servidor
 * Execute este primeiro, depois execute TCPClientExample em outro terminal.
 */
public class TCPServerExample {
    public static void main(String[] args) {
        TCPChannel servidor = new TCPChannel("canal_exemplo", 8080, true);

        try {
            System.out.println("=== SERVIDOR TCP ===");
            servidor.start(null); // Escuta na porta 8080

            System.out.println("Servidor pronto. Aguardando cliente...");

            // Envia mensagem de boas-vindas
            List<Object> msgBoasVindas = List.of("Olá", "Cliente", "Bem-vindo!");
            servidor.send(msgBoasVindas);

            // Recebe resposta do cliente
            List<Object> respostaCliente = servidor.receive();
            System.out.println("Servidor recebeu do cliente: " + respostaCliente);

            // Envia dados numéricos
            List<Object> dados = List.of(42, 3.14, true, "fim");
            servidor.send(dados);

            // Recebe confirmação
            List<Object> confirmacao = servidor.receive();
            System.out.println("Servidor recebeu confirmação: " + confirmacao);

            System.out.println("Comunicação finalizada com sucesso!");

        } catch (Exception e) {
            System.err.println("Erro no servidor: " + e.getMessage());
            e.printStackTrace();
        } finally {
            servidor.close();
        }
    }
}
