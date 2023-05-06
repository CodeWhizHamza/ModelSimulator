package com.hamza.modelsim.components;

import com.hamza.modelsim.abstractcomponents.Point;
import com.hamza.modelsim.constants.ChipConstants;
import com.hamza.modelsim.constants.Colors;
import com.hamza.modelsim.constants.State;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Chip {
    private final AnchorPane chip;
    private final String name;
    private final String[] functions;
    private final SimpleObjectProperty<Point> position;
    private final List<InputChipPin> inputPins;
    private final List<OutputChipPin> outputPins;

    private final List<String> inputs;
    private final List<String> outputs;

    private final ObservableList<State> inputValues;
    private final ObservableList<State> outputValues;

    private double xOffset;
    private double yOffset;

    public Chip(String name, String[] functions, Point position) {
        this.name = name;
        this.functions = functions;
        this.position = new SimpleObjectProperty<>();
        this.position.set(position);
        inputPins = new ArrayList<>();
        outputPins = new ArrayList<>();

        this.outputs = getOutputs();
        this.inputs = getInputs();

        inputValues = FXCollections.observableArrayList();
        outputValues = FXCollections.observableArrayList();

        chip = new AnchorPane();
        chip.setLayoutX(this.position.get().getX());
        chip.setLayoutY(this.position.get().getY());
        chip.setPrefWidth(ChipConstants.chipWidth);
        chip.setBackground(Background.fill(Colors.chipColor));

        this.position.addListener(change -> {
            chip.setLayoutX(this.position.get().getX());
            chip.setLayoutY(this.position.get().getY());
        });

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

        nameText.setLayoutX(ChipConstants.chipWidth / 2 - nameText.getBoundsInLocal().getWidth() / 2);
        nameText.setLayoutY(height / 2 + height / 12);

        chip.getChildren().add(nameText);
        addOutputPins();

        // Behaviours
        makeChipDraggable();
        initializeInputValues();
        observeChangesInInputs();
        updateConnectionPoints();
        updateOutputs();
        listenToChangesInOutputs();
        updateOutputPins();
    }

    public static String extractFunctionName(String expression) {
        int equalsIndex = expression.indexOf("=");
        return expression.substring(0, equalsIndex).trim();
    }

    private void listenToChangesInOutputs() {
        outputValues.addListener((ListChangeListener<? super State>) change -> updateOutputPins());
    }

    private void updateOutputPins() {
        for (int i = 0; i < outputValues.size(); i++)
            outputPins.get(i).setState(outputValues.get(i));
    }

    private void initializeInputValues() {
        inputValues.clear();
        inputPins.forEach(pin -> inputValues.add(pin.getState().getValue()));
    }

    private void observeChangesInInputs() {
        inputPins.forEach(pin -> pin.getState().addListener((observableValue, state, t1) -> {
            initializeInputValues();
            updateOutputs();
        }));
    }

    private void updateOutputs() {
        outputValues.clear();
        for (var expression : functions) {
            boolean result = solveExpression(expression);
            outputValues.add(result ? State.HIGH : State.LOW);
        }
    }

    private boolean solveExpression(String expression) {
        Context context = Context.enter();
        Object result;
        try {
            Scriptable scope = context.initStandardObjects();
            expression = extractExpression(expression, extractInputs(expression));
            result = executeFunction(context, expression, scope);
        } finally {
            Context.exit();
        }
        return (Boolean) result;
    }

    private Object executeFunction(Context context, String expression, Scriptable scope) {
        String function = "function solveBooleanFunction() { return " + expression + "; }";
        context.evaluateString(scope, function, "BooleanFunction", 1, null);
        Object solveFunction = scope.get("solveBooleanFunction", scope);
        org.mozilla.javascript.Function javaFunction = (org.mozilla.javascript.Function) solveFunction;
        return javaFunction.call(context, scope, scope, new Object[]{});
    }

    @NotNull
    private String extractExpression(String expression, List<String> inputs) {
        expression = expression
                .substring(expression.indexOf("=") + 1)
                .replaceAll("&", "&&")
                .replaceAll("\\|", "||");

        for (var input : inputs) {
            int index = this.inputs.indexOf(input);
            State state = inputValues.get(index);
            expression = expression.replace(input, ((Boolean) (state == State.HIGH)).toString());
        }
        return expression;
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
            chip.setLayoutY(e.
                    getSceneY() - yOffset);

            updateConnectionPoints();
        });
    }

    private void updateConnectionPoints() {
        List<ChipPin> combined = new ArrayList<>(inputPins);
        combined.addAll(outputPins);
        combined.forEach(pin -> pin.setConnectionPoint(calculateConnectionPoint(pin.getConnector())));
    }

    private void addInputPins() {

        for (int i = 0; i < inputs.size(); i++) {
            Circle pin = new Circle(ChipConstants.chipPinRadius);
            pin.setCenterX(ChipConstants.chipPinRadius);
            pin.setCenterY(getCenterYForPinCircle(i));
            pin.setFill(Colors.terminalGreyColor);
            addToolTipToPin(inputs, i, pin);

            var inputPin = new InputChipPin(State.LOW, pin, this);
            inputPin.setConnectionPoint(new Point(pin.getCenterX(), pin.getCenterY()));
            inputPins.add(inputPin);

            chip.getChildren().add(pin);
        }
    }

    private double getCenterYForPinCircle(int index) {
        return ChipConstants.chipPinMargin + ChipConstants.chipPinRadius * (2 * index + 1) + ChipConstants.chipPinGap * index;
    }

    private void addOutputPins() {
        for (int i = 0; i < outputs.size(); i++) {
            Circle pin = new Circle(ChipConstants.chipPinRadius);
            pin.setCenterX(chip.getPrefWidth() - ChipConstants.chipPinRadius);
            pin.setCenterY(getCenterYForPinCircle(i));
            pin.setFill(Colors.terminalGreyColor);
            addToolTipToPin(outputs, i, pin);

            var outputPin = new OutputChipPin(State.LOW, pin, this);
            outputPin.setConnectionPoint(new Point(pin.getCenterX(), pin.getCenterY()));
            outputPins.add(outputPin);

            chip.getChildren().add(pin);
        }
    }

    private Point calculateConnectionPoint(Circle pin) {
        return new Point(pin.getBoundsInLocal().getCenterX() + chip.getLayoutX(), pin.getBoundsInLocal().getCenterY() + chip.getLayoutY());
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

        for (var function : functions) {
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
            if (!token.matches("(AND|OR|NOT)") && token.length() > 0) {
                inputsSet.add(token);
            }
        }

        return new ArrayList<>(inputsSet);
    }

    public void draw(Canvas canvas) {
        canvas.getDrawable().getChildren().add(chip);
    }

    public AnchorPane getPane() {
        return chip;
    }

    public List<InputChipPin> getInputPins() {
        return inputPins;
    }

    public List<OutputChipPin> getOutputPins() {
        return outputPins;
    }

    public void setPosition(Point position) {
        this.position.set(position);
    }
}
