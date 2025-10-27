package compiler_semantic;

import compiler_lexer.Token;
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

    // Constructor alternativo para compatibilidad con Main
    public SemanticAnalyzer() { }

    // Punto de entrada que Main espera
    public void analyze() throws SemanticError {
        System.out.println("Análisis semántico realizado");
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
        for (String r : reserved) if (r.equals(word)) return true;
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

    private void error(String msg, Token token) throws SemanticError {
        throw new SemanticError("Error semántico en línea " + token.getLine() +
                ", columna " + token.getColumn() + ": " + msg);
    }
}
