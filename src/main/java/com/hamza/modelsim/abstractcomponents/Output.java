package com.hamza.modelsim.abstractcomponents;

public class Output implements IODatable {

    private int value;

    public Output(int value) {
        setValue(value);
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public void setValue(int value) {
        if (value != 0 && value != 1) throw new IllegalArgumentException("Output can only be 0 or 1.");
        this.value = value;
    }
}
