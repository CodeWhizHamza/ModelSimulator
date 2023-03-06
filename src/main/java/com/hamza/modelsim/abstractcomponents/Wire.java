package com.hamza.modelsim.abstractcomponents;

import java.util.ArrayList;

public class Wire {
    private Point start;
    private Point end;
    private ArrayList<Point> points;

    public Wire() {
        start = null;
        points = null;
        end = null;
    }

    public void addPoint(Point point) {
        points.add(point);
    }

    public ArrayList<Point> getPoints() {
        return points;
    }
    public Point getStart() {
        return start;
    }
    public void setStart(Point start) {
        this.start = start;
    }
    public Point getEnd() {
        return end;
    }
    public void setEnd(Point end) {
        this.end = end;
    }
}
