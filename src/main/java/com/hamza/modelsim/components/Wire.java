package com.hamza.modelsim.components;

import com.hamza.modelsim.abstractcomponents.Pin;
import com.hamza.modelsim.abstractcomponents.Point;
import com.hamza.modelsim.constants.Colors;
import com.hamza.modelsim.constants.Size;
import com.hamza.modelsim.constants.State;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

public class Wire {
    private final Pin source;
    private final ObservableList<Point> points;
    private final Polyline line;
    private final SimpleObjectProperty<State> state = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Point> mousePosition;
    private Pin destination;

    private InputPin inputPin;
    private OutputPin outputPin;

    public Wire(Pin source) {
        this.source = source;
        this.destination = null;
        this.points = FXCollections.observableArrayList();
        this.state.set(State.LOW);
        mousePosition = new SimpleObjectProperty<>();
        mousePosition.set(
                new Point(source.getConnectionPoint().getX(), source.getConnectionPoint().getY())
        );

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

    private void handleSourceMovement() {
        source.getPane()
                .layoutYProperty()
                .addListener((observableValue, number, t1) -> updateLine());
    }

    private void handleDestinationPinMovement() {
        destination.getPane()
                .layoutYProperty()
                .addListener((observableValue, number, t1) -> updateLine());
    }

    private void updateLine() {
        line.getPoints().clear();

        Point sourceLocation = source.getConnectionPoint();
        line.getPoints().addAll(sourceLocation.getX(), sourceLocation.getY());

        for (Point p : points) {
            line.getPoints().addAll(p.getX(), p.getY());
        }

        if (destination == null) {
            line.getPoints().addAll(mousePosition.get().getX(), mousePosition.get().getY());
        } else {
            Point destinationLocation = destination.getConnectionPoint();
            line.getPoints().addAll(destinationLocation.getX(), destinationLocation.getY());
        }
    }

    private void handleStateChange() {
        state.addListener((observableValue, number, t1) -> {
            if (observableValue.getValue() == State.HIGH)
                line.setStroke(Colors.activeWireColor);
            else
                line.setStroke(Color.WHITE);
        });
    }

    private void handlePointsChange() {
        points.addListener((ListChangeListener<? super Point>) change -> updateLine());
    }

    private void handleMouseMovement() {
        mousePosition.addListener(change -> updateLine());
    }

    public void draw(Canvas canvas) {
        canvas.getDrawable().getChildren().add(line);
    }

    public void setDestination(Pin destination) {
        this.destination = destination;
        handleDestinationPinMovement();
        updateLine();

        /*
          Doing things after completing the wire.
         */
        setInputAndOutputPins();
        listenForStateChanges();
    }
    private void setInputAndOutputPins() {
        if (source instanceof InputPin) {
            inputPin = (InputPin) source;
            outputPin = (OutputPin) destination;
        } else {
            inputPin = (InputPin) destination;
            outputPin = (OutputPin) source;
        }
    }
    private void listenForStateChanges() {
        inputPin.getState().addListener((observableValue, number, t1) -> {
            state.set(t1);
            propagateStateToOutput();
        });
    }

    private void propagateStateToOutput() {
        outputPin.setState(state.get());
    }

    public void setMousePosition(Point p) {
        Point lastPoint;
        if (points.size() == 0)
            lastPoint = source.getConnectionPoint();
        else
            lastPoint = points.get(points.size() - 1);

        double angle = getAngle(p, lastPoint);
        double hypo = getHypo(p, lastPoint);

        p = new Point(lastPoint.getX() + hypo * Math.cos(angle), lastPoint.getY() - hypo * Math.sin(angle));
        mousePosition.set(p);
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
    public Pin getSourcePin() {
        return source;
    }

    public void addPoint(Point p) {
        points.add(p);
    }
}
