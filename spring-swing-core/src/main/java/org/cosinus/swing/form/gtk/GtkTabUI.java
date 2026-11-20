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

package org.cosinus.swing.form.gtk;

import lombok.Getter;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.cosinus.swing.ui.UIAlign;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;

import static org.cosinus.swing.color.SystemColor.MENU_SELECTION_BACKGROUND;
import static org.cosinus.swing.color.SystemColor.PANEL_BACKGROUND;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class GtkTabUI extends BasicTabbedPaneUI {

    @Autowired
    private ApplicationUIHandler uiHandler;

    private final Color accentColor;
    private final Color backGrounfColor;

    @Getter
    private final UIAlign tabAreaAlignment;

    public GtkTabUI() {
        this(UIAlign.LEFT);
    }

    public GtkTabUI(UIAlign tabAreaAlignment) {
        injectContext(this);
        this.tabAreaAlignment = tabAreaAlignment != null ? tabAreaAlignment : UIAlign.LEFT;
        accentColor = uiHandler.getColor(MENU_SELECTION_BACKGROUND);
        backGrounfColor = uiHandler.getColor(PANEL_BACKGROUND);
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        tabInsets = new Insets(6, 14, 6, 14);
        contentBorderInsets = new Insets(1, 0, 0, 0);
        tabAreaInsets = new Insets(4, 4, 0, 4);
    }

    @Override
    protected LayoutManager createLayoutManager() {
        // the scroll layout of BasicTabbedPaneUI is private and cannot be extended
        return tabPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT ?
            super.createLayoutManager() :
            new AlignedTabbedPaneLayout();
    }

    @Override
    protected void paintTabBackground(Graphics g, int placement, int idx,
                                      int x, int y, int w, int h, boolean selected) {
        g.setColor(selected ? backGrounfColor.brighter() : backGrounfColor);
        g.fillRect(x, y, w, h);
    }

    @Override
    protected void paintTabBorder(Graphics g, int placement, int idx,
                                  int x, int y, int w, int h, boolean selected) {
        if (selected) {
            g.setColor(accentColor);
            g.fillRect(x, y + h - 3, w, 3);
        }
    }

    @Override
    protected void paintFocusIndicator(Graphics g, int placement, Rectangle[] r,
                                       int idx, Rectangle icon, Rectangle text, boolean selected) {
    }

    @Override
    protected void paintContentBorder(Graphics g, int placement, int selIdx) {
        g.setColor(backGrounfColor.darker());
        Rectangle rectangle = tabPane.getBounds();
        int height = calculateTabAreaHeight(placement, runCount, maxTabHeight);
        g.drawLine(0, height, rectangle.width, height);
    }

    /**
     * Tab layout which shifts the tab runs according to the tab area alignment.
     */
    private class AlignedTabbedPaneLayout extends TabbedPaneLayout {

        @Override
        protected void calculateTabRects(int tabPlacement, int tabCount) {
            super.calculateTabRects(tabPlacement, tabCount);
            if (tabCount == 0 || tabAreaAlignment == UIAlign.LEFT) {
                return;
            }

            boolean horizontal = tabPlacement == TOP || tabPlacement == BOTTOM;
            Insets insets = tabPane.getInsets();
            Insets areaInsets = getTabAreaInsets(tabPlacement);
            int available = horizontal ?
                tabPane.getWidth() - insets.left - insets.right - areaInsets.left - areaInsets.right :
                tabPane.getHeight() - insets.top - insets.bottom - areaInsets.top - areaInsets.bottom;

            for (int run = 0; run < runCount; run++) {
                int first = tabRuns[run];
                int last = lastTabInRun(tabCount, run);
                Rectangle firstRect = rects[first];
                Rectangle lastRect = rects[last];
                int used = horizontal ?
                    lastRect.x + lastRect.width - firstRect.x :
                    lastRect.y + lastRect.height - firstRect.y;

                int free = available - used;
                if (free <= 0) {
                    continue;
                }

                int shift = tabAreaAlignment == UIAlign.CENTER ? free / 2 : free;
                for (int i = first; i <= last; i++) {
                    if (horizontal) {
                        rects[i].x += shift;
                    } else {
                        rects[i].y += shift;
                    }
                }
            }
        }
    }
}
