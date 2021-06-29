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

package org.cosinus.swing.error.form;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.action.EscapeAction;
import org.cosinus.swing.error.SwingSevereException;
import org.cosinus.swing.error.TranslatableRuntimeException;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.cosinus.swing.window.Dialog;
import org.cosinus.swing.window.Frame;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;
import java.util.Optional;

import static java.awt.BorderLayout.*;
import static java.lang.String.format;
import static javax.swing.JPanel.WHEN_IN_FOCUSED_WINDOW;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.cosinus.swing.border.Borders.emptyBorder;
import static org.cosinus.swing.border.Borders.emptyInsets;
import static java.util.Optional.ofNullable;

/**
 * Default implementation of {@link ErrorForm} as dialog
 */
public class DefaultErrorForm extends Dialog<Void> implements ErrorForm, ActionListener {

    private static final Logger LOG = LogManager.getLogger(DefaultErrorForm.class);

    private static final String ERROR_WINDOW_TITLE = "Error";

    private static final String ERROR_MESSAGE = "<html><body style='width: 300px'><p>%s</p><br/>%s</html>";

    private JLabel txtDescription;

    private JButton continueButton, exitButton, detailsButton;

    private JTextArea txaDetails;

    private JScrollPane panDetails;

    private int detailsHeight = 280;

    @Autowired
    private Translator translator;

    @Autowired
    private ApplicationUIHandler uiHandler;

    public DefaultErrorForm(Frame parent) {
        super(parent, ERROR_WINDOW_TITLE, true, false);
    }

    public DefaultErrorForm(Dialog parent) {
        super(parent, ERROR_WINDOW_TITLE, true, false);
    }

    @Override
    public void initComponents() {
        try {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setTitle(translate("ErrorForm.title"));

            txtDescription = new JLabel();

            exitButton = new JButton(translate("ErrorForm.exit"));
            continueButton = new JButton(translate("ErrorForm.continue"));
            detailsButton = new JButton(translate("ErrorForm.details"));

            exitButton.setMargin(emptyInsets());
            continueButton.setMargin(emptyInsets());
            detailsButton.setMargin(emptyInsets());

            Dimension buttonDimension = new Dimension(90, 28);
            exitButton.setPreferredSize(buttonDimension);
            continueButton.setPreferredSize(buttonDimension);
            detailsButton.setPreferredSize(buttonDimension);

            exitButton.addActionListener(this);
            continueButton.addActionListener(this);
            detailsButton.addActionListener(this);

            JPanel panImage = new JPanel(new BorderLayout());
            panImage.setBorder(emptyBorder(10));
            panImage.add(new JLabel(uiHandler.getErrorIcon()), NORTH);

            txaDetails = new JTextArea();
            txaDetails.setEditable(false);
            txaDetails.setFont(uiHandler.getLabelFont());
            txaDetails.setBackground(uiHandler.getInactiveCaptionColor());
            txaDetails.setForeground(uiHandler.getInactiveCaptionTextColor());

            panDetails = new JScrollPane();
            panDetails.setViewportView(txaDetails);
            panDetails.setVisible(false);

            JPanel panButtonsRight = new JPanel();
            panButtonsRight.setLayout(new FlowLayout(FlowLayout.RIGHT));
            panButtonsRight.add(continueButton);
            panButtonsRight.add(exitButton);

            JPanel panButtonsLeft = new JPanel();
            panButtonsLeft.add(detailsButton);

            JPanel panButtons = new JPanel(new BorderLayout());
            panButtons.add(panButtonsRight, EAST);
            panButtons.add(panButtonsLeft, WEST);

            JPanel panData = new JPanel(new BorderLayout(10, 10));
            panData.setBorder(emptyBorder(2));
            panData.add(panImage, WEST);
            panData.add(txtDescription, CENTER);
            panData.add(panButtons, SOUTH);

            getContentPane().add(panData, NORTH);
            getContentPane().add(panDetails, CENTER);

            panData.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(getKeyStroke("ESCAPE"), "escape");
            panData.getActionMap().put("escape", new EscapeAction(this));
            panData.setBorder(emptyBorder(5));
        } catch (Exception ex) {
            LOG.error("Exception while initiating error form", ex);
        }
    }

    @Override
    public void showError(Throwable throwable) {
        boolean severe = throwable instanceof SwingSevereException;
        txtDescription.setText(format(ERROR_MESSAGE,
                                      translate(severe ? "ErrorForm.severe-message" : "ErrorForm.message"),
                                      throwable.getLocalizedMessage()));

        try (StringWriter writer = new StringWriter()) {
            throwable.printStackTrace(new PrintWriter(writer));
            txaDetails.setText(writer.toString());
            txaDetails.setCaretPosition(0);
        } catch (IOException ex) {
            LOG.error("", ex);
        }

        continueButton.setVisible(!severe);

        pack();
        centerWindow();
        setVisible(true);
    }

    private void showDetails() {
        if (panDetails.isVisible()) {
            detailsHeight = panDetails.getHeight();
            setSize(getWidth(), getHeight() - panDetails.getHeight());
            panDetails.setVisible(false);
        } else {
            setSize(getWidth(), getHeight() + detailsHeight);
            panDetails.setVisible(true);
        }
        centerWindow();
        validate();
    }

    @Override
    public void close() {
        setVisible(false);
    }

    private void exit() {
        System.exit(0);
    }

    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == exitButton) {
                exit();
            } else if (e.getSource() == continueButton) {
                close();
            } else if (e.getSource() == detailsButton) {
                showDetails();
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
        }
    }

    private String translate(String key) {
        return translator.translate(key);
    }
}
