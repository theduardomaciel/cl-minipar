package examples;

import parser.TCPChannel;
import java.util.*;

/**
 * Exemplo de uso de TCPChannel - Processo Cliente
 * Execute TCPServerExample primeiro, depois execute este.
 */
public class TCPClientExample {
    public static void main(String[] args) {
        // Aguarda um pouco para garantir que o servidor está rodando
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        
        TCPChannel cliente = new TCPChannel("canal_exemplo", 8080, false);
        
        try {
            System.out.println("=== CLIENTE TCP ===");
            cliente.start("localhost"); // Conecta ao servidor em localhost:8080
            
            System.out.println("Cliente conectado ao servidor!");
            
            // Recebe mensagem de boas-vindas
            List<Object> msgServidor = cliente.receive();
            System.out.println("Cliente recebeu do servidor: " + msgServidor);
            
            // Envia resposta
            List<Object> resposta = List.of("Olá", "Servidor", "Obrigado!");
            cliente.send(resposta);
            
            // Recebe dados numéricos
            List<Object> dados = cliente.receive();
            System.out.println("Cliente recebeu dados: " + dados);
            
            // Envia confirmação
            List<Object> confirmacao = List.of("OK", "Dados recebidos");
            cliente.send(confirmacao);
            
            System.out.println("Comunicação finalizada com sucesso!");
            
        } catch (Exception e) {
            System.err.println("Erro no cliente: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cliente.close();
        }
    }
}
