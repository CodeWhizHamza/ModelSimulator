package com.hamza.modelsim.components;

import com.hamza.modelsim.abstractcomponents.Pin;
import com.hamza.modelsim.constants.Colors;
import com.hamza.modelsim.constants.State;
import com.hamza.modelsim.constants.TerminalConstants;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class InputPin extends Pin {
    private static int terminalsCount = 0;
    private String name;
    private int state;

    public InputPin(double y) {
        super(y);
        name = TerminalConstants.name + " " + terminalsCount++;

        getPane().setLayoutX(TerminalConstants.leftX);
        state = State.LOW;

        getConnectionPoint().setX(getPane().layoutXProperty().get() + 85 + TerminalConstants.connectorRadius);
        getConnectionPoint().setY(getPane().layoutYProperty().get() + 22.5 + TerminalConstants.connectorRadius);

        getBase().setLayoutX(2.5);
        getBase().setLayoutY(0);
        getBase().setHeight(getPane().getPrefHeight());
        getBase().setWidth(TerminalConstants.baseWidth);
        getBase().setFill(Colors.terminalBaseColor);

        getExtender().setWidth(TerminalConstants.extenderWidth);
        getExtender().setHeight(TerminalConstants.extenderHeight);
        getExtender().setLayoutX(74);
        getExtender().setLayoutY(26);
        getExtender().setFill(Colors.terminalGreyColor);

        getButton().setCenterX(17 + TerminalConstants.buttonRadius);
        getButton().setCenterY(TerminalConstants.buttonRadius);
        getButton().setRadius(TerminalConstants.buttonRadius);
        getButton().setFill(getValue() == 0 ? Colors.terminalGreyColor : Colors.terminalActiveColor);

        // Connector
        getConnector().setCenterX(85 + TerminalConstants.connectorRadius);
        getConnector().setCenterY(22.5 + TerminalConstants.connectorRadius);
        getConnector().setRadius(TerminalConstants.connectorRadius);
        getConnector().setFill(getValue() == 0 ? Colors.terminalGreyColor : Colors.terminalActiveColor);

        Text labelForName = new Text(name);
        labelForName.setFill(Colors.white);
        labelForName.setLayoutX(110);
        labelForName.setLayoutY(35);

        // behaviour
        setTerminalToggler();

        // TODO: display popup which consists a delete button and input field for name.
        getBase().setOnMouseClicked(e -> {
            System.out.println("Clicked");
        });

        getPane().getChildren().add(getBase());
        getPane().getChildren().add(getExtender());
        getPane().getChildren().add(getButton());
        getPane().getChildren().add(getConnector());
        getPane().getChildren().add(labelForName);
    }

    private void setTerminalToggler() {
        getButton().setOnMouseClicked(e -> {
            if(e.getButton().compareTo(MouseButton.PRIMARY) != 0) return ;
            if (getValue() == 1) setValue(0);
            else setValue(1);
        });
    }


    public void draw(Pane canvas) {
        canvas.getChildren().add(getPane());
    }
    public int getValue() {
        return state;
    }

    public void setValue(int value) {
        state = value;
        getButton().setFill(getValue() == State.LOW ? Colors.terminalGreyColor : Colors.terminalActiveColor);
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
