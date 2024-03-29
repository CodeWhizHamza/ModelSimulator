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
                line.setStroke(Colors.highWireColor);
            else
                line.setStroke(Colors.lowWireColor);
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
        line.setStroke(Color.WHEAT);

        handleSourceMovement();
        handleStateChange();
        handlePointsChange();
        handleMouseMovement();

        this.state.set(State.LOW);
    }

    public Wire(Object source, Wire clickedWire, Point position) {
        this.source = source;
        this.destination = null;
        this.points = FXCollections.observableArrayList();
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
        line.setStroke(Color.WHEAT);

        addPointsFromPreviousWire(clickedWire, position);

        handleSourceMovement();
        handleStateChange();
        handlePointsChange();
        handleMouseMovement();

        this.state.set(State.LOW);
    }

    private void addPointsFromPreviousWire(Wire clickedWire, Point position) {
        Point wireStart = null, wireEnd = null;
        if (clickedWire.getInputPin() != null)
            wireStart = clickedWire.getInputPin().getConnectionPoint();
        else if (clickedWire.getInputFromChip() != null)
            wireStart = clickedWire.getInputFromChip().getConnectionPoint();

        if (clickedWire.getOutputPin() != null)
            wireEnd = clickedWire.getOutputPin().getConnectionPoint();
        else if (clickedWire.getOutputToChip() != null)
            wireEnd = clickedWire.getOutputToChip().getConnectionPoint();

        ObservableList<Point> points = FXCollections.observableArrayList();
        for (var point : clickedWire.getPoints())
            points.add(new Point(point.getX(), point.getY()));

        points.add(0, wireStart);
        points.add(wireEnd);

        for (int i = 0; i < points.size() - 1; i++) {
            double slope = getSlope(points.get(i + 1), points.get(i));
            double distanceBetweenLineAndPoint = Math.abs(getDistanceBetweenPointAndLine(points.get(i), position, -slope));

            if (distanceBetweenLineAndPoint <= Size.WIRE_STROKE_SIZE) {
                if (i != 0)
                    addPoint(points.get(i));
                addPoint(position);
                break;
            } else {
                if (i == 0) continue;
                addPoint(points.get(i));
            }
        }
    }

    private double getDistanceBetweenPointAndLine(Point pointForLine, Point testPoint, double slope) {
        return testPoint.getY() - pointForLine.getY() - slope * (testPoint.getX() - pointForLine.getX());
    }

    private double getSlope(Point point1, Point point2) {
        // the lower the point, the higher value of Y
        return (point1.getY() - point2.getY()) / (point2.getX() - point1.getX());
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
