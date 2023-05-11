package com.hamza.modelsim;

import com.hamza.modelsim.abstractcomponents.*;
import com.hamza.modelsim.components.*;
import com.hamza.modelsim.components.MenuBar;
import com.hamza.modelsim.constants.*;
import javafx.application.Application;
import javafx.collections.*;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.*;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
    private boolean isShiftDown;

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
        isShiftDown = false;

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

            if (chip.getOutputPins() == null) return;

            chip.getOutputPins().forEach(pin -> pin.getConnector().setOnMouseClicked(e -> handleOutputChipPinClicked(pin, e)));
        }));

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SHIFT) isShiftDown = true;
        });
        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.SHIFT) isShiftDown = false;
        });

        scene.setOnMouseClicked(e -> {
            if (!(e.getTarget() instanceof Pane)) return;

            if (e.getButton() == MouseButton.SECONDARY) {
                if(isWireDrawing) {
                    isWireDrawing = false;
                    wires.remove(wires.size() - 1);
                }
                if (isChipDrawing) {
                    isChipDrawing = false;
                    chips.remove(chips.size() - 1);
                }
            }
            if (e.getButton() == MouseButton.PRIMARY) {
                if (chips.size() > 0 && isChipDrawing) {
                    var chip = chips.get(chips.size() - 1);
                    chips.remove(chips.size() - 1);
                    Point position = new Point(e.getSceneX(), e.getScreenY());
                    if (chip.getFunctions() != null)
                        chips.add(new Chip(chip.getName(), chip.getFunctions(), position));
                    else
                       chips.add(new Chip(chip.getName(), position));
                    isChipDrawing = false;
                }
                if ( wires.size() > 0 && isWireDrawing) {
                    if (!isShiftDown) {
                        wires.get(wires.size() - 1).addPoint(
                                new Point(mousePosition.getX(), mousePosition.getY())
                        );
                    } else {
                        Point p = getLastPointPositionOfWire();

                        double absDiffX = Math.abs(mousePosition.getX() - p.getX());
                        double absDiffY = Math.abs(mousePosition.getY() - p.getY());

                        if (absDiffY > absDiffX) {
                            wires.get(wires.size() - 1).addPoint(new Point(p.getX(), mousePosition.getY()));
                        } else {
                            wires.get(wires.size() - 1).addPoint(new Point(mousePosition.getX(), p.getY()));
                        }
                    }
                }
            }

        });
        scene.setOnMouseMoved(e -> {
            mousePosition.setX(e.getSceneX());
            mousePosition.setY(e.getSceneY());

            if (wires.size() != 0 && isWireDrawing) {

                if (!isShiftDown) {
                    wires.get(wires.size() - 1).setMousePosition(
                        new Point(mousePosition.getX(), mousePosition.getY())
                    );
                } else {
                    Point p = getLastPointPositionOfWire();

                    double absDiffX = Math.abs(mousePosition.getX() - p.getX());
                    double absDiffY = Math.abs(mousePosition.getY() - p.getY());

                    if (absDiffY > absDiffX) {
                        wires.get(wires.size() - 1).setMousePosition(p.getX(), mousePosition.getY());
                    } else {
                        wires.get(wires.size() - 1).setMousePosition(mousePosition.getX(), p.getY());
                    }
                }

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

            wires.forEach(wire -> wire.getLine().setOnMouseClicked(e -> {
                if (e.getButton() != MouseButton.PRIMARY) return;
                Point position = new Point(mousePosition.getX(), mousePosition.getY());

                if (!isWireDrawing) {
                    System.out.println("Wire is drawing.");
//                    if (wire.getInputPin() != null) {
//                        wires.add(new Wire(wire.getInputPin(), wire, position));
//                    } else if (wire.getOutputToChip() != null) {
//                        wires.add(new Wire(wire.getOutputToChip(), wire, position));
//                    }
////                    isWireDrawing = true;
                } else {
                    wires.get(wires.size() - 1).addPoint(position);
                }
            }));

            wires.forEach(wire -> {
                ContextMenu contextMenu = new ContextMenu();
                MenuItem deleteMenuItem = new MenuItem("Delete");
                contextMenu.getItems().add(deleteMenuItem);
                wire.getLine().setOnContextMenuRequested(event -> contextMenu.show(wire.getLine(), event.getScreenX(), event.getScreenY()));
                deleteMenuItem.setOnAction(event -> {
                    wire.removeListeners();
                    wires.remove(wire);
                });
            });
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
                    Point position = new Point(mousePosition.getX(), mousePosition.getY() + 20);
                    if (chip.getFunctions().length > 0)
                        chips.add(new Chip(chip.getName(), chip.getFunctions(), position));
                    else
                        chips.add(new Chip(chip.getName(), position));
                });
                menuBar.addButton(button);
            });
        });

        // * Add default 3 gates.
        availableChips.add(new ChipLabel("NOT", "F=!A"));
        availableChips.add(new ChipLabel("AND", "F=A&B"));
        availableChips.add(new ChipLabel("OR", "F=A|B"));
        availableChips.add(new ChipLabel("3 in OR", "F=A|B|C"));
        availableChips.add(new ChipLabel("3 in AND", "F=A&B&C"));
        availableChips.add(new ChipLabel("NAND", "F=!(A&B)"));
        availableChips.add(new ChipLabel("NOR", "F=!(A|B)"));
        availableChips.add(new ChipLabel("7-Seg", ""));
        
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

        inputPinsBase.setOnMouseClicked(addNewInputTerminal());
        for (var pin : inputPins)
            pin.draw(canvas.getDrawable());

        // OUTPUT terminals
        outputPinsBase.setOnMouseClicked(addNewOutputTerminal());
        for (var pin : outputPins)
            pin.draw(canvas.getDrawable());

        // CHIPS
        chips.addListener((ListChangeListener<? super Chip>) change -> {
            ObservableList<Node> children = canvas.getDrawable().getChildren();
            children.removeIf(child -> child instanceof AnchorPane);
            chips.forEach(chip -> chip.draw(canvas));

            chips.forEach(chip -> {
                ContextMenu contextMenu = new ContextMenu();
                MenuItem deleteMenuItem = new MenuItem("Delete");
                contextMenu.getItems().add(deleteMenuItem);
                chip.getPane().setOnContextMenuRequested(event -> contextMenu.show(chip.getPane(), event.getScreenX(), event.getScreenY()));
                deleteMenuItem.setOnAction(event -> deleteChip(chip));
            });
        });

        inputPins.addListener((ListChangeListener<? super InputPin>) change -> {
            for (var pin : inputPins)
                canvas.getDrawable().getChildren().removeIf(pin.getDrawable()::equals);
            for (var pin : inputPins)
                pin.draw(canvas.getDrawable());

            inputPins.forEach(this::showContextMenuOnPinClick);
        });
        outputPins.addListener((ListChangeListener<? super OutputPin>) change -> {
            for (var pin : outputPins)
                canvas.getDrawable().getChildren().removeIf(pin.getDrawable()::equals);
            for (var pin : outputPins)
                pin.draw(canvas.getDrawable());

            outputPins.forEach(this::showContextMenuOnPinClick);
        });

        scene.setFill(Colors.backgroundColor);
        mainStage.setScene(scene);
        mainStage.show();
    }

    private Point getLastPointPositionOfWire() {
        Wire last = wires.get(wires.size() - 1);
        double x = 0, y = 0;

        if (last.getPoints().size() == 0) {

            if (last.getSourcePin() instanceof InputPin) {
                x = ((InputPin) last.getSourcePin()).getConnectionPoint().getX();
                y = ((InputPin) last.getSourcePin()).getConnectionPoint().getY();
            } else if (last.getSourcePin() instanceof OutputPin) {
                x = ((OutputPin) last.getSourcePin()).getConnectionPoint().getX();
                y = ((OutputPin) last.getSourcePin()).getConnectionPoint().getY();
            } else if (last.getSourcePin() instanceof InputChipPin) {
                x = ((InputChipPin) last.getSourcePin()).getConnectionPoint().getX();
                y = ((InputChipPin) last.getSourcePin()).getConnectionPoint().getY();
            } else if (last.getSourcePin() instanceof OutputChipPin) {
                x = ((OutputChipPin) last.getSourcePin()).getConnectionPoint().getX();
                y = ((OutputChipPin) last.getSourcePin()).getConnectionPoint().getY();
            }

        } else {
            x = last.getPoints().get(last.getPoints().size() - 1).getX();
            y = last.getPoints().get(last.getPoints().size() - 1).getY();
        }

        return new Point(x, y);
    }

//    private static void startWireFromMid(Wire wire, Point position) {
//        if (wire.getPoints().size() == 0) {
//            wire.addPoint(position);
//        } else {
//            int lastIndex = wire.getPoints().size() - 1;
//            Point first = wire.getPoints().get(0);
//            Point last = wire.getPoints().get(lastIndex);
//            if (first == last) {
//                if (position.getX() <= first.getX()) {
//                    wire.getPoints().add(0, new Point(position.getX(), position.getY()));
//                } else {
//                    wire.getPoints().add(lastIndex + 1, new Point(position.getX(), position.getY()));
//                }
//            }
//        }
//    }

    private void showContextMenuOnPinClick(InputPin pin) {
        Node node = pin.getPane();

        ContextMenu contextMenu = new ContextMenu();
        TextField textField = makeTextField(pin, contextMenu);

        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(event -> {
            removeAllConnectedWiresToPin(pin);

            // To remove it from view
            canvas.getDrawable().getChildren().removeIf(pin.getDrawable()::equals);
            inputPins.remove(pin);
        });
        contextMenu.getItems().addAll( new CustomMenuItem(textField), new SeparatorMenuItem(),deleteMenuItem);
        node.setOnContextMenuRequested(event -> {
            textField.setText(pin.getName());
            contextMenu.show(node, event.getScreenX(), event.getScreenY());
        });
    }

    @NotNull
    private TextField makeTextField(Object pin, ContextMenu contextMenu) {
        TextField textField = new TextField();
        textField.requestFocus();

        textField.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.SPACE) {
                textField.setText(textField.getText().replaceAll("\\s", ""));
                textField.positionCaret(textField.getLength());
            }
        });
        // consume the event to prevent it from closing the context menu
        textField.addEventFilter(MouseEvent.MOUSE_CLICKED, Event::consume);

        textField.setOnAction(e -> {
            if (pin instanceof InputPin)
                ((InputPin) pin).setName(textField.getText());
            else
                ((OutputPin) pin).setName(textField.getText());
            contextMenu.hide();
        });
        return textField;
    }

    private void showContextMenuOnPinClick(OutputPin pin) {
        Node node = pin.getPane();

        ContextMenu contextMenu = new ContextMenu();
        TextField textField = makeTextField(pin, contextMenu);

        MenuItem deleteMenuItem = new MenuItem("Delete");
        contextMenu.getItems().addAll( new CustomMenuItem(textField), new SeparatorMenuItem(),deleteMenuItem);
        node.setOnContextMenuRequested(event -> {
            textField.setText(pin.getName());
            contextMenu.show(node, event.getScreenX(), event.getScreenY());
        });
        deleteMenuItem.setOnAction(event -> {
            removeAllConnectedWiresToPin(pin);

            // To remove it from view
            canvas.getDrawable().getChildren().removeIf(pin.getDrawable()::equals);
            outputPins.remove(pin);
        });
    }

    private void removeAllConnectedWiresToPin(InputPin pin) {
        List<Wire> toBeRemoved = new ArrayList<>();
        for(Wire wire : wires) {
            if (wire.getInputPin() == pin)
                toBeRemoved.add(wire);
        }
        deleteWires(toBeRemoved);
    }

    private void removeAllConnectedWiresToPin(OutputPin pin) {
        List<Wire> toBeRemoved = new ArrayList<>();
        for(Wire wire : wires) {
            if (wire.getOutputPin() == pin)
                toBeRemoved.add(wire);
        }
        deleteWires(toBeRemoved);
    }

    private void deleteWires(List<Wire> toBeRemoved) {
        for(var wire : toBeRemoved)
            wire.removeListeners();
        wires.removeAll(toBeRemoved);
    }
    private void deleteChip(Chip chip) {
        chips.remove(chip);
        chip.removeAllListeners();

        List<Wire> wiresToBeRemoved = new ArrayList<>();
        for(Wire wire : wires) {
            InputChipPin input = wire.getOutputToChip();
            OutputChipPin output = wire.getInputFromChip();
            if (chip.getInputPins().stream().anyMatch(pin -> pin == input)) {
                wiresToBeRemoved.add(wire);
            }
            if (chip.getOutputPins() != null && chip.getOutputPins().stream().anyMatch(pin -> pin == output)) {
                wiresToBeRemoved.add(wire);
            }
        }
        deleteWires(wiresToBeRemoved);
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
    private EventHandler<MouseEvent> addNewOutputTerminal() {
        return e -> outputPins.add(new OutputPin(e.getSceneY() - TerminalConstants.height / 2));
    }

    @NotNull
    private EventHandler<MouseEvent> addNewInputTerminal() {
        return e -> inputPins.add(new InputPin(e.getSceneY() - TerminalConstants.height / 2));
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