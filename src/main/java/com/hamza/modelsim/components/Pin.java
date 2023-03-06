package com.hamza.modelsim.components;

import com.hamza.modelsim.abstractcomponents.Point;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;

public interface Pin {
    int getValue();
    void setValue(int value);
    Pane getDrawable();
    void setName(String name);
    String getName();
    void setY(double y);
    Circle getConnector();
    Point getConnectionPoint();
}
