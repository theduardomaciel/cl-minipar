package io;

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
}
