package me.illusion.cosmos.utilities.menu.selection;

import java.util.ArrayList;
import java.util.List;
import me.illusion.cosmos.utilities.menu.math.Point;

public class HollowRectangularSelection implements Selection {

    private final Point firstPoint;
    private final Point secondPoint;
    private int thickness = 1;

    public HollowRectangularSelection(Point firstPoint, Point secondPoint) {
        this.firstPoint = firstPoint;
        this.secondPoint = secondPoint;
    }

    public HollowRectangularSelection(Point firstPoint, Point secondPoint, int thickness) {
        this.firstPoint = firstPoint;
        this.secondPoint = secondPoint;
        this.thickness = thickness;
    }

    public int getThickness() {
        return thickness;
    }

    public void setThickness(int thickness) {
        this.thickness = thickness;
    }


    @Override
    public List<Integer> getSlots() {
        List<Integer> slots = new ArrayList<>();

        for (int thickness = 0; thickness < this.thickness; thickness++) {
            Point firstPoint = new Point(this.firstPoint.getX() + thickness, this.firstPoint.getY() + thickness);
            Point secondPoint = new Point(this.secondPoint.getX() - thickness, this.secondPoint.getY() - thickness);

            slots.addAll(getSlots(firstPoint, secondPoint));
        }

        return slots;
    }

    private int getSlot(int x, int y) {
        return y * 9 + x;
    }

    private List<Integer> getSlots(Point firstPoint, Point secondPoint) {
        List<Integer> slots = new ArrayList<>();

        for (int x = firstPoint.getX(); x <= secondPoint.getX(); x++) {
            slots.add(getSlot(x, firstPoint.getY()));
            slots.add(getSlot(x, secondPoint.getY()));
        }

        for (int y = firstPoint.getY(); y <= secondPoint.getY(); y++) {
            slots.add(getSlot(firstPoint.getX(), y));
            slots.add(getSlot(secondPoint.getX(), y));
        }

        return slots;
    }
}
