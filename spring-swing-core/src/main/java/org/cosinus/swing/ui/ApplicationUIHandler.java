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
import org.cosinus.swing.context.SpringSwingComponent;
import org.cosinus.swing.exec.ProcessExecutor;
import org.cosinus.swing.translate.Translator;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.*;
import java.io.File;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.awt.Font.PLAIN;
import static java.awt.event.InputEvent.ALT_DOWN_MASK;
import static java.awt.event.InputEvent.META_DOWN_MASK;
import static java.util.Arrays.stream;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC;

/**
 * UIManager handler
 */
@SpringSwingComponent
public class ApplicationUIHandler {

    private static final Logger LOG = LogManager.getLogger(ApplicationUIHandler.class);

    private static final String FOLDER_ICON_KEY = "FileView.directoryIcon";

    private static final String FILE_ICON_KEY = "FileView.fileIcon";

    private static final String TEXT_FONT_KEY = "TextField.font";

    private static final String LABEL_FONT_KEY = "Label.font";

    public static final String OS_LIGHT_THEME = "Light";

    public static final String OS_DARK_THEME = "Dark";

    private Map<String, LookAndFeelInfo> lookAndFeelMap;

    private Map<String, String> labelsMap;

    private final Translator translator;

    private final ProcessExecutor processExecutor;

    public ApplicationUIHandler(Translator translator,
                                ProcessExecutor processExecutor) {
        this.translator = translator;
        this.processExecutor = processExecutor;
    }

    public void translateDefaultUILabels() {
        getUILabelsMap().forEach(this::translateDefaultUILabels);
    }

    public void translateDefaultUILabels(String key,
                                         String value) {
        try {
            String translation = translator.translate(value);
            if (!translation.equals(value)) {
                UIManager.put(key,
                              translation);
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
        Arrays.stream(components)
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
            lookAndFeelMap = Arrays.stream(UIManager.getInstalledLookAndFeels())
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
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    }

    public KeyStroke getControlDownKeyStroke(int keyCode) {
        return getKeyStroke(keyCode, controlDownKeyMask());
    }

    public KeyStroke getAltDownKeyStroke(int keyCode) {
        return getKeyStroke(keyCode, ALT_DOWN_MASK);
    }

    public Map<String, String> getUILabelsMap() {
        if (labelsMap == null) {
            labelsMap = Stream.of(
                    mapEntry("OptionPane.yesButtonText", "yes"),
                    mapEntry("OptionPane.noButtonText", "no"),
                    mapEntry("OptionPane.cancelButtonText", "cancel"),
                    mapEntry("ColorChooser.okText", "ok"),
                    mapEntry("ColorChooser.cancelText", "cancel"),
                    mapEntry("ColorChooser.resetText", "reset"),
                    mapEntry("ColorChooser.swatchesNameText", "colorchooser_swatchesName"),
                    mapEntry("ColorChooser.swatchesRecentText", "colorchooser_swatchesRecentText"),
                    mapEntry("ColorChooser.hsbNameText", "colorchooser_hsbName"),
                    mapEntry("ColorChooser.rgbNameText", "colorchooser_rgbName"),
                    mapEntry("ColorChooser.previewText", "colorchooser_previewName"),
                    mapEntry("ColorChooser.rgbRedText", "colorchooser_rgbRedLabel"),
                    mapEntry("ColorChooser.rgbGreenText", "colorchooser_rgbGreenLabel"),
                    mapEntry("ColorChooser.rgbBlueText", "colorchooser_rgbBlueLabel"),
                    mapEntry("ColorChooser.hsbRedText", "colorchooser_hsbRedText"),
                    mapEntry("ColorChooser.hsbGreenText", "colorchooser_hsbGreenText"),
                    mapEntry("ColorChooser.hsbBlueText", "colorchooser_hsbBlueText"),
                    mapEntry("ColorChooser.hsbHueText", "colorchooser_hsbHueText"),
                    mapEntry("ColorChooser.hsbSaturationText", "colorchooser_hsbSaturationText"),
                    mapEntry("ColorChooser.hsbBrightnessText", "colorchooser_hsbBrightnessText"),
                    mapEntry("ColorChooser.sampleText", "colorchooser_sampleText"),
                    mapEntry("FileChooser.acceptAllFileFilterText", "filechooser_acceptAllFileFilterText"),
                    mapEntry("FileChooser.cancelButtonText", "filechooser_cancelButtonText"),
                    mapEntry("FileChooser.cancelButtonToolTipText", "filechooser_cancelButtonToolTipText"),
                    mapEntry("FileChooser.detailsViewButtonAccessibleName", "filechooser_detailsViewButtonAccessibleName"),
                    mapEntry("FileChooser.detailsViewButtonToolTipText", "filechooser_detailsViewButtonToolTipText"),
                    mapEntry("FileChooser.directoryDescriptionText", "filechooser_directoryDescriptionText"),
                    mapEntry("FileChooser.fileDescriptionText", "filechooser_fileDescriptionText"),
                    mapEntry("FileChooser.fileNameLabelText", "filechooser_fileNameLabelText"),
                    mapEntry("FileChooser.filesOfTypeLabelText", "filechooser_filesOfTypeLabelText"),
                    mapEntry("FileChooser.helpButtonText", "filechooser_helpButtonText"),
                    mapEntry("FileChooser.helpButtonToolTipText", "filechooser_helpButtonToolTipText"),
                    mapEntry("FileChooser.homeFolderAccessibleName", "filechooser_homeFolderAccessibleName"),
                    mapEntry("FileChooser.homeFolderToolTipText", "filechooser_homeFolderToolTipText"),
                    mapEntry("FileChooser.listViewButtonAccessibleName", "filechooser_listViewButtonAccessibleName"),
                    mapEntry("FileChooser.listViewButtonToolTipText", "filechooser_listViewButtonToolTipText"),
                    mapEntry("FileChooser.lookInLabelText", "filechooser_lookInLabelText"),
                    mapEntry("FileChooser.newFolderAccessibleName", "filechooser_newFolderAccessibleName"),
                    mapEntry("FileChooser.newFolderErrorText", "filechooser_newFolderErrorText"),
                    mapEntry("FileChooser.newFolderToolTipText", "filechooser_newFolderToolTipText"),
                    mapEntry("FileChooser.openButtonText", "filechooser_openButtonText"),
                    mapEntry("FileChooser.openButtonToolTipText", "filechooser_openButtonToolTipText"),
                    mapEntry("FileChooser.saveButtonText", "filechooser_saveButtonText"),
                    mapEntry("FileChooser.saveButtonToolTipText", "filechooser_saveButtonToolTipText"),
                    mapEntry("FileChooser.updateButtonText", "filechooser_updateButtonText"),
                    mapEntry("FileChooser.updateButtonToolTipText", "filechooser_updateButtonToolTipText"),
                    mapEntry("FileChooser.upFolderAccessibleName", "filechooser_upFolderAccessibleName"),
                    mapEntry("FileChooser.upFolderToolTipText", "filechooser_upFolderToolTipText"))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        return labelsMap;
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
