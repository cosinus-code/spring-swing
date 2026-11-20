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

import lombok.Getter;
import org.cosinus.swing.form.gtk.GtkTabUI;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.cosinus.swing.ui.UIAlign;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class TabbedPane extends JTabbedPane {

    @Autowired
    private ApplicationUIHandler uiHandler;

    @Getter
    private UIAlign tabAreaAlignment = UIAlign.LEFT;

    public TabbedPane() {
        injectContext(this);
        init();
    }

    public TabbedPane(UIAlign tabAreaAlignment) {
        this.tabAreaAlignment = tabAreaAlignment;
        injectContext(this);
        init();
    }

    public TabbedPane(int tabPlacement) {
        super(tabPlacement);
        injectContext(this);
        init();
    }

    public TabbedPane(int tabPlacement, int tabLayoutPolicy) {
        super(tabPlacement, tabLayoutPolicy);
        injectContext(this);
        init();
    }

    protected void init() {
        updateUI();
    }

    /**
     * Install the {@link GtkTabUI} when the current look-and-feel is GTK,
     * otherwise use the tabbed pane UI of the current look-and-feel.
     * <p>
     * This is also called by the {@link JTabbedPane} constructor, before the context is injected,
     * in which case the default UI is installed and {@link #init()} will update it later.
     */
    @Override
    public void updateUI() {
        if (uiHandler != null && uiHandler.isLookAndFeelGTK()) {
            setUI(new GtkTabUI(tabAreaAlignment));
        } else {
            super.updateUI();
        }
    }

    public void setTabAreaAlignment(UIAlign tabAreaAlignment) {
        if (this.tabAreaAlignment != tabAreaAlignment) {
            this.tabAreaAlignment = tabAreaAlignment;
            updateUI();
            revalidate();
            repaint();
        }
    }
}
