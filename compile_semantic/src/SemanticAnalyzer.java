package compile_semantic.src;

import compile_lexer.src.Token;
import compile_lexer.src.TokenType;
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

    // Declara una variable en la tabla de símbolos
    public void declareVariable(Token idToken, String type) {
        String name = idToken.lexeme;
        if (symbolTable.containsKey(name)) {
            error("Variable ya declarada: " + name, idToken);
        } else {
            symbolTable.put(name, type);
        }
    }

    // Verifica que una variable haya sido declarada antes de usarse
    public void useVariable(Token idToken) {
        String name = idToken.lexeme;
        if (!symbolTable.containsKey(name)) {
            error("Variable no declarada: " + name, idToken);
        }
    }

    // Verifica asignaciones y compatibilidad de tipos
    public void assignVariable(Token idToken, String exprType) {
        String name = idToken.lexeme;
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

    // Devuelve el tipo de una variable si está declarada
    public String getType(String name) {
        return symbolTable.get(name);
    }

    private void error(String msg, Token token) {
        System.err.println("Error semántico en línea " + token.line +
                           ", columna " + token.column + ": " + msg);
    }
}
