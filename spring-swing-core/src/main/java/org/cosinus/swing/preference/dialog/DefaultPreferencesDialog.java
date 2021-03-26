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

package org.cosinus.swing.preference.dialog;

import org.cosinus.swing.border.Borders;
import org.cosinus.swing.context.ApplicationHandler;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.error.ValidationException;
import org.cosinus.swing.form.Dialog;
import org.cosinus.swing.form.control.Control;
import org.cosinus.swing.preference.Preference;
import org.cosinus.swing.preference.PreferenceType;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.preference.PreferencesProvider;
import org.cosinus.swing.preference.control.*;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION;
import static javax.swing.SwingUtilities.updateComponentTreeUI;
import static org.cosinus.swing.border.Borders.emptyBorder;
import static org.cosinus.swing.border.Borders.lineBorder;
import static org.cosinus.swing.layout.SpringLayoutUtils.makeCompactGrid;
import static org.cosinus.swing.preference.PreferenceType.*;
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

    private JList<String> preferenceSetList;

    private JPanel preferencesSetPanel;

    private JPanel preferenceSouthPanel;

    private JButton restoreDefaultsButton;

    private JButton okButton;

    private JButton cancelButton;

    private JButton applyButton;

    private final Map<PreferenceType, PreferenceControlProvider<?>> preferenceControlProvidersMap;

    private final Map<String, Control> preferenceControlsMap;

    public DefaultPreferencesDialog(Frame frame, String title) {
        super(frame, title, true);
        this.preferenceControlProvidersMap = getPreferenceControlProvidersMap();
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

        restoreDefaultsButton = new JButton(translator.translate("restore-defaults"));
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
            preferenceSetList = new JList<>(
                preferences.getPreferenceSetsMap()
                    .keySet()
                    .stream()
                    .map(this::translatePreferenceName)
                    .toArray(String[]::new));
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
        JPanel preferencesPanel = new JPanel(new SpringLayout());

        preferences.forEach((name, preference) -> addPreferenceControl(preferencesPanel, name, preference));
        makeCompactGrid(preferencesPanel, preferences.size(), 2, 5, 5, 5, 5);

        preferencesCardFramePanel.add(preferencesPanel, BorderLayout.NORTH);

        JScrollPane preferencesScrollPane = new JScrollPane();
        preferencesScrollPane.setFocusable(false);
        preferencesScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        preferencesScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        preferencesScrollPane.setViewportView(preferencesCardFramePanel);
        preferencesScrollPane.setBorder(emptyBorder(1));

        preferencesSetPanel.add(translatePreferenceName(setName), preferencesScrollPane);
    }

    private <T, R> void addPreferenceControl(JPanel preferencesPanel, String name, Preference<T, R> preference) {
        String label = translatePreferenceName(name);
        ofNullable(getPreferenceControlProvider(preference))
            .ifPresent(provider -> {
                Control<R> preferenceControl = provider.getPreferenceControl(preference);
                preferenceControlsMap.put(name, preferenceControl);

                Component preferenceComponent = (Component) preferenceControl;
                preferencesPanel.add(preferenceControl.createAssociatedLabel(label));
                preferencesPanel.add(preferenceComponent);
            });
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            ((CardLayout) preferencesSetPanel.getLayout())
                .show(preferencesSetPanel, preferenceSetList.getSelectedValue());
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
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
    }

    private void apply() {
        boolean lookAndFeelChanged = isLookAndFeelChanged();
        if (updatePreferences() && savePreferences()) {
            applyPreferences();
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
        return preferences.findPreference(LOOK_AND_FEEL)
            .map(Preference::getValue)
            .map(Object::toString)
            .map(initialLookAndFeel -> ofNullable(preferenceControlsMap.get(LOOK_AND_FEEL))
                .map(Control::getValue)
                .map(Object::toString)
                .filter(initialLookAndFeel::equals)
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
        AtomicBoolean success = new AtomicBoolean(true);
        preferenceControlsMap
            .forEach((name, preferenceControl) -> {
                try {
                    preferences.updatePreference(name, preferenceControl.getValue());
                } catch (ValidationException ex) {
                    //TODO: to collect validation errors
                    errorHandler.handleError(this, format("Cannot update preferences '%s' with value '%s': %s",
                                                          name,
                                                          ex.getValue(),
                                                          ex.getLocalizedMessage()));
                    success.set(false);
                }
            });
        return success.get();
    }

    private void applyPreferences() {
        applicationHandler.reloadApplication();
        //TODO: to translate whole dialog
    }

    protected void restoreDefaultPreferences() {
        preferencesProvider.getDefaultPreferences()
            .map(Preferences::getPreferencesMap)
            .ifPresent(preferencesMap -> preferencesMap
                .forEach((name, preference) -> ofNullable(preferenceControlsMap.get(name))
                    .ifPresent(control -> control.setValue(preference.getRealValue()))));
    }

    public void updateUI() {
        if (preferenceSetList != null) {
            Color inactiveCaptionColor = uiHandler.getColor("inactiveCaption");
            Color controlColor = uiHandler.getColor("control");
            if (inactiveCaptionColor != null && !inactiveCaptionColor.equals(controlColor)) {
                preferenceSetList.setBackground(inactiveCaptionColor);
                preferenceSetList.setForeground(uiHandler.getColor("inactiveCaptionText"));
            } else {
                preferenceSetList.setBackground(uiHandler.getColor("List.background"));
                preferenceSetList.setForeground(uiHandler.getColor("List.foreground"));
            }
        }
        if (preferenceSouthPanel != null) {
            preferenceSouthPanel.setBorder(lineBorder(uiHandler.getColor("controlHighlight"),
                                                      1, 0, 0, 0));
        }
    }

    protected String translatePreferenceName(String name) {
        return translator.translate("preference-" + name);
    }

    protected <T, R> PreferenceControlProvider<R> getPreferenceControlProvider(Preference<T, R> preference) {
        return (PreferenceControlProvider<R>) preferenceControlProvidersMap.get(preference.getType());
    }

    private Map<PreferenceType, PreferenceControlProvider<?>> getPreferenceControlProvidersMap() {
        Map<PreferenceType, PreferenceControlProvider<?>> preferenceControlProvidersMap = new HashMap<>();
        preferenceControlProvidersMap.put(TEXT, new TextPreferenceControlProvider());
        preferenceControlProvidersMap.put(BOOLEAN, new BooleanPreferenceControlProvider());
        preferenceControlProvidersMap.put(INTEGER, new IntegerPreferenceControlProvider());
        preferenceControlProvidersMap.put(LONG, new LongPreferenceControlProvider());
        preferenceControlProvidersMap.put(FLOAT, new FloatPreferenceControlProvider());
        preferenceControlProvidersMap.put(DOUBLE, new DoublePreferenceControlProvider());
        preferenceControlProvidersMap.put(LANGUAGE, new LanguagePreferenceControlProvider());
        preferenceControlProvidersMap.put(LAF, new LookAndFeelPreferenceControlProvider());
        preferenceControlProvidersMap.put(FILE, new FilePreferenceControlProvider());
        preferenceControlProvidersMap.put(FOLDER, new FolderPreferenceControlProvider());
        preferenceControlProvidersMap.put(COLOR, new ColorPreferenceControlProvider());
        preferenceControlProvidersMap.put(FONT, new FontPreferenceControlProvider());
        preferenceControlProvidersMap.put(DATE, new DatePreferenceControlProvider());
        return preferenceControlProvidersMap;
    }

    @Override
    public void translate() {

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
}
