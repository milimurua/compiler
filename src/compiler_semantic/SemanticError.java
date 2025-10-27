package compiler_semantic;

/**
 * Representa un error semántico detectado durante el análisis.
 */
public class SemanticError extends Exception {
    public SemanticError(String message) {
        super(message);
    }
}
