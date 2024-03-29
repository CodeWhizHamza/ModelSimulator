package com.hamza.modelsim.abstractcomponents;

import com.hamza.modelsim.constants.Colors;
import com.hamza.modelsim.constants.LayoutConstants;
import com.hamza.modelsim.constants.TerminalConstants;
import javafx.scene.Cursor;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;

public abstract class Pin {
    private final Pane pane;
    private final Point connectionPoint;
    private final Rectangle base;
    private final Rectangle extender;
    private final Circle connector;
    private final Circle button;

    public Pin(double y) {
        pane = new Pane();
        setY(y);
        pane.setPrefHeight(TerminalConstants.height);
        connectionPoint = new Point();
        base = new Rectangle();
        extender = new Rectangle();
        button = new Circle();
        connector = new Circle();

        // behaviors
        observeChangesInY();
        addHoverEffectToBase(getBase());
        addHoverEffectToButton();
        addHoverEffectToConnector();
        makeTerminalDraggable(getBase());
    }

    private void addHoverEffectToBase(Rectangle base) {
        base.setOnMouseEntered(e -> {
            base.setFill(Colors.warmWhite);
            base.getScene().setCursor(Cursor.HAND);
        });
        base.setOnMouseExited(e -> {
            base.setFill(Colors.terminalBaseColor);
            base.getScene().setCursor(Cursor.DEFAULT);
        });
    }

    protected void observeChangesInY() {
        pane.layoutYProperty().addListener((observableValue, number, t1) -> {
            connectionPoint.setX(pane.layoutXProperty().get() + 85 + TerminalConstants.connectorRadius);
            connectionPoint.setY(pane.getLayoutY() + connector.getLayoutY() + TerminalConstants.connectorRadius + 22.5);
        });
    }

    private void addHoverEffectToButton() {
        button.setOnMouseEntered(e -> button.getScene().setCursor(Cursor.HAND));
        button.setOnMouseExited(e -> button.getScene().setCursor(Cursor.DEFAULT));
    }

    private void addHoverEffectToConnector() {
        connector.setOnMouseEntered(e -> {
            connector.getScene().setCursor(Cursor.HAND);
            connector.setFill(Colors.white);
        });
        connector.setOnMouseExited(e -> {
            connector.getScene().setCursor(Cursor.DEFAULT);
            connector.setFill(Colors.terminalGreyColor);
        });
    }

    private void makeTerminalDraggable(Rectangle base) {
        double maxYValue = Screen.getPrimary().getBounds().getHeight() - LayoutConstants.menuHeight - 12 - pane.getPrefHeight() / 2;
        base.setOnMouseDragged(e -> {
            double mouseY = e.getSceneY();
            if (mouseY > maxYValue || mouseY < 16 + pane.getPrefHeight() / 2)
                return;
            pane.setLayoutY(mouseY - pane.getPrefHeight() / 2);
        });
    }

    public void setY(double y) {
        pane.setLayoutY(y);
    }

    public Pane getPane() {
        return pane;
    }

    public Point getConnectionPoint() {
        return connectionPoint;
    }

    public Circle getConnector() {
        return connector;
    }

    public Circle getButton() {
        return button;
    }

    public Rectangle getBase() {
        return base;
    }

    public Rectangle getExtender() {
        return extender;
    }

    public Pane getDrawable() {
        return pane;
    }

}


