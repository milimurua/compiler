/**
 * TokenType: palabras reservadas, identificadores, literales, operadores, delimitadores, EOF, ERROR.
 */
enum TokenType {
    // palabras
    LONG, DOUBLE, IF, THEN, ELSE, WHILE, BREAK, READ, WRITE, TRUE, FALSE,

    // Identificadores
    ID, INT_CONST, REAL_CONST, STRING_CONST,

    // Operadores y delimitadores
    PLUS, MINUS, STAR, SLASH,
    GT, LT, GE, LE, EQEQ, NEQ, DIAMOND_NEQ, ASSIGN,
    LPAREN, RPAREN, LBRACE, RBRACE, SEMICOLON,
    PLUSEQ, MINUSEQ, STAREQ, SLASHEQ, ANDAND, OROR, NOT,

    // Error y Especiales
    EOF, ERROR
}
