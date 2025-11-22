package org.cosinus.swing.boot.ui;

import static java.util.Arrays.stream;
import static javax.swing.SwingUtilities.updateComponentTreeUI;
import static org.cosinus.stream.Streams.flatComponentsStream;

import java.awt.*;
import org.cosinus.swing.boot.initialize.LookAndFeelInitializer;
import org.cosinus.swing.form.FormComponent;
import org.cosinus.swing.ui.listener.UIChangeController;
import org.cosinus.swing.ui.listener.UIChangeListener;

public class DefaultUIChangeListener implements UIChangeListener {

    private final LookAndFeelInitializer lookAndFeelInitializer;

    public DefaultUIChangeListener(final UIChangeController uiChangeController,
                                   final LookAndFeelInitializer lookAndFeelInitializer) {
        this.lookAndFeelInitializer = lookAndFeelInitializer;
        uiChangeController.registerUIChangeListener(this);
    }

    @Override
    public void uiThemeChanged() {
        lookAndFeelInitializer.initialize();
        stream(Frame.getWindows())
            .filter(Component::isVisible)
            .forEach(window -> {
                updateComponentTreeUI(window);
                flatComponentsStream(window)
                    .filter(component -> component instanceof FormComponent)
                    .map(FormComponent.class::cast)
                    .forEach(FormComponent::updateForm);
                window.repaint();
            });
    }
}
