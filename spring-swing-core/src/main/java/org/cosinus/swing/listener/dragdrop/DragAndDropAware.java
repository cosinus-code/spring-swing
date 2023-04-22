package org.cosinus.swing.listener.dragdrop;

import org.cosinus.swing.listener.SimpleMouseListener;
import org.cosinus.swing.listener.SimpleMouseMotionListener;

import java.awt.*;
import java.awt.event.MouseEvent;

import static javax.swing.SwingUtilities.isLeftMouseButton;

public interface DragAndDropAware {

    default void addDragAndDropListener(DragAndDropListener listener) {
        DragAndDrop dragAndDrop = getDragAndDrop();
        if (!(this instanceof Component component)) {
            throw new IllegalStateException("Cannot add drag and drop listener for a non awt Component");
        }
        component.addMouseListener(new SimpleMouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragAndDrop.start(e.getPoint());
                listener.start(dragAndDrop);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isLeftMouseButton(e)) {
                    if (dragAndDrop.isDragging()) {
                        dragAndDrop.drag(e.getPoint());
                        listener.drop(dragAndDrop);
                        dragAndDrop.drop();
                    }
                }
            }
        });

        component.addMouseMotionListener(new SimpleMouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isLeftMouseButton(e)) {
                    dragAndDrop.drag(e.getPoint());
                    listener.drag(dragAndDrop);
                }
            }
        });
    }

    default DragAndDrop getDragAndDrop() {
        return new DragAndDrop();
    }
}
