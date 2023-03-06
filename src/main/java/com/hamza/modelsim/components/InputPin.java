package com.hamza.modelsim.components;

import com.hamza.modelsim.abstractcomponents.IODatable;
import com.hamza.modelsim.abstractcomponents.Input;
import com.hamza.modelsim.abstractcomponents.Point;
import com.hamza.modelsim.constants.Colors;
import com.hamza.modelsim.constants.LayoutConstants;
import com.hamza.modelsim.constants.TerminalConstants;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Screen;

public class InputPin implements Pin {
    private static int terminalsCount = 0;
    private final Pane pane;
    private String name;
    private IODatable state;
    private Point connectionPoint;
    private Circle connector;

    public InputPin(double y) {
        name = TerminalConstants.name + " " + terminalsCount++;

        pane = new Pane();
        pane.setPrefHeight(TerminalConstants.height);
        pane.setLayoutX(TerminalConstants.leftX);
        setY(y);

        state = new Input(0);

        connectionPoint = new Point();
        connectionPoint.setX(pane.layoutXProperty().get() + 85 + TerminalConstants.connectorRadius);
        connectionPoint.setY(pane.layoutYProperty().get() + 22.5 + TerminalConstants.connectorRadius);

        Rectangle base = new Rectangle();
        base.setLayoutX(2.5);
        base.setLayoutY(0);
        base.setHeight(pane.getPrefHeight());
        base.setWidth(TerminalConstants.baseWidth);
        base.setFill(Colors.terminalBaseColor);

        Rectangle extender = new Rectangle();
        extender.setWidth(TerminalConstants.extenderWidth);
        extender.setHeight(TerminalConstants.extenderHeight);
        extender.setLayoutX(74);
        extender.setLayoutY(26);
        extender.setFill(Colors.terminalGreyColor);

        Circle button = new Circle();
        button.setCenterX(17 + TerminalConstants.buttonRadius);
        button.setCenterY(TerminalConstants.buttonRadius);
        button.setRadius(TerminalConstants.buttonRadius);
        button.setFill(getValue() == 0 ? Colors.terminalGreyColor : Colors.terminalActiveColor);

        // Connector
        connector = new Circle();
        connector.setCenterX(85 + TerminalConstants.connectorRadius);
        connector.setCenterY(22.5 + TerminalConstants.connectorRadius);
        connector.setRadius(TerminalConstants.connectorRadius);
        connector.setFill(getValue() == 0 ? Colors.terminalGreyColor : Colors.terminalActiveColor);

        Text labelForName = new Text(name);
        labelForName.setFill(Colors.white);
        labelForName.setLayoutX(110);
        labelForName.setLayoutY(35);

        // behaviour
        observeChangesInY();
        addHoverEffectToBase(base);
        addHoverEffectToButton(button);
        addHoverEffectToConnector();
        toggleTerminalState(button);
        makeTerminalDraggable(base);

        // TODO: display popup which consists a delete button and input field for name.
        base.setOnMouseClicked(e -> {
            System.out.println("Clicked");
        });

        pane.getChildren().add(base);
        pane.getChildren().add(extender);
        pane.getChildren().add(button);
        pane.getChildren().add(connector);
        pane.getChildren().add(labelForName);

        // loadImages();
    }

    private void observeChangesInY() {
        pane.layoutYProperty().addListener((observableValue, number, t1) -> {
            connectionPoint.setX(pane.layoutXProperty().get() + 85 + TerminalConstants.connectorRadius);
            connectionPoint.setY(pane.layoutYProperty().get() + t1.doubleValue() + 22.5 + TerminalConstants.connectorRadius);
        });
    }
    private static void addHoverEffectToBase(Rectangle base) {
        base.setOnMouseEntered(e -> {
            base.setFill(Colors.warmWhite);
            base.getScene().setCursor(Cursor.HAND);
        });
        base.setOnMouseExited(e -> {
            base.setFill(Colors.terminalBaseColor);
            base.getScene().setCursor(Cursor.DEFAULT);
        });
    }
    private static void addHoverEffectToButton(Circle button) {
        button.setOnMouseEntered(e -> {
            button.getScene().setCursor(Cursor.HAND);
        });
        button.setOnMouseExited(e -> {
            button.getScene().setCursor(Cursor.DEFAULT);
        });
    }
    private void addHoverEffectToConnector() {
        connector.setOnMouseEntered(e -> {
            connector.getScene().setCursor(Cursor.HAND);
        });
        connector.setOnMouseExited(e -> {
            connector.getScene().setCursor(Cursor.DEFAULT);
        });
    }
    private void toggleTerminalState(Circle button) {
        button.setOnMouseClicked(e -> {
            if(e.getButton().compareTo(MouseButton.PRIMARY) != 0) return ;

            if (getValue() == 1) setValue(0);
            else setValue(1);

            button.setFill(getValue() == 0 ? Colors.terminalGreyColor : Colors.terminalActiveColor);
        });
    }
    private void makeTerminalDraggable(Rectangle base) {
        double maxYValue = Screen.getPrimary().getBounds().getHeight() - LayoutConstants.menuHeight - 12 - pane.getPrefHeight() / 2;
        base.setOnMouseDragged(e -> {
            double mouseY = e.getSceneY();
            if (mouseY > maxYValue || mouseY < 16 + pane.getPrefHeight() / 2) return;

            pane.setLayoutY(mouseY - pane.getPrefHeight() / 2);

        });
    }

    public void draw(Pane canvas) {
        canvas.getChildren().add(pane);
    }

    @Override
    public int getValue() {
        return state.getValue();
    }

    @Override
    public void setValue(int value) {
        state.setValue(value);
    }

    @Override
    public Pane getDrawable() {
        return pane;
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setY(double y) {
        pane.setLayoutY(y);
    }

    @Override
    public Circle getConnector() {
        return connector;
    }

    public Point getConnectionPoint() {
        return connectionPoint;
    }
}
