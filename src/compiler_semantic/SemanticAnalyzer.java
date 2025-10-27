package compiler_semantic;

import compiler_lexer.Token;
import compiler_lexer.TokenType;
import java.util.HashMap;
import java.util.Map;

/**
 * Analizador semántico básico:
 * - Controla declaraciones duplicadas
 * - Controla uso de variables no declaradas
 * - Controla asignaciones con tipo incorrecto
 */
public class SemanticAnalyzer {

    private final Map<String, String> symbolTable = new HashMap<>();
    private final String src;

    public SemanticAnalyzer(String src) { this.src = src; }

    public void analyze() throws SemanticError {
        System.out.println("Análisis semántico realizado");

        String[] lines = "\\r?\\n".split(src);
        int lineNum = 0;

        for (String line : lines) {
            lineNum++;
            line = line.trim();

            if (line.isEmpty() || line.startsWith("//") || line.startsWith("/*")) continue;

            // --- Declaraciones ---
            if (line.startsWith("int ") || line.startsWith("boolean ") ||
                    line.startsWith("long ") || line.startsWith("double ")) {

                String type = "\\s+".split(line)[0];
                String decl = line.substring(type.length()).trim();

                if (decl.endsWith(";")) decl = decl.substring(0, decl.length() - 1);

                String[] vars = decl.split(",");

                for (String v : vars) {
                    String varName = v.trim();

                    if (varName.contains("=")) {
                        varName = varName.split("=")[0].trim();
                    }

                    Token idToken = new Token(TokenType.ID, varName, lineNum, 1);
                    declareVariable(idToken, type);
                }
                continue;
            }

            // Asignación
            if (line.contains("=") && !line.contains("==")) {
                String left = line.split("=")[0].trim();
                left = left.replace("+", "").replace("-", "")
                        .replace("*", "").replace("/", "")
                        .replace(";", "").trim();

                Token idToken = new Token(TokenType.ID, left, lineNum, 1);
                assignVariable(idToken, inferType(line));
                continue;
            }

            // read/write
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

    public void declareVariable(Token idToken, String type) {
        String name = idToken.getLexeme();

        if (isReservedWord(name)) {
            try {
                error("No se puede usar palabra reservada como identificador: " + name, idToken);
            } catch (SemanticError e) {
                throw new RuntimeException(e);
            }
            return;
        }

        if (symbolTable.containsKey(name)) {
            try {
                error("Variable ya declarada: " + name, idToken);
            } catch (SemanticError e) {
                throw new RuntimeException(e);
            }
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
            if (r.equals(word)) return true;
        }
        return false;
    }

    public void useVariable(Token idToken) {
        String name = idToken.getLexeme();
        if (!symbolTable.containsKey(name)) {
            try {
                error("Variable no declarada: " + name, idToken);
            } catch (SemanticError e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void assignVariable(Token idToken, String exprType) {
        String name = idToken.getLexeme();
        if (!symbolTable.containsKey(name)) {
            try {
                error("Variable no declarada antes de asignar: " + name, idToken);
            } catch (SemanticError e) {
                throw new RuntimeException(e);
            }
            return;
        }
        String varType = symbolTable.get(name);
        if (!varType.equals(exprType)) {
            try {
                error("Tipos incompatibles: variable '" + name + "' es " + varType +
                        " pero se intenta asignar " + exprType, idToken);
            } catch (SemanticError e) {
                throw new RuntimeException(e);
            }
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
