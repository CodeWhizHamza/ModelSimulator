package com.hamza.modelsim.components;

import com.hamza.modelsim.abstractcomponents.IODatable;
import com.hamza.modelsim.abstractcomponents.Input;
import com.hamza.modelsim.abstractcomponents.Point;
import com.hamza.modelsim.constants.Colors;
import com.hamza.modelsim.constants.LayoutConstants;
import com.hamza.modelsim.constants.TerminalConstants;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Screen;

public class OutputPin extends Pin {
    private static int terminalsCount = 0;
    private String name;
    private IODatable state;

    public OutputPin(double y) {
        super(y);
        name = TerminalConstants.name + " " + terminalsCount++;

        getPane().setLayoutX(TerminalConstants.rightX);
        getPane().setPrefWidth(100);
        state = new Input(0);

        getConnectionPoint().setX(getPane().layoutXProperty().get() + 85 + TerminalConstants.connectorRadius);
        getConnectionPoint().setY(getPane().layoutYProperty().get() + 22.5 + TerminalConstants.connectorRadius);

        getBase().setHeight(getPane().getPrefHeight());
        getBase().setWidth(TerminalConstants.baseWidth);
        getBase().setLayoutX(getPane().getPrefWidth() - getBase().getWidth() - 2.5);
        getBase().setLayoutY(0);
        getBase().setFill(Colors.terminalBaseColor);

        getExtender().setWidth(TerminalConstants.extenderWidth);
        getExtender().setHeight(TerminalConstants.extenderHeight);
        getExtender().setLayoutX(13);
        getExtender().setLayoutY(26);
        getExtender().setFill(Colors.terminalGreyColor);

        getButton().setCenterX(getPane().getPrefWidth() - 17 - TerminalConstants.buttonRadius);
        getButton().setCenterY(TerminalConstants.buttonRadius);
        getButton().setRadius(TerminalConstants.buttonRadius);
        getButton().setFill(getValue() == 0 ? Colors.terminalGreyColor : Colors.terminalActiveColor);

        // Connector
        getConnector().setCenterX(getPane().getPrefWidth() - 82 - TerminalConstants.connectorRadius);
        getConnector().setCenterY(22.5 + TerminalConstants.connectorRadius);
        getConnector().setRadius(TerminalConstants.connectorRadius);
        getConnector().setFill(getValue() == 0 ? Colors.terminalGreyColor : Colors.terminalActiveColor);

        // TODO: display popup which consists a delete button and input field for name.
        getBase().setOnMouseClicked(e -> {
            System.out.println("Clicked");
        });

        getPane().getChildren().add(getBase());
        getPane().getChildren().add(getExtender());
        getPane().getChildren().add(getButton());
        getPane().getChildren().add(getConnector());
    }

    public void draw(Pane canvas) {
        canvas.getChildren().add(getPane());
    }
    public int getValue() {
        return state.getValue();
    }

    public void setValue(int value) {
        state.setValue(value);
        getButton().setFill(getValue() == 0 ? Colors.terminalGreyColor : Colors.terminalActiveColor);
    }
    public String getName() {
        return name;
    }
}
