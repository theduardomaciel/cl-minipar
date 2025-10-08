package lexer;

/**
 * Representa um token identificado pelo lexer.
 *
 * @param type   Tipo do token.
 * @param lexeme Texto correspondente ao token.
 * @param line   Linha onde o token foi encontrado.
 * @param column Coluna onde o token foi encontrado.
 */
public record Token(TokenType type, String lexeme, int line, int column) {

    /**
     * Retorna uma representação em string do token.
     *
     * @return String formatada com os detalhes do token.
     */
    @Override
    public String toString() {
        return String.format("Token{type=%s, lexeme='%s', line=%d, col=%d}",
                type, lexeme, line, column);
    }
}