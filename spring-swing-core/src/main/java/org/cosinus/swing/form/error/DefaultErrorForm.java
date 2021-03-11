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

package org.cosinus.swing.form.error;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.action.EscapeAction;
import org.cosinus.swing.context.SwingAutowired;
import org.cosinus.swing.form.Dialog;
import org.cosinus.swing.form.Frame;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.ui.ApplicationUIHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static java.awt.BorderLayout.*;
import static java.lang.String.format;
import static javax.swing.JPanel.WHEN_IN_FOCUSED_WINDOW;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.cosinus.swing.border.Borders.borderEmpty;
import static org.cosinus.swing.border.Borders.insetsEmpty;

/**
 * Default implementation of {@link ErrorForm} as dialog
 */
public class DefaultErrorForm extends Dialog<Void> implements ErrorForm, ActionListener {

    private static final Logger LOG = LogManager.getLogger(DefaultErrorForm.class);

    private static final String ERROR_WINDOW_TITLE = "Error";

    private static final String ERROR_MESSAGE = "<html><body style='width: 300px'><p>%s</p><br/>%s</html>";

    private JLabel txtDescription;

    private JButton btnContinue, btnExit, btnDetails;

    private JTextArea txaDetails;

    private JScrollPane panDetails;

    private int detailsHeight = 280;

    @SwingAutowired
    private Translator translator;

    @SwingAutowired
    private ApplicationUIHandler uiHandler;

    /**
     * Creates new form ErrorForm
     */
    public DefaultErrorForm(Frame parent,
                            boolean modal) {
        super(parent,
              ERROR_WINDOW_TITLE,
              modal);
        initComponents();
    }

    /**
     * Creates new form ErrorForm
     */
    public DefaultErrorForm(Dialog parent,
                            boolean modal) {
        super(parent,
              ERROR_WINDOW_TITLE,
              modal);
        initComponents();
    }

    @Override
    public void initComponents() {
        try {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setTitle(translate("error"));

            txtDescription = new JLabel();

            btnExit = new JButton(translate("form_error_exit"));
            btnContinue = new JButton(translate("form_error_continue"));
            btnDetails = new JButton(translate("form_error_details"));

            btnExit.setMargin(insetsEmpty());
            btnContinue.setMargin(insetsEmpty());
            btnDetails.setMargin(insetsEmpty());

            Dimension buttonDimension = new Dimension(90, 28);
            btnExit.setPreferredSize(buttonDimension);
            btnContinue.setPreferredSize(buttonDimension);
            btnDetails.setPreferredSize(buttonDimension);

            btnExit.addActionListener(this);
            btnContinue.addActionListener(this);
            btnDetails.addActionListener(this);

            JPanel panImage = new JPanel(new BorderLayout());
            panImage.setBorder(borderEmpty(10));
            panImage.add(new JLabel(uiHandler.getErrorIcon()), NORTH);

            txaDetails = new JTextArea();
            txaDetails.setEditable(false);
            txaDetails.setFont(uiHandler.getLabelFont());
            txaDetails.setBackground(uiHandler.getColor("inactiveCaption"));

            panDetails = new JScrollPane();
            panDetails.setViewportView(txaDetails);
            panDetails.setVisible(false);

            JPanel panButtonsRight = new JPanel();
            panButtonsRight.setLayout(new FlowLayout(FlowLayout.RIGHT));
            panButtonsRight.add(btnContinue);
            panButtonsRight.add(btnExit);

            JPanel panButtonsLeft = new JPanel();
            panButtonsLeft.add(btnDetails);

            JPanel panButtons = new JPanel(new BorderLayout());
            panButtons.add(panButtonsRight, EAST);
            panButtons.add(panButtonsLeft, WEST);

            JPanel panData = new JPanel(new BorderLayout(10, 10));
            panData.setBorder(borderEmpty(2));
            panData.add(panImage, WEST);
            panData.add(txtDescription, CENTER);
            panData.add(panButtons, SOUTH);

            getContentPane().add(panData, NORTH);
            getContentPane().add(panDetails, CENTER);

            panData.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(getKeyStroke("ESCAPE"), "escape");
            panData.getActionMap().put("escape", new EscapeAction(this));
            panData.setBorder(borderEmpty(5));
        } catch (Exception ex) {
            LOG.error("Exception while initiating error form", ex);
        }
    }

    public void showError(Throwable throwable) {
        txtDescription.setText(format(ERROR_MESSAGE,
                                      translate("form_error_message"),
                                      throwable.getLocalizedMessage()));

        try (StringWriter writer = new StringWriter()) {
            throwable.printStackTrace(new PrintWriter(writer));
            txaDetails.setText(writer.toString());
            txaDetails.setCaretPosition(0);
        } catch (IOException ex) {
            LOG.error("", ex);
        }

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

    private void close() {
        setVisible(false);
    }

    private void exit() {
        System.exit(0);
    }

    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == btnExit) {
                exit();
            } else if (e.getSource() == btnContinue) {
                close();
            } else if (e.getSource() == btnDetails) {
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
