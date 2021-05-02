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

package org.cosinus.swing.form.control;

import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.cosinus.swing.validation.ValidationError;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static java.awt.Cursor.HAND_CURSOR;
import static java.awt.Cursor.TEXT_CURSOR;
import static java.util.Optional.ofNullable;
import static javax.swing.filechooser.FileSystemView.getFileSystemView;
import static org.cosinus.swing.border.Borders.emptyBorder;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;
import static org.cosinus.swing.ui.ApplicationUIHandler.FOLDER_ICON_KEY;

/**
 * Extension of {@link JTextField} used for controlling a {@link File} object.
 * <p>
 * The {@link org.cosinus.swing.dialog.FileChooser} is used to browse for files,
 * when "folder" icon painted on the right part of the control is clicked.
 * <p>
 * It supports the full {@link Control} functionality of highlighting the validation errors.
 */
public class FileTextField extends JTextField implements Control<File>, MouseListener, MouseMotionListener {

    private static final int ICON_X_GAP = 2;

    @Autowired
    private ApplicationUIHandler uiHandler;

    @Autowired
    private DialogHandler dialogHandler;

    private final boolean folderOnly;

    private Icon icon;

    public FileTextField() {
        this(null, false);
    }

    public FileTextField(File file) {
        this(file, false);
    }

    public FileTextField(File file, boolean folderOnly) {
        super(file != null ? file.getAbsolutePath() : null);
        injectContext(this);

        this.folderOnly = folderOnly;

        initIcon();
        if (icon != null) {
            setBorder(new CompoundBorder(
                getBorder(),
                emptyBorder(0, 0, 0, icon.getIconWidth() + 2 * ICON_X_GAP)));
        }
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    protected void initIcon() {
        this.icon = getIcon();
    }

    protected Icon getIcon() {
        return uiHandler != null ?
            ofNullable(uiHandler.getIcon(FOLDER_ICON_KEY))
                .orElseGet(() -> getFileSystemView().getSystemIcon(new File(System.getProperty("user.home")))) :
            null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (icon != null) {
            paintIcon(g, icon);
        }
    }

    private void paintIcon(Graphics g, Icon icon) {
        Insets iconInsets = getBorder().getBorderInsets(this);
        icon.paintIcon(this, g, getWidth() - iconInsets.right + ICON_X_GAP, (this.getHeight() - icon.getIconHeight()) / 2);
    }

    public boolean isMouseOverIcon(MouseEvent e) {
        Insets iconInsets = getBorder().getBorderInsets(this);
        return e.getX() > getWidth() - iconInsets.right + ICON_X_GAP;
    }

    @Override
    public void updateUI() {
        super.updateUI();
        initIcon();
    }

    @Override
    public File getControlValue() {
        return new File(getText());
    }

    @Override
    public void setControlValue(File file) {
        setText(file.getAbsolutePath());
    }

    @Override
    public List<ValidationError> validateValue() {
        return ofNullable(getControlValue())
            .filter(Predicate.not(File::exists))
            .map(file -> createValidationError(folderOnly ?
                                                   "validation.fileNotFound" :
                                                   "validation.folderNotFound"))
            .map(Collections::singletonList)
            .orElseGet(Collections::emptyList);
    }

    @Override
    public void processFocusEvent(FocusEvent event) {
        processFocusEvent(event, super::processFocusEvent);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isMouseOverIcon(e)) {
            dialogHandler.chooseFile(null, folderOnly, this);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        setCursor(Cursor.getPredefinedCursor(isMouseOverIcon(e) ? HAND_CURSOR : TEXT_CURSOR));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }
}
