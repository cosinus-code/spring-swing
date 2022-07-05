package org.cosinus.swing.listener.dragdrop;

public interface DragAndDropListener {

    default void start(DragAndDrop dragAndDrop) {
    }

    default void drag(DragAndDrop dragAndDrop) {
    }

    default void drop(DragAndDrop dragAndDrop) {
    }
}
