package compile_semantic.src;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase auxiliar para manejar la tabla de símbolos.
 * Guarda el nombre de las variables y su tipo.
 */
public class SymbolTable {
    private final Map<String, String> table = new HashMap<>();

    public boolean declare(String name, String type) {
        if (table.containsKey(name)) return false;
        table.put(name, type);
        return true;
    }

    public boolean isDeclared(String name) {
        return table.containsKey(name);
    }

    public String getType(String name) {
        return table.get(name);
    }
}
