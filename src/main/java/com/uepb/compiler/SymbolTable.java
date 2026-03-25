package com.uepb.compiler;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, Variables> table = new HashMap<>();

    public void add(String name, Variables var) {
        table.put(name, var);
    }

    public boolean exists(String name) {
        return table.containsKey(name);
    }

    public Variables get(String name) {
        return table.get(name);
    }
}
