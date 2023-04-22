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

package org.cosinus.swing.dialog;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.window.Dialog;
import org.cosinus.swing.preference.dialog.PreferencesDialogProvider;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.ui.ApplicationUIHandler;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;
import static java.util.stream.IntStream.range;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFileChooser.DIRECTORIES_ONLY;
import static javax.swing.JOptionPane.*;
import static org.cosinus.swing.dialog.DialogOption.retryWithIgnore;
import static org.cosinus.swing.dialog.DialogOption.retryWithSkip;
import static org.cosinus.swing.ui.ApplicationUIHandler.OPTION_PANE_INPUT_DIALOG_TITLE;
import static org.cosinus.swing.ui.ApplicationUIHandler.OPTION_PANE_MESSAGE_DIALOG_TITLE;

/**
 * Handler for options panels
 */
public class DialogHandler {

    private static final Logger LOG = LogManager.getLogger(DialogHandler.class);

    private static final String ERROR = "error";

    private static final String SELECT = "select";

    private final Translator translator;

    private final ApplicationUIHandler uiHandler;

    private final PreferencesDialogProvider preferencesDialogProvider;

    public DialogHandler(Translator translator,
                         ApplicationUIHandler uiHandler,
                         PreferencesDialogProvider preferencesDialogProvider) {
        this.translator = translator;
        this.uiHandler = uiHandler;
        this.preferencesDialogProvider = preferencesDialogProvider;
    }

    public <T> Dialog<T> showDialog(Supplier<Dialog<T>> dialogInitiator) {
        Dialog<T> dialog = dialogInitiator.get();
        dialog.init();
        dialog.setVisible(true);
        return dialog;
    }

    public boolean confirm(String message) {
        return confirm(null, message);
    }

    public boolean confirm(Component comp,
                           String message) {
        return confirm(comp,
                       message,
                       translator.translate("question"),
                       YES_NO_CANCEL_OPTION);
    }

    public boolean confirm(Component comp, String message, String title, int options) {
        return YES_OPTION == showConfirmDialog(comp, message, title, options);
    }

    public DialogOption retryWithSkipDialog(Window parentWindow, String message) {
        return retryDialog(parentWindow, message, retryWithSkip());
    }

    public DialogOption retryWithIgnoreDialog(Window parentWindow, String message) {
        return retryDialog(parentWindow, message, retryWithIgnore());
    }

    public DialogOption retryDialog(Window parentWindow, String message, DialogOption[] options) {
        return showCustomOptionDialog(parentWindow,
                                      translator.translate(ERROR),
                                      message,
                                      DEFAULT_OPTION,
                                      INFORMATION_MESSAGE,
                                      uiHandler.getIcon("OptionPane.errorIcon"),
                                      1,
                                      0,
                                      options);
    }

    public void showInfo(String message) {
        showInfo(null, message);
    }

    public void showInfo(Component comp, String message) {
        JOptionPane.showMessageDialog(comp, message);
//        showMessageDialog(
//            comp,
//            message,
//            translator.translate("information"),
//            INFORMATION_MESSAGE);
    }

    public int showConfirmation(String message) {
        return showConfirmation(null, message);
    }

    public int showConfirmation(Component comp, String message) {
        return showConfirmation(
            comp,
            message,
            translator.translate("question"),
            YES_NO_CANCEL_OPTION);
    }

    public int showConfirmation(Component comp, String message, String title, int options) {
        return showConfirmDialog(comp, message, title, options);
    }

    public int showConfirmationDialog(Component comp, String message, String title, int options) {
        return showConfirmDialog(comp, message, title, options);
    }

    public Optional<String> showInputDialog(Object message) {
        return showInputDialog(null, message);
    }

    public Optional<String> showInputDialog(Component parentComponent, Object message) {
        return showInputDialog(parentComponent,
                               message,
                               uiHandler.getString(OPTION_PANE_INPUT_DIALOG_TITLE),
                               QUESTION_MESSAGE);
    }

    public Optional<String> showInputDialog(Component parentComponent, Object message, String title, int messageType) {
        return ofNullable(showInputDialog(parentComponent,
                                          message,
                                          title,
                                          messageType,
                                          null,
                                          null,
                                          null))
            .map(Object::toString);
    }

    public Object showInputDialog(Component parentComponent,
                                  Object message,
                                  String title,
                                  int messageType,
                                  Icon icon,
                                  Object[] selectionValues,
                                  Object initialSelectionValue) {
        OptionsDialog pane = new OptionsDialog(message,
                                               messageType,
                                               OK_CANCEL_OPTION);

        pane.setWantsInput(true);
        pane.setSelectionValues(selectionValues);
        pane.setInitialSelectionValue(initialSelectionValue);
        pane.setComponentOrientation(parentComponent);

        pane.showDialog(parentComponent,
                        title);
        Object value = pane.getInputValue();
        return value == UNINITIALIZED_VALUE ? null : value;
    }

    public void showMessageDialog(Component parentComponent, Object message) {
        showMessageDialog(parentComponent,
                          message,
                          uiHandler.getString(OPTION_PANE_MESSAGE_DIALOG_TITLE),
                          INFORMATION_MESSAGE);
    }

    public void showMessageDialog(Component parentComponent, Object message, String title, int messageType) {
        showMessageDialog(parentComponent, message, title, messageType, null);
    }

    public void showMessageDialog(Component parentComponent,
                                  Object message,
                                  String title,
                                  int messageType,
                                  Icon icon) {
        showOptionDialog(parentComponent,
                         title,
                         message,
                         DEFAULT_OPTION,
                         messageType,
                         icon,
                         null);
    }

    public int showOptionDialog(Component parentComponent,
                                String title,
                                Object message,
                                int optionType,
                                int messageType,
                                Icon icon,
                                Object[] options) {
        return showOptionDialog(parentComponent,
                                title,
                                message,
                                optionType,
                                messageType,
                                icon,
                                1,
                                0,
                                options);
    }

    public int showOptionDialog(Component parentComponent,
                                String title,
                                Object message,
                                int optionType,
                                int messageType,
                                Icon icon,
                                int rows,
                                int width,
                                Object[] options) {
        Object selectedValue = showCustomOptionDialog(parentComponent,
                                                      title,
                                                      message,
                                                      messageType,
                                                      optionType,
                                                      icon,
                                                      rows,
                                                      width,
                                                      options);

        if (selectedValue == null) {
            return CLOSED_OPTION;
        }
        if (options == null) {
            return selectedValue instanceof Integer ?
                (Integer) selectedValue :
                CLOSED_OPTION;
        }

        return range(0, options.length)
            .filter(i -> options[i].equals(selectedValue))
            .findFirst()
            .orElse(CLOSED_OPTION);
    }

    public <T> T showCustomOptionDialog(Component parentComponent,
                                        String title,
                                        Object message,
                                        int messageType,
                                        int optionType,
                                        Icon icon,
                                        int rows,
                                        int width,
                                        T[] options) {
        String[] translatedOptions = ofNullable(options)
            .stream()
            .flatMap(Arrays::stream)
            .map(Object::toString)
            .map(translator::translate)
            .toArray(String[]::new);
        OptionsDialog dialog = new OptionsDialog(message,
                                                 messageType,
                                                 optionType,
                                                 icon,
                                                 translatedOptions);

        dialog.setComponentOrientation(parentComponent);
        dialog.setRows(rows);

        RunnableFuture<Object> show = new FutureTask<>(
            () -> dialog.showDialog(parentComponent,
                                    title,
                                    width));
        SwingUtilities.invokeLater(show);

        try {
            String selectedValue = show.get().toString();
            return ofNullable(options)
                .flatMap(opt -> range(0, options.length)
                    .filter(i -> translatedOptions[i].equals(selectedValue))
                    .mapToObj(i -> options[i])
                    .findFirst())
                .orElse(null);
        } catch (InterruptedException | ExecutionException ex) {
            LOG.error("Error occurred during showing custom option dialog", ex);
            return null;
        }
    }

    public File chooseFile(Window parent) {
        return chooseFile(parent, false, null, null);
    }

    public File chooseFile(Window parent, boolean folderOnly) {
        return chooseFile(parent, folderOnly, null, null);
    }

    public File chooseFile(Window parent, boolean folderOnly, JTextField textField) {
        return chooseFile(parent, folderOnly, textField, null);
    }

    public File chooseFile(Window parent, boolean folderOnly, JTextField textField, File fileStart) {
        File file = ofNullable(textField)
            .map(JTextField::getText)
            .map(File::new)
            .filter(File::exists)
            .orElseGet(() -> ofNullable(fileStart)
                .filter(File::exists)
                .orElse(null));

        FileChooser choose = new FileChooser(file);
        if (folderOnly) {
            choose.setFileSelectionMode(DIRECTORIES_ONLY);
        }
        if (choose.showDialog(parent, translator.translate(SELECT)) != APPROVE_OPTION) {
            return null;
        }

        File currentFile = choose.getSelectedFile();
        if (textField != null) {
            textField.setText(currentFile.getPath());
        }
        return currentFile;
    }

    public File chooseFileToSave(Window parent) {
        FileChooser choose = new FileChooser();
        if (choose.showSaveDialog(parent) != APPROVE_OPTION) {
            return null;
        }

        File currentFile = choose.getSelectedFile();
        if (currentFile != null &&
            !currentFile.exists() &&
            !confirm(parent, translator.translate("fileAlreadyExists", currentFile.getName()))) {
            return null;
        }
        return currentFile;
    }

    public void showPreferencesDialog(Frame parentFrame) {
        showDialog(() -> preferencesDialogProvider.getPreferencesDialog(parentFrame));
    }

    public Optional<Font> chooseFont(Font font) {
        return chooseFont((org.cosinus.swing.window.Frame) null, "", true, true, null, font);
    }

    public Optional<Font> chooseFont(org.cosinus.swing.window.Frame frame, Font font) {
        return chooseFont(frame, "", true, true, null, font);
    }

    public Optional<Font> chooseFont(org.cosinus.swing.window.Frame frame, String title, Font font) {
        return chooseFont(frame, title, true, true, null, font);
    }

    public Optional<Font> chooseFont(org.cosinus.swing.window.Frame frame, String title, String text, Font font) {
        return chooseFont(frame, title, true, true, text, font);
    }

    public Optional<Font> chooseFont(org.cosinus.swing.window.Frame frame, String title, boolean modal, boolean manageWindowSettings, String text, Font font) {
        return showDialog(() -> new FontChooser(frame, title, modal, manageWindowSettings, text, font)).response();
    }

    public Optional<Font> chooseFont(Dialog<?> dialog, Font font) {
        return showDialog(() -> new FontChooser(dialog, "", true, true, null, font)).response();
    }

    public Optional<Font> chooseFont(Dialog<?> dialog, String title, Font font) {
        return showDialog(() -> new FontChooser(dialog, title, true, true, null, font)).response();
    }

    public Optional<Font> chooseFont(Dialog<?> dialog, String title, String text, Font font) {
        return showDialog(() -> new FontChooser(dialog, title, true, true, text, font)).response();
    }

    public Optional<Font> chooseFont(Dialog<?> dialog, String title, boolean modal, boolean manageWindowSettings, String text, Font font) {
        return showDialog(() -> new FontChooser(dialog, title, modal, manageWindowSettings, text, font)).response();
    }
}
