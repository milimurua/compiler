package compile_sintactic.src;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class SyntacticAnalyzer {

    private final String src;
    private int pos = 0;
    private int line = 1;
    private int col = 1;

    public SyntacticAnalyzer(String src) {
        this.src = src;
    }


    private boolean isEOF() { return pos >= src.length(); }
    private char current() { return isEOF() ? (char)-1 : src.charAt(pos); }
    private char peek(int a) { int p = pos + a; return p >= src.length() ? (char)-1 : src.charAt(p); }
    private void advance() {
        if (isEOF()) return;
        if (src.charAt(pos) == '\n') { line++; col = 1; }
        else col++;
        pos++;
    }

    private void skipSpaces() {
        while (!isEOF()) {
            char c = current();
            if (Character.isWhitespace(c)) { advance(); continue; }
            if (c == '/' && peek(1) == '/') { // comentario linea
                while (!isEOF() && current() != '\n') advance();
                continue;
            }
            if (c == '/' && peek(1) == '*') { // comentario bloque
                advance(); advance();
                while (!isEOF() && !(current() == '*' && peek(1) == '/')) advance();
                if (!isEOF()) { advance(); advance(); }
                continue;
            }
            break;
        }
    }

    private void error(String msg) {
        throw new SyntacticError(msg + " en linea " + line + ", columna " + col);
    }

    private boolean startsWithKeyword(String kw) {
        skipSpaces();
        int p = pos;
        for (int i = 0; i < kw.length(); i++) {
            if (current() != kw.charAt(i)) { pos = p; return false; }
            advance();
        }
        boolean ok = !(Character.isLetterOrDigit(current()) || current() == '_');
        pos = p;
        return ok;
    }

    private boolean acceptKeyword(String kw) {
        skipSpaces();
        int p = pos;
        for (int i = 0; i < kw.length(); i++) {
            if (current() != kw.charAt(i)) { pos = p; return false; }
            advance();
        }
        if (Character.isLetterOrDigit(current()) || current() == '_') { pos = p; return false; }
        return true;
    }

    private String readIdentifier() {
        skipSpaces();
        if (!Character.isLetter(current()) && current() != '_') error("Se esperaba identificador");
        StringBuilder b = new StringBuilder();
        while (!isEOF() && (Character.isLetterOrDigit(current()) || current() == '_')) {
            b.append(current()); advance();
        }
        return b.toString();
    }

    private void expectChar(char c) {
        skipSpaces();
        if (current() != c) error("Se esperaba '" + c + "' pero se encontro '" + current() + "'");
        advance();
    }

    private void readIntegerLiteral() {
        skipSpaces();
        if (!Character.isDigit(current())) error("Se esperaba literal entero");
        while (!isEOF() && Character.isDigit(current())) advance();
    }

    /* ---------------- gramática ---------------- */

    public void parseProgram() {
        skipSpaces();
        boolean any = false;
        while (!isEOF()) {
            skipSpaces();
            if (startsWithKeyword("class")) {
                parseClassDecl();
                any = true;
            } else {
                if (!isEOF()) error("Se esperaba 'class' al inicio de una declaracion de clase");
            }
            skipSpaces();
        }
        if (!any) error("Archivo sin clases");
    }

    private void parseClassDecl() {
        if (!acceptKeyword("class")) error("Se esperaba 'class'");
        readIdentifier(); // nombre de clase
        skipSpaces();
        expectChar('{');
        skipSpaces();
        while (!isEOF() && current() != '}') {
            parseMemberDecl();
            skipSpaces();
        }
        expectChar('}');
    }

    private void parseMemberDecl() {
        skipSpaces();
        // acepto modificadores simples
        while (true) {
            if (acceptKeyword("public")) continue;
            if (acceptKeyword("private")) continue;
            if (acceptKeyword("static")) continue;
            break;
        }

        skipSpaces();
        boolean isVoid = false;
        String type = null;
        if (startsWithKeyword("void")) { acceptKeyword("void"); isVoid = true; type = "void"; }
        else if (startsWithKeyword("int")) { type = "int"; acceptKeyword("int"); }
        else if (startsWithKeyword("boolean")) { type = "boolean"; acceptKeyword("boolean"); }
        else if (Character.isLetter(current()) || current() == '_') {
            type = readIdentifier(); // tipo personalizado (ej: MyType)
        } else {
            error("Tipo o 'void' esperado en miembro de clase");
        }

        skipSpaces();
        String name = readIdentifier();
        skipSpaces();
        if (current() == '(') {
            parseMethodRest();
        } else {
            parseFieldRest();
        }
    }

    private void parseFieldRest() {
        skipSpaces();
        if (current() == '=') {
            advance();
            parseExpression();
        }
        skipSpaces();
        expectChar(';');
    }

    private void parseMethodRest() {
        expectChar('(');
        skipSpaces();
        if (current() != ')') {
            parseParamList();
        }
        expectChar(')');
        skipSpaces();
        parseBlock();
    }

    private void parseParamList() {
        skipSpaces();
        // param: type id
        parseParam();
        skipSpaces();
        while (current() == ',') {
            advance();
            parseParam();
            skipSpaces();
        }
    }

    private void parseParam() {
        // tipo simple
        skipSpaces();
        if (startsWithKeyword("int")) { acceptKeyword("int"); }
        else if (startsWithKeyword("boolean")) { acceptKeyword("boolean"); }
        else readIdentifier();
        skipSpaces();
        readIdentifier(); // nombre del parametro
    }

    private void parseBlock() {
        skipSpaces();
        expectChar('{');
        skipSpaces();
        while (!isEOF() && current() != '}') {
            parseStatement();
            skipSpaces();
        }
        expectChar('}');
    }

    private void parseStatement() {
        skipSpaces();
        if (current() == '{') { parseBlock(); return; }
        if (startsWithKeyword("if")) { parseIf(); return; }
        if (startsWithKeyword("while")) { parseWhile(); return; }
        if (startsWithKeyword("return")) {
            acceptKeyword("return");
            skipSpaces();
            if (current() != ';') parseExpression();
            expectChar(';');
            return;
        }
        // var decl
        if (startsWithKeyword("int") || startsWithKeyword("boolean")) {
            parseVarDecl();
            expectChar(';');
            return;
        }
        // empty statement
        if (current() == ';') { advance(); return; }
        // expression statement
        parseExpression();
        expectChar(';');
    }

    private void parseVarDecl() {
        if (acceptKeyword("int")) { /* ok */ }
        else if (acceptKeyword("boolean")) { /* ok */ }
        else error("Tipo en declaracion de variable");
        skipSpaces();
        readIdentifier();
        skipSpaces();
        if (current() == '=') {
            advance();
            parseExpression();
        }
    }

    private void parseIf() {
        acceptKeyword("if");
        skipSpaces();
        expectChar('(');
        parseExpression();
        expectChar(')');
        skipSpaces();
        parseStatement();
        skipSpaces();
        if (startsWithKeyword("else")) {
            acceptKeyword("else");
            skipSpaces();
            parseStatement();
        }
    }

    private void parseWhile() {
        acceptKeyword("while");
        skipSpaces();
        expectChar('(');
        parseExpression();
        expectChar(')');
        skipSpaces();
        parseStatement();
    }

    /* ---------------- expresiones (simplificadas) ---------------- */

    private void parseExpression() {
        parseAssignment();
    }

    private void parseAssignment() {
        parseEquality();
        skipSpaces();
        if (current() == '=') {
            advance();
            parseAssignment();
        }
    }

    private void parseEquality() {
        parseRelational();
        skipSpaces();
        while ((current() == '=' && peek(1) == '=') || (current() == '!' && peek(1) == '=')) {
            advance(); advance();
            parseRelational();
            skipSpaces();
        }
    }

    private void parseRelational() {
        parseAdditive();
        skipSpaces();
        while (current() == '<' || current() == '>') {
            advance();
            if (current() == '=') advance();
            parseAdditive();
            skipSpaces();
        }
    }

    private void parseAdditive() {
        parseMultiplicative();
        skipSpaces();
        while (current() == '+' || current() == '-') {
            advance();
            parseMultiplicative();
            skipSpaces();
        }
    }

    private void parseMultiplicative() {
        parseUnary();
        skipSpaces();
        while (current() == '*' || current() == '/') {
            advance();
            parseUnary();
            skipSpaces();
        }
    }

    private void parseUnary() {
        skipSpaces();
        if (current() == '!' || current() == '-') {
            advance();
            parseUnary();
        } else {
            parsePrimary();
        }
    }

    private void parsePrimary() {
        skipSpaces();
        char c = current();
        if (c == '(') {
            advance();
            parseExpression();
            expectChar(')');
            return;
        }
        if (Character.isDigit(c)) { readIntegerLiteral(); return; }
        if (startsWithKeyword("true")) { acceptKeyword("true"); return; }
        if (startsWithKeyword("false")) { acceptKeyword("false"); return; }
        if (Character.isLetter(c) || c == '_') {
            // identificador o llamada a metodo
            readIdentifier();
            skipSpaces();
            if (current() == '(') {
                advance();
                skipSpaces();
                if (current() != ')') {
                    parseArgList();
                }
                expectChar(')');
            }
            return;
        }
        error("Expresion primaria invalida");
    }

    private void parseArgList() {
        parseExpression();
        skipSpaces();
        while (current() == ',') {
            advance();
            parseExpression();
            skipSpaces();
        }
    }

    /* ---------------- helper publico para uso externo ---------------- */

    public static void analyzeFile(String path) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(path)), "UTF-8");
            SyntacticAnalyzer p = new SyntacticAnalyzer(content);
            p.parseProgram();
            p.skipSpaces();
            if (!p.isEOF()) p.error("Contenido extra despues del programa");
            System.out.println("PARSE OK");
        } catch (SyntacticError e) {
            System.err.println("PARSE ERROR: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error leyendo archivo: " + e.getMessage());
        }
    }
}
