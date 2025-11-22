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

package org.cosinus.swing.preference.dialog;

import org.cosinus.swing.border.Borders;
import org.cosinus.swing.context.ApplicationHandler;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.form.ScrollPane;
import org.cosinus.swing.form.control.Control;
import org.cosinus.swing.form.control.ControlType;
import org.cosinus.swing.layout.SpringGridLayout;
import org.cosinus.swing.preference.Preference;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.preference.PreferencesProvider;
import org.cosinus.swing.translate.Translatable;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.cosinus.swing.validation.SimpleValidationContext;
import org.cosinus.swing.validation.ValidationContext;
import org.cosinus.swing.window.Dialog;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION;
import static javax.swing.SwingUtilities.updateComponentTreeUI;
import static org.cosinus.swing.border.Borders.emptyBorder;
import static org.cosinus.swing.border.Borders.lineBorder;
import static org.cosinus.swing.color.SystemColor.LIST_BACKGROUND;
import static org.cosinus.swing.color.SystemColor.LIST_FOREGROUND;
import static org.cosinus.swing.preference.Preferences.LOOK_AND_FEEL;

public class DefaultPreferencesDialog extends Dialog<Void> implements ListSelectionListener, ActionListener {

    @Autowired
    private Translator translator;

    @Autowired
    private Preferences preferences;

    @Autowired
    private PreferencesProvider preferencesProvider;

    @Autowired
    private ApplicationUIHandler uiHandler;

    @Autowired
    private ErrorHandler errorHandler;

    @Autowired
    private ApplicationHandler applicationHandler;

    private PreferenceListModel preferenceListModel;

    private JList<String> preferenceSetList;

    private JPanel preferencesSetPanel;

    private JPanel preferenceSouthPanel;

    private JButton restoreDefaultsButton;

    private JButton okButton;

    private JButton cancelButton;

    private JButton applyButton;

    private final Map<String, Control> preferenceControlsMap;

    public DefaultPreferencesDialog(Frame frame, String title) {
        super(frame, title, true, true);
        preferenceControlsMap = new HashMap<>();
    }

    @Override
    public void initComponents() {
        getContentPane().setLayout(new BorderLayout());

        preferencesSetPanel = new JPanel(new CardLayout());
        preferences.getPreferenceSetsMap()
            .forEach(this::addPreferencesCardPanel);

        getContentPane().add(preferencesSetPanel, BorderLayout.CENTER);
        setSize(600, 500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        restoreDefaultsButton = new JButton(translator.translate("restoreDefaults"));
        okButton = new JButton(translator.translate("ok"));
        applyButton = new JButton(translator.translate("apply"));
        cancelButton = new JButton(translator.translate("cancel"));

        cancelButton.addActionListener(this);
        applyButton.addActionListener(this);
        okButton.addActionListener(this);
        restoreDefaultsButton.addActionListener(this);

        JPanel restoreDefaultsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        restoreDefaultsPanel.add(restoreDefaultsButton);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(applyButton);
        buttonsPanel.add(okButton);

        preferenceSouthPanel = new JPanel(new BorderLayout());
        preferenceSouthPanel.add(restoreDefaultsPanel, BorderLayout.WEST);
        preferenceSouthPanel.add(buttonsPanel, BorderLayout.EAST);

        getContentPane().add(preferenceSouthPanel, BorderLayout.SOUTH);

        if (preferences.getPreferenceSetsMap().size() > 1) {
            preferenceListModel = new PreferenceListModel();
            preferenceSetList = new JList<>(preferenceListModel);
            preferenceSetList.setCellRenderer(new PreferenceListCellRenderer());
            preferenceSetList.setSelectionMode(SINGLE_INTERVAL_SELECTION);
            preferenceSetList.addListSelectionListener(this);
            getContentPane().add(preferenceSetList, BorderLayout.WEST);

            preferenceSetList.setSelectedIndex(0);
        }

        updateUI();
    }

    private void addPreferencesCardPanel(String setName, Map<String, Preference> preferences) {
        JPanel preferencesCardFramePanel = new JPanel(new BorderLayout());
        JPanel preferencesPanel = new JPanel();
        SpringGridLayout preferencesLayout = new SpringGridLayout(preferencesPanel,
            preferences.size(), 2,
            5, 5,
            5, 5);
        preferencesPanel.setLayout(preferencesLayout);

        preferences.forEach((name, preference) -> addPreferenceControl(preferencesPanel, name, preference));
        preferencesLayout.pack();

        preferencesCardFramePanel.add(preferencesPanel, BorderLayout.NORTH);

        ScrollPane preferencesScrollPane = new ScrollPane();
        preferencesScrollPane.setFocusable(false);
        preferencesScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        preferencesScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        preferencesScrollPane.setViewportView(preferencesCardFramePanel);
        preferencesScrollPane.setBorder(emptyBorder(1));

        preferencesSetPanel.add(setName, preferencesScrollPane);
    }

    private <T, R> void addPreferenceControl(JPanel preferencesPanel, String name, Preference<T, R> preference) {
        String label = translatePreferenceName(name);
        ofNullable(preference)
            .map(Preference::getType)
            .map(ControlType::getControlProvider)
            .map(provider -> provider.getControl(preference))
            .ifPresent(control -> {
                preferenceControlsMap.put(name, control);

                Component preferenceComponent = (Component) control;
                preferencesPanel.add(control.createAssociatedLabel(label));
                preferencesPanel.add(preferenceComponent);
            });
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        try {
            if (!e.getValueIsAdjusting()) {
                ((CardLayout) preferencesSetPanel.getLayout())
                    .show(preferencesSetPanel, preferenceListModel.getKey(preferenceSetList.getSelectedIndex()));
            }
        } catch (Exception ex) {
            errorHandler.handleError(ex);
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        try {
            if (event.getSource() == okButton) {
                apply();
                close();
            } else if (event.getSource() == applyButton) {
                apply();
            } else if (event.getSource() == cancelButton) {
                close();
            } else if (event.getSource() == restoreDefaultsButton) {
                restoreDefaultPreferences();
            }
        } catch (Exception ex) {
            errorHandler.handleError(ex);
        }
    }

    private void apply() {
        boolean lookAndFeelChanged = isLookAndFeelChanged();
        boolean languageChanged = isLanguageChanged();
        if (updatePreferences() && savePreferences()) {
            applyPreferences();
            if (languageChanged) {
                translate();
                preferenceSetList.repaint();
            }
            if (lookAndFeelChanged) {
                updateLookAndFeel();
            }
        }
    }

    private void updateLookAndFeel() {
        try {

            updateComponentTreeUI(getParent());
            updateComponentTreeUI(this);
            updateUI();
        } catch (Exception ex) {
            errorHandler.handleError(ex);
        }
    }

    protected boolean isLookAndFeelChanged() {
        return isPreferenceChanged(LOOK_AND_FEEL);
    }

    protected boolean isLanguageChanged() {
        return isPreferenceChanged(Preferences.LANGUAGE);
    }

    protected boolean isPreferenceChanged(String preferenceName) {
        return preferences.findPreference(preferenceName)
            .map(Preference::getValue)
            .map(Object::toString)
            .map(initialPreference -> ofNullable(preferenceControlsMap.get(preferenceName))
                .map(Control::getControlValue)
                .map(Object::toString)
                .filter(initialPreference::equals)
                .isEmpty())
            .orElse(false);
    }

    private boolean savePreferences() {
        try {
            preferencesProvider.savePreferences(preferences);
            return true;
        } catch (IOException e) {
            errorHandler.handleError(e);
            return false;
        }
    }

    private boolean updatePreferences() {
        ValidationContext validationContext = new SimpleValidationContext();
        preferenceControlsMap
            .values()
            .stream()
            .map(Control::validateValue)
            .forEach(validationContext::addValidationErrors);

        if (validationContext.hasErrors()) {
            errorHandler.handleValidationErrors(this, validationContext.getValidationErrors());
            return false;
        }

        preferenceControlsMap
            .forEach((name, preferenceControl) -> preferences
                .updatePreference(name, preferenceControl.getControlValue()));
        return true;
    }

    private void applyPreferences() {
        applicationHandler.reloadApplication();
    }

    protected void restoreDefaultPreferences() {
        preferencesProvider.getDefaultPreferences()
            .map(Preferences::getPreferencesMap)
            .ifPresent(preferencesMap -> preferencesMap
                .forEach((name, preference) -> ofNullable(preferenceControlsMap.get(name))
                    .ifPresent(control -> control.setControlValue(preference.getRealValue()))));
    }

    public void updateUI() {
        if (preferenceSetList != null) {
            preferenceSetList.setBackground(uiHandler.getColor(LIST_BACKGROUND));
            preferenceSetList.setForeground(uiHandler.getColor(LIST_FOREGROUND));
        }
        if (preferenceSouthPanel != null) {
            preferenceSouthPanel.setBorder(lineBorder(uiHandler.getControlHighlightColor(),
                1, 0, 0, 0));
        }
    }

    protected String translatePreferenceName(String name) {
        return translator.translate("preference-" + name);
    }

    @Override
    public void translate() {
        restoreDefaultsButton.setText(translator.translate("restoreDefaults"));
        okButton.setText(translator.translate("ok"));
        applyButton.setText(translator.translate("apply"));
        cancelButton.setText(translator.translate("cancel"));
        ofNullable(preferenceListModel)
            .ifPresent(PreferenceListModel::translate);
        preferenceControlsMap.forEach(
            (name, preferenceControl) -> preferenceControl.updateAssociatedLabel(translatePreferenceName(name)));
    }

    private static class PreferenceListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setBorder(Borders.emptyBorder(4, 8, 4, 8));
            return label;
        }
    }

    private class PreferenceListModel extends AbstractListModel<String> implements Translatable {

        @Serial
        private static final long serialVersionUID = 2474868725194138977L;

        private final List<String> preferenceNames;

        private final Map<String, String> itemsMap;

        private PreferenceListModel() {
            this.preferenceNames = new ArrayList<>(preferences.getPreferenceSetsMap().keySet());
            this.itemsMap = preferenceNames
                .stream()
                .collect(Collectors.toMap(Function.identity(),
                    DefaultPreferencesDialog.this::translatePreferenceName));
        }

        @Override
        public int getSize() {
            return preferenceNames.size();
        }

        @Override
        public String getElementAt(int index) {
            return itemsMap.get(preferenceNames.get(index));
        }

        @Override
        public void translate() {
            preferenceNames.forEach(
                name -> itemsMap.put(name, DefaultPreferencesDialog.this.translatePreferenceName(name)));
        }

        public String getKey(int index) {
            return preferenceNames.get(index);
        }
    }
}
