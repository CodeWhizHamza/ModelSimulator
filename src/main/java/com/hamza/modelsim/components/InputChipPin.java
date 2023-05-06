package com.hamza.modelsim.components;

import com.hamza.modelsim.constants.State;
import javafx.scene.shape.Circle;

public class InputChipPin extends ChipPin {
    public InputChipPin(State state, Circle connector, Chip chip) {
        super(state, connector, chip);
    }
}
