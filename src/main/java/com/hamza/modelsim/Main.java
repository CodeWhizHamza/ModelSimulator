package com.hamza.modelsim;

import com.google.gson.Gson;
import com.hamza.modelsim.abstractcomponents.ChipLabel;
import com.hamza.modelsim.abstractcomponents.Level;
import com.hamza.modelsim.abstractcomponents.Point;
import com.hamza.modelsim.components.*;
import com.hamza.modelsim.constants.Colors;
import com.hamza.modelsim.constants.LayoutConstants;
import com.hamza.modelsim.constants.State;
import com.hamza.modelsim.constants.TerminalConstants;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Main extends Application {
    public static ObservableList<InputPin> inputPins;
    public static ObservableList<OutputPin> outputPins;
    public static ObservableList<Wire> wires;
    public static ObservableList<Chip> chips;
    public static ObservableList<ChipLabel> availableChips;
    public static Canvas canvas;
    public static Stage mainStage;
    public static Level currentLevel;
    public static Canvas playgroundCanvas;
    private static boolean isWireDrawing;
    private static boolean isChipDrawing;
    private static boolean isShiftDown;
    private static Point mousePosition;
    private static List<Level> levels;
    private static long startTime;
    private static Image goldenStar;
    private static Image grayStar;
    private static Image lock;
    private static Image help;
    private static Image truthTable;
    private static Image check;
    private static Image cross;
    private static Image menu;
    private static Image retry;
    private static Image next;
    private double timeRemaining = 0;

    public static void main(String[] args) {
        launch(args);
    }

    private static void testForInputs(Level level, HashMap<String, Boolean> testResults, int i) {
        StringBuilder input = new StringBuilder("Inputs: ");
        for (var key : level.inputs.keySet()) {
            inputPins.stream().filter(pin -> pin.getName().equals(key)).forEach(pin -> pin.setState(level.inputs.get(key)[i] == 0 ? State.LOW : State.HIGH));
            input.append(key).append("=").append(level.inputs.get(key)[i]).append(" ");
        }
        input.append("\nOutputs: ");
        AtomicBoolean isTrue = new AtomicBoolean(true);
        for (var key : level.outputs.keySet()) {
            outputPins.stream().filter(pin -> pin.getName().equals(key)).forEach(pin -> {
                if (pin.getState() == State.HIGH && level.outputs.get(key)[i] == 0)
                    isTrue.set(false);
                if (pin.getState() == State.LOW & level.outputs.get(key)[i] == 1)
                    isTrue.set(false);

                input.append(key).append("=").append(pin.getState() == State.HIGH ? "1" : "0").append(" ");
            });
        }

        testResults.put(input.toString(), isTrue.get());
    }

    @NotNull
    private static VBox createRoot() {
        VBox root = new VBox();
        root.setBackground(Background.fill(Colors.backgroundColor));
        root.setFillWidth(true);
        root.setSpacing(0);
        return root;
    }

    private static Popup showResultsPopup(ScrollPane scrollContainer, HashMap<String, Boolean> testResults) {
        Popup popup = new Popup();
        VBox popupContent = new VBox();
        popupContent.setPrefWidth(500);
        popupContent.setPrefHeight(500);
        popupContent.setSpacing(16);
        popupContent.setStyle("-fx-background-color: #444; -fx-padding: 24px;");

        Text titleLabel = new Text(testResults.values().stream().filter(v -> v).count() + "/" + testResults.size() + " Passed");
        titleLabel.setFill(Colors.white);
        titleLabel.setStyle("-fx-font-family: Calibri; -fx-font-size: 32px; -fx-font-weight: bold;");

        Button closeButton = new Button("× Close");
        closeButton.setStyle("-fx-font-size: 16px; -fx-background-color: #333");
        closeButton.setOnAction(event -> popup.hide());

        VBox results = new VBox();
        results.setSpacing(8);
        results.setFillWidth(true);

        for (var key : testResults.keySet().stream().sorted().toList()) {
            Text testQuery = new Text(key);
            testQuery.setFill(Colors.white);
            testQuery.setWrappingWidth(400);
            testQuery.setStyle("-fx-font-size: 18px; -fx-font-family: Calibri;");

            ImageView image = new ImageView(testResults.get(key) ? check : cross);

            HBox result = new HBox();
            result.getChildren().add(image);
            result.getChildren().add(testQuery);
            result.setSpacing(8);
            results.getChildren().add(result);
        }

        scrollContainer.setContent(popupContent);
        scrollContainer.setStyle("-fx-background-color: #444;");
        scrollContainer.setFitToHeight(true);
        scrollContainer.setFitToWidth(true);
        Platform.runLater(() -> scrollContainer.setVvalue(0));

        popupContent.getChildren().addAll(titleLabel, results, closeButton);
        popup.getContent().add(scrollContainer);
        popup.setAutoHide(true);
        popup.show(mainStage);

        return popup;
    }

    private static void insertInputAndOutputPins(Level level) {
        AtomicReference<Double> y = new AtomicReference<>((double) 60);
        level.inputs.keySet().forEach(key -> {
            InputPin pin = new InputPin(y.get());
            pin.setName(key);
            inputPins.add(pin);
            y.updateAndGet(v -> v + TerminalConstants.height);
        });

        y.updateAndGet(v -> 16.0);
        level.outputs.keySet().stream().sorted((s1, s2) -> {
            int n1, n2;
            try {
                n1 = Integer.parseInt(s1);
                n2 = Integer.parseInt(s2);
            } catch (NumberFormatException e) {
                return s1.compareTo(s2);
            }
            return Integer.compare(n1, n2);
        }).forEach(key -> {
            OutputPin pin = new OutputPin(y.get());
            pin.setName(key);
            outputPins.add(pin);
            y.updateAndGet(v -> {
                if (level.outputs.size() >= 14)
                    return v + TerminalConstants.height - 18;
                else
                    return v + TerminalConstants.height;
            });
        });
    }

    @NotNull
    private static EventHandler<MouseEvent> handleMouseMoved() {
        return e -> {
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
            if (chips.size() != 0 && isChipDrawing) {
                chips.get(chips.size() - 1).setPosition(new Point(mousePosition.getX(), mousePosition.getY()));
            }
        };
    }

    @NotNull
    private static ListChangeListener<? super OutputPin> handleChangeInOutputPins() {
        return change -> {
            for (var pin : outputPins)
                canvas.getDrawable().getChildren().removeIf(pin.getDrawable()::equals);
            for (var pin : outputPins)
                pin.draw(canvas.getDrawable());

            outputPins.forEach(Main::showContextMenuOnPinClick);
        };
    }

    @NotNull
    private static ListChangeListener<? super InputPin> handleChangeInInputPins() {
        return change -> {
            for (var pin : inputPins)
                canvas.getDrawable().getChildren().removeIf(pin.getDrawable()::equals);
            for (var pin : inputPins)
                pin.draw(canvas.getDrawable());

            inputPins.forEach(Main::showContextMenuOnPinClick);
        };
    }

    @NotNull
    private static ListChangeListener<? super Chip> handleChangeInChips() {
        return change -> {
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
        };
    }

    private static void setStageScene(Scene scene) {
        mainStage.setScene(scene);
        mainStage.setFullScreen(false);
        mainStage.setFullScreen(true);
    }

    private static Point getLastPointPositionOfWire() {
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

    private static void showContextMenuOnPinClick(InputPin pin) {
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
        contextMenu.getItems().addAll(new CustomMenuItem(textField), new SeparatorMenuItem(), deleteMenuItem);
        node.setOnContextMenuRequested(event -> {
            textField.setText(pin.getName());
            contextMenu.show(node, event.getScreenX(), event.getScreenY());
        });
    }

    @NotNull
    private static TextField makeTextField(Object pin, ContextMenu contextMenu) {
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

    private static void showContextMenuOnPinClick(OutputPin pin) {
        Node node = pin.getPane();

        ContextMenu contextMenu = new ContextMenu();
        TextField textField = makeTextField(pin, contextMenu);

        MenuItem deleteMenuItem = new MenuItem("Delete");
        contextMenu.getItems().addAll(new CustomMenuItem(textField), new SeparatorMenuItem(), deleteMenuItem);
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

    private static void removeAllConnectedWiresToPin(InputPin pin) {
        List<Wire> toBeRemoved = new ArrayList<>();
        for (Wire wire : wires) {
            if (wire.getInputPin() == pin)
                toBeRemoved.add(wire);
        }
        deleteWires(toBeRemoved);
    }

    private static void removeAllConnectedWiresToPin(OutputPin pin) {
        List<Wire> toBeRemoved = new ArrayList<>();
        for (Wire wire : wires) {
            if (wire.getOutputPin() == pin)
                toBeRemoved.add(wire);
        }
        deleteWires(toBeRemoved);
    }

    private static void deleteWires(List<Wire> toBeRemoved) {
        for (var wire : toBeRemoved)
            wire.removeListeners();
        wires.removeAll(toBeRemoved);
    }

    private static void deleteChip(Chip chip) {
        chips.remove(chip);
        chip.removeAllListeners();

        List<Wire> wiresToBeRemoved = new ArrayList<>();
        for (Wire wire : wires) {
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

    private static void handleOutputChipPinClicked(OutputChipPin pin, MouseEvent e) {
        if (e.getButton() != MouseButton.PRIMARY) return;

        if (isWireDrawing) {
            Wire currentWire = wires.get(wires.size() - 1);

            if (currentWire.getSourcePin() instanceof OutputChipPin) return;
            if (currentWire.getSourcePin() instanceof InputPin) return;

            currentWire.setDestination(pin);
            isWireDrawing = false;
        } else {
            isWireDrawing = true;
            wires.add(new Wire(pin));
        }
    }

    private static void handleInputPinClicked(InputPin pin, MouseEvent e) {
        if (e.getButton() != MouseButton.PRIMARY) return;

        if (isWireDrawing) {
            Wire currentWire = wires.get(wires.size() - 1);

            if (currentWire.getSourcePin() instanceof InputPin) return;
            if (currentWire.getSourcePin() instanceof OutputChipPin) return;

            currentWire.setDestination(pin);
            isWireDrawing = false;
        } else {
            isWireDrawing = true;
            wires.add(new Wire(pin));
        }
    }

    private static void handleInputChipPinClicked(InputChipPin pin, MouseEvent e) {
        if (e.getButton() != MouseButton.PRIMARY) return;

        if (isWireDrawing) {
            Wire currentWire = wires.get(wires.size() - 1);

            if (currentWire.getSourcePin() instanceof OutputPin) return;
            if (currentWire.getSourcePin() instanceof InputChipPin) return;

            currentWire.setDestination(pin);
            isWireDrawing = false;
        } else {
            isWireDrawing = true;
            wires.add(new Wire(pin));
        }
    }

    private static void handleOutputPinClicked(OutputPin pin, MouseEvent e) {
        if (e.getButton() != MouseButton.PRIMARY) return;

        if (isWireDrawing) {
            Wire currentWire = wires.get(wires.size() - 1);

            if (currentWire.getSourcePin() instanceof InputChipPin) return;
            if (currentWire.getSourcePin() instanceof OutputPin) return;

            currentWire.setDestination(pin);
            isWireDrawing = false;
        } else {
            isWireDrawing = true;
            wires.add(new Wire(pin));
        }
    }

    private static void addStyleSheet(Scene scene, String filename) {
        URL stylesheetURLCanBeNull = Main.class.getClassLoader().getResource(filename);
        String stylesheet = Objects.requireNonNull(stylesheetURLCanBeNull).toExternalForm();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(stylesheet);
    }

    @NotNull
    private static EventHandler<MouseEvent> addNewOutputTerminal() {
        return e -> outputPins.add(new OutputPin(e.getSceneY() - TerminalConstants.height / 2));
    }

    @NotNull
    private static EventHandler<MouseEvent> addNewInputTerminal() {
        return e -> inputPins.add(new InputPin(e.getSceneY() - TerminalConstants.height / 2));
    }

    public static Rectangle makeRectangle(double x, double y, double width, double height, Color color) {
        Rectangle rect = new Rectangle();
        rect.setX(x);
        rect.setY(y);
        rect.setWidth(width);
        rect.setHeight(height);
        rect.setFill(color);
        return rect;
    }

    public void addNewGate() {
        var background = new Pane();
        background.setPrefHeight(1080);
        background.setPrefWidth(1920);
        background.setLayoutY(0);
        background.setLayoutX(0);
        background.setBackground(Background.fill(Color.color(0.2, 0.2, 0.2, .95)));

        var box = new VBox();
        box.setBackground(Background.fill(Color.rgb(30, 30, 30)));
        box.setPrefWidth(800);
        box.setPrefHeight(700);

        box.setLayoutX(300);
        box.setLayoutY(60);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-padding: 20px 60px;");

        var title = new Text("Add new gate");
        title.setFill(Colors.warmWhite);
        title.setStyle("-fx-font-family: Calibri; -fx-font-size: 22px;");
        box.getChildren().add(title);

        // the text fields.
        var nameLabel = new Text("Name:");
        nameLabel.setFill(Colors.warmWhite);
        nameLabel.setStyle("-fx-padding: 25px 0 0 0;");
        var nameField = new TextField();

        var functionsLabel = new Text("Functions:");
        functionsLabel.setFill(Colors.warmWhite);
        functionsLabel.setStyle("-fx-padding: 25px 0 0 0;");
        ObservableList<TextField> functions = FXCollections.observableArrayList(new TextField());

        var addFunction = new Button("+ add function");
        addFunction.setOnAction(e -> functions.add(new TextField()));

        var fieldsBox = new VBox();
        fieldsBox.getChildren().addAll(nameLabel, nameField, functionsLabel);
        fieldsBox.getChildren().addAll(functions);
        fieldsBox.getChildren().add(addFunction);

        box.getChildren().add(fieldsBox);


        functions.addListener((ListChangeListener<? super TextField>) change -> {
            fieldsBox.getChildren().clear();
            fieldsBox.getChildren().addAll(nameLabel, nameField, functionsLabel);
            fieldsBox.getChildren().addAll(functions);
            fieldsBox.getChildren().add(addFunction);
        });

        var closeButton = new Button("Close");
        var addButton = new Button("Add gate");

        closeButton.setOnAction(e -> playgroundCanvas.getDrawable().getChildren().removeIf(child -> child == background));

        addButton.setOnAction(e -> {
            if (nameField.getCharacters().isEmpty()) {
                nameLabel.setText("Name: *required (please enter name)");
                return;
            } else {
                nameLabel.setText("Name: ");
            }

            var validFunctions = functions.stream().filter(f -> !f.getCharacters().isEmpty()).toList();
            if (validFunctions.size() == 0) {
                functionsLabel.setText("Functions: *required (please enter at least one function)");
                return;
            } else {
                functionsLabel.setText("Functions: ");
            }
            validFunctions = validFunctions.stream().filter(f -> validateBooleanFunction(f.getCharacters().toString())).toList();

            if (validFunctions.size() < 1) {
                functionsLabel.setText("Functions: *(please enter valid boolean function)");
                return;
            } else {
                functionsLabel.setText("Functions:");
            }

            availableChips.add(new ChipLabel(nameField.getText(), validFunctions.stream().map(field -> field.getCharacters().toString()).toArray(String[]::new)));
            playgroundCanvas.getDrawable().getChildren().removeIf(child -> child == background);

        });

        var buttons = new HBox();
        buttons.getChildren().add(closeButton);
        buttons.getChildren().add(addButton);
        box.getChildren().add(buttons);

        background.getChildren().add(box);
        playgroundCanvas.add(background);
    }

    public boolean validateBooleanFunction(String function) {
        int equalsIndex = function.indexOf("=");

        if (equalsIndex < 0)
            return false;

        String functionName = function.substring(0, equalsIndex).trim();

        if (functionName.equals(""))
            return false;

        if (function.length() < equalsIndex + 1)
            return false;

        Set<String> inputsSet = new HashSet<>();
        String[] parts = function
                .replaceAll("\\|", "OR")
                .replaceAll("&", "AND")
                .replaceAll("!", "NOT")
                .substring(equalsIndex + 1)
                .replaceAll("[()]", "")
                .split("\\s*(AND|OR|NOT)\\s*");

        for (String token : parts) {
            if (!token.matches("(AND|OR|NOT)") && token.length() > 0) {
                inputsSet.add(token);
            }
        }

        var inputs = new ArrayList<>(inputsSet);
        String expression = function.substring(equalsIndex + 1);
        expression = expression
                .substring(expression.indexOf("=") + 1)
                .replaceAll("&", "&&")
                .replaceAll("\\|", "||");

        for (var input : inputs) {
            expression = expression.replaceAll(input, "true");
        }

        Context context = Context.enter();
        try {
            Scriptable scope = context.initStandardObjects();
            String testFunction = "function solveBooleanFunction() { return " + expression + "; }";
            context.evaluateString(scope, testFunction, "BooleanFunction", 1, null);
            Object solveFunction = scope.get("solveBooleanFunction", scope);
            org.mozilla.javascript.Function javaFunction = (org.mozilla.javascript.Function) solveFunction;

            javaFunction.call(context, scope, scope, new Object[]{});

        } catch (Exception e) {
            System.out.println("Exception raised in the function");
            return false;
        } finally {
            Context.exit();
        }

        return true;
    }

    @Override
    public void start(Stage mainStage) {
        Main.mainStage = mainStage;
        mainStage.setTitle("Model simulator");
        mainStage.setFullScreen(true);
        mainStage.setFullScreenExitHint("");
        mainStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

        levels = new ArrayList<>();

        try {
            goldenStar = new Image(new FileInputStream("src/main/resources/images/golden-star.png"));
            grayStar = new Image(new FileInputStream("src/main/resources/images/gray-star.png"));
            lock = new Image(new FileInputStream("src/main/resources/images/lock.png"));
            help = new Image(new FileInputStream("src/main/resources/images/help.png"));
            truthTable = new Image(new FileInputStream("src/main/resources/images/truth-table.png"));
            check = new Image(new FileInputStream("src/main/resources/images/check.png"));
            cross = new Image(new FileInputStream("src/main/resources/images/cross.png"));
            menu = new Image(new FileInputStream("src/main/resources/images/menu.png"));
            retry = new Image(new FileInputStream("src/main/resources/images/retry.png"));
            next = new Image(new FileInputStream("src/main/resources/images/next.png"));
        } catch (Exception e) {
            System.out.println("Image cannot be loaded.");
        }

        showStartMenu();
        mainStage.show();
    }

    public void showStartMenu() {
        Button play = new Button("Play Game");
        Button gotoPlayground = new Button("Goto Playground");
        Button quitBtn = new Button("Quit");

        play.setOnAction(e -> showLevelsScreen());
        gotoPlayground.setOnAction(e -> initPlayground(null));
        quitBtn.setOnAction(e -> System.exit(0));

        VBox root = createRoot();
        root.setFillWidth(true);
        root.setAlignment(Pos.CENTER);
        root.setSpacing(40);

        Text title = new Text("Circuit Conundrum");
        title.setFill(Colors.white);
        title.getStyleClass().add("my-text");

        VBox menuBox = new VBox(play, gotoPlayground, quitBtn);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setSpacing(10);

        root.getChildren().add(title);
        root.getChildren().add(menuBox);

        Scene scene = new Scene(root);
        addStyleSheet(scene, "menu-styles.css");
        setStageScene(scene);
    }

    public void showLevelsScreen() {
        loadLevels();

        VBox root = createRoot();
        root.getStyleClass().add("container");
        root.setFillWidth(true);
        root.setSpacing(40);

        Button backButton = new Button("← back");
        backButton.setOnAction(e -> showStartMenu());

        Text title = new Text("Levels     ");
        title.setFill(Colors.white);
        title.getStyleClass().add("my-text");

        BorderPane titleBar = new BorderPane();
        titleBar.setLeft(backButton);
        titleBar.setCenter(title);

        FlowPane levelsContainer = new FlowPane();
        levelsContainer.setHgap(40);
        levelsContainer.setVgap(40);
        levelsContainer.setPrefWrapLength(1305);
        levelsContainer.setAlignment(Pos.TOP_CENTER);
        levelsContainer.getStyleClass().add("levels-container");

        List<VBox> levelBoxes = generateLevelBoxes();

        for (var box : levelBoxes)
            levelsContainer.getChildren().add(box);

        ScrollPane scrollContainer = new ScrollPane();
        scrollContainer.setContent(levelsContainer);
        scrollContainer.getStyleClass().add("scroll-container");
        scrollContainer.setFitToHeight(true);
        scrollContainer.setFitToWidth(true);

        root.getChildren().add(titleBar);
        root.getChildren().add(scrollContainer);

        Scene scene = new Scene(root);
        addStyleSheet(scene, "levels.css");
        setStageScene(scene);
    }

    private List<VBox> generateLevelBoxes() {
        List<VBox> boxes = new ArrayList<>();

        for (var level : levels) {
            VBox box = new VBox();
            box.getStyleClass().add("box");
            box.setPrefHeight(343);
            box.setPrefWidth(407);
            box.setSpacing(64);
            box.setAlignment(Pos.CENTER);

            Label name = new Label(level.name);
            name.getStyleClass().add("box-label");

            if (level.isLocked) {
                ImageView image = new ImageView(lock);
                box.getChildren().add(image);

                box.setOnMouseClicked(mouseEvent -> {
                    if (mouseEvent.getButton() != MouseButton.PRIMARY) return;

                    Timeline animation = new Timeline();
                    animation.setCycleCount(1);
                    double rotation = 8;
                    animation.getKeyFrames().addAll(
                            new KeyFrame(Duration.ZERO, e -> image.setStyle("-fx-rotate: 0deg")),
                            new KeyFrame(Duration.millis(62), e -> image.setStyle("-fx-rotate: " + rotation + "deg")),
                            new KeyFrame(Duration.millis(62 * 2), e -> image.setStyle("-fx-rotate: 0deg")),
                            new KeyFrame(Duration.millis(62 * 3), e -> image.setStyle("-fx-rotate: -" + rotation + "deg")),
                            new KeyFrame(Duration.millis(62 * 4), e -> image.setStyle("-fx-rotate: 0deg")),
                            new KeyFrame(Duration.millis(62 * 5), e -> image.setStyle("-fx-rotate: " + rotation + "deg")),
                            new KeyFrame(Duration.millis(62 * 6), e -> image.setStyle("-fx-rotate: 0deg")),
                            new KeyFrame(Duration.millis(62 * 7), e -> image.setStyle("-fx-rotate: -" + rotation + "deg")),
                            new KeyFrame(Duration.millis(62 * 8), e -> image.setStyle("-fx-rotate: 0deg"))
                    );
                    animation.play();
                });
            } else {
                HBox stars = new HBox();
                stars.setSpacing(36);
                stars.setAlignment(Pos.BASELINE_CENTER);
                stars.getStyleClass().add("stars-container");
                int starsAdded = 0;

                for (int i = 0; i < level.previousScore; i++, starsAdded++)
                    stars.getChildren().add(new ImageView(goldenStar));

                for (; starsAdded < 3; starsAdded++)
                    stars.getChildren().add(new ImageView(grayStar));

                stars.getChildren().get(1).setStyle("-fx-translate-y: -22px");
                box.getChildren().add(stars);


                box.setOnMouseClicked(e -> {
                    if (e.getButton() != MouseButton.PRIMARY) return;
                    initPlayground(level);
                });
            }
            box.getChildren().add(name);
            boxes.add(box);

        }

        return boxes;
    }

    private void loadLevels() {
        levels.clear();
        List<File> levelsFiles = new ArrayList<>(getFiles());
        levelsFiles.sort(Comparator.comparing(File::getName));

        Gson gson = new Gson();
        for (File level : levelsFiles) {
            try {
                levels.add(gson.fromJson(new FileReader(getLevelString(level.getName())), Level.class));
            } catch (FileNotFoundException e) {
                System.out.println("Cannot open file: " + level.getName());
            }
        }
    }

    private String getLevelString(String fileName) {
        return "src/main/resources/levels/" + fileName;
    }

    private List<File> getFiles() {
        File directory = new File("src/main/resources/levels");
        return List.of(Objects.requireNonNull(directory.listFiles()));
    }

    public void initPlayground(Level level) {
        currentLevel = level;
        inputPins = FXCollections.observableArrayList();
        outputPins = FXCollections.observableArrayList();
        wires = FXCollections.observableArrayList();
        chips = FXCollections.observableArrayList();
        availableChips = FXCollections.observableArrayList();

        isWireDrawing = false;
        isChipDrawing = false;
        isShiftDown = false;

        startTime = System.currentTimeMillis();
        mousePosition = new Point();

        VBox root = createRoot();
        Scene scene = new Scene(root);
        addStyleSheet(scene, "main.css");
        canvas = new Canvas();

        playgroundCanvas = canvas;

        // Show title on top of screen
        Text title = new Text(level == null ? "Playground" : level.name);
        title.setFont(new Font("Arial", 32));
        title.setFill(Colors.white);
        title.setStyle("-fx-text-fill: #ffffff; -fx-text-alignment: center; -fx-font-size: 32px;");
        title.setLayoutX(canvas.getDrawable().getPrefWidth() / 2 - title.getBoundsInLocal().getWidth() / 2);
        title.setLayoutY(40);
        canvas.add(title);

        // Hint
        if (level != null) {
            Text hint = new Text("Hint: " + level.hint);
            hint.setFont(new Font("Arial", 16));
            hint.setFill(Colors.warmWhite);
            hint.setLayoutX(30);
            hint.setLayoutY(canvas.getDrawable().getPrefHeight() - hint.getBoundsInLocal().getHeight() - 4);
            canvas.add(hint);
            hint.setVisible(false);
        }

        // Truth table and help button
        Button helpButton = new Button();
        helpButton.setGraphic(new ImageView(help));
        helpButton.setStyle("-fx-padding: 0");
        helpButton.setLayoutX(24);
        helpButton.setLayoutY(10);
        if (level != null)
            canvas.add(helpButton);

        ScrollPane scrollContainer = new ScrollPane();
        scrollContainer.getStyleClass().add("scroll-container");
        helpButton.setOnAction(e -> {

            Popup popup = new Popup();
            VBox popupContent = new VBox();
            popupContent.setPrefWidth(500);
            popupContent.setPrefHeight(500);
            popupContent.setSpacing(16);
            popupContent.setStyle("-fx-background-color: #444; -fx-padding: 24px;");

            Text titleLabel = new Text("Description");
            titleLabel.setFill(Colors.white);
            titleLabel.setStyle("-fx-font-family: Calibri; -fx-font-size: 32px; -fx-font-weight: bold;");

            Button closeButton = new Button("× Close");
            closeButton.setStyle("-fx-font-size: 16px; -fx-background-color: #333");
            closeButton.setOnAction(event -> popup.hide());


            Text description = new Text(level != null ? level.description : "");
            description.setFill(Colors.white);
            description.setWrappingWidth(440);
            description.setStyle("-fx-font-size: 18px; -fx-font-family: Calibri;");

            scrollContainer.setContent(popupContent);
            scrollContainer.setStyle("-fx-background-color: #444;");
            scrollContainer.setFitToHeight(true);
            scrollContainer.setFitToWidth(true);
            scrollContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            Platform.runLater(() -> scrollContainer.setVvalue(0));

            popupContent.getChildren().addAll(titleLabel, description, closeButton);
            popup.getContent().add(scrollContainer);
            popup.setAutoHide(true);
            popup.show(mainStage);
        });

        Button truthTableButton = new Button();
        truthTableButton.setGraphic(new ImageView(truthTable));
        truthTableButton.setStyle("-fx-padding: 0");
        truthTableButton.setLayoutX(72);
        truthTableButton.setLayoutY(10);
        if (level != null)
            canvas.add(truthTableButton);

        truthTableButton.setOnAction(e -> {
            if (level == null) return;

            Popup popup = new Popup();
            VBox popupContent = new VBox();
            popupContent.setPrefWidth(500);
            popupContent.setPrefHeight(500);
            popupContent.setSpacing(16);
            popupContent.setStyle("-fx-background-color: #444; -fx-padding: 24px;");

            Text titleLabel = new Text("Truth table");
            titleLabel.setFill(Colors.white);
            titleLabel.setStyle("-fx-font-family: Calibri; -fx-font-size: 32px; -fx-font-weight: bold;");

            Button closeButton = new Button("× Close");
            closeButton.setStyle("-fx-font-size: 16px; -fx-background-color: #333");
            closeButton.setOnAction(event -> popup.hide());

            GridPane table = new GridPane();
            table.setAlignment(Pos.CENTER);
            table.setVgap(8);
            table.setHgap(12);
            table.setBorder(Border.stroke(Colors.white));
            table.setStyle("-fx-padding: 4px");

            List<String> keys = new ArrayList<>(level.inputs.keySet());
            keys.addAll(level.outputs.keySet());

            HashMap<String, Integer[]> values = new HashMap<>();
            for (var key : level.inputs.keySet()) {
                values.put(key, level.inputs.get(key));
            }
            for (var key : level.outputs.keySet()) {
                values.put(key, level.outputs.get(key));
            }

            for (int i = 0; i < keys.size(); i++) {
                List<Text> fields = new ArrayList<>();
                Text text = new Text(keys.get(i));
                text.setStyle("-fx-fill: #fff; -fx-font-size: 20px; -fx-font-family: Calibri; -fx-font-weight: bold; -fx-text-alignment: center;");
                fields.add(text);

                for (var value : values.get(keys.get(i))) {
                    text = new Text(Integer.toString(value));
                    text.setStyle("-fx-fill: #fff; -fx-font-size: 20px; -fx-font-family: Calibri; -fx-font-weight: bold; -fx-text-alignment: center;");
                    fields.add(text);
                }

                table.addColumn(i, fields.toArray(new Text[]{}));
            }
            scrollContainer.setContent(popupContent);
            scrollContainer.setStyle("-fx-background-color: #444;");
            scrollContainer.setFitToWidth(true);
            scrollContainer.setFitToHeight(true);
            Platform.runLater(() -> scrollContainer.setVvalue(0));

            popupContent.getChildren().addAll(titleLabel, table, closeButton);
            popup.getContent().add(scrollContainer);
            popup.setAutoHide(true);
            popup.show(mainStage);
        });

        Button testCircuitButton = new Button("Test Circuit");
        testCircuitButton.setStyle("-fx-background-color: #222; -fx-padding: 12px 24px");
        testCircuitButton.setLayoutY(canvas.getDrawable().getPrefHeight() - 50);
        testCircuitButton.setLayoutX(canvas.getDrawable().getPrefWidth() - 180);
        if (level != null)
            canvas.add(testCircuitButton);

        /*
        ** TESTS
        1. equal inputs
        2. equal outputs
        3. Truth table matches.
         */
        testCircuitButton.setOnAction(e -> {
            if (level == null) return;
            HashMap<String, Boolean> testResults = new HashMap<>();

            testResults.put("inputs matches", level.inputs.size() == inputPins.size());
            testResults.put("outputs matches", level.outputs.size() == outputPins.size());
            @SuppressWarnings("SuspiciousMethodCalls") int numberOfRows = level.inputs.get(level.inputs.keySet().toArray()[0]).length;

            Timeline timeline = new Timeline();
            int delay = 200;
            for (int i = 0; i < numberOfRows; i++) {
                int finalI = i;
                timeline.getKeyFrames().add(new KeyFrame(Duration.millis((i + 1) * delay), actionEvent -> testForInputs(level, testResults, finalI)));
            }
            timeline.play();
            timeline.setOnFinished(actionEvent -> {
                AtomicReference<Popup> resultsPopup = new AtomicReference<>();
                Timeline tl = new Timeline();
                tl.getKeyFrames().add(
                        new KeyFrame(Duration.seconds(0), a -> resultsPopup.set(showResultsPopup(scrollContainer, testResults)))
                );
                tl.getKeyFrames().add(new KeyFrame(Duration.seconds(2), ignored -> {
                    if (testResults.values().stream().filter(v -> !v).toList().size() > 0)
                        return; // No win

                    // Win condition
                    resultsPopup.get().hide();

                    ArrayList<String> couldDoBetter = new ArrayList<>();
                    int stars = 1;

                    if (level.maxGates >= chips.size())
                        stars += 1;
                    else
                        couldDoBetter.add("Try to use less number of gates.");

                    if ((System.currentTimeMillis() - startTime) / 1000 <= level.maxTime)
                        stars += 1;
                    else
                        couldDoBetter.add("Try to solve it fast.");

                    if (level.previousScore < stars)
                        level.previousScore = stars;
                    int nextIndex = levels.indexOf(level) + 1;
                    if (nextIndex != levels.size())
                        levels.get(nextIndex).isLocked = false;
                    saveLevels();

                    var background = new Pane();
                    background.setPrefWidth(1920);
                    background.setPrefHeight(1080);
                    background.setBackground(Background.fill(Color.color(0.2, 0.2, 0.2, .95)));
                    background.setLayoutX(0);
                    background.setLayoutY(0);

                    var box = new VBox();
                    box.setBackground(Background.fill(Color.rgb(30, 30, 30)));
                    box.setPrefWidth(800);
                    box.setPrefHeight(700);

                    box.setLayoutX(300);
                    box.setLayoutY(60);
                    box.setSpacing(50);
                    box.setAlignment(Pos.CENTER);
                    box.setStyle("-fx-padding: 20px 60px;");

                    Text titleLabel = new Text("You Won");
                    titleLabel.setFill(Colors.white);
                    titleLabel.setStyle("-fx-font-family: Calibri; -fx-font-size: 32px; -fx-font-weight: bold;");

                    HBox starsContainer = new HBox();
                    starsContainer.setAlignment(Pos.CENTER);

                    for (int i = 0; i < stars; i++) {
                        ImageView goldenStarImageView = new ImageView(goldenStar);
                        starsContainer.getChildren().add(goldenStarImageView);
                    }

                    for (int i = 0; i < 3 - stars; i++)
                        starsContainer.getChildren().add(new ImageView(grayStar));

                    starsContainer.getChildren().get(1).setStyle("-fx-translate-y: -22px;");

                    VBox improvements = new VBox();
                    for (var improvement : couldDoBetter) {
                        Text text = new Text(improvement);
                        text.setStyle("-fx-fill: #fff; -fx-font-size: 14px; -fx-font-family: Calibri;");
                        improvements.getChildren().add(text);
                    }

                    HBox buttons = new HBox();
                    buttons.setAlignment(Pos.CENTER);
                    buttons.setSpacing(16);

                    Button menuButton = new Button();
                    menuButton.setGraphic(new ImageView(menu));
                    menuButton.setOnAction(event -> showLevelsScreen());

                    Button retryButton = new Button();
                    retryButton.setGraphic(new ImageView(retry));
                    retryButton.setOnAction(event -> initPlayground(level));

                    Button nextButton = new Button();
                    nextButton.setGraphic(new ImageView(next));
                    nextButton.setOnAction(event -> initPlayground(levels.get(nextIndex)));

                    buttons.getChildren().addAll(menuButton, retryButton);
                    if (nextIndex != levels.size())
                        buttons.getChildren().add(nextButton);

                    box.getChildren().addAll(titleLabel, starsContainer, improvements, buttons);
                    background.getChildren().add(box);
                    playgroundCanvas.add(background);
                }));
                tl.play();
            });

        });


        /*
        Show time remaining for best things.
         */
        if (level != null) {
            timeRemaining = level.maxTime;
            var timeLabel = new Text("Best Time remaining: " + timeRemaining);
            timeLabel.setFill(Colors.white);
            timeLabel.setStyle("-fx-font-family: Calibri; -fx-font-size: 20px;");
            timeLabel.setLayoutY(20);
            timeLabel.setLayoutX(150);
            canvas.add(timeLabel);
            Timeline timeTimeline = new Timeline();
            timeTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1), frameEvent -> {
                if (timeRemaining < 1) return;
                timeRemaining -= 1;
                timeLabel.setText("Best Time remaining: " + timeRemaining);
            }));
            timeTimeline.setCycleCount(level.maxTime);
            timeTimeline.play();
        }


        inputPins.addListener((ListChangeListener<? super InputPin>) change -> inputPins.forEach(pin -> pin.getConnector().setOnMouseClicked(e -> handleInputPinClicked(pin, e))));
        outputPins.addListener((ListChangeListener<? super OutputPin>) change -> outputPins.forEach(pin -> pin.getConnector().setOnMouseClicked(e -> handleOutputPinClicked(pin, e))));
        chips.addListener((ListChangeListener<? super Chip>) change -> chips.forEach(chip -> {
            chip.getInputPins().forEach(pin -> pin.getConnector().setOnMouseClicked(e -> handleInputChipPinClicked(pin, e)));

            if (chip.getOutputPins() == null) return;
            chip.getOutputPins().forEach(pin -> pin.getConnector().setOnMouseClicked(e -> handleOutputChipPinClicked(pin, e)));
        }));

        scene.setOnKeyPressed(e -> isShiftDown = e.getCode() == KeyCode.SHIFT);
        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.SHIFT) isShiftDown = false;
        });

        scene.setOnMouseClicked(e -> {
            if (!(e.getTarget() instanceof Pane)) return;
            if (e.getButton() == MouseButton.SECONDARY) {
                if (isWireDrawing) {
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
                if (wires.size() > 0 && isWireDrawing) {
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
        scene.setOnMouseMoved(handleMouseMoved());

        // redraw when wires array changes.
        wires.addListener((ListChangeListener<? super Wire>) change -> {
            // Polyline means it's a wire.
            canvas.getDrawable().getChildren().removeIf(node -> node instanceof Polyline);
            wires.forEach(wire -> wire.draw(canvas));

            wires.forEach(wire -> wire.getLine().setOnMouseClicked(e -> {
                if (e.getButton() != MouseButton.PRIMARY) return;
                Point position = new Point(mousePosition.getX(), mousePosition.getY());

                if (!isWireDrawing) {
                    if (wire.getInputPin() != null) {
                        wires.add(new Wire(wire.getInputPin(), wire, position));
                    } else if (wire.getOutputToChip() != null) {
                        wires.add(new Wire(wire.getOutputToChip(), wire, position));
                    }
                    isWireDrawing = true;
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


        HBox menuBarContainer = new HBox();
        menuBarContainer.setAlignment(Pos.CENTER_LEFT);
        menuBarContainer.setPrefWidth(Screen.getPrimary().getBounds().getWidth());
        menuBarContainer.setPrefHeight(LayoutConstants.menuHeight);
        menuBarContainer.setBackground(Background.fill(Colors.menusColor));

        ObservableList<Button> buttons = FXCollections.observableArrayList();
        menuBarContainer.getChildren().addAll(buttons);

        buttons.addListener((ListChangeListener<? super Button>) change -> {
            menuBarContainer.getChildren().clear();
            menuBarContainer.getChildren().addAll(buttons);
        });

        root.getChildren().add(menuBarContainer);
        menuBarContainer.getChildren().addListener((ListChangeListener<? super Node>) change -> {
            root.getChildren().removeIf(node -> node == menuBarContainer);
            root.getChildren().add(menuBarContainer);
        });

        availableChips.addListener((ListChangeListener<? super ChipLabel>) change -> {
            buttons.clear();

            Button menuButton = new Button("Menu");
            menuButton.setOnAction(menuEvent -> {
                ContextMenu menuContainer = new ContextMenu();
                menuContainer.setWidth(300);

                var gotoMainMenu = new MenuItem("Goto MainMenu");
                gotoMainMenu.setOnAction(e -> showStartMenu());

                var resetMenuItem = new MenuItem("Reset");
                resetMenuItem.setOnAction(e -> initPlayground(Main.currentLevel));

                var addGateMenuItem = new MenuItem("Add Gate");
                addGateMenuItem.setOnAction(e -> addNewGate());

                var showLevelsMenuItem = new MenuItem("Goto Levels");
                showLevelsMenuItem.setOnAction(e -> showLevelsScreen());

                menuContainer.getItems().add(resetMenuItem);
                if (level == null)
                    menuContainer.getItems().add(addGateMenuItem);
                else
                    menuContainer.getItems().add(showLevelsMenuItem);
                menuContainer.getItems().add(gotoMainMenu);
                menuContainer.show(scene.getWindow(), 0, scene.getHeight() - 250);
            });
            buttons.add(menuButton);

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
                buttons.add(button);
            });
        });

        // Add available gates
        if (level != null) {
            for (String key : level.availableGates.keySet()) {
                availableChips.add(new ChipLabel(key, level.availableGates.get(key)));
            }
        } else {
            availableChips.add(new ChipLabel("AND", "F=A&B"));
            availableChips.add(new ChipLabel("OR", "F=A|B"));
            availableChips.add(new ChipLabel("NOT", "F=!A"));
            availableChips.add(new ChipLabel("7-Seg", ""));
        }

        double xPosition = 0;
        double yPosition = 8;
        double paddingY = yPosition * 2;
        double borderWidth = 20;
        double borderHeight = Screen.getPrimary().getBounds().getHeight() - paddingY - LayoutConstants.menuHeight;

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


        chips.addListener(handleChangeInChips());
        inputPins.addListener(handleChangeInInputPins());
        outputPins.addListener(handleChangeInOutputPins());

        if (level != null)
            insertInputAndOutputPins(level);

        scene.setFill(Colors.backgroundColor);
        setStageScene(scene);
    }

    private void saveLevels() {
        Gson gson = new Gson();
        for (var level : levels) {
            String filename = getLevelString(level.filename);
            FileWriter writer;
            try {
                writer = new FileWriter(filename);
                String json = gson.toJson(level);
                writer.write(json);
                writer.close();
            } catch (IOException e) {
                System.out.println("Levels cannot be saved.");
                e.printStackTrace();
            }
        }
        loadLevels();
    }
}