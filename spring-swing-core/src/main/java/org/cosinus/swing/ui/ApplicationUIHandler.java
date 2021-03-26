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

package org.cosinus.swing.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.exec.ProcessExecutor;
import org.cosinus.swing.translate.Translator;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.Border;
import java.awt.*;
import java.io.File;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.awt.Font.PLAIN;
import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.event.InputEvent.ALT_DOWN_MASK;
import static java.awt.event.InputEvent.META_DOWN_MASK;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC;

/**
 * UIManager handler
 */
public class ApplicationUIHandler {

    private static final Logger LOG = LogManager.getLogger(ApplicationUIHandler.class);

    public static final String OPTION_PANE_MESSAGE_DIALOG_TITLE = "OptionPane.messageDialogTitle";

    public static final String OPTION_PANE_INPUT_DIALOG_TITLE = "OptionPane.inputDialogTitle";

    public static final String OPTION_PANE_ERROR_ICON = "OptionPane.errorIcon";

    public static final String FOLDER_ICON_KEY = "FileView.directoryIcon";

    public static final String FILE_ICON_KEY = "FileView.fileIcon";

    public static final String TEXT_FONT_KEY = "TextField.font";

    public static final String LABEL_FONT_KEY = "Label.font";

    public static final String OS_LIGHT_THEME = "Light";

    public static final String OS_DARK_THEME = "Dark";

    private Map<String, LookAndFeelInfo> lookAndFeelMap;

    private Set<String> uiTranslationKeys;

    private final Translator translator;

    private final ProcessExecutor processExecutor;

    public ApplicationUIHandler(Translator translator,
                                ProcessExecutor processExecutor) {
        this.translator = translator;
        this.processExecutor = processExecutor;
    }

    public void translateDefaultUILabels() {
        getTranslationKeys().forEach(this::translateDefaultUILabels);
    }

    public void translateDefaultUILabels(String key) {
        try {
            String translation = translator.translate(key);
            if (!key.equals(translation)) {
                UIManager.getDefaults().put(key, translation);
            } else {
                UIManager.getDefaults().remove(key);
            }
        } catch (Exception ex) {
            LOG.error("Failed to translate ui key: " + key,
                      ex);
        }
    }

    public String getString(String key) {
        return UIManager.getString(key);
    }

    public Font getTextFont() {
        return getFont(TEXT_FONT_KEY);
    }

    public Font getLabelFont() {
        return getFont(LABEL_FONT_KEY);
    }

    public void setGeneralFont(Component... components) {
        stream(components)
            .forEach(component -> setGeneralFont(component,
                                                 false));
    }

    public void setGeneralFont(Component comp,
                               boolean normal) {
        Optional.ofNullable(getTextFont())
            .map(f -> normal ? f.deriveFont(PLAIN) : f)
            .ifPresent(comp::setFont);
    }

    public Optional<Icon> getDefaultFileIcon(File file) {
        return Optional.ofNullable(UIManager.get(file.isDirectory() ? FOLDER_ICON_KEY : FILE_ICON_KEY))
            .filter(icon -> Icon.class.isAssignableFrom(icon.getClass()))
            .map(Icon.class::cast);
    }

    public Optional<Icon> getDefaultIcon() {
        return Optional.ofNullable(UIManager.getIcon(FILE_ICON_KEY));
    }

    public void setDefaultIcon(Icon icon) {
        UIManager.put(FILE_ICON_KEY, icon);
    }

    public String getDefaultLookAndFeelClassName() {
        return UIManager.getSystemLookAndFeelClassName();
    }

    public String getCrossPlatformLookAndFeelClassName() {
        return UIManager.getCrossPlatformLookAndFeelClassName();
    }

    public void setLookAndFeel(String lookAndFeel) {
        try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (Exception e) {
            LOG.error("Failed to load lookAndFeel: " + lookAndFeel, e);
        }
    }

    public String getLookAndFeel() {
        return UIManager.getLookAndFeel().getName();
    }

    public Map<String, LookAndFeelInfo> getAvailableLookAndFeels() {
        if (lookAndFeelMap == null) {
            lookAndFeelMap = stream(UIManager.getInstalledLookAndFeels())
                .collect(Collectors.toMap(LookAndFeelInfo::getName,
                                          Function.identity()));
        }
        return lookAndFeelMap;
    }

    public boolean isDarkTheme() {
        return processExecutor.getOsTheme()
            .map(theme -> theme.startsWith(OS_DARK_THEME))
            .orElse(false);
    }

    public boolean isLookAndFeelGTK() {
        return getLookAndFeel().startsWith("GTK");
    }

    public boolean isLookAndFeelWindows() {
        return getLookAndFeel().startsWith("Windows");
    }

    public Color getControlDarkColor() {
        return getColor("controlDkShadow");
    }

    public int controlDownKeyMask() {
        return IS_OS_MAC ?
            META_DOWN_MASK :
            getDefaultToolkit().getMenuShortcutKeyMaskEx();
    }

    public KeyStroke getControlDownKeyStroke(int keyCode) {
        return getKeyStroke(keyCode, controlDownKeyMask());
    }

    public KeyStroke getAltDownKeyStroke(int keyCode) {
        return getKeyStroke(keyCode, ALT_DOWN_MASK);
    }

    public Set<String> getTranslationKeys() {
        if (uiTranslationKeys == null) {
            uiTranslationKeys = Stream.of(
                "OptionPane.yesButtonText",
                "OptionPane.noButtonText",
                "OptionPane.cancelButtonText",
                "ColorChooser.okText",
                "ColorChooser.cancelText",
                "ColorChooser.resetText",
                "ColorChooser.swatchesNameText",
                "ColorChooser.swatchesRecentText",
                "ColorChooser.hsbNameText",
                "ColorChooser.rgbNameText",
                "ColorChooser.previewText",
                "ColorChooser.rgbRedText",
                "ColorChooser.rgbGreenText",
                "ColorChooser.rgbBlueText",
                "ColorChooser.hsbRedText",
                "ColorChooser.hsbGreenText",
                "ColorChooser.hsbBlueText",
                "ColorChooser.hsbHueText",
                "ColorChooser.hsbSaturationText",
                "ColorChooser.hsbBrightnessText",
                "ColorChooser.sampleText",
                "FileChooser.acceptAllFileFilterText",
                "FileChooser.cancelButtonText",
                "FileChooser.cancelButtonToolTipText",
                "FileChooser.detailsViewButtonAccessibleName",
                "FileChooser.detailsViewButtonToolTipText",
                "FileChooser.directoryDescriptionText",
                "FileChooser.fileDescriptionText",
                "FileChooser.fileNameLabelText",
                "FileChooser.filesOfTypeLabelText",
                "FileChooser.helpButtonText",
                "FileChooser.helpButtonToolTipText",
                "FileChooser.homeFolderAccessibleName",
                "FileChooser.homeFolderToolTipText",
                "FileChooser.listViewButtonAccessibleName",
                "FileChooser.listViewButtonToolTipText",
                "FileChooser.lookInLabelText",
                "FileChooser.newFolderAccessibleName",
                "FileChooser.newFolderErrorText",
                "FileChooser.newFolderToolTipText",
                "FileChooser.openButtonText",
                "FileChooser.openButtonToolTipText",
                "FileChooser.saveButtonText",
                "FileChooser.saveButtonToolTipText",
                "FileChooser.updateButtonText",
                "FileChooser.updateButtonToolTipText",
                "FileChooser.upFolderAccessibleName",
                "FileChooser.upFolderToolTipText")
                .collect(toSet());
        }
        return uiTranslationKeys;
    }

    private Map.Entry<String, String> mapEntry(String key, String value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    public Icon getErrorIcon() {
        return getIcon("OptionPane.errorIcon");
    }

    public Icon getIcon(String key) {
        return UIManager.getIcon(key);
    }

    public Font getFont(String key) {
        return UIManager.getFont(key);
    }

    public Color getColor(String key) {
        return UIManager.getColor(key);
    }

    public Border getBorder(String key) {
        return UIManager.getBorder(key);
    }

    public Rectangle getGraphicsDevicesBound() {
        return stream(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices())
            .map(GraphicsDevice::getDefaultConfiguration)
            .map(GraphicsConfiguration::getBounds)
            .reduce(new Rectangle(), this::add);
    }

    private Rectangle add(Rectangle r1, Rectangle r2) {
        r1.add(r2);
        return r1;
    }
}
