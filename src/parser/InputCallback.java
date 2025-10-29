package parser;

/**
 * Interface para callback de entrada de dados.
 * Permite ao interpretador solicitar entrada do usuário de forma assíncrona.
 */
public interface InputCallback {
    /**
     * Solicita uma linha de texto do usuário.
     * @return String lida do usuário
     */
    String readLine() throws Exception;
    
    /**
     * Solicita um número do usuário.
     * @return Número lido do usuário
     */
    double readNumber() throws Exception;
    
    /**
     * Solicita configuração de canal TCP do usuário.
     * @param canalName Nome do canal sendo configurado
     * @param comp1 Nome do primeiro componente
     * @param comp2 Nome do segundo componente
     * @return TCPChannelConfig com as configurações (isServer, host, port)
     */
    TCPChannelConfig requestTCPConfig(String canalName, String comp1, String comp2) throws Exception;
    
    /**
     * Classe para encapsular configurações de canal TCP
     */
    class TCPChannelConfig {
        public final boolean isServer;
        public final String host;
        public final int port;
        
        public TCPChannelConfig(boolean isServer, String host, int port) {
            this.isServer = isServer;
            this.host = host;
            this.port = port;
        }
    }
}
