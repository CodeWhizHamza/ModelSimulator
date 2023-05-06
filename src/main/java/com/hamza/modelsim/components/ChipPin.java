package com.hamza.modelsim.components;

import com.hamza.modelsim.abstractcomponents.Point;
import com.hamza.modelsim.constants.ChipConstants;
import com.hamza.modelsim.constants.Colors;
import com.hamza.modelsim.constants.State;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.shape.Circle;

public abstract class ChipPin {
    private final SimpleObjectProperty<State> state = new SimpleObjectProperty<>();
    private Point connectionPoint;
    private final Circle connector;
    private Chip chip;

    public ChipPin(State state, Circle connector, Chip chip) {
        this.state.set(state);
        this.connector = connector;
        this.chip = chip;
    }

    public Circle getConnector() {
        return connector;
    }

    public SimpleObjectProperty<State> getState() {
        return state;
    }
    public void setState(State state) {
        this.state.set(state);
    }
    public Point getConnectionPoint() {
        return connectionPoint;
    }
    public void setConnectionPoint(Point connectionPoint) {
        this.connectionPoint = connectionPoint;
    }
    public Chip getParent() {
        return chip;
    }
}
