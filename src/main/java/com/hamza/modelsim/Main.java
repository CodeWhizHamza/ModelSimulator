package com.hamza.modelsim;

import com.hamza.modelsim.abstractcomponents.Point;
import com.hamza.modelsim.components.*;
import com.hamza.modelsim.constants.Colors;
import com.hamza.modelsim.constants.TerminalConstants;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Transform;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.Objects;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class Main extends Application {
    public static ObservableList<InputPin> inputPins;
    public static ObservableList<OutputPin> outputPins;
    public static ObservableList<Wire> wires;
    public static ObservableList<Chip> chips;
    public static Scene scene;
    public static Canvas canvas;

    private boolean isWireDrawing;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage mainStage) {

        mainStage.setTitle("Model simulator");
        mainStage.setFullScreen(true);
        mainStage.setFullScreenExitHint("");
        mainStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

        inputPins = FXCollections.observableArrayList();
        outputPins = FXCollections.observableArrayList();
        wires = FXCollections.observableArrayList();
        chips = FXCollections.observableArrayList();
        isWireDrawing = false;

        VBox root = new VBox();
        root.setBackground(Background.fill(Colors.backgroundColor));
        root.setFillWidth(true);
        root.setSpacing(0);
        scene = new Scene(root);

        URL stylesheetURLCanBeNull = getClass().getClassLoader().getResource("main.css");
        String stylesheet = Objects.requireNonNull(stylesheetURLCanBeNull).toExternalForm();
        scene.getStylesheets().add(stylesheet);

        canvas = new Canvas();
        MenuBar menuBar = new MenuBar(scene);

        /*
          When user clicks on the input pin, a wire will start drawing
          setting isWireDrawing = true, and the starting position of wire
          to the clicked location of mouse.
         */
        inputPins.addListener((ListChangeListener<? super InputPin>) change -> inputPins.forEach(pin -> pin.getConnector().setOnMouseClicked(e -> {
            if (e.getButton() != MouseButton.PRIMARY) return;

            // if wire is already drawing, no need to draw another one.
            if (isWireDrawing) {
                Wire currentWire = wires.get(wires.size() - 1);
                if (currentWire.getSourcePin() instanceof InputPin) return;
                currentWire.setDestination(pin);
                isWireDrawing = false;
            } else {
                isWireDrawing = true;
                wires.add(new Wire(pin));
            }

        })));

        /*
          When wire is drawing and output pin is clicked, wire gets completed.
         */
        outputPins.addListener((ListChangeListener<? super OutputPin>) change -> {
            for (var pin : outputPins) {
                pin.getConnector().setOnMouseClicked(e -> {
                    if (e.getButton() != MouseButton.PRIMARY) return;

                    if (isWireDrawing) {
                        Wire currentWire = wires.get(wires.size() - 1);
                        if (currentWire.getSourcePin() instanceof OutputPin) return;
                        currentWire.setDestination(pin);
                        isWireDrawing = false;
                    } else {
                        isWireDrawing = true;
                        wires.add(new Wire(pin));
                    }
                });
            }
        });

        scene.setOnMouseClicked(e -> {
            // We will only register mid-points, when the empty area (pane) is clicked.
            if(!(e.getTarget() instanceof Pane)) return;

            /*
            Register point on Left click.
            Delete current wire on right click.
             */
            if (e.getButton() == MouseButton.PRIMARY && wires.size() > 0 && isWireDrawing) {
                wires.get(wires.size() - 1).addPoint(new Point(e.getSceneX(), e.getSceneY()));
            } else if (e.getButton() == MouseButton.SECONDARY) {
                if (wires.size() == 0 || !isWireDrawing) return;
                wires.remove(wires.size() - 1);
                isWireDrawing = false;
            }
        });
        scene.setOnMouseMoved(e -> {
            if (wires.size() == 0 || !isWireDrawing) return;
            wires.get(wires.size() - 1).setMousePosition(new Point(e.getSceneX(), e.getSceneY()));
        });

        // redraw when wires array changes.
        wires.addListener((ListChangeListener<? super Wire>) change -> {
            // Polyline means it's a wire.
            canvas.getDrawable().getChildren().removeIf(node -> node instanceof Polyline);
            wires.forEach(wire -> wire.draw(canvas));
        });

        root.getChildren().add(canvas.getDrawable());
        root.getChildren().add(menuBar.getDrawable());

        double xPosition = 0;
        double yPosition = 8;
        double paddingY = yPosition * 2;
        double borderWidth = 20;
        double borderHeight = Screen.getPrimary().getBounds().getHeight() - paddingY - menuBar.getHeight();

        Rectangle inputPinsBase = makeRectangle(
                xPosition, yPosition,
                borderWidth, borderHeight, Colors.bordersColor
        );
        Rectangle outputPinsBase = makeRectangle(
                Screen.getPrimary().getBounds().getWidth() - xPosition - borderWidth,
                yPosition, borderWidth, borderHeight, Colors.bordersColor
        );

        canvas.add(inputPinsBase);
        canvas.add(outputPinsBase);

        inputPinsBase.setOnMouseClicked(addNewInputTerminal(canvas));
        for (var pin : inputPins)
            pin.draw(canvas.getDrawable());

        // OUTPUT terminals
        outputPinsBase.setOnMouseClicked(addNewOutputTerminal(canvas));
        for (var pin : outputPins)
            pin.draw(canvas.getDrawable());

        // CHIPS
        chips.addListener((ListChangeListener<? super Chip>) change -> {
            chips.forEach(chip -> {
                chip.getInputPins().forEach(pin -> pin.getConnector().setOnMouseClicked(e -> {
                    if (e.getButton() != MouseButton.PRIMARY) return;

                    if (isWireDrawing) {
                        Wire currentWire = wires.get(wires.size() - 1);
                        if (currentWire.getSourcePin() instanceof InputChipPin) return;
                        currentWire.setDestination(pin);
                        isWireDrawing = false;
                    } else {
                        isWireDrawing = true;
                        wires.add(new Wire(pin));
                    }
                }));
                chip.getOutputPins().forEach(pin -> pin.getConnector().setOnMouseClicked(e -> {
                    if (e.getButton() != MouseButton.PRIMARY) return;

                    if (isWireDrawing) {
                        Wire currentWire = wires.get(wires.size() - 1);
                            if (currentWire.getSourcePin() instanceof OutputChipPin) return;
                        currentWire.setDestination(pin);
                        isWireDrawing = false;
                    } else {
                        isWireDrawing = true;
                        wires.add(new Wire(pin));
                    }
                }));
            });

            canvas.getDrawable().getChildren().removeIf(
                node -> chips.stream().anyMatch(chip -> chip.getPane() == node)
            );
            chips.forEach(chip -> chip.draw(canvas));
        });
        chips.add(new Chip("AND", "F=A&B", new Point(450, 300)));
        chips.add(new Chip("NOT", "F=!A", new Point(450, 450)));

        scene.setFill(Colors.backgroundColor);
        mainStage.setScene(scene);
        mainStage.show();
    }

    @NotNull
    private EventHandler<MouseEvent> addNewOutputTerminal(Canvas canvas) {
        return e -> {
            outputPins.add(new OutputPin(e.getSceneY() - TerminalConstants.height / 2));

            for (var pin : outputPins) {
                canvas.getDrawable().getChildren().removeIf(pin.getDrawable()::equals);
            }
            for (var pin : outputPins)
                pin.draw(canvas.getDrawable());
        };
    }

    @NotNull
    private EventHandler<MouseEvent> addNewInputTerminal(Canvas canvas) {
        return e -> {
            inputPins.add(new InputPin(e.getSceneY() - TerminalConstants.height / 2));

            for (var pin : inputPins) {
                canvas.getDrawable().getChildren().removeIf(pin.getDrawable()::equals);
            }
            for (var pin : inputPins)
                pin.draw(canvas.getDrawable());
        };
    }

    public Rectangle makeRectangle(double x, double y, double width, double height, Color color) {
        Rectangle rect = new Rectangle();
        rect.setX(x);
        rect.setY(y);
        rect.setWidth(width);
        rect.setHeight(height);
        rect.setFill(color);
        return rect;
    }
}