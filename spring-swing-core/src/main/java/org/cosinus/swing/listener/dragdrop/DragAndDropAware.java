/*
 * Copyright 2025 Cosinus Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

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
