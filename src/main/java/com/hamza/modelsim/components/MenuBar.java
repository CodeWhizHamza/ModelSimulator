package com.hamza.modelsim.components;

import com.hamza.modelsim.constants.Colors;
import com.hamza.modelsim.constants.LayoutConstants;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;


public class MenuBar {
    private final HBox container;
    private final Scene scene;
    private final ObservableList<Button> buttons;

    public MenuBar(Scene scene) {
        this.scene = scene;
        container = createContainer();
        buttons = FXCollections.observableArrayList();

        addShowMenuButton();
        populateMenu();
        buttons.addListener((ListChangeListener<? super Button>) change -> populateMenu());
    }

    private void addShowMenuButton() {
        Button button = new Button("MENU");
        button.setOnAction(this::showMenu);
        addButton(button);
    }

    public void clearButtons() {
        buttons.clear();
        addShowMenuButton();
    }

    public void addButton(Button button) {
        buttons.add(button);
    }

    @NotNull
    private HBox createContainer() {
        final HBox container;
        container = new HBox();
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPrefWidth(Screen.getPrimary().getBounds().getWidth());
        container.setPrefHeight(LayoutConstants.menuHeight);
        container.setBackground(Background.fill(Colors.menusColor));
        return container;
    }

    public void populateMenu() {
        container.getChildren().clear();
        container.getChildren().addAll(buttons);
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
