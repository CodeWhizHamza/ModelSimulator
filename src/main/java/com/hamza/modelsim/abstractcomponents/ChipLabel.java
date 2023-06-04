package com.hamza.modelsim.abstractcomponents;

public class ChipLabel {
    private final String name;
    private final String[] functions;

    public ChipLabel(String name, String function) {
        this(name, function.equals("") ? new String[]{} : new String[]{function});
    }

    public ChipLabel(String name, String[] functions) {
        this.name = name;
        this.functions = functions;
    }

    public String getName() {
        return name;
    }

    public String[] getFunctions() {
        return functions;
    }
}
