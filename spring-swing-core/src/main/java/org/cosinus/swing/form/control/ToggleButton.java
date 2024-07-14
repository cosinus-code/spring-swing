package org.cosinus.swing.form.control;

import javax.swing.*;

import java.util.Optional;

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class ToggleButton extends JToggleButton implements Control<Boolean> {

    public ToggleButton() {
        injectContext(this);
    }

    public ToggleButton(String text) {
        super(text);
        injectContext(this);
    }

    public ToggleButton(Icon icon) {
        super(icon);
        injectContext(this);
    }

    public ToggleButton(String text, boolean selected) {
        super(text, selected);
        injectContext(this);
    }

    public ToggleButton(Icon icon, boolean selected) {
        super(icon, selected);
        injectContext(this);
    }

    @Override
    public Boolean getControlValue() {
        return isSelected();
    }

    @Override
    public void setControlValue(Boolean value) {
        setSelected(value);
    }

    @Override
    public Label createAssociatedLabel(String labelText) {
        updateAssociatedLabel(labelText);
        return new Label();
    }

    @Override
    public Optional<JLabel> getAssociatedLabel() {
        return Optional.empty();
    }

    @Override
    public void updateAssociatedLabel(String labelText) {
        setText(labelText);
    }
}
