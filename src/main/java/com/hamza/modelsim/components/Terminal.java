package com.hamza.modelsim.components;

import com.hamza.modelsim.Main;
import com.hamza.modelsim.abstractcomponents.IODatable;
import com.hamza.modelsim.abstractcomponents.Output;
import com.hamza.modelsim.abstractcomponents.Point;
import com.hamza.modelsim.abstractcomponents.Wire;
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

public class Terminal {
    private static int terminalsCount = 0;
    private final Pane container;
    private String name;
    private final IODatable out;
    private final Point wireConnectionPoint;
    private final Circle connector;
    private boolean isConnectedClicked;

    public Terminal(double y, boolean isLeft, boolean shouldCount) {
        if(shouldCount)
            terminalsCount += 1;

        connector = new Circle();
        out = new Output(0);
        wireConnectionPoint = new Point();
        name = TerminalConstants.name + " " + terminalsCount;
        isConnectedClicked = false;

        container = new Pane();
        container.setPrefHeight(TerminalConstants.height);
        setY(y);

        if (isLeft) makeLeftTerminal();
        else makeRightTerminal();

        connector.setOnMouseClicked(e -> {
            Wire wire = new Wire();
            if (e.getButton().compareTo(MouseButton.PRIMARY) == 0) {
                if (!isConnectedClicked) {
                    wire.setStart(wireConnectionPoint);
                    Main.wires.add(wire);
                }
                isConnectedClicked = true;
            }
            else
                isConnectedClicked = false;




        });

    }

    private void makeLeftTerminal() {
        container.setLayoutX(TerminalConstants.leftX);
        wireConnectionPoint.setX(container.layoutXProperty().get() + 85 + TerminalConstants.connectorRadius);
        wireConnectionPoint.setY(container.layoutYProperty().get() + 22.5 + TerminalConstants.connectorRadius);

        Rectangle base = new Rectangle();
        base.setLayoutX(2.5);
        base.setLayoutY(0);
        base.setHeight(container.getPrefHeight());
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
        button.setFill(getOut() == 0 ? Colors.terminalGreyColor : Colors.terminalActiveColor);

        // Connector
        connector.setCenterX(85 + TerminalConstants.connectorRadius);
        connector.setCenterY(22.5 + TerminalConstants.connectorRadius);
        connector.setRadius(TerminalConstants.connectorRadius);
        connector.setFill(getOut() == 0 ? Colors.terminalGreyColor : Colors.terminalActiveColor);

        Text labelForName = new Text(name);
        labelForName.setFill(Colors.white);
        labelForName.setLayoutX(110);
        labelForName.setLayoutY(35);

        // Actions
        observeChangesInY();
        addHoverEffectToBase(base);
        addHoverEffectToButton(button);
        changeConnectorColorAndCursorOnHover(connector);
        toggleTerminalState(button, connector);
        makeTerminalDraggable(base);

        // TODO: display popup which consists a delete button and input field for name.
        base.setOnMouseClicked(e -> {
            System.out.println("Clicked");
        });

        container.getChildren().add(base);
        container.getChildren().add(extender);
        container.getChildren().add(button);
        container.getChildren().add(connector);
        container.getChildren().add(labelForName);
    }

    private void makeRightTerminal() {
        container.setScaleX(-1);
        container.setLayoutX(TerminalConstants.rightX);
        wireConnectionPoint.setX(container.layoutXProperty().get() + 85 + TerminalConstants.connectorRadius);
        wireConnectionPoint.setY(container.layoutYProperty().get() + 22.5 + TerminalConstants.connectorRadius);

        Rectangle base = new Rectangle();
        base.setLayoutX(2.5);
        base.setLayoutY(0);
        base.setHeight(container.getPrefHeight());
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
        button.setFill(getOut() == 0 ? Colors.terminalGreyColor : Colors.terminalActiveColor);

        // Connector
        Circle connector = new Circle();
        connector.setCenterX(85 + TerminalConstants.connectorRadius);
        connector.setCenterY(22.5 + TerminalConstants.connectorRadius);
        connector.setRadius(TerminalConstants.connectorRadius);
        connector.setFill(getOut() == 0 ? Colors.terminalGreyColor : Colors.terminalActiveColor);

        // Actions
        observeChangesInY();
        addHoverEffectToBase(base);
        addHoverEffectToButton(button);
        changeConnectorColorAndCursorOnHover(connector);
        toggleTerminalState(button, connector);
        makeTerminalDraggable(base);

        // TODO: display popup which consists a delete button and input field for name.
        base.setOnMouseClicked(e -> {
            System.out.println("Clicked");
        });

        container.getChildren().add(base);
        container.getChildren().add(extender);
        container.getChildren().add(button);
        container.getChildren().add(connector);
    }

    private void observeChangesInY() {
        container.layoutYProperty().addListener((observableValue, number, t1) -> {
            wireConnectionPoint.setX(container.layoutXProperty().get() + 85 + TerminalConstants.connectorRadius);
            wireConnectionPoint.setY(container.layoutYProperty().get() + t1.doubleValue() + 22.5 + TerminalConstants.connectorRadius);
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

    private static void changeConnectorColorAndCursorOnHover(Circle connector) {
        connector.setOnMouseEntered(e -> {
            connector.setFill(Colors.terminalGreyColor);
            connector.getScene().setCursor(Cursor.HAND);
            connector.setRadius(connector.getRadius() + 0.5);
        });
        connector.setOnMouseExited(e -> {
            connector.setFill(Colors.terminalBaseColor);
            connector.getScene().setCursor(Cursor.DEFAULT);
            connector.setRadius(TerminalConstants.connectorRadius);
        });
    }

    private void toggleTerminalState(Circle button, Circle connector) {
        button.setOnMouseClicked(e -> {
            if(e.getButton().compareTo(MouseButton.PRIMARY) != 0) return ;

            if (getOut() == 1) setOut(0);
            else setOut(1);

            button.setFill(getOut() == 0 ? Colors.terminalGreyColor : Colors.terminalActiveColor);
            connector.setFill(getOut() == 0 ? Colors.terminalGreyColor : Colors.terminalActiveColor);
        });
    }

    private void makeTerminalDraggable(Rectangle base) {
        double maxYValue = Screen.getPrimary().getBounds().getHeight() - LayoutConstants.menuHeight - 12 - container.getPrefHeight() / 2;
        base.setOnMouseDragged(e -> {
            double mouseY = e.getSceneY();
            if (mouseY > maxYValue || mouseY < 16 + container.getPrefHeight() / 2) return;

            container.setLayoutY(mouseY - container.getPrefHeight() / 2);

        });
    }

    public Point getWireConnectionPoint() {
        return wireConnectionPoint;
    }
    public Pane getDrawable() { return container; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getOut() { return out.getValue(); }
    public void setOut(int value) { out.setValue(value); }
    public void setY(double y) { container.setLayoutY(y); }
}
