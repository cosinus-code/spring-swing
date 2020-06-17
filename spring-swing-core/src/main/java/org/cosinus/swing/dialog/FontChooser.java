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

import org.cosinus.swing.context.SwingApplicationContext;
import org.cosinus.swing.context.SwingAutowired;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.form.Dialog;
import org.cosinus.swing.form.Frame;
import org.cosinus.swing.translate.Translator;

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
import static org.cosinus.swing.util.FontUtils.getFontStyle;

/**
 * Font chooser dialog
 */
public class FontChooser extends Dialog<Font> implements ActionListener, ListSelectionListener {

    private static final int DEFAULT_FONT_SIZE = 10;

    private JButton btnOK, btnCancel;
    private JLabel sample;
    private JScrollPane scrFont, scrStyle, scrSize;
    private JList<String> lstFont, lstStyle, lstSize;
    private JTextField txtFont, txtStyle, txtSize;
    private Font font;

    private Map<Integer, Integer> fontStylesMap;

    @SwingAutowired
    private Translator translator;

    @SwingAutowired
    private ErrorHandler errorHandler;

    public FontChooser(SwingApplicationContext context,
                       Dialog dialog, String title, boolean modal, String text, Font font) {
        super(context, dialog, title, modal);
        init(text, font);
    }

    public FontChooser(SwingApplicationContext context,
                       Frame frame, String title, boolean modal, String text, Font font) {
        super(context, frame, title, modal);
        init(text, font);
    }

    public void init(String text, Font font) {
        this.font = font;

        scrFont = new JScrollPane();
        scrStyle = new JScrollPane();
        scrSize = new JScrollPane();

        lstFont = new JList<>();
        lstStyle = new JList<>(new String[]{
                translate("form_font_plain"),
                translate("form_font_bold"),
                translate("form_font_italic"),
                translate("form_font_bold") + " " + translate("form_font_italic"),
        });
        lstSize = new JList<>(new String[]{"3", "5", "8", "10", "12", "14", "18", "24", "36", "48"});

        lstStyle.setSelectedValue(getFontStyle(font), true);
        lstSize.setSelectedValue(Integer.toString(font.getSize()), true);

        lstFont.getSelectionModel().addListSelectionListener(this);
        lstStyle.getSelectionModel().addListSelectionListener(this);
        lstSize.getSelectionModel().addListSelectionListener(this);

        scrStyle.setPreferredSize(new Dimension(100, 0));
        scrSize.setPreferredSize(new Dimension(60, 0));

        btnOK = new JButton(translate("form_font_ok"));
        btnCancel = new JButton(translate("form_font_cancel"));

        txtFont = new JTextField(font.getFamily());
        txtStyle = new JTextField(getFontStyle(font));
        txtSize = new JTextField(Integer.toString(font.getSize()));

        txtFont.setEnabled(false);
        txtStyle.setEnabled(false);

        btnOK.addActionListener(this);
        btnCancel.addActionListener(this);
        txtSize.addActionListener(this);

        sample = new JLabel(isEmpty(text) ?
                                    translate("form_font_sample") :
                                    text.substring(0, min(40, text.length())));
        sample.setBorder(createTitledBorder(" " + translate("form_font_preview") + " "));
        sample.setPreferredSize(new Dimension(0, 100));

        JLabel lblFont = new JLabel(translate("form_font_fonts"));
        JLabel lblStyle = new JLabel(translate("form_font_style"));
        JLabel lblSize = new JLabel(translate("form_font_size"));

        scrFont.getViewport().add(lstFont);
        scrStyle.getViewport().add(lstStyle);
        scrSize.getViewport().add(lstSize);

        LayoutManager gridLayout = new GridLayout(2, 1);
        LayoutManager borderLayout = new BorderLayout(5, 5);

        JPanel panFontText = new JPanel(gridLayout);
        panFontText.add(lblFont);
        panFontText.add(txtFont);

        JPanel panStyleText = new JPanel(gridLayout);
        panStyleText.add(lblStyle);
        panStyleText.add(txtStyle);

        JPanel panSizeText = new JPanel(gridLayout);
        panSizeText.add(lblSize);
        panSizeText.add(txtSize);

        JPanel panFont = new JPanel(borderLayout);
        panFont.add(panFontText, NORTH);
        panFont.add(scrFont, CENTER);

        JPanel panStyle = new JPanel(borderLayout);
        panStyle.add(panStyleText, NORTH);
        panStyle.add(scrStyle, CENTER);

        JPanel panSize = new JPanel(borderLayout);
        panSize.add(panSizeText, NORTH);
        panSize.add(scrSize, CENTER);

        JPanel panStyleSize = new JPanel(borderLayout);
        panStyleSize.add(panStyle, WEST);
        panStyleSize.add(panSize, EAST);

        JPanel panButtons = new JPanel(new GridLayout(1, 2, 5, 0));
        panButtons.add(btnOK, null);
        panButtons.add(btnCancel, null);

        JPanel panSouth = new JPanel(new FlowLayout(RIGHT, 0, 0));
        panSouth.setBorder(createEmptyBorder(0, 0, 10, 10));
        panSouth.add(panButtons);

        JPanel panNorth = new JPanel(borderLayout);
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
                                                 translate("form_font_not_a_number", txtSize.getText()));
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
