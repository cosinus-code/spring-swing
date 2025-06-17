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

package org.cosinus.swing.dialog;

import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.window.Dialog;
import org.cosinus.swing.window.Frame;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.awt.BorderLayout.*;
import static java.awt.FlowLayout.RIGHT;
import static java.awt.Font.*;
import static java.lang.Math.min;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.BorderFactory.createTitledBorder;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.cosinus.swing.util.FontUtils.getFontStyleText;

/**
 * Font chooser dialog
 */
public class FontChooser extends Dialog<Font> implements ActionListener, ListSelectionListener {

    private static final int DEFAULT_FONT_SIZE = 10;

    @Autowired
    private Translator translator;

    @Autowired
    private ErrorHandler errorHandler;

    private final String text;

    private final Font font;

    private JButton btnOK, btnCancel;

    private JLabel sample;

    private JList<String> lstFont, lstStyle, lstSize;

    private JTextField txtFont, txtStyle, txtSize;

    private Map<Integer, Integer> fontStylesMap;

    public FontChooser(Dialog dialog, String title, boolean modal, boolean manageWindowSettings, String text, Font font) {
        super(dialog, title, modal, manageWindowSettings);
        this.text = text;
        this.font = font;
    }

    public FontChooser(Frame frame, String title, boolean modal, boolean manageWindowSettings, String text, Font font) {
        super(frame, title, modal, manageWindowSettings);
        this.text = text;
        this.font = font;
    }

    @Override
    public void initComponents() {
        JScrollPane scrFont = new JScrollPane();
        JScrollPane scrStyle = new JScrollPane();
        JScrollPane scrSize = new JScrollPane();

        lstFont = new JList<>();
        lstStyle = new JList<>(new String[]{
            translate("FontChooser.plain"),
            translate("FontChooser.bold"),
            translate("FontChooser.italic"),
            translate("FontChooser.bold") + " " + translate("FontChooser.italic"),
        });
        lstSize = new JList<>(new String[]{"3", "5", "8", "10", "12", "14", "18", "24", "36", "48"});

        lstStyle.setSelectedValue(getFontStyleText(font), true);
        lstSize.setSelectedValue(Integer.toString(font.getSize()), true);

        lstFont.getSelectionModel().addListSelectionListener(this);
        lstStyle.getSelectionModel().addListSelectionListener(this);
        lstSize.getSelectionModel().addListSelectionListener(this);

        scrStyle.setPreferredSize(new Dimension(100, 0));
        scrSize.setPreferredSize(new Dimension(60, 0));

        btnOK = new JButton(translate("FontChooser.ok"));
        btnCancel = new JButton(translate("FontChooser.cancel"));

        txtFont = new JTextField(font.getFamily());
        txtStyle = new JTextField(getFontStyleText(font));
        txtSize = new JTextField(Integer.toString(font.getSize()));

        txtFont.setEnabled(false);
        txtStyle.setEnabled(false);

        btnOK.addActionListener(this);
        btnCancel.addActionListener(this);
        txtSize.addActionListener(this);

        sample = new JLabel(isEmpty(text) ?
                                translate("FontChooser.sample") :
                                text.substring(0, min(40, text.length())));
        sample.setBorder(createTitledBorder(" " + translate("FontChooser.preview") + " "));
        sample.setPreferredSize(new Dimension(0, 100));

        JLabel lblFont = new JLabel(translate("FontChooser.fonts"));
        JLabel lblStyle = new JLabel(translate("FontChooser.style"));
        JLabel lblSize = new JLabel(translate("FontChooser.size"));

        scrFont.getViewport().add(lstFont);
        scrStyle.getViewport().add(lstStyle);
        scrSize.getViewport().add(lstSize);

        JPanel panFontText = new JPanel(new GridLayout(2, 1));
        panFontText.add(lblFont);
        panFontText.add(txtFont);

        JPanel panStyleText = new JPanel(new GridLayout(2, 1));
        panStyleText.add(lblStyle);
        panStyleText.add(txtStyle);

        JPanel panSizeText = new JPanel(new GridLayout(2, 1));
        panSizeText.add(lblSize);
        panSizeText.add(txtSize);

        JPanel panFont = new JPanel(new BorderLayout(5, 5));
        JPanel panStyle = new JPanel(new BorderLayout(5, 5));
        JPanel panSize = new JPanel(new BorderLayout(5, 5));
        JPanel panStyleSize = new JPanel(new BorderLayout(5, 5));

        panFont.add(panFontText, NORTH);
        panFont.add(scrFont, CENTER);

        panStyle.add(panStyleText, NORTH);
        panStyle.add(scrStyle, CENTER);

        panSize.add(panSizeText, NORTH);
        panSize.add(scrSize, CENTER);

        panStyleSize.add(panStyle, WEST);
        panStyleSize.add(panSize, EAST);

        JPanel panButtons = new JPanel(new GridLayout(1, 2, 5, 0));
        panButtons.add(btnOK, null);
        panButtons.add(btnCancel, null);

        JPanel panSouth = new JPanel(new FlowLayout(RIGHT, 0, 0));
        panSouth.setBorder(createEmptyBorder(0, 0, 10, 10));
        panSouth.add(panButtons);

        JPanel panNorth = new JPanel(new BorderLayout(5, 5));
        panNorth.setBorder(createEmptyBorder(10, 10, 10, 10));
        panNorth.add(panFont, CENTER);
        panNorth.add(panStyleSize, EAST);
        panNorth.add(sample, SOUTH);

        lstFont.setModel(new DefaultListModel<>());

        lstFont.setSelectionMode(SINGLE_SELECTION);
        lstStyle.setSelectionMode(SINGLE_SELECTION);
        lstSize.setSelectionMode(SINGLE_SELECTION);

        getContentPane().add(panSouth, SOUTH);
        getContentPane().add(panNorth, CENTER);

        setSize(new Dimension(408, 330));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        loadFonts();
    }

    private void setSampleFont(boolean fromList) {
        if (fromList) {
            txtFont.setText(lstFont.getSelectedValue());
            txtStyle.setText(lstStyle.getSelectedValue());
            if (lstSize.getSelectedIndex() > -1) {
                txtSize.setText(lstSize.getSelectedValue());
            }
        }

        int style = Optional.of(lstStyle.getSelectedIndex())
            .map(getFontStylesMap()::get)
            .orElse(PLAIN);

        int size = ofNullable(txtSize.getText())
            .filter(not(String::isEmpty))
            .map(text -> {
                try {
                    return Integer.parseInt(txtSize.getText());
                } catch (NumberFormatException ex) {
                    errorHandler.handleError(this,
                                             translate("FontChooser.not-number", txtSize.getText()));
                    return null;
                }
            })
            .orElse(DEFAULT_FONT_SIZE);

        txtSize.setText(Integer.toString(size));

        if (!fromList) {
            lstSize.setSelectedValue(txtSize.getText(), true);
        }

        sample.setFont(new Font(lstFont.getSelectedValue(), style, size));
        sample.revalidate();
    }

    private void loadFonts() {
        String[] availableFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        Arrays.stream(availableFonts)
            .forEach(font -> ((DefaultListModel<String>) lstFont.getModel()).addElement(font));
        IntStream.range(0, availableFonts.length)
            .filter(index -> availableFonts[index].equals(font.getFamily()))
            .findFirst()
            .ifPresent(index -> {
                lstFont.setSelectedIndex(index);
                lstFont.scrollRectToVisible(lstFont.getCellBounds(index, index));
            });
    }

    @Override
    protected Font getDialogResponse() {
        return sample.getFont();
    }

    private void setFont() {
        setSampleFont(false);
        dispose();
    }

    public void actionPerformed(ActionEvent evt) {
        try {
            if (evt.getSource() == btnOK) setFont();
            else if (evt.getSource() == btnCancel) cancel();
            else if (evt.getSource() == txtSize) setSampleFont(false);
        } catch (Exception ex) {
            errorHandler.handleError(this, ex);
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        try {
            if (!e.getValueIsAdjusting()) {
                setSampleFont(true);
            }
        } catch (Exception ex) {
            errorHandler.handleError(this, ex);
        }
    }

    private Map<Integer, Integer> getFontStylesMap() {
        if (fontStylesMap == null) {
            fontStylesMap = new HashMap<>();
            fontStylesMap.put(0, PLAIN);
            fontStylesMap.put(1, BOLD);
            fontStylesMap.put(2, ITALIC);
            fontStylesMap.put(3, BOLD | ITALIC);
        }
        return fontStylesMap;
    }

    private String translate(String key, Object... arguments) {
        return translator.translate(key, arguments);
    }
}
