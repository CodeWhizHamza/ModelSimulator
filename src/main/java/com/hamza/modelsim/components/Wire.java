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
    private Pin source;
    private Pin destination;
    private ObservableList<Point> points;
    private Polyline line;
    private IntegerProperty state;
    private SimpleObjectProperty<Point> mousePosition;

    public Wire(Pin source) {
        this.source = source;
        this.destination = null;
        this.points = FXCollections.observableArrayList();
        this.state = new SimpleIntegerProperty(State.LOW);
        mousePosition = new SimpleObjectProperty<>();
        mousePosition.set(
            new Point(source.getConnectionPoint().getX(), source.getConnectionPoint().getY())
        );

        this.line = new Polyline();
        line.setStrokeLineJoin(StrokeLineJoin.ROUND);
        line.setStrokeLineCap(StrokeLineCap.ROUND);
        line.setStrokeWidth(Size.WIRE_STROKE_SIZE);
        line.setStroke(Color.WHITE);


        handlePinMovement();
        handleStateChange();
        handlePointsChange();
        handleMouseMovement();
    }
    private void handlePinMovement() {
        source.getPane()
                .layoutYProperty()
                .addListener((observableValue, number, t1) -> updateLine());

        if (destination == null) return;

        destination.getPane()
                .layoutYProperty()
                .addListener((observableValue, number, t1) -> updateLine());
    }
    private void updateLine() {
        line.getPoints().clear();

        Point sourceLocation = source.getConnectionPoint();
        line.getPoints().addAll(sourceLocation.getX(), sourceLocation.getY());

        for(Point p : points) {
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
            if (observableValue.getValue().intValue() == State.HIGH)
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
    }
    public void setMousePosition(Point p) {
        mousePosition.set(p);
    }
    public void addPoint(Point p) {
        points.add(p);
    }
}
