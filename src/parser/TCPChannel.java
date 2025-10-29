package parser;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Canal de comunicação via TCP para comunicação entre processos.
 * Suporta dois modos: servidor (escuta conexões) e cliente (conecta ao servidor).
 */
public class TCPChannel {
    private final String name;
    private final int port;
    private final boolean isServer;
    private ServerSocket serverSocket;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean connected = false;

    /**
     * Cria um canal TCP.
     * @param name Nome do canal
     * @param port Porta TCP (0 para alocar automaticamente se servidor)
     * @param isServer true para modo servidor, false para modo cliente
     */
    public TCPChannel(String name, int port, boolean isServer) {
        this.name = name;
        this.port = port;
        this.isServer = isServer;
    }

    /**
     * Inicia o canal (servidor escuta ou cliente conecta).
     * @param host Host para conectar (apenas modo cliente)
     */
    public void start(String host) throws IOException {
        if (isServer) {
            serverSocket = new ServerSocket(port);
            System.out.println("[TCPChannel " + name + "] Servidor escutando na porta " + serverSocket.getLocalPort());
            // Aguarda conexão (blocking)
            socket = serverSocket.accept();
            System.out.println("[TCPChannel " + name + "] Cliente conectado: " + socket.getRemoteSocketAddress());
        } else {
            socket = new Socket(host, port);
            System.out.println("[TCPChannel " + name + "] Conectado ao servidor " + host + ":" + port);
        }
        
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
        connected = true;
    }

    /**
     * Envia uma mensagem pelo canal.
     * @param message Lista de objetos serializáveis
     */
    public void send(List<Object> message) throws IOException {
        if (!connected) throw new IllegalStateException("Canal não conectado");
        
        // Serializa a lista como ArrayList para garantir que seja serializável
        ArrayList<Object> serializableMsg = new ArrayList<>(message);
        out.writeObject(serializableMsg);
        out.flush();
        System.out.println("[TCPChannel " + name + "] Enviado: " + serializableMsg);
    }

    /**
     * Recebe uma mensagem do canal (blocking).
     * @return Lista de objetos recebidos
     */
    @SuppressWarnings("unchecked")
    public List<Object> receive() throws IOException, ClassNotFoundException {
        if (!connected) throw new IllegalStateException("Canal não conectado");
        
        Object obj = in.readObject();
        if (obj instanceof List) {
            List<Object> msg = (List<Object>) obj;
            System.out.println("[TCPChannel " + name + "] Recebido: " + msg);
            return msg;
        }
        throw new IOException("Mensagem inválida recebida: " + obj);
    }

    /**
     * Fecha o canal e libera recursos.
     */
    public void close() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
            connected = false;
            System.out.println("[TCPChannel " + name + "] Canal fechado");
        } catch (IOException e) {
            System.err.println("[TCPChannel " + name + "] Erro ao fechar canal: " + e.getMessage());
        }
    }

    public String getName() { return name; }
    public int getPort() { return isServer ? serverSocket.getLocalPort() : port; }
    public boolean isConnected() { return connected; }
    public boolean isServer() { return isServer; }

    @Override
    public String toString() {
        return "<TCPChannel " + name + " " + (isServer ? "server" : "client") + ":" + getPort() + ">";
    }
}
