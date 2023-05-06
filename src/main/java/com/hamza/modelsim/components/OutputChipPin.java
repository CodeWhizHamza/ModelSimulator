package com.hamza.modelsim.components;

import com.hamza.modelsim.constants.State;
import javafx.scene.shape.Circle;

public class OutputChipPin extends ChipPin {
    public OutputChipPin(State state, Circle connector, Chip chip) {
        super(state, connector, chip);
    }
}
