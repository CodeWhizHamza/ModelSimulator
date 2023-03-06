package com.hamza.modelsim.components;

import com.hamza.modelsim.constants.LayoutConstants;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;

public class Canvas {
    private Pane pane;

    public Canvas() {
        pane = new Pane();

        pane.setPrefWidth(Screen.getPrimary().getBounds().getWidth());
        pane.setPrefHeight(Screen.getPrimary().getBounds().getHeight() - LayoutConstants.menuHeight);

    }

    public void add(Node node) {
        pane.getChildren().add(node);
    }

    public Pane getDrawable() { return pane; }
}
