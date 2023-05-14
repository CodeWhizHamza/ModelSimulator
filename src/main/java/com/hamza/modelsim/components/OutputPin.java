package com.hamza.modelsim.components;

import com.hamza.modelsim.abstractcomponents.Pin;
import com.hamza.modelsim.constants.Colors;
import com.hamza.modelsim.constants.State;
import com.hamza.modelsim.constants.TerminalConstants;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class OutputPin extends Pin {
    private String name;
    private final SimpleObjectProperty<State> state = new SimpleObjectProperty<>();
    private final Text labelForName;

    public OutputPin(double y) {
        super(y);
        name = TerminalConstants.name;

        getPane().setLayoutX(TerminalConstants.rightX);
        getPane().setPrefWidth(100);
        state.set(State.LOW);

        getConnectionPoint().setX(getPane().layoutXProperty().get() + TerminalConstants.connectorRadius);
        getConnectionPoint().setY(getPane().layoutYProperty().get() + 20 + TerminalConstants.connectorRadius);

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
        getButton().setFill(state.get() == State.LOW ? Colors.terminalGreyColor : Colors.terminalActiveColor);

        // Connector
        getConnector().setCenterX(getPane().getPrefWidth() - 82 - TerminalConstants.connectorRadius);
        getConnector().setCenterY(20 + TerminalConstants.connectorRadius);
        getConnector().setRadius(TerminalConstants.connectorRadius);
        getConnector().setFill(Colors.terminalGreyColor);


        labelForName = new Text(name);
        labelForName.setFill(Colors.white);

        FlowPane nameBox = new FlowPane();
        nameBox.setPrefWidth(labelForName.getBoundsInLocal().getWidth() + 2 * 4);
        nameBox.getChildren().add(labelForName);
        nameBox.setAlignment(Pos.CENTER);
        nameBox.setColumnHalignment(HPos.CENTER);
        nameBox.setBackground(Background.fill(Color.rgb(33, 33, 33)));
        nameBox.setLayoutX(40);
        nameBox.setLayoutY(22);

        getPane().getChildren().add(getBase());
        getPane().getChildren().add(getExtender());
        getPane().getChildren().add(getButton());
        getPane().getChildren().add(getConnector());
        getPane().getChildren().add(nameBox);

        observeChangesInY();
    }

    @Override
    protected void observeChangesInY() {
        getPane().layoutYProperty().addListener((observableValue, number, t1) -> {
            getConnectionPoint().setX(getPane().layoutXProperty().get() + TerminalConstants.connectorRadius);
            getConnectionPoint().setY(getPane().layoutYProperty().get() + 20 + TerminalConstants.connectorRadius);
        });
    }

    public void draw(Pane canvas) {
        canvas.getChildren().add(getPane());
    }

    public void setState(State value) {
        state.set(value);
        getButton().setFill(state.get() == State.LOW ? Colors.terminalGreyColor : Colors.terminalActiveColor);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        labelForName.setText(name);
    }

    public State getState() {
        return state.get();
    }

    public SimpleObjectProperty<State> stateProperty() {
        return state;
    }
}
