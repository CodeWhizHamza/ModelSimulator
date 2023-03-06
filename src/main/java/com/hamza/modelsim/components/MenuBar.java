package com.hamza.modelsim.components;

import com.hamza.modelsim.constants.Colors;
import com.hamza.modelsim.constants.LayoutConstants;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;


public class MenuBar {
    private final HBox container;
    private final Scene scene;

    public MenuBar(Scene scene) {
        this.scene = scene;
        container = new HBox();

        container.setAlignment(Pos.CENTER_LEFT);
        container.setPrefWidth(Screen.getPrimary().getBounds().getWidth());
        container.setPrefHeight(LayoutConstants.menuHeight);
        container.setBackground(Background.fill(Colors.menusColor));

        populateMenu();

    }

    public void populateMenu() {
        Button button = new Button("MENU");
        button.setOnAction(this::showMenu);
        container.getChildren().add(button);
    }

    public void showMenu(Event event) {
        ContextMenu menuContainer = new ContextMenu();
        menuContainer.setWidth(300);

        var quitMenu = new MenuItem("QUIT");
        quitMenu.setOnAction(e -> {
            var stage = (Stage) scene.getWindow();
            stage.close();
        });

        menuContainer.getItems().add(new MenuItem("NEW PROJECT"));
        menuContainer.getItems().add(new MenuItem("OPEN PROJECT"));
        menuContainer.getItems().add(new MenuItem("SAVE COMPONENT"));
        menuContainer.getItems().add(new MenuItem("SAVE PROJECT"));
        menuContainer.getItems().add(quitMenu);

        menuContainer.show(scene.getWindow(), 0, 500);
    }

    public HBox getDrawable() {
        return container;
    }
    public double getHeight() {
        return LayoutConstants.menuHeight;
    }
}
