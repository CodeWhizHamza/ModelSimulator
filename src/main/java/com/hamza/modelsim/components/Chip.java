package com.hamza.modelsim.components;

import com.hamza.modelsim.abstractcomponents.Point;
import com.hamza.modelsim.constants.ChipConstants;
import com.hamza.modelsim.constants.Colors;
import com.hamza.modelsim.constants.State;
import javafx.geometry.Pos;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Chip {
    private String name;
    private String[] functions;
    private Point position;

    private final BorderPane chip;

    private List<InputChipPin> inputPins;
    private List<OutputChipPin> outputPins;

    private List<String> inputs;
    private List<String> outputs;

    private int sizeFactor;

    private double xOffset;
    private double yOffset;

    public Chip(String name, String function, Point position) {
        this(name, new String[] {function}, position);
    }
    public Chip(String name, String[] functions, Point position) {
        this.name = name;
        this.functions = functions;
        this.position = position;
        inputPins = new ArrayList<>();
        outputPins = new ArrayList<>();

        this.outputs = getOutputs();
        this.inputs = getInputs();

        sizeFactor = Math.max(inputs.size(), outputs.size());

        chip = new BorderPane();
        chip.setLayoutX(position.getX());
        chip.setLayoutY(position.getY());
        chip.setPrefWidth(ChipConstants.chipWidth);
        chip.setBackground(Background.fill(Colors.chipColor));

        double height;
        if (inputs.size() >= outputs.size()) {
            height = inputs.size() * (2 * ChipConstants.chipPinRadius + ChipConstants.chipPinGap) + 2 * ChipConstants.chipPinMargin - ChipConstants.chipPinGap;
        } else {
            height = outputs.size() * (2 * ChipConstants.chipPinRadius + ChipConstants.chipPinGap) + 2 * ChipConstants.chipPinMargin - ChipConstants.chipPinGap;
        }
        chip.setPrefHeight(height);

        addInputPins();

        Text nameText = new Text(name);
        nameText.setFill(Colors.white);
        nameText.setFont(new Font("Arial", 18));
        HBox.setHgrow(nameText, Priority.ALWAYS);

        BorderPane.setAlignment(nameText, Pos.CENTER);
        chip.setCenter(nameText);

        addOutputPins();

        makeChipDraggable();

    }

    private void makeChipDraggable() {
        chip.setOnMousePressed(e -> {
            xOffset = e.getSceneX() - chip.getLayoutX();
            yOffset = e.getSceneY() - chip.getLayoutY();
        });
        chip.setOnMouseReleased(e -> {
            xOffset = 0;
            yOffset = 0;
        });
        chip.setOnMouseDragged(e -> {
            chip.setLayoutX(e.getSceneX() - xOffset);
            chip.setLayoutY(e.getSceneY() - yOffset);
        });
    }

    private void addInputPins() {
        FlowPane inputCirclePane = getPinsPane(inputs, "input");
        BorderPane.setAlignment(inputCirclePane, Pos.CENTER_LEFT);
        chip.setLeft(inputCirclePane);
    }

    private void addOutputPins() {
        FlowPane outputCirclePane = getPinsPane(outputs, "output");
        BorderPane.setAlignment(outputCirclePane, Pos.CENTER_RIGHT);
        chip.setRight(outputCirclePane);
    }

    @NotNull
    private FlowPane getPinsPane(List<String> list, String type) {
        FlowPane circlePane = getFlowPane();
        for (int i = 0; i < list.size(); i++) {
            Circle pin = getPinCircle(i, list);
            circlePane.getChildren().add(pin);
            if (type.equals("input")) {
                inputPins.add(new InputChipPin(State.LOW, pin));
            } else {
                outputPins.add(new OutputChipPin(State.LOW, pin));
            }
        }
        circlePane.setLayoutX(type.equals("input") ? 0 : chip.getPrefWidth() - circlePane.getBoundsInLocal().getWidth());
        return circlePane;
    }

    @NotNull
    private static FlowPane getFlowPane() {
        FlowPane inputCirclePane = new FlowPane();
        inputCirclePane.setVgap(ChipConstants.chipPinGap);
        inputCirclePane.setPrefWrapLength(ChipConstants.chipPinRadius * 2);
        inputCirclePane.setAlignment(Pos.CENTER);
        return inputCirclePane;
    }

    @NotNull
    private Circle getPinCircle(int i, List<String> list) {
        Circle pin = new Circle();
        pin.setRadius(ChipConstants.chipPinRadius);
        pin.setFill(Colors.terminalBaseColor);
        addToolTipToPin(list, i, pin);
        return pin;
    }

    private void addToolTipToPin(List<String> inputs, int i, Circle pin) {
        Tooltip tooltip = new Tooltip("  " + inputs.get(i) + "  ");
        Tooltip.install(pin, tooltip);
        tooltip.setFont(new Font("Arial", 16));

        pin.setOnMouseEntered(e -> tooltip.show(pin, e.getScreenX() + 8, e.getScreenY() + 8));
        pin.setOnMouseExited(event -> tooltip.hide());
    }

    private List<String> getOutputs() {
        List<String> outputs = new ArrayList<>();

        for (var function : functions) {
            outputs.add(extractFunctionName(function));
        }

        return outputs;
    }

    private List<String> getInputs() {
        Set<String> inputs = new HashSet<>();

        for(var function : functions) {
            inputs.addAll(extractInputs(function));
        }

        return new ArrayList<>(inputs);
    }

    private List<String> extractInputs(String expression) {
        Set<String> inputsSet = new HashSet<>();
        int equalsIndex = expression.indexOf("=");
        String[] tokens = expression
            .replaceAll("&", "AND")
            .replaceAll("\\|", "OR")
            .replaceAll("!", "NOT")
            .substring(equalsIndex + 1)
            .replaceAll("[()]", "")
            .split("\\s*(AND|OR|NOT)\\s*");

        for (String token : tokens) {
            if (!token.matches("(AND|OR|NOT)")) {
                inputsSet.add(token);
            }
        }

        return new ArrayList<>(inputsSet);
    }

    public static String extractFunctionName(String expression) {
        int equalsIndex = expression.indexOf("=");
        return expression.substring(0, equalsIndex).trim();
    }

    public void draw(Canvas canvas) {
        canvas.getDrawable().getChildren().add(chip);
    }
    public BorderPane getPane() {
        return chip;
    }

    public List<InputChipPin> getInputPins() {
        return inputPins;
    }
    public List<OutputChipPin> getOutputPins() {
        return outputPins;
    }
    public Point[] getInputPoints() {
        return getPointsArrayOf(inputPins.stream().map(ChipPin::getConnector).toList());
    }
    public Point[] getOutputPoints() {
        return getPointsArrayOf(outputPins.stream().map(ChipPin::getConnector).toList());
    }
    private Point[] getPointsArrayOf(List<Circle> list) {
        return list.stream()
            .map(circle -> new Point(chip.getLayoutX() + circle.getCenterX(), chip.getLayoutY() + circle.getCenterY()))
            .toArray(Point[]::new);
    }
}
