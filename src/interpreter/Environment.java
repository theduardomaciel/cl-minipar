package interpreter;

import java.util.HashMap;
import java.util.Map;

/**
 * Ambiente de execução com escopos encadeados.
 */
public class Environment {
    private final Environment enclosing; // pode ser null para escopo global
    private final Map<String, Object> values = new HashMap<>();

    public Environment() {
        this.enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    /** Define uma variável neste escopo. */
    public synchronized void define(String name, Object value) {
        values.put(name, value);
    }

    /** Obtém o valor de uma variável procurando no escopo atual e ancestrais. */
    public synchronized Object get(String name) {
        if (values.containsKey(name)) return values.get(name);
        if (enclosing != null) return enclosing.get(name);
        throw new RuntimeException("Variável não definida: " + name);
    }

    /** Atribui valor a uma variável existente (procura no encadeamento). */
    public synchronized void assign(String name, Object value) {
        if (values.containsKey(name)) {
            values.put(name, value);
            return;
        }
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
        throw new RuntimeException("Variável não definida: " + name);
    }

    /** Retorna o ambiente pai. */
    public Environment enclosing() {
        return enclosing;
    }

    /** Retorna o mapa de valores (para uso interno). */
    public Map<String, Object> getValues() {
        return values;
    }

    /** Cria uma cópia rasa do ambiente (somente valores no escopo atual). */
    public Environment shallowCopy() {
        Environment e = new Environment(enclosing);
        e.values.putAll(this.values);
        return e;
    }
}
