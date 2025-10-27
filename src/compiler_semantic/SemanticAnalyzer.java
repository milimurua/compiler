package compiler_semantic;

import compiler_lexer.Token;
import compiler_lexer.TokenType;
import java.util.HashMap;
import java.util.Map;

public class SemanticAnalyzer {

    private final Map<String, String> symbolTable = new HashMap<>();
    private final String src;

    public SemanticAnalyzer(String src) {
        this.src = src;
    }

    public void analyze() throws SemanticError {
        System.out.println("Análisis semántico iniciado");

        // Separar líneas (sin expresiones regulares)
        String[] lines = src.replace("\r", "").split("\n");
        int lineNum = 0;

        for (String line : lines) {
            lineNum++;
            line = line.trim();

            if (line.isEmpty() || line.startsWith("//") || line.startsWith("/*")) continue;

            // Declaración de variables
            if (line.startsWith("int ") || line.startsWith("boolean ") ||
                    line.startsWith("long ") || line.startsWith("double ")) {

                int firstSpace = line.indexOf(' ');
                String type = (firstSpace != -1) ? line.substring(0, firstSpace) : line;
                String decl = line.substring(type.length()).trim();

                if (decl.endsWith(";")) decl = decl.substring(0, decl.length() - 1);

                String[] vars = decl.split(","); // literal, no regex

                for (String part : vars) {
                    String varName = part.trim();
                    if (varName.contains("=")) varName = varName.split("=")[0].trim();
                    Token token = new Token(TokenType.ID, varName, lineNum, 1);
                    declareVariable(token, type);
                }
                continue;
            }

            // Asignaciones simples
            if (line.contains("=") && !line.contains("==")) {
                String left = line.substring(0, line.indexOf('=')).trim();
                left = left.replace("+", "").replace("-", "")
                        .replace("*", "").replace("/", "")
                        .replace(";", "").trim();

                Token idToken = new Token(TokenType.ID, left, lineNum, 1);
                assignVariable(idToken, inferType(line));
                continue;
            }

            // Lectura o escritura
            if (line.startsWith("read(") || line.startsWith("write(")) {
                int start = line.indexOf('(') + 1;
                int end = line.indexOf(')');
                if (start > 0 && end > start) {
                    String var = line.substring(start, end).trim();
                    Token idToken = new Token(TokenType.ID, var, lineNum, 1);
                    useVariable(idToken);
                }
            }
        }

        System.out.println("Análisis semántico completado sin errores");
    }

    public void declareVariable(Token idToken, String type) throws SemanticError {
        String name = idToken.getLexeme().trim().replace(";", "");

        if (isReservedWord(name)) {
            error("No se puede usar palabra reservada como identificador: " + name, idToken);
            return;
        }

        if (symbolTable.containsKey(name)) {
            error("Variable ya declarada: " + name, idToken);
        } else {
            symbolTable.put(name, type);
        }
    }


    private boolean isReservedWord(String word) {
        String[] reserved = {
                "if", "then", "else", "while", "read", "write",
                "int", "boolean", "long", "double", "true", "false"
        };
        for (String r : reserved) {
            if (r.equalsIgnoreCase(word)) return true;
        }
        return false;
    }

    public void useVariable(Token idToken) throws SemanticError {
        String name = idToken.getLexeme().trim().replace(";", "");
        if (!symbolTable.containsKey(name)) {
            error("Variable no declarada: " + name, idToken);
        }
    }

    public void assignVariable(Token idToken, String exprType) throws SemanticError {
        String name = idToken.getLexeme().trim().replace(";", "");
        if (!symbolTable.containsKey(name)) {
            error("Variable no declarada antes de asignar: " + name, idToken);
            return;
        }
        String varType = symbolTable.get(name);
        if (!varType.equals(exprType)) {
            error("Tipos incompatibles: variable '" + name + "' es " + varType +
                    " pero se intenta asignar " + exprType, idToken);
        }
    }


    private String inferType(String line) {
        if (line.contains("\"")) return "string";
        if (line.contains("true") || line.contains("false")) return "boolean";
        if (line.contains(".")) return "double";
        return "int";
    }

    private void error(String msg, Token token) throws SemanticError {
        throw new SemanticError("Error semántico en línea " + token.getLine() +
                ", columna " + token.getColumn() + ": " + msg);
    }
}
