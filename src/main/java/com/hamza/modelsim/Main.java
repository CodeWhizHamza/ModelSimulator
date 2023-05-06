package com.hamza.modelsim;

import com.hamza.modelsim.abstractcomponents.*;
import com.hamza.modelsim.components.*;
import com.hamza.modelsim.constants.*;
import javafx.application.Application;
import javafx.collections.*;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.*;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.Objects;

public class Main extends Application {
    public static ObservableList<InputPin> inputPins;
    public static ObservableList<OutputPin> outputPins;
    public static ObservableList<Wire> wires;
    public static ObservableList<Chip> chips;
    public static ObservableList<ChipLabel> availableChips;
    public static Scene scene;
    public static Canvas canvas;

    private boolean isWireDrawing;
    private boolean isChipDrawing;

    private Point mousePosition;

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
        availableChips = FXCollections.observableArrayList();

        isWireDrawing = false;
        isChipDrawing = false;

        mousePosition = new Point();

        VBox root = createRoot();
        scene = new Scene(root);
        addStyleSheet();
        canvas = new Canvas();
        MenuBar menuBar = new MenuBar(scene);

        inputPins.addListener((ListChangeListener<? super InputPin>) change -> inputPins.forEach(pin -> pin.getConnector().setOnMouseClicked(e -> handleInputPinClicked(pin, e))));
        outputPins.addListener((ListChangeListener<? super OutputPin>) change -> outputPins.forEach(pin -> pin.getConnector().setOnMouseClicked(e -> handleOutputPinClicked(pin, e))));
        chips.addListener((ListChangeListener<? super Chip>) change -> chips.forEach(chip -> {
            chip.getInputPins().forEach(pin -> pin.getConnector().setOnMouseClicked(e -> handleInputChipPinClicked(pin, e)));
            chip.getOutputPins().forEach(pin -> pin.getConnector().setOnMouseClicked(e -> handleOutputChipPinClicked(pin, e)));
        }));

        scene.setOnMouseClicked(e -> {
            if (!(e.getTarget() instanceof Pane)) return;

            if (e.getButton() == MouseButton.PRIMARY && chips.size() > 0 && isChipDrawing) {
                chips.get(chips.size() - 1).setPosition(new Point(e.getSceneX(), e.getSceneY()));
                isChipDrawing = false;
            } else if (e.getButton() == MouseButton.SECONDARY){
                if(chips.size() == 0 || !isChipDrawing) return;
                chips.remove(chips.size() - 1);
                isChipDrawing = false;
            }

            if (e.getButton() == MouseButton.PRIMARY && wires.size() > 0 && isWireDrawing) {
                wires.get(wires.size() - 1).addPoint(new Point(e.getSceneX(), e.getSceneY()));
            } else if (e.getButton() == MouseButton.SECONDARY) {
                if (wires.size() == 0 || !isWireDrawing) return;
                wires.remove(wires.size() - 1);
                isWireDrawing = false;
            }
        });
        scene.setOnMouseMoved(e -> {
            mousePosition.setX(e.getSceneX());
            mousePosition.setY(e.getSceneY());

            if (wires.size() != 0 && isWireDrawing) {
                wires.get(wires.size() - 1).setMousePosition(new Point(e.getSceneX(), e.getSceneY()));
            }
            if(chips.size() != 0 && isChipDrawing) {
                chips.get(chips.size() - 1).setPosition(new Point(mousePosition.getX(), mousePosition.getY()));
            }
        });

        // redraw when wires array changes.
        wires.addListener((ListChangeListener<? super Wire>) change -> {
            // Polyline means it's a wire.
            canvas.getDrawable().getChildren().removeIf(node -> node instanceof Polyline);
            wires.forEach(wire -> wire.draw(canvas));
        });

        root.getChildren().add(canvas.getDrawable());
        root.getChildren().add(menuBar.getDrawable());
        menuBar.getDrawable().getChildren().addListener((ListChangeListener<? super Node>) change -> {
            root.getChildren().removeIf(node -> node == menuBar.getDrawable());
            root.getChildren().add(menuBar.getDrawable());
        });

        availableChips.addListener((ListChangeListener<? super ChipLabel>) change -> {
            menuBar.clearButtons();
            availableChips.forEach(chip -> {
                Button button = new Button(chip.getName());
                button.setOnAction(e -> {
                    isChipDrawing = true;
                    chips.add(new Chip(chip.getName(), chip.getFunctions(), new Point(mousePosition.getX(), mousePosition.getY() + 20)));
                });
                menuBar.addButton(button);
            });
        });

        // * Add default 3 gates.
        availableChips.add(new ChipLabel("NOT", "F=!A"));
        availableChips.add(new ChipLabel("AND", "F=A&B"));
        availableChips.add(new ChipLabel("OR", "F=A|B"));

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
            ObservableList<Node> children = canvas.getDrawable().getChildren();
            children.removeIf(child -> child instanceof AnchorPane);

            chips.forEach(chip -> chip.draw(canvas));
        });

        scene.setFill(Colors.backgroundColor);
        mainStage.setScene(scene);
        mainStage.show();
    }

    private void handleOutputChipPinClicked(OutputChipPin pin, MouseEvent e) {
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
    }

    private void handleInputChipPinClicked(InputChipPin pin, MouseEvent e) {
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
    }

    private void handleOutputPinClicked(OutputPin pin, MouseEvent e) {
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
    }

    private void handleInputPinClicked(InputPin pin, MouseEvent e) {
        if (e.getButton() != MouseButton.PRIMARY) return;

        if (isWireDrawing) {
            Wire currentWire = wires.get(wires.size() - 1);
            if (currentWire.getSourcePin() instanceof InputPin) return;
            currentWire.setDestination(pin);
            isWireDrawing = false;
        } else {
            isWireDrawing = true;
            wires.add(new Wire(pin));
        }
    }

    @NotNull
    private static VBox createRoot() {
        VBox root = new VBox();
        root.setBackground(Background.fill(Colors.backgroundColor));
        root.setFillWidth(true);
        root.setSpacing(0);
        return root;
    }

    private void addStyleSheet() {
        URL stylesheetURLCanBeNull = getClass().getClassLoader().getResource("main.css");
        String stylesheet = Objects.requireNonNull(stylesheetURLCanBeNull).toExternalForm();
        scene.getStylesheets().add(stylesheet);
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