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

package org.cosinus.swing.text;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.lang.String.join;

/**
 * Wrapped text builder.
 * <p>
 * It allows to wrap a text with or without separators.
 */
public class WrappedTextBuilder extends ArrayList<String> {

    private final int width;

    private final FontMetrics fontMetrics;

    public WrappedTextBuilder(int width, JComponent component) {
        this(width, component.getFontMetrics(component.getFont()));
    }

    public WrappedTextBuilder(int width, FontMetrics fontMetrics) {
        this.width = width;
        this.fontMetrics = fontMetrics;
    }

    /**
     * Wrap a text on a list of separators or on the row width.
     *
     * @param text       the text to wrap
     * @param separators the list of separator characters
     * @return this
     */
    public WrappedTextBuilder wrapOnSeparators(String text, String separators) {
        String regex = separators
            .chars()
            .mapToObj(c -> (char) c)
            .map(c -> format("(?<=\\%s)", c))
            .collect(Collectors.joining("|", "(", ")"));

        Arrays.stream(text.split(regex))
            .forEach(this::addText);
        return this;
    }

    /**
     * Wrap a text on the row width.
     * <p>
     * This method doesn't apply any separator related logic.
     *
     * @param text the text to wrap
     * @return this
     */
    private List<String> wrap(String text) {
        List<String> textRows = new ArrayList<>();
        StringBuilder tempText = new StringBuilder();
        text.chars()
            .mapToObj(c -> (char) c)
            .forEach(c -> {
                if (isTextExceedingRowWidth(tempText.toString() + c)) {
                    textRows.add(tempText.toString());
                    tempText.setLength(0);
                }
                tempText.append(c);
            });

        return textRows;
    }

    /**
     * Add a text to this wrapped text builder.
     * <p>
     * This method doesn't care about separators, so the separators should be applied before.
     * <p>
     * If, after the text is added, the current row will exceed the maximum width,
     * then the given text will go to the next line.
     * <p>
     * If the text itself exceeds the maximum width, then the text will be added in multiple rows.
     *
     * @param text the text to add
     * @return this
     */
    public WrappedTextBuilder addText(String text) {
        if (isTextExceedingRowWidth(text)) {
            addAll(wrap(text));
        } else if (isEmpty()) {
            add(text);
        } else {
            String newText = get(size() - 1).concat(text);
            if (isTextExceedingRowWidth(newText)) {
                add(text);
            } else {
                set(size() - 1, newText);
            }
        }
        return this;
    }

    /**
     * Ask if a text exceeds the allowed row width.
     *
     * @param text the text to check
     * @return true if the text exceeds the allowed row width
     */
    private boolean isTextExceedingRowWidth(String text) {
        return fontMetrics.stringWidth(text) >= width;
    }

    private int getWrappedTextWidth() {
        return stream()
            .map(fontMetrics::stringWidth)
            .reduce(0, Math::max);
    }

    private int getWrappedTextHeight() {
        return size() * fontMetrics.getHeight();
    }

    /**
     * Get the wrapped text as rows.
     *
     * @return the list of rows
     */
    public List<String> toWrappedTextRows() {
        return this;
    }

    /**
     * Get the wrapped text.
     *
     * @param html if true, the wrapped text must be returned as HTML
     * @return the wrapped text
     */
    public WrappedText toWrappedText(boolean html) {
        return toWrappedText(html, true);
    }

    /**
     * Get the wrapped text.
     *
     * @param html     if true, the wrapped text must be returned as HTML
     * @param centered it true, the wrapped text must be centered
     * @return the wrapped text
     */
    public WrappedText toWrappedText(boolean html, boolean centered) {
        return html ?
            centered ?
                toWrappedHtmlCenteredText() :
                toWrappedHtmlText() :
            toWrappedText();
    }

    /**
     * Get the wrapped text.
     *
     * @return the wrapped text
     */
    public WrappedText toWrappedText() {
        return getWrappedText(join("\n", this));
    }

    /**
     * Get the wrapped text.
     *
     * @return the wrapped text as HTML body
     */
    private String toHtmlWrappedTextBody() {
        return join("<p>", this);
    }

    /**
     * Get the wrapped text.
     *
     * @return the wrapped text as centered HTML body
     */
    private String getHtmlCenteredWrappedTextBody() {
        return "<center>" + join("<p>", this) + "</center>";
    }

    /**
     * Get the wrapped text.
     *
     * @return the wrapped text as HTML
     */
    public WrappedText toWrappedHtmlText() {
        return getWrappedText("<html>" + toHtmlWrappedTextBody() + "</html>");
    }

    /**
     * Get the wrapped text.
     *
     * @return the wrapped text as centered HTML
     */
    public WrappedText toWrappedHtmlCenteredText() {
        return getWrappedText("<html>" + getHtmlCenteredWrappedTextBody() + "</html>");
    }

    private WrappedText getWrappedText(String text) {
        return new WrappedText(text, getWrappedTextWidth(), getWrappedTextHeight());
    }
}
