package com.hamza.modelsim.abstractcomponents;

public class Input implements IODatable {

    private int value;

    public Input(int value) {
        setValue(value);
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public void setValue(int value) {
        if (value != 0 && value != 1) throw new IllegalArgumentException("Input can only be 0 or 1.");
        this.value = value;
    }
}
