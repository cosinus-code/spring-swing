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

package org.cosinus.swing.ui;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.cosinus.swing.color.SystemColor;
import org.cosinus.swing.translate.Translator;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.awt.Cursor.DEFAULT_CURSOR;
import static java.awt.Cursor.HAND_CURSOR;
import static java.awt.Cursor.getPredefinedCursor;
import static java.awt.Font.PLAIN;
import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.event.InputEvent.ALT_DOWN_MASK;
import static java.awt.event.InputEvent.META_DOWN_MASK;
import static java.awt.event.KeyEvent.VK_BACK_SPACE;
import static java.awt.event.KeyEvent.VK_DELETE;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_TAB;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.concat;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC;
import static org.cosinus.swing.border.Borders.emptyBorder;
import static org.cosinus.swing.color.SystemColor.CONTROL;
import static org.cosinus.swing.color.SystemColor.CONTROL_DK_SHADOW;
import static org.cosinus.swing.color.SystemColor.CONTROL_HIGHLIGHT;
import static org.cosinus.swing.color.SystemColor.INACTIVE_CAPTION;
import static org.cosinus.swing.color.SystemColor.INACTIVE_CAPTION_TEXT;
import static org.cosinus.swing.color.SystemColor.INTERNAL_FRAME_INACTIVE_TITLE_BACKGROUND;
import static org.cosinus.swing.color.SystemColor.TEXTAREA_INACTIVE_FOREGROUND;
import static org.cosinus.swing.color.SystemColor.TEXT_PANE_SELECTION_BACKGROUND;
import static org.cosinus.swing.util.FontUtils.getFontDescription;

/**
 * UIManager handler.
 * <p>
 * The intention is to use this handler methods instead UIManager static methods,
 * to isolate UIManager for easier reimplementation.
 */
@Slf4j
public class ApplicationUIHandler {

    public static final String OPTION_PANE_MESSAGE_DIALOG_TITLE = "OptionPane.messageDialogTitle";

    public static final String OPTION_PANE_INPUT_DIALOG_TITLE = "OptionPane.inputDialogTitle";

    public static final String OPTION_PANE_ERROR_ICON = "OptionPane.errorIcon";

    public static final String FOLDER_ICON_KEY = "FileView.directoryIcon";

    public static final String FILE_ICON_KEY = "FileView.fileIcon";

    public static final String TEXT_FONT_KEY = "TextField.font";

    public static final String LABEL_FONT_KEY = "Label.font";

    private Map<String, LookAndFeelInfo> lookAndFeelMap;

    private final Translator translator;

    private final Set<LookAndFeelInfo> lookAndFeels;

    public ApplicationUIHandler(final Translator translator,
                                final Set<LookAndFeelInfo> lookAndFeels) {
        this.translator = translator;
        this.lookAndFeels = lookAndFeels;
    }

    /**
     * Translate default UI related texts.
     */
    public void translateDefaultUILabels() {
        stream(SystemText.values())
            .map(SystemText::getKey)
            .forEach(this::translateDefaultUILabel);
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
            log.error("Failed to translate ui key: {}", key, ex);
        }
    }

    /**
     * Get a UI text.
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
     * @param isFolder true if the file is  a folder
     * @return the default icon of the file
     */
    public Optional<Icon> getDefaultFileIcon(boolean isFolder) {
        return ofNullable(UIManager.get(isFolder ? FOLDER_ICON_KEY : FILE_ICON_KEY))
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
            log.error("Failed to load lookAndFeel: {}", lookAndFeel, e);
        }
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

    public Optional<String> findLookAndFeelByName(String nameToFind) {
        return getAvailableLookAndFeels()
            .values()
            .stream()
            .filter(lookAdnFeel -> lookAdnFeel.getName().startsWith(nameToFind))
            .findFirst()
            .map(LookAndFeelInfo::getClassName);
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
     * Check if the current look-and-feel is GTK.
     *
     * @return true if the current look-and-feel is GTK
     */
    public boolean isLookAndFeelMac() {
        return getLookAndFeel().startsWith("Mac");
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
     * Get the current control down keystroke for key code
     *
     * @param keyCode the key code
     * @return the current control down keystroke
     */
    public KeyStroke getControlDownKeyStroke(int keyCode) {
        return getKeyStroke(keyCode, getControlDownKeyMask());
    }

    /**
     * Get the alt down keystroke for key code
     *
     * @param keyCode the key code
     * @return the alt down keystroke
     */
    public KeyStroke getAltDownKeyStroke(int keyCode) {
        return getKeyStroke(keyCode, ALT_DOWN_MASK);
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

    public Font getDefaultMonospacedFont() {
        return new Font(Font.MONOSPACED, PLAIN, 12);
    }

    /**
     * Get an UI default int.
     *
     * @param key the icon key
     * @return the int
     */
    public int getInt(String key) {
        return UIManager.getInt(key);
    }

    /**
     * Get a UI default color.
     *
     * @param color the color
     * @return the color
     */
    public Color getColor(SystemColor color) {
        return ofNullable(color)
            .map(SystemColor::getKey)
            .map(UIManager::getColor)
            .map(Color::getRGB)
            .map(Color::new)
            .orElse(null);
    }

    public Color getControlColor() {
        return getColor(CONTROL);
    }

    public Color getControlHighlightColor() {
        return getColor(CONTROL_HIGHLIGHT);
    }

    public Color getInactiveCaptionColor() {
        return getColor(INACTIVE_CAPTION);
    }

    public Color getInactiveCaptionTextColor() {
        return getColor(INACTIVE_CAPTION_TEXT);
    }

    public Color getControlDarkShadowColor() {
        return getColor(CONTROL_DK_SHADOW);
    }

    public Optional<Color> getInactiveBackgroundColor() {
        return ofNullable(getColor(INTERNAL_FRAME_INACTIVE_TITLE_BACKGROUND))
            .filter(color -> !color.equals(getInactiveForegroundColor().orElse(null)))
            .or(() -> ofNullable(getInactiveCaptionColor()));
    }

    public Optional<Color> getInactiveForegroundColor() {
        return ofNullable(getColor(TEXTAREA_INACTIVE_FOREGROUND))
            .or(() -> ofNullable(getInactiveCaptionTextColor()));
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
     * Initialize default UI fonts.
     */
    public void initializeDefaultUIFonts() {
        if (isLookAndFeelGTK() && getDefaultToolkit().getScreenResolution() == 96) {
            //this is a workaround for https://bugzilla.redhat.com/show_bug.cgi?id=508185
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
            .keySet()
            .stream()
            .map(o -> new ImmutablePair<>(o.toString(), UIManager.get(o)))
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

    public boolean isProgressTextAllowed() {
        return !isLookAndFeelMac();
    }

    public Cursor getHandCursor() {
        return getCursor(HAND_CURSOR);
    }

    public Cursor getDefaultCursor() {
        return getCursor(DEFAULT_CURSOR);
    }

    public Cursor getCursor(int cursorId) {
        return getPredefinedCursor(cursorId);
    }

    public void makeSimpleButton(AbstractButton button) {
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBorder(emptyBorder(0));
        button.setPreferredSize(new Dimension(30, 30));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(getColor(TEXT_PANE_SELECTION_BACKGROUND));
                button.setCursor(getHandCursor());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(null);
                button.setCursor(getDefaultCursor());
            }
        });
    }

    public String computeColorThemeChecksum() {
        return computeUIThemeChecksum(value -> value instanceof Color);
    }

    public String computeUIThemeChecksum() {
        return computeUIThemeChecksum(value -> true);
    }

    public String computeUIThemeChecksum(final Predicate<Object> filter) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            UIManager
                .getDefaults()
                .keySet()
                .stream()
                .filter(Objects::nonNull)
                .filter(filter)
                .forEach(value -> {
                    switch (value) {
                        case Color color -> {
                            sha.update((byte) color.getRed());
                            sha.update((byte) color.getGreen());
                            sha.update((byte) color.getBlue());
                        }
                        case Font font -> sha.update(getFontDescription(font).getBytes());
                        case Integer intValue -> sha.update((byte) intValue.intValue());
                        case Boolean booleanValue -> sha.update((byte) (booleanValue ? 1 : 0));
                        default -> sha.update(value.toString().getBytes());
                    }
                });
            return new String(sha.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isHexaChar(int character) {
        return character >= '0' && character <= '9'
            || character >= 'A' && character <= 'F'
            || character >= 'a' && character <= 'f';
    }

    public boolean isMovementKey(KeyEvent keyEvent) {
        return keyEvent.getKeyCode() == KeyEvent.VK_LEFT
            || keyEvent.getKeyCode() == KeyEvent.VK_RIGHT
            || keyEvent.getKeyCode() == KeyEvent.VK_UP
            || keyEvent.getKeyCode() == KeyEvent.VK_DOWN
            || keyEvent.getKeyCode() == KeyEvent.VK_HOME
            || keyEvent.getKeyCode() == KeyEvent.VK_END
            || keyEvent.getKeyCode() == KeyEvent.VK_PAGE_UP
            || keyEvent.getKeyCode() == KeyEvent.VK_PAGE_DOWN;
    }

    public boolean isDeleteKey(KeyEvent keyEvent) {
        return keyEvent.getKeyCode() == KeyEvent.VK_DELETE
            || keyEvent.getKeyCode() == KeyEvent.VK_BACK_SPACE;
    }

    public boolean isEditorKey(KeyEvent keyEvent) {
        return keyEvent.getKeyCode() == VK_ENTER
            || keyEvent.getKeyCode() == VK_TAB
            || keyEvent.getKeyCode() == VK_DELETE
            || keyEvent.getKeyCode() == VK_BACK_SPACE;
    }

    public boolean isLetter(char character) {
        return character >= ' ' && character <= '~';
    }

}
