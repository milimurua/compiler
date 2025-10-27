package compiler_semantic;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase auxiliar para manejar la tabla de símbolos.
 * Guarda el nombre de las variables y su tipo.
 */
public class SymbolTable {
    private final Map<String, String> table = new HashMap<>();

    public boolean contains(String name) {
        return table.containsKey(name);
    }

    public void put(String name, String type) {
        table.put(name, type);
    }

    public String get(String name) {
        return table.get(name);
    }

    public Map<String, String> getAll() {
        return table;
    }
}
