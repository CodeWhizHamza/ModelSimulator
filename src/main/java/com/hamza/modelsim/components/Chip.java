package com.hamza.modelsim.components;

import com.hamza.modelsim.abstractcomponents.Point;
import com.hamza.modelsim.constants.ChipConstants;
import com.hamza.modelsim.constants.Colors;
import com.hamza.modelsim.constants.State;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
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
    private String[] functions;
    private final SimpleObjectProperty<Point> position;
    private final List<InputChipPin> inputPins;
    private List<OutputChipPin> outputPins;

    private final List<String> inputs;
    private List<String> outputs;

    private final ObservableList<State> inputValues;
    private ObservableList<State> outputValues;
    private ObservableList<Polygon> segments;

    private final ListChangeListener<? super State> outputValuesListener = change -> updateOutputPins();
    private final ChangeListener<State> inputChangesListener = (observableValue, state, t1) -> {
        initializeInputValues();
        updateOutputs();
    };
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
        chip.setMinWidth(ChipConstants.chipWidth);
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

        double width = nameText.getBoundsInLocal().getWidth() + 2 * (ChipConstants.chipPadding);
        chip.setPrefWidth(Math.max(width, ChipConstants.chipWidth));

        nameText.setLayoutX(width / 2 - nameText.getBoundsInLocal().getWidth() / 2);
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

    public Chip(String name, Point position) {
        this.name = name;
        this.position = new SimpleObjectProperty<>();
        this.position.set(position);
        inputPins = new ArrayList<>();
        this.inputs = new ArrayList<>(List.of(new String[]{"sign", "a", "b", "c", "d", "e", "f", "g"}));

        inputValues = FXCollections.observableArrayList();
        segments = FXCollections.observableArrayList();

        chip = new AnchorPane();
        chip.setLayoutX(this.position.get().getX());
        chip.setLayoutY(this.position.get().getY());
        chip.setPrefWidth(ChipConstants.chipWidth);
        chip.setBackground(Background.fill(Colors.chipColor));

        this.position.addListener(change -> {
            chip.setLayoutX(this.position.get().getX());
            chip.setLayoutY(this.position.get().getY());
        });

        double height = inputs.size() * (2 * ChipConstants.chipPinRadius + ChipConstants.chipPinGap) + 2 * ChipConstants.chipPinMargin - ChipConstants.chipPinGap;
        chip.setPrefHeight(height);

        addInputPins();
        addSegments();

        // Behaviours
        makeChipDraggable();
        initializeInputValues();
        observeChangesInInputs();
        updateConnectionPoints();

        inputValues.addListener((ListChangeListener<? super State>) change -> {
            for(int i = 0; i < inputValues.size(); i++) {
                segments.get(i).setFill(
                        inputValues.get(i) == State.HIGH
                                ? Colors.terminalActiveColor
                                : Colors.terminalGreyColor
                );
            }
        });
    }

    public void addSegments() {
        Group segmentsHolder = new Group();

        Polygon sign = getHSegment();
        Polygon a = getHSegment();
        Polygon b = getVSegment();
        Polygon c = getVSegment();
        Polygon d = getHSegment();
        Polygon e = getVSegment();
        Polygon f = getVSegment();
        Polygon g = getHSegment();

        double height = 78;
        double width = 74;

        sign.setLayoutX(0);
        sign.setLayoutY(height / 2 - 2);

        a.setLayoutX(width / 2 + 2);
        a.setLayoutY(0);

        b.setLayoutX(width - 6);
        b.setLayoutY(6);

        c.setLayoutX(width - 6);
        c.setLayoutY(4 + height / 2);

        d.setLayoutX(width / 2 + 2);
        d.setLayoutY(height - 6);

        e.setLayoutX(width / 2 - 4);
        e.setLayoutY(4 + height / 2);

        f.setLayoutX(width / 2 - 4);
        f.setLayoutY(6);

        g.setLayoutX(width / 2 + 2);
        g.setLayoutY(height / 2 - 2);

        segmentsHolder.getChildren().add(sign);
        segmentsHolder.getChildren().add(a);
        segmentsHolder.getChildren().add(b);
        segmentsHolder.getChildren().add(c);
        segmentsHolder.getChildren().add(d);
        segmentsHolder.getChildren().add(e);
        segmentsHolder.getChildren().add(f);
        segmentsHolder.getChildren().add(g);

        segments.addAll(sign, a, b, c, d, e, f, g);

        double holderHeight = segmentsHolder.getBoundsInLocal().getHeight();
        double holderWidth = segmentsHolder.getBoundsInLocal().getWidth();

        segmentsHolder.setLayoutX(chip.getPrefWidth() / 2 - holderWidth / 2);
        segmentsHolder.setLayoutY(chip.getPrefHeight() / 2 - holderHeight / 2);

        chip.getChildren().add(segmentsHolder);
    }

    @NotNull
    private static Polygon getVSegment() {
        double width = 8;
        double height = 32;
        Polygon hexagon = new Polygon();
        hexagon.getPoints().addAll(
                width / 2, 0.0,
                width,         height / 5,
                width,         4 * height / 5,
                width / 2, height,
                0.0,           4 * height / 5,
                0.0,           height / 5,
                width / 2,     0.0
        );
        hexagon.setFill(Colors.terminalGreyColor);
        return hexagon;
    }
    @NotNull
    private static Polygon getHSegment() {
        double width = 32;
        double height = 8;
        Polygon hexagon = new Polygon();
        hexagon.getPoints().addAll(
                0.0,       height / 2,
                width / 5,     0.0,
                4 * width / 5, 0.0,
                width,         height / 2,
                4 * width / 5, height,
                width / 5,     height,
                0.0,           height / 2
        );
        hexagon.setFill(Colors.terminalGreyColor);
        return hexagon;
    }

    public static String extractFunctionName(String expression) {
        int equalsIndex = expression.indexOf("=");
        return expression.substring(0, equalsIndex).trim();
    }

    private void listenToChangesInOutputs() {
        outputValues.addListener(outputValuesListener);
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
        inputPins.forEach(pin -> pin.getState().addListener(inputChangesListener));
    }

    private void updateOutputs() {
        if (outputValues == null) return;

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
        if (outputPins != null)
            combined.addAll(outputPins);
        combined.forEach(pin -> pin.setConnectionPoint(calculateConnectionPoint(pin.getConnector())));
    }

    private void addInputPins() {

        for (int i = 0; i < inputs.size(); i++) {
            Circle pin = getCircle(ChipConstants.chipPinRadius, i, inputs);

            var inputPin = new InputChipPin(State.LOW, pin, this);
            inputPin.setConnectionPoint(new Point(pin.getCenterX(), pin.getCenterY()));
            inputPins.add(inputPin);

            chip.getChildren().add(pin);
        }
    }

    private void addOutputPins() {
        for (int i = 0; i < outputs.size(); i++) {
            Circle pin = getCircle(chip.getPrefWidth() - ChipConstants.chipPinRadius, i, outputs);

            var outputPin = new OutputChipPin(State.LOW, pin, this);
            outputPin.setConnectionPoint(new Point(pin.getCenterX(), pin.getCenterY()));
            outputPins.add(outputPin);

            chip.getChildren().add(pin);
        }
    }

    @NotNull
    private Circle getCircle(double chipPinRadius, int i, List<String> inputs) {
        Circle pin = new Circle(ChipConstants.chipPinRadius);
        pin.setCenterX(chipPinRadius);
        pin.setCenterY(getCenterYForPinCircle(i));
        pin.setFill(Colors.terminalGreyColor);
        addToolTipToPin(inputs, i, pin);

        pin.setOnMouseEntered(e -> {
            pin.setCursor(Cursor.HAND);
            pin.setFill(Colors.white);
        });
        pin.setOnMouseExited(e -> {
            pin.setCursor(Cursor.DEFAULT);
            pin.setFill(Colors.terminalGreyColor);
        });

        return pin;
    }

    private double getCenterYForPinCircle(int index) {
        return ChipConstants.chipPinMargin + ChipConstants.chipPinRadius * (2 * index + 1) + ChipConstants.chipPinGap * index;
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

    public void removeAllListeners() {
        if (outputValues != null)
            outputValues.removeListener(outputValuesListener);
        inputPins.forEach(pin -> pin.getState().removeListener(inputChangesListener));
    }

    public String getName() {
        return name;
    }

    public String[] getFunctions() {
        return functions;
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
