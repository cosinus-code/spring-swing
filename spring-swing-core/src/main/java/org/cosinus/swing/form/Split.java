/*
 * Copyright 2020 Cosinus Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cosinus.swing.form;

import org.cosinus.swing.context.SwingApplicationContext;
import org.cosinus.swing.context.SwingAutowired;
import org.cosinus.swing.context.SwingInject;
import org.cosinus.swing.store.ApplicationStorage;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.beans.PropertyChangeEvent;
import java.util.Arrays;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Custom splitter
 */
public class Split extends JSplitPane implements SwingInject, FormComponent {

    private static final Logger LOG = LoggerFactory.getLogger(Split.class);

    private static final String SPLIT = "split";

    private static final String SPLIT_PROPERTY_NAME = "dividerLocation";

    private String splitName;

    private boolean isDividerLocationLoading;

    private boolean lastPercentDivider;

    protected BasicSplitPaneDivider divider;

    private final int defaultDividerLocation;

    @SwingAutowired
    public ApplicationStorage applicationStorage;

    @SwingAutowired
    public Translator translator;

    @SwingAutowired
    public ApplicationUIHandler uiHandler;

    public Split(String splitName,
                 int defaultDividerLocation,
                 SwingApplicationContext context) {
        this.splitName = splitName;
        this.defaultDividerLocation = defaultDividerLocation;
        injectSwingContext(context);
    }

    public void initComponent() {
        initDivider();
        initListeners();
        updateForm();
        loadDividerLocation();
    }

    protected void initListeners() {
        addPropertyChangeListener(new BasicSplitPaneDivider((BasicSplitPaneUI) getUI()) {
            public void propertyChange(PropertyChangeEvent event) {
                try {
                    if (event.getPropertyName().equals(SPLIT_PROPERTY_NAME) && !isDividerLocationLoading) {
                        applicationStorage.saveInt(applicationStorage.key(SPLIT, splitName),
                                                   Integer.parseInt(event.getNewValue().toString()));
                    }
                    super.propertyChange(event);
                } catch (Exception ex) {
                    LOG.error("Cannot save divider location", ex);
                }
            }
        });
    }

    protected void initDivider() {
        divider = Arrays.stream(getComponents())
                .filter(component -> BasicSplitPaneDivider.class.isAssignableFrom(component.getClass()))
                .map(BasicSplitPaneDivider.class::cast)
                .findFirst()
                .orElse(null);
    }

    public void updateForm() {
    }

    public void loadDividerLocation() {
        isDividerLocationLoading = true;
        setDividerLocation(applicationStorage.getInt(applicationStorage.key(SPLIT, splitName), defaultDividerLocation));
        isDividerLocationLoading = false;
    }

    public void moveSplitter(int percent) {
        moveSplitter(percent, true);
    }

    public void moveSplitter(int value, boolean percent) {
        setDividerLocation(percent ?
                                   min(max(value, 20), 80) * getWidth() / 100 :
                                   max(value, 0));

        lastDividerLocation = value;
        lastPercentDivider = percent;
    }

    @Override
    public void setVisible(boolean visible) {
        if (!visible) {
            lastDividerLocation = getDividerLocation();
        }
        super.setVisible(visible);
        if (visible) {
            moveSplitter(lastDividerLocation, lastPercentDivider);
        }
        divider.setVisible(visible);
    }

    @Override
    public void initComponents() {

    }

    @Override
    public void updateFormUI() {

    }

    @Override
    public void translateForm() {

    }

    @Override
    public void initContent() {

    }
}
