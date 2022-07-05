package org.cosinus.swing.listener.dragdrop;

import java.awt.*;

import static java.util.Optional.ofNullable;

public class DragAndDrop {

    private Point dragStartPoint;
    private Point draggingPoint;

    public void start(Point point) {
        dragStartPoint = point;
    }

    public void drag(Point point) {
        draggingPoint = point;
    }

    public double getX() {
        return ofNullable(dragStartPoint)
            .map(Point::getX)
            .flatMap(startX -> ofNullable(draggingPoint)
                .map(Point::getX)
                .map(endX -> endX - startX))
            .orElse(0D);
    }

    public double getY() {
        return ofNullable(dragStartPoint)
            .map(Point::getY)
            .flatMap(startY -> ofNullable(draggingPoint)
                .map(Point::getY)
                .map(endY -> endY - startY))
            .orElse(0D);
    }

    public boolean isDragging() {
        return dragStartPoint != null && draggingPoint != null;
    }


    public void drop() {
        dragStartPoint = null;
        draggingPoint = null;
    }
}
