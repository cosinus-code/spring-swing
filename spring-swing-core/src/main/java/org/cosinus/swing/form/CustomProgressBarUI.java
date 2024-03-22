package org.cosinus.swing.form;

import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static org.cosinus.swing.color.SystemColor.MENU_SELECTION_BACKGROUND;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Rounded custom progress bar ui
 */
public class CustomProgressBarUI extends BasicProgressBarUI {

    @Autowired
    private ApplicationUIHandler uiHandler;

    public CustomProgressBarUI() {
        injectContext(this);
    }

    @Override
    protected void paintIndeterminate(Graphics g, JComponent c) {
        Rectangle rectangle = new Rectangle();
        getBox(rectangle);
        paintProgress(g, c, rectangle);
    }

    @Override
    protected void paintDeterminate(Graphics g, JComponent c)
    {
        Insets b = progressBar.getInsets(); // area for border
        int barRectWidth = progressBar.getWidth() - (b.right + b.left);
        int barRectHeight = progressBar.getHeight() - (b.top + b.bottom);

        if (barRectWidth <= 0 || barRectHeight <= 0) {
            return;
        }

        int amountFull = getAmountFull(b, barRectWidth, barRectHeight);

        Rectangle rectangle = new Rectangle(b.left, b.top,
            amountFull + b.left, barRectHeight + b.top);

        paintProgress(g, c, rectangle);
    }

    private void paintProgress(final Graphics g, final JComponent c, Rectangle rectangle) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        g2d.setColor(uiHandler.getColor(MENU_SELECTION_BACKGROUND));
        g2d.fillRoundRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height - 1, 10, 10);
    }
}
