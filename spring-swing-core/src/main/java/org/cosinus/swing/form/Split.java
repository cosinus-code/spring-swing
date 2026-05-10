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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cosinus.swing.store.ApplicationStorage;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.stream;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Extension of the {@link JSplitPane}
 * which will automatically inject the application context.
 */
@Slf4j
public class Split extends JSplitPane implements FormComponent {

    private static final String SPLIT = "split";

    private static final String SPLIT_PROPERTY_NAME = "dividerLocation";

    @Autowired
    protected ApplicationStorage applicationStorage;

    @Autowired
    protected Translator translator;

    private final String splitName;

    protected BasicSplitPaneDivider divider;

    @Setter
    protected boolean keepRelativeLocationOnResize;

    private final int defaultDividerLocation;

    public Split(String splitName, int defaultDividerLocation) {
        injectContext(this);
        this.splitName = splitName;
        this.defaultDividerLocation = defaultDividerLocation;
    }

    public void initComponent() {
        if (keepRelativeLocationOnResize) {
            setResizeWeight(0.5);
        }
        this.divider = getDivider();
        initListeners();
        loadDividerLocation();
    }

    protected void initListeners() {
        addPropertyChangeListener(event -> {
            try {
                if (event.getPropertyName().equals(SPLIT_PROPERTY_NAME)) {
                    saveDividerLocation(Integer.parseInt(event.getNewValue().toString()));
                }
            } catch (Exception ex) {
                log.error("Cannot save divider location", ex);
            }
        });
    }

    protected BasicSplitPaneDivider getDivider() {
        return stream(getComponents())
            .filter(component -> BasicSplitPaneDivider.class.isAssignableFrom(component.getClass()))
            .map(BasicSplitPaneDivider.class::cast)
            .findFirst()
            .orElse(null);
    }

    public synchronized void loadDividerLocation() {
        setDividerLocation(applicationStorage.getInt(getStorageKey(), defaultDividerLocation));
    }

    protected synchronized void saveDividerLocation(int location) {
        applicationStorage.saveInt(getStorageKey(), location);
    }

    protected String getStorageKey() {
        return applicationStorage.key(SPLIT, splitName);
    }

    public void moveSplitter(int percent) {
        moveSplitter(percent, true);
    }

    public void moveSplitter(int value, boolean percent) {
        int location = percent ?
            min(max(value, 20), 80) * getWidth() / 100 :
            max(value, 0);
        setDividerLocation(location);
    }

    public void setVisibleDivider(boolean visible) {
        if (visible) {
            loadDividerLocation();
        }
        if (divider != null) {
            divider.setVisible(visible);
        }
    }
}
