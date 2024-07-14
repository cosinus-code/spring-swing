package org.cosinus.swing.form.control;

import javax.swing.*;

import java.awt.*;

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class FindTextField extends JTextField implements Control<FindText> {

    private FindText value;

    private boolean toggled;

    public FindTextField(String text) {
        injectContext(this);
        value = new FindText(text);
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawString(value.getText(), 0, 0);
    }

    @Override
    public FindText getControlValue() {
        return value;
    }

    @Override
    public void setControlValue(FindText value) {
        this.value = value;
    }
}
