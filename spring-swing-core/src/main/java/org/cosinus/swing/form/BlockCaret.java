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

package org.cosinus.swing.form;

import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.geom.Rectangle2D;

import static java.awt.RenderingHints.*;
import static org.cosinus.swing.color.Colors.inverseColor;
import static org.cosinus.swing.color.SystemColor.EDITOR_PANE_CARET_FOREGROUND;
import static org.cosinus.swing.color.SystemColor.TEXT_PANE_SELECTION_BACKGROUND;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class BlockCaret extends DefaultCaret {

    protected final TextComponent textComponent;

    @Autowired
    private ApplicationUIHandler uiHandler;

    public BlockCaret(final TextComponent textComponent) {
        injectContext(this);
        this.textComponent = textComponent;
    }

    @Override
    public void paint(final Graphics graphics) {
        textComponent.getRectangleAtPosition(getDot())
            .ifPresent(caretRectangle -> {
                Graphics2D g2d = (Graphics2D) graphics.create();
                g2d.setColor(getCaretBackground());
                g2d.fill(new Rectangle2D.Double(
                    caretRectangle.getX(),
                    caretRectangle.getY(),
                    textComponent.getCharWidthAtPosition(getDot()),
                    caretRectangle.getHeight()
                ));
                g2d.setColor(inverseColor(getCaretForeground()));
                g2d.setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
                int fontHeight = textComponent.getFontMetrics().getAscent();
                textComponent.getCharAtPosition(getDot()).ifPresent(character ->
                    g2d.drawString("" + character,
                        (int) caretRectangle.getX(),
                        (int) (caretRectangle.getY() + fontHeight)
                    )
                );
            });
    }

    @Override
    protected synchronized void damage(final Rectangle rectangle) {
        textComponent.getRectangleAtPosition(getDot())
            .ifPresent(caretRectangle -> {
                x = (int) caretRectangle.getX();
                y = (int) caretRectangle.getY();
                width = (int) Math.ceil(textComponent.getCharWidthAtPosition(getDot()));
                height = (int) caretRectangle.getHeight();
                repaint();
            });
    }

    public Color getCaretBackground() {
        return uiHandler.getColor(TEXT_PANE_SELECTION_BACKGROUND);
    }

    public Color getCaretForeground() {
        return uiHandler.getColor(EDITOR_PANE_CARET_FOREGROUND);
    }
}
