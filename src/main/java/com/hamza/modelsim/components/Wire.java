package com.hamza.modelsim.components;

import com.hamza.modelsim.abstractcomponents.Pin;
import com.hamza.modelsim.abstractcomponents.Point;
import com.hamza.modelsim.constants.Colors;
import com.hamza.modelsim.constants.Size;
import com.hamza.modelsim.constants.State;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

import java.util.ArrayList;
import java.util.HashMap;

public class Wire {
    private final ObservableList<Point> points;
    private final Polyline line;
    private final SimpleObjectProperty<State> state = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Point> mousePosition;
    private final Object source;
    private final ChangeListener<State> stateChangeListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends State> observableValue, State state, State t1) {
            if (observableValue.getValue() == State.HIGH)
                line.setStroke(Colors.activeWireColor);
            else
                line.setStroke(Color.WHITE);
        }
    };
    private Object destination;
    private final ChangeListener<Number> updateLineListener = (observableValue, number, t1) -> updateLine();
    private final ListChangeListener<? super Point> pointListChangeListener = (ListChangeListener<Point>) change -> updateLine();
    private final InvalidationListener mousePositionListener = observable -> updateLine();
    private InputPin inputPin;
    private OutputPin outputPin;
    private OutputChipPin inputFromChip;
    private InputChipPin outputToChip;
    private final ChangeListener<State> stateChangeAndPropagateListener = (observableValue, number, t1) -> {
        state.set(t1);
        propagateStateToOutput();
    };

    public Wire(Object source) {
        this.source = source;
        this.destination = null;
        this.points = FXCollections.observableArrayList();
        this.state.set(State.LOW);
        mousePosition = new SimpleObjectProperty<>();
        if (source instanceof Pin) {
            mousePosition.set(
                    new Point(((Pin) source).getConnectionPoint().getX(), ((Pin) source).getConnectionPoint().getY())
            );
        } else {
            mousePosition.set(
                    new Point(((ChipPin) source).getConnectionPoint().getX(), ((ChipPin) source).getConnectionPoint().getY())
            );
        }

        this.line = new Polyline();
        line.setStrokeLineJoin(StrokeLineJoin.ROUND);
        line.setStrokeLineCap(StrokeLineCap.ROUND);
        line.setStrokeWidth(Size.WIRE_STROKE_SIZE);
        line.setStroke(Color.WHITE);


        handleSourceMovement();
        handleStateChange();
        handlePointsChange();
        handleMouseMovement();
    }
    public Wire(Object source, Wire clickedWire, Point position) {
        this.source = source;
        this.destination = null;
        this.points = FXCollections.observableArrayList();
        this.state.set(State.LOW);
        mousePosition = new SimpleObjectProperty<>();
        if (source instanceof Pin) {
            mousePosition.set(
                new Point(((Pin) source).getConnectionPoint().getX(), ((Pin) source).getConnectionPoint().getY())
            );
        } else {
            mousePosition.set(
                new Point(((ChipPin) source).getConnectionPoint().getX(), ((ChipPin) source).getConnectionPoint().getY())
            );
        }

        this.line = new Polyline();
        line.setStrokeLineJoin(StrokeLineJoin.ROUND);
        line.setStrokeLineCap(StrokeLineCap.ROUND);
        line.setStrokeWidth(Size.WIRE_STROKE_SIZE);
        line.setStroke(Color.WHITE);

//        addPointsFromPreviousWire(clickedWire, position);

        handleSourceMovement();
        handleStateChange();
        handlePointsChange();
        handleMouseMovement();
    }

//    private void addPointsFromPreviousWire(Wire clickedWire, Point position) {
//        System.out.println("Reached to add previous points.");
//        HashMap<Point, Double> pointsWithDistance = new HashMap<>();
//
//        Point wireStart;
//        if(clickedWire.getSourcePin() instanceof Pin)
//            wireStart = ((Pin) clickedWire.getSourcePin()).getConnectionPoint();
//        else
//            wireStart = ((ChipPin) clickedWire.getSourcePin()).getConnectionPoint();
//
//        for(int i = 0; i < clickedWire.getPoints().size(); i++) {
//            pointsWithDistance.put(clickedWire.getPoints().get(i), getDistanceFrom(wireStart, clickedWire.getPoints(), i));
//        }
//
//        System.out.println(pointsWithDistance.isEmpty());
//        System.out.println(pointsWithDistance.keySet());
//        System.out.println(pointsWithDistance.values());
//
//
//    }

//    private double getDistanceFrom(Point start, ObservableList<Point> points, int index) {
//        double totalDistance = 0;
//        points.add(0, start);
//
//        for(int i = 1; i <= index; i++) {
//            totalDistance += distance(points.get(i), points.get(i - 1));
//        }
//
//        return totalDistance;
//    }
    private double distance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));
    }

    private void handleSourceMovement() {
        updateLineForChangesIn(source);
    }

    private void handleDestinationPinMovement() {
        updateLineForChangesIn(destination);
    }

    private void updateLineForChangesIn(Object pin) {
        if (pin instanceof ChipPin) {
            ((ChipPin) pin).getParent().getPane().layoutXProperty().addListener(updateLineListener);
            ((ChipPin) pin).getParent().getPane().layoutYProperty().addListener(updateLineListener);
        } else {
            ((Pin) pin).getPane()
                    .layoutYProperty()
                    .addListener(updateLineListener);
        }
    }

    private void updateLine() {
        line.getPoints().clear();

        Point sourceLocation;
        if (source instanceof Pin)
            sourceLocation = ((Pin) source).getConnectionPoint();
        else
            sourceLocation = ((ChipPin) source).getConnectionPoint();

        line.getPoints().addAll(sourceLocation.getX(), sourceLocation.getY());

        for (Point p : points) {
            line.getPoints().addAll(p.getX(), p.getY());
        }

        if (destination == null) {
            line.getPoints().addAll(mousePosition.get().getX(), mousePosition.get().getY());
        } else {
            Point destinationLocation;
            if (destination instanceof Pin)
                destinationLocation = ((Pin) destination).getConnectionPoint();
            else
                destinationLocation = ((ChipPin) destination).getConnectionPoint();

            line.getPoints().addAll(destinationLocation.getX(), destinationLocation.getY());
        }
    }

    private void handleStateChange() {
        state.addListener(stateChangeListener);
    }

    private void handlePointsChange() {
        points.addListener(pointListChangeListener);
    }

    private void handleMouseMovement() {
        mousePosition.addListener(mousePositionListener);
    }

    public void draw(Canvas canvas) {
        canvas.getDrawable().getChildren().add(line);
        line.toBack();
    }

    public void setDestination(Object destination) {
        this.destination = destination;
        handleDestinationPinMovement();
        updateLine();

        /*
         * Doing things after completing the wire.
         */
        setInputAndOutputPins();
        listenForStateChanges();
        updateState();
        propagateStateToOutput();
    }

    private void updateState() {
        if (inputFromChip != null)
            state.set(inputFromChip.getState().get());
        else
            state.set(inputPin.getState().get());

    }

    private void setInputAndOutputPins() {
        if (source instanceof InputPin) {
            inputPin = (InputPin) source;
        } else if (source instanceof OutputPin) {
            outputPin = (OutputPin) source;
        } else if (source instanceof OutputChipPin) {
            inputFromChip = (OutputChipPin) source;
        } else {
            outputToChip = (InputChipPin) source;
        }
        if (destination instanceof InputPin) {
            inputPin = (InputPin) destination;
        } else if (destination instanceof OutputPin) {
            outputPin = (OutputPin) destination;
        } else if (destination instanceof InputChipPin) {
            outputToChip = (InputChipPin) destination;
        } else {
            inputFromChip = (OutputChipPin) destination;
        }

    }

    private void listenForStateChanges() {
        if (inputPin != null)
            inputPin.getState().addListener(stateChangeAndPropagateListener);
        else
            inputFromChip.getState().addListener(stateChangeAndPropagateListener);
    }

    private void propagateStateToOutput() {
        if (outputPin != null)
            outputPin.setState(state.get());
        else
            outputToChip.setState(state.get());
    }
    private void resetState() {
        if (outputPin != null)
            outputPin.setState(State.LOW);
        else
            outputToChip.setState(State.LOW);
    }

    public void setMousePosition(Point p) {
        Point lastPoint;
        if (points.size() == 0) {
            if (source instanceof Pin)
                lastPoint = ((Pin) source).getConnectionPoint();
            else
                lastPoint = ((ChipPin) source).getConnectionPoint();
        } else
            lastPoint = points.get(points.size() - 1);

        double angle = getAngle(p, lastPoint);
        double hypo = getHypo(p, lastPoint);

        p = new Point(lastPoint.getX() + hypo * Math.cos(angle), lastPoint.getY() - hypo * Math.sin(angle));
        mousePosition.set(p);
    }

    public Point getMousePosition() {
        return mousePosition.get();
    }

    public void setMousePosition(double x, double y) {
        mousePosition.set(new Point(x, y));
    }

    private double getAngle(Point p1, Point p2) {
        double angle = Math.atan2(Math.abs(p2.getY() - p1.getY()), Math.abs(p2.getX() - p1.getX()));

        double diffX = p1.getX() - p2.getX();
        double diffY = p2.getY() - p1.getY();

        if (diffX < 0 && diffY < 0) {
            angle = Math.PI + angle;
        } else {
            if (diffX < 0) angle = Math.PI - angle;
            if (diffY < 0) angle = 2 * Math.PI - angle;
        }
        return angle;
    }

    private double getHypo(Point p1, Point p2) {
        double perp = Math.abs(p2.getY() - p1.getY());
        double base = Math.abs(p2.getX() - p1.getX());
        return Math.sqrt(perp * perp + base * base) - Size.MOUSE_MARGIN;
    }

    public Object getSourcePin() {
        return source;
    }

    public void removeListeners() {
        resetState();

        if (source instanceof ChipPin) {
            ((ChipPin) source).getParent().getPane().layoutXProperty().removeListener(updateLineListener);
            ((ChipPin) source).getParent().getPane().layoutYProperty().removeListener(updateLineListener);
        } else {
            ((Pin) source).getPane()
                    .layoutYProperty()
                    .removeListener(updateLineListener);
        }

        state.removeListener(stateChangeListener);
        points.removeListener(pointListChangeListener);
        mousePosition.removeListener(mousePositionListener);

        if (inputPin != null)
            inputPin.getState().removeListener(stateChangeAndPropagateListener);
        else
            inputFromChip.getState().removeListener(stateChangeAndPropagateListener);
    }

    public InputPin getInputPin() {
        return inputPin;
    }

    public OutputPin getOutputPin() {
        return outputPin;
    }

    public OutputChipPin getInputFromChip() {
        return inputFromChip;
    }

    public InputChipPin getOutputToChip() {
        return outputToChip;
    }

    public void addPoint(Point p) {
        points.add(p);
    }

    public Polyline getLine() {
        return line;
    }

    public ObservableList<Point> getPoints() {
        return points;
    }
}
