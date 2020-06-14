/*
 * Copyright 2020 Cosinus Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cosinus.swing.gtk;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JToolBar;

/**
 *
 */
public class GTKSeparator extends JToolBar.Separator {
    private JComponent parent;

    /**
     * Creates a new instance of GTKSeparator
     */
    public GTKSeparator(JComponent parent) {
        this.parent = parent;
    }

    @Override
    public void paint(Graphics g) {
        //super.paint(g);
        int height = parent.getHeight() - parent.getInsets().top - parent.getInsets().bottom;
        setSeparatorSize(new Dimension(10, height));

        int margin = 3;
        g.setColor(GTKColors.getVertical1());
        g.drawLine(4, margin, 4, height - margin);
        g.setColor(GTKColors.getVertical2());
        g.drawLine(5, margin, 5, height - margin);
    }
}
