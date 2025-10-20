public class Scanner {

    private String src;
    private int index = 0;
    private int line = 1;
    private int column = 1;

    public Scanner(String src) {
        this.src = src;
    }

    // Devuelve el carácter actual sin avanzar
    private char peek() {
        if (index < src.length()) {
            return src.charAt(index);
        } else {
            return '\0';
        }
    }

    // Avanza un carácter y actualiza la posición
    private char next() {
        char c = peek();
        if (c == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        index++;
        return c;
    }

    // Si el siguiente carácter es el esperado, lo consume
    private boolean match(char expected) {
        if (peek() == expected) {
            next();
            return true;
        }
        return false;
    }

    // Devuelve el siguiente token
    public Token nextToken() {

        // Ignorar espacios, saltos y comentarios
        while (true) {
            char c = peek();

            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                next();
                continue;
            }

            // Comentarios
            if (c == '/') {
                int startLine = line;
                int startCol = column;
                next();

                // Comentario de una línea
                if (peek() == '/') {
                    while (peek() != '\n' && peek() != '\0') {
                        next();
                    }
                    continue;
                }

                // Comentario de varias líneas
                if (peek() == '*') {
                    next();
                    boolean cerrado = false;

                    while (peek() != '\0') {
                        if (peek() == '*') {
                            next();
                            if (peek() == '/') {
                                next();
                                cerrado = true;
                                break;
                            }
                        } else {
                            next();
                        }
                    }

                    if (!cerrado) {
                        return error("Comentario sin cierre", startLine, startCol);
                    }

                    continue;
                }

                // Si no era comentario, devolver '/'
                return new Token(TokenType.SLASH, "/", startLine, startCol);
            }

            break;
        }

        // Fin de archivo
        if (peek() == '\0') {
            return new Token(TokenType.EOF, "", line, column);
        }

        // Cadenas
        if (peek() == '"') {
            int startLine = line;
            int startCol = column;
            next(); // consumir la comilla

            String text = "";
            while (peek() != '"' && peek() != '\0') {
                char c = next();
                if (c == '\n') {
                    // Si no se permiten saltos dentro de cadenas, sería error
                    return error("Cadena sin cierre", startLine, startCol);
                }
                text += c;
            }

            if (peek() != '"') {
                return error("Cadena sin cierre", startLine, startCol);
            }

            next(); // cerrar comillas
            return new Token(TokenType.STRING_CONST, text, startLine, startCol);
        }

        // Números
        if (Character.isDigit(peek())) {
            int startLine = line;
            int startCol = column;
            String number = "";

            while (Character.isDigit(peek())) {
                number += next();
            }

            if (peek() == '.') {
                number += next();
                if (!Character.isDigit(peek())) {
                    return error("Número real mal formado", startLine, startCol);
                }

                while (Character.isDigit(peek())) {
                    number += next();
                }

                return new Token(TokenType.REAL_CONST, number, startLine, startCol);
            }

            return new Token(TokenType.INT_CONST, number, startLine, startCol);
        }

        // Identificadores y palabras reservadas
        if (Character.isLetter(peek()) || peek() == '_') {
            int startLine = line;
            int startCol = column;
            String word = "";

            while (Character.isLetterOrDigit(peek()) || peek() == '_') {
                word += next();
            }

            switch (word) {
                case "long": return new Token(TokenType.LONG, word, startLine, startCol);
                case "double": return new Token(TokenType.DOUBLE, word, startLine, startCol);
                case "if": return new Token(TokenType.IF, word, startLine, startCol);
                case "then": return new Token(TokenType.THEN, word, startLine, startCol);
                case "else": return new Token(TokenType.ELSE, word, startLine, startCol);
                case "while": return new Token(TokenType.WHILE, word, startLine, startCol);
                case "break": return new Token(TokenType.BREAK, word, startLine, startCol);
                case "read": return new Token(TokenType.READ, word, startLine, startCol);
                case "write": return new Token(TokenType.WRITE, word, startLine, startCol);
                case "true": return new Token(TokenType.TRUE, word, startLine, startCol);
                case "false": return new Token(TokenType.FALSE, word, startLine, startCol);
            }

            return new Token(TokenType.ID, word, startLine, startCol);
        }

        // Operadores y símbolos
        int startLine = line;
        int startCol = column;
        char c = next();

        if (c == '+') {
            if (match('=')) return new Token(TokenType.PLUSEQ, "+=", startLine, startCol);
            return new Token(TokenType.PLUS, "+", startLine, startCol);
        }

        if (c == '-') {
            if (match('=')) return new Token(TokenType.MINUSEQ, "-=", startLine, startCol);
            return new Token(TokenType.MINUS, "-", startLine, startCol);
        }

        if (c == '*') {
            if (match('=')) return new Token(TokenType.STAREQ, "*=", startLine, startCol);
            return new Token(TokenType.STAR, "*", startLine, startCol);
        }

        if (c == '>') {
            if (match('=')) return new Token(TokenType.GE, ">=", startLine, startCol);
            return new Token(TokenType.GT, ">", startLine, startCol);
        }

        if (c == '<') {
            if (match('=')) return new Token(TokenType.LE, "<=", startLine, startCol);
            if (match('>')) return new Token(TokenType.DIAMOND_NEQ, "<>", startLine, startCol);
            return new Token(TokenType.LT, "<", startLine, startCol);
        }

        if (c == '=') {
            if (match('=')) return new Token(TokenType.EQEQ, "==", startLine, startCol);
            return new Token(TokenType.ASSIGN, "=", startLine, startCol);
        }

        if (c == '!') {
            if (match('=')) return new Token(TokenType.NEQ, "!=", startLine, startCol);
            return new Token(TokenType.NOT, "!", startLine, startCol);
        }

        if (c == '&') {
            if (match('&')) return new Token(TokenType.ANDAND, "&&", startLine, startCol);
        }

        if (c == '|') {
            if (match('|')) return new Token(TokenType.OROR, "||", startLine, startCol);
        }

        if (c == '(') return new Token(TokenType.LPAREN, "(", startLine, startCol);
        if (c == ')') return new Token(TokenType.RPAREN, ")", startLine, startCol);
        if (c == '{') return new Token(TokenType.LBRACE, "{", startLine, startCol);
        if (c == '}') return new Token(TokenType.RBRACE, "}", startLine, startCol);
        if (c == ';') return new Token(TokenType.SEMICOLON, ";", startLine, startCol);

        // Si no se reconoce el símbolo
        return error("Símbolo no reconocido: '" + c + "'", startLine, startCol);
    }

    private Token error(String msg, int ln, int col) {
        return new Token(TokenType.ERROR, "Error léxico [" + ln + ":" + col + "]: " + msg, ln, col);
    }
}
