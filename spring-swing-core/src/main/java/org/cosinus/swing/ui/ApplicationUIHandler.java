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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.exec.ProcessExecutor;
import org.cosinus.swing.translate.Translator;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.event.InputEvent.ALT_DOWN_MASK;
import static java.awt.event.InputEvent.META_DOWN_MASK;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC;

/**
 * UIManager handler.
 * <p>
 * The intention is to use this handler methods instead UIManager static methods,
 * to isolate UIManager for easier reimplementation.
 */
public class ApplicationUIHandler {

    private static final Logger LOG = LogManager.getLogger(ApplicationUIHandler.class);

    private static final String DEFAULT_GNOME_ICON_THEME = "Default";

    private static final String GNOME_ICON_THEME_NAME_PROPERTY = "gnome.Net/IconThemeName";

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

    private final Set<LookAndFeelInfo> lookAndFeels;

    public ApplicationUIHandler(Translator translator,
                                ProcessExecutor processExecutor,
                                Set<LookAndFeelInfo> lookAndFeels) {
        this.translator = translator;
        this.processExecutor = processExecutor;
        this.lookAndFeels = lookAndFeels;
    }

    /**
     * Translate default UI related texts.
     */
    public void translateDefaultUILabels() {
        getTranslationKeys().forEach(this::translateDefaultUILabel);
    }

    /**
     * Translate default UI related texts.
     *
     * @param key the key of the text to translate
     */
    public void translateDefaultUILabel(String key) {
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

    /**
     * Get an UI text.
     *
     * @param key the key of the text
     * @return the text
     */
    public String getString(String key) {
        return UIManager.getString(key);
    }

    /**
     * Get the UI text font.
     *
     * @return the text font
     */
    public Font getTextFont() {
        return getFont(TEXT_FONT_KEY);
    }

    /**
     * Get the UI label font.
     *
     * @return the label font
     */
    public Font getLabelFont() {
        return getFont(LABEL_FONT_KEY);
    }

    /**
     * Get default icon for a file.
     *
     * @param file the file
     * @return the default icon of the file
     */
    public Optional<Icon> getDefaultFileIcon(File file) {
        return ofNullable(UIManager.get(file.isDirectory() ? FOLDER_ICON_KEY : FILE_ICON_KEY))
            .filter(icon -> Icon.class.isAssignableFrom(icon.getClass()))
            .map(Icon.class::cast);
    }

    /**
     * Get the default file icon.
     *
     * @return the file icon
     */
    public Optional<Icon> getDefaultFileIcon() {
        return ofNullable(UIManager.getIcon(FILE_ICON_KEY));
    }

    /**
     * Set the default file icon.
     *
     * @param icon the icon to set as default
     */
    public void setDefaultFileIcon(Icon icon) {
        UIManager.put(FILE_ICON_KEY, icon);
    }

    /**
     * Set the default folder icon.
     *
     * @param icon the icon to set as default
     */
    public void setDefaultFolderIcon(Icon icon) {
        UIManager.put(FOLDER_ICON_KEY, icon);
    }

    /**
     * Get the default look-and-feel class name.
     *
     * @return the look-and-feel class name
     */
    public String getDefaultLookAndFeelClassName() {
        return UIManager.getSystemLookAndFeelClassName();
    }

    /**
     * Get the cross-platform look-and-feel class name.
     *
     * @return the look-and-feel class name
     */
    public String getCrossPlatformLookAndFeelClassName() {
        return UIManager.getCrossPlatformLookAndFeelClassName();
    }

    /**
     * Set the current look-and-feel of the application.
     *
     * @param lookAndFeel the look-and-feel to set
     */
    public void setLookAndFeel(String lookAndFeel) {
        try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (Exception e) {
            LOG.error("Failed to load lookAndFeel: " + lookAndFeel, e);
        }

//        LOG.info("------------------------------------------- ookandfeel colors: " + lookAndFeel);
//        UIManager.getLookAndFeelDefaults().entrySet()
//            .stream()
//            .filter(entry -> entry.getValue() instanceof Color)
//            .forEach(entry -> LOG.info(entry.getKey() + " -> " + Colors.getColorDescription((Color) entry.getValue())));
    }

    /**
     * Get the name of the current look-and-feel.
     *
     * @return the name of the current look-and-feel
     */
    public String getLookAndFeel() {
        return UIManager.getLookAndFeel().getName();
    }

    /**
     * Get the map of available look-and-feels.
     *
     * @return the map of available look-and-feels
     */
    public Map<String, LookAndFeelInfo> getAvailableLookAndFeels() {
        if (lookAndFeelMap == null) {
            lookAndFeelMap = concat(stream(UIManager.getInstalledLookAndFeels()),
                                    lookAndFeels.stream())
                .collect(Collectors.toMap(LookAndFeelInfo::getName,
                                          Function.identity()));
        }
        return lookAndFeelMap;
    }

    /**
     * Check if the current theme of the Operating System is dark.
     *
     * @return true if the current OS theme is dark
     */
    public boolean isDarkTheme() {
        return processExecutor.getOsTheme()
            .map(theme -> theme.startsWith(OS_DARK_THEME))
            .orElse(false);
    }

    /**
     * Check if the current look-and-feel is GTK.
     *
     * @return true if the current look-and-feel is GTK
     */
    public boolean isLookAndFeelGTK() {
        return getLookAndFeel().startsWith("GTK");
    }

    /**
     * Check if the current look-and-feel is Windows.
     *
     * @return true if the current look-and-feel is Windows
     */
    public boolean isLookAndFeelWindows() {
        return getLookAndFeel().startsWith("Windows");
    }

    /**
     * Get the current control down key mask.
     *
     * @return the current control down key mask
     */
    public int getControlDownKeyMask() {
        return IS_OS_MAC ?
            META_DOWN_MASK :
            getDefaultToolkit().getMenuShortcutKeyMaskEx();
    }

    /**
     * Get the current control down key stroke for key code
     *
     * @param keyCode the key code
     * @return the current control down key stroke
     */
    public KeyStroke getControlDownKeyStroke(int keyCode) {
        return getKeyStroke(keyCode, getControlDownKeyMask());
    }

    /**
     * Get the alt down key stroke for key code
     *
     * @param keyCode the key code
     * @return the alt down key stroke
     */
    public KeyStroke getAltDownKeyStroke(int keyCode) {
        return getKeyStroke(keyCode, ALT_DOWN_MASK);
    }

    /**
     * Get UI default translation keys.
     *
     * @return the UI default translation keys
     */
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

    /**
     * Get default error icon.
     *
     * @return get default error icon
     */
    public Icon getErrorIcon() {
        return getIcon("OptionPane.errorIcon");
    }

    /**
     * Get an UI default icon.
     *
     * @param key the icon key
     * @return the icon
     */
    public Icon getIcon(String key) {
        return UIManager.getIcon(key);
    }

    /**
     * Get an UI default font.
     *
     * @param key the icon key
     * @return the font
     */
    public Font getFont(String key) {
        return UIManager.getFont(key);
    }

    /**
     * Get an UI default color.
     *
     * @param key the icon key
     * @return the color
     */
    public Color getColor(String key) {
        return ofNullable(UIManager.getColor(key))
            .map(Color::getRGB)
            .map(Color::new)
            .orElse(null);
    }

    public Color getControlColor() {
        return getColor("control");
    }

    public Color getControlHighlightColor() {
        return getColor("controlHighlight");
    }

    public Color getInactiveCaptionColor() {
        return getColor("inactiveCaption");
    }

    public Color getInactiveCaptionTextColor() {
        return getColor("inactiveCaptionText");
    }

    public Color getControlDarkShadowColor() {
        return getColor("controlDkShadow");
    }

    public Optional<Color> getInactiveBackgroundColor() {
        return ofNullable(getInactiveCaptionColor())
            .or(() -> ofNullable(getColor("ScrollBar.background")))
            .filter(color -> !color.equals(getControlColor()))
            .or(() -> ofNullable(getColor("MenuItem.selectionBackground")));
    }

    public Optional<Color> getInactiveForegroundColor() {
        return ofNullable(getInactiveCaptionTextColor())
            .or(() -> ofNullable(getColor("TextField.foreground")));
    }

    /**
     * Get an UI default border.
     *
     * @param key the icon key
     * @return the border
     */
    public Border getBorder(String key) {
        return UIManager.getBorder(key);
    }

    /**
     * Get the current screen bound.
     *
     * @return the current screen bound
     */
    public Rectangle getScreenBound() {
        return stream(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices())
            .map(GraphicsDevice::getDefaultConfiguration)
            .map(GraphicsConfiguration::getBounds)
            .reduce(new Rectangle(), this::add);
    }

    private Rectangle add(Rectangle r1, Rectangle r2) {
        r1.add(r2);
        return r1;
    }

    /**
     * Get the Gnome icon theme.
     *
     * @return the Gnome icon theme
     */
    public String getGnomeIconTheme() {
        return ofNullable(getDefaultToolkit().getDesktopProperty(GNOME_ICON_THEME_NAME_PROPERTY))
            .map(Object::toString)
            .orElse(DEFAULT_GNOME_ICON_THEME);
    }

    /**
     * Initialize default UI fonts.
     */
    public void initializeDefaultUIFonts() {
        if (isLookAndFeelGTK() && getDefaultToolkit().getScreenResolution() == 96) {
            //this is an workaround for https://bugzilla.redhat.com/show_bug.cgi?id=508185
            //still reproducible in Ubuntu
            getDefaultFontsMap().forEach(
                (key, font) -> setDefaultFont(key, new FontUIResource(font.deriveFont(13f))));
        }
    }

    /**
     * Get the UI default fonts map.
     *
     * @return the UI default fonts map
     */
    public Map<String, Font> getDefaultFontsMap() {
        return UIManager
            .getDefaults()
            .entrySet()
            .stream()
            .map(entry -> new ImmutablePair<>(entry.getKey().toString(), UIManager.get(entry.getKey())))
            .filter(entry -> entry.getValue() instanceof FontUIResource)
            .collect(toMap(Pair::getKey,
                           entry -> (FontUIResource) entry.getValue(),
                           (v1, v2) -> v1,
                           HashMap::new));
    }

    /**
     * Set default UI font.
     *
     * @param key  the key of the font to set
     * @param font the font to set
     */
    public void setDefaultFont(String key, Font font) {
        UIManager.put(key, font);
    }
}
