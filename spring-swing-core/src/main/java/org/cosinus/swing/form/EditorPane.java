package org.cosinus.swing.form;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class EditorPane extends JEditorPane {

    public EditorPane() {
        injectContext(this);
    }

    public EditorPane(URL initialPage) throws IOException {
        super(initialPage);
        injectContext(this);
    }

    public EditorPane(String url) throws IOException {
        super(url);
        injectContext(this);
    }

    public EditorPane(String type, String text) {
        super(type, text);
        injectContext(this);
    }
}
