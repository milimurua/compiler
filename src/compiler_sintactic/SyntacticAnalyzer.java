package compiler_sintactic;
import compiler_semantic.SemanticAnalyzer;
import compiler_lexer.Token;
import compiler_lexer.TokenType;

public class SyntacticAnalyzer {

    private String src;
    private int pos = 0;
    private int line = 1;
    private int col = 1;
    private final SemanticAnalyzer semantic = new SemanticAnalyzer(src);


    public SyntacticAnalyzer(String src) {
        this.src = src != null ? src : "";
    }

    private boolean end() { return pos >= src.length(); }
    private char cur() { return end() ? '\0' : src.charAt(pos); }

    private void next() {
        if (end()) return;
        if (cur() == '\n') { line++; col = 1; }
        else col++;
        pos++;
    }

    private void skip() {
        while (!end()) {
            char c = cur();
            if (Character.isWhitespace(c)) { next(); continue; }
            if (c == '/' && peek() == '/') { while (!end() && cur() != '\n') next(); continue; }
            if (c == '/' && peek() == '*') {
                next();
                do next();
                while (!end() && !(cur() == '*' && peek() == '/'));
                if (!end()) { next(); next(); }
                continue;
            }
            break;
        }
    }

    private char peek() {
        int p = pos + 1;
        return p >= src.length() ? '\0' : src.charAt(p);
    }

    private void error(String msg) {
        throw new SyntacticError(msg + " en línea " + line + ", columna " + col);
    }

    private void expect(char c) {
        skip();
        if (cur() != c) error("Se esperaba '" + c + "'");
        next();
    }

    private void id() {
        skip();
        if (!Character.isLetter(cur()) && cur() != '_')
            error("Se esperaba identificador");
        while (!end() && (Character.isLetterOrDigit(cur()) || cur() == '_')) next();
    }

    private void num() {
        skip();
        if (!Character.isDigit(cur())) error("Se esperaba número");
        while (!end() && Character.isDigit(cur())) next();
        if (cur() == '.') {
            next();
            if (!Character.isDigit(cur())) error("Número mal formado");
            while (!end() && Character.isDigit(cur())) next();
        }
    }

    public void parseProgram() {
        skip();
        if (end()) error("Archivo vacío o sin instrucciones válidas");
        while (!end()) {
            parseStatement();
            skip();
        }
    }

    private void parseStatement() {
        skip();
        if (starts("int") || starts("boolean") || starts("long") || starts("double")) { parseDecl(); return; }
        if (starts("if")) { parseIf(); return; }
        if (starts("while")) { parseWhile(); return; }
        if (starts("read")) { parseRead(); return; }
        if (starts("write")) { parseWrite(); return; }
        if (cur() == '{') { parseBlock(); return; }

        parseExpression();
        skip();
        if (cur() == ';') next();
        else error("Se esperaba ';' al final de la sentencia");
    }

    private void parseDecl() {
        // Detectar tipo
        String type = null;
        if (starts("int")) { accept("int"); type = "int"; }
        else if (starts("boolean")) { accept("boolean"); type = "boolean"; }
        else if (starts("long")) { accept("long"); type = "long"; }
        else if (starts("double")) { accept("double"); type = "double"; }
        else error("Tipo no reconocido");

        skip();

        // Leer identificador
        StringBuilder name = new StringBuilder();
        if (!Character.isLetter(cur()) && cur() != '_') error("Se esperaba identificador");
        while (!end() && (Character.isLetterOrDigit(cur()) || cur() == '_')) {
            name.append(cur());
            next();
        }

        // Crear token para el analizador semántico
        Token idToken = new Token(TokenType.ID, name.toString(), line, col);
        semantic.declareVariable(idToken, type);

        skip();

        // Declaraciones múltiples separadas por coma
        while (cur() == ',') {
            next();
            skip();

            StringBuilder nextName = new StringBuilder();
            if (!Character.isLetter(cur()) && cur() != '_') error("Se esperaba identificador");
            while (!end() && (Character.isLetterOrDigit(cur()) || cur() == '_')) {
                nextName.append(cur());
                next();
            }

            Token nextId = new Token(TokenType.ID, nextName.toString(), idToken.getLine(), idToken.getColumn());
            semantic.declareVariable(nextId, type);

            skip();
        }

        // Asignación opcional
        if (cur() == '=') {
            next();
            parseExpression();
        }

        skip();
        if (cur() == ';') next();
        else error("Se esperaba ';'");
    }

    private void parseType() {
        if (starts("int")) accept("int");
        else if (starts("boolean")) accept("boolean");
        else if (starts("long")) accept("long");
        else if (starts("double")) accept("double");
        else error("Tipo no reconocido");
    }

    private void parseIf() {
        accept("if");
        skip();
        if (cur() == '(') { next(); parseExpression(); if (cur() == ')') next(); else error("Se esperaba ')'"); }
        else parseExpression();
        skip();
        if (starts("then")) accept("then");
        skip();
        parseStatement();
        skip();
        if (starts("else")) { accept("else"); skip(); parseStatement(); }
    }

    private void parseWhile() {
        accept("while");
        skip();
        if (cur() == '(') { next(); parseExpression(); if (cur() == ')') next(); else error("Se esperaba ')'"); }
        else parseExpression();
        skip();
        parseStatement();
    }

    private void parseRead() {
        accept("read");
        skip();
        expect('(');
        id();
        expect(')');
        skip();
        if (cur() == ';') next();
    }

    private void parseWrite() {
        accept("write");
        skip();
        expect('(');
        parseExpression();
        expect(')');
        skip();
        if (cur() == ';') next();
    }

    private void parseBlock() {
        expect('{');
        skip();
        while (!end() && cur() != '}') {
            parseStatement();
            skip();
        }
        if (cur() == '}') next();
        else error("Se esperaba '}'");
    }

    private void parseExpression() {
        skip();
        parseTerm();
        skip();
        while (starts("&&") || starts("||") ||
                starts("==") || starts("!=") ||
                starts(">=") || starts("<=") ||
                cur() == '>' || cur() == '<' || cur() == '+' || cur() == '-') {
            if (starts("&&")) accept("&&");
            else if (starts("||")) accept("||");
            else if (starts("==")) accept("==");
            else if (starts("!=")) accept("!=");
            else if (starts(">=")) accept(">=");
            else if (starts("<=")) accept("<=");
            else { next(); }
            parseTerm();
            skip();
        }
    }

    private void parseTerm() {
        skip();
        parseFactor();
        skip();
        while (starts("*=") || starts("/=") || cur() == '*' || cur() == '/') {
            if (starts("*=")) accept("*=");
            else if (starts("/=")) accept("/=");
            else next();
            parseFactor();
            skip();
        }
    }

    private void parseFactor() {
        skip();
        if (Character.isDigit(cur())) { num(); return; }
        if (Character.isLetter(cur()) || cur() == '_') {
            id();
            skip();
            if (starts("+=")) { accept("+="); parseExpression(); return; }
            if (starts("-=")) { accept("-="); parseExpression(); return; }
            if (starts("*=")) { accept("*="); parseExpression(); return; }
            if (starts("/=")) { accept("/="); parseExpression(); return; }
            if (cur() == '=') { next(); parseExpression(); return; }
            return;
        }
        if (cur() == '(') {
            next();
            parseExpression();
            if (cur() == ')') next();
            else error("Se esperaba ')'");
            return;
        }
        error("Expresión inválida");
    }

    private boolean starts(String kw) {
        skip();
        return src.startsWith(kw, pos);
    }

    private void accept(String kw) {
        skip();
        if (!src.startsWith(kw, pos)) error("Se esperaba '" + kw + "'");
        pos += kw.length();
        col += kw.length();
    }
}
