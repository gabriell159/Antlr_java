package com.uepb.compiler;

import java.util.LinkedList;
import java.util.Optional;

public class ScopeControl {
    private final LinkedList<SymbolTable> stack;

    public ScopeControl() {
        stack = new LinkedList<>();
        createScope();
    }

    public final void createScope(){
        stack.push(new SymbolTable());
    }
    public final void dropScope(){
        stack.pop();
    }
    public final SymbolTable getCurrentScope(){
        return stack.peek();
    }
    public Variables declare(String name, int address){
    var current = getCurrentScope();

    if (current.exists(name)) {
        throw new RuntimeException("Variável já declarada: " + name);
    }

    var variable = new Variables(name, address);
    current.add(name, variable);

    return variable;
    }

    public Optional<Variables> lookup(String name){
        return stack.stream()
                .filter(table -> table.exists(name))
                .findFirst()
                .map(table -> table.get(name));
    }
}
