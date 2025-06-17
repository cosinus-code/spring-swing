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
