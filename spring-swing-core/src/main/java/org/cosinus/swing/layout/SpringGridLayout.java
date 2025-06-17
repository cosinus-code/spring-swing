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

package org.cosinus.swing.layout;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static java.util.stream.IntStream.range;
import static javax.swing.Spring.constant;
import static javax.swing.Spring.sum;

/**
 * Extension of {@link SpringLayout} which allows to pack components in a grid format.
 * <p>
 * Calling {@link SpringGridLayout#pack()} will arrange the component
 * according to the parameters provided to the constructor.
 */
public class SpringGridLayout extends SpringLayout {

    private final Container target;

    private final int rows;
    private final int cols;

    private final int initialX;
    private final int initialY;

    private final int xPad;
    private final int yPad;

    private final SpringDimension[] columnDimensions;
    private final SpringDimension[] rowDimensions;

    private Spring totalWidth;
    private Spring totalHeight;

    private int rowHeight;

    private Map<Integer, Spring> columnWidth;

    public SpringGridLayout(Container target,
                            int rows, int cols,
                            int initialX, int initialY,
                            int xPad, int yPad) {
        this.target = target;
        this.rows = rows;
        this.cols = cols;
        this.initialX = initialX;
        this.initialY = initialY;
        this.xPad = xPad;
        this.yPad = yPad;

        this.columnDimensions = new SpringDimension[cols];
        this.rowDimensions = new SpringDimension[rows];
    }

//    public void setColumnWidth(int column, int width) {
//        columnWidth.put(column, new WidthSpring(width));
//    }


    public void setRowHeight(int rowHeight) {
        this.rowHeight = rowHeight;
    }

    /**
     * Pack the components in a grid.
     */
    public void pack() {
        computeColumnsConstraints();
        computeRowsConstraints();
        updateCellConstraints();
        updateParentConstraints();
    }

    /**
     * Compute the columns constraints.
     */
    protected void computeColumnsConstraints() {
        totalWidth = constant(initialX);
        range(0, cols).forEach(col -> {
            Spring width = computeColumnWidth(col);
            columnDimensions[col] = new SpringDimension(totalWidth, width);
            totalWidth = sum(totalWidth, sum(width, constant(xPad)));
        });
    }

    /**
     * Compute the rows constraints.
     */
    protected void computeRowsConstraints() {
        totalHeight = constant(initialY);
        range(0, rows).forEach(row -> {
            Spring height = computeRowHeight(row);
            rowDimensions[row] = new SpringDimension(totalHeight, height);
            totalHeight = sum(totalHeight, sum(height, constant(yPad)));
        });
    }

    /**
     * Compute the column width.
     *
     * @param col the column index
     * @return the computed column width
     */
    protected Spring computeColumnWidth(int col) {
        return range(0, rows)
            .mapToObj(row -> getCellConstraints(row, col))
            .map(Constraints::getWidth)
            .reduce(constant(0), Spring::max);
    }

    /**
     * Compute the row height.
     *
     * @param row the row
     * @return the computed row height
     */
    private Spring computeRowHeight(int row) {
        return range(0, cols)
            .mapToObj(col -> getCellConstraints(row, col))
            .map(Constraints::getHeight)
            .reduce(constant(0), Spring::max);
    }

    /**
     * Update the grid cells constraints
     */
    private void updateCellConstraints() {
        range(0, cols).forEach(
            col -> range(0, rows).forEach(
                row -> {
                    Constraints cellConstraints = getCellConstraints(row, col);

                    cellConstraints.setX(columnDimensions[col].getStart());
                    cellConstraints.setY(rowDimensions[row].getStart());
                    cellConstraints.setWidth(columnDimensions[col].getSize());
                    cellConstraints.setHeight(rowDimensions[row].getSize());
                }));
    }

    /**
     * Update the parent constraints
     */
    private void updateParentConstraints() {
        Constraints parentConstraints = getConstraints(target);
        parentConstraints.setConstraint(EAST, totalWidth);
        parentConstraints.setConstraint(SOUTH, totalHeight);
    }

    /**
     * Get constraints for a grid cell.
     *
     * @param row the row index
     * @param col the column index
     * @return the cell constraints
     */
    private Constraints getCellConstraints(int row, int col) {
        return ofNullable(target.getComponent(row * cols + col))
            .map(this::getConstraints)
            .orElseThrow(IndexOutOfBoundsException::new);
    }
}
