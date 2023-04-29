package com.hamza.modelsim;

import com.hamza.modelsim.abstractcomponents.Point;
import com.hamza.modelsim.components.Wire;
import com.hamza.modelsim.components.*;
import com.hamza.modelsim.constants.Colors;
import com.hamza.modelsim.constants.TerminalConstants;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {
    public static ObservableList<InputPin> inputPins;
    public static ObservableList<OutputPin> outputPins;
    public static ObservableList<Wire> wires;
    public static Scene scene;
    public static Canvas canvas;

    private Point mousePosition;
    private Point firstClick;
    Polyline l;

    @Override
    public void start(Stage mainStage) {
        mainStage.setTitle("Model simulator");
        mainStage.setFullScreen(true);
        mainStage.setFullScreenExitHint("");
        mainStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

        inputPins = FXCollections.observableArrayList();
        outputPins = FXCollections.observableArrayList();
        wires = FXCollections.observableArrayList();
        mousePosition = new Point();
        firstClick = null;
        l = new Polyline();
        l.setStroke(Color.WHITE);
        l.setStrokeWidth(3);
        l.setStrokeLineCap(StrokeLineCap.ROUND);
        l.setStrokeLineJoin(StrokeLineJoin.ROUND);

        List<Point> points = new ArrayList<>();

        VBox root = new VBox();
        root.setBackground(Background.fill(Colors.backgroundColor));
        root.setFillWidth(true);
        root.setSpacing(0);

        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getClassLoader().getResource("main.css").toExternalForm());

        canvas = new Canvas();
        MenuBar menuBar = new MenuBar(scene);

        scene.setOnMouseMoved(e -> {
            mousePosition.setX(e.getSceneX());
            mousePosition.setY(e.getSceneY());

            if (firstClick != null) {
                l.getPoints().clear();
                l.getPoints().addAll(firstClick.getX(), firstClick.getY());

                for(var p : points) {
                    l.getPoints().add(p.getX());
                    l.getPoints().add(p.getY());
                }

                l.getPoints().addAll(mousePosition.getX(), mousePosition.getY());
            } else {
                l.getPoints().clear();
            }
        });
        canvas.add(l);

        scene.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                if (firstClick == null) {
                    firstClick = new Point(mouseEvent.getSceneX(), mouseEvent.getSceneY());
                } else {
                    points.add(new Point(mouseEvent.getSceneX(), mouseEvent.getSceneY()));
                }
            } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                firstClick = null;
                points.clear();
            }
        });

        root.getChildren().add(canvas.getDrawable());
        root.getChildren().add(menuBar.getDrawable());

        double xPosition = 0;
        double yPosition = 8;
        double paddingY = yPosition * 2;
        double borderWidth = 20;
        double borderHeight = Screen.getPrimary().getBounds().getHeight() - paddingY - menuBar.getHeight();

        Rectangle inputTerminalsBase = makeRectangle(
                xPosition, yPosition,
                borderWidth, borderHeight, Colors.bordersColor
        );
        Rectangle outputTerminalsBase = makeRectangle(
                Screen.getPrimary().getBounds().getWidth() - xPosition - borderWidth,
                yPosition, borderWidth, borderHeight, Colors.bordersColor
        );

        canvas.add(inputTerminalsBase);
        canvas.add(outputTerminalsBase);

        // INPUT terminals
        // TODO: You're able to fix this SOS, do it then...
        // displayTestTerminal(canvas, inputTerminalsBase);

        inputTerminalsBase.setOnMouseClicked(addNewInputTerminal(canvas));

        for(var pin : inputPins)
            pin.draw(canvas.getDrawable());

        // OUTPUT terminals
        outputTerminalsBase.setOnMouseClicked(addNewOutputTerminal(canvas));
        for(var pin : outputPins)
            pin.draw(canvas.getDrawable());


        scene.setFill(Colors.backgroundColor);
        mainStage.setScene(scene);
        mainStage.show();
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

    public static void main(String[] args) { launch(args); }
}