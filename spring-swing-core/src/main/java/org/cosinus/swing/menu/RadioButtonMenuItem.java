package org.cosinus.swing.menu;

import org.cosinus.swing.form.FormComponent;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.event.ActionListener;

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class RadioButtonMenuItem extends JRadioButtonMenuItem implements FormComponent {

    @Autowired
    protected Translator translator;

    private final JRadioButtonMenuItem altMenuItem;

    private final String key;

    public RadioButtonMenuItem(ActionListener action,
                               String key) {
        this(action,
            key,
            false,
            null);
    }

    public RadioButtonMenuItem(ActionListener action,
                               boolean selected,
                               String key) {
        this(action,
            key,
            selected,
            null);
    }

    public RadioButtonMenuItem(ActionListener action,
                               String key,
                               boolean selected,
                               KeyStroke keyStroke) {
        super();
        injectContext(this);

        this.key = key;

        super.addActionListener(action);
        super.setAccelerator(keyStroke);
        super.setSelected(selected);

        altMenuItem = new JRadioButtonMenuItem();
        altMenuItem.addActionListener(action);
        altMenuItem.setAccelerator(keyStroke);
    }

    public void setText(String text) {
        altMenuItem.setText(text);
        super.setText(text);
    }

    public JRadioButtonMenuItem getAltMenuItem() {
        return altMenuItem;
    }

    public String getActionKey() {
        return key;
    }

    @Override
    public void initComponents() {

    }

    @Override
    public void translate() {
        setText(translator.translate(key));
    }
}
