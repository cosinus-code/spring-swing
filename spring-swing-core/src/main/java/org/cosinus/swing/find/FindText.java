package org.cosinus.swing.find;

import java.util.Objects;

public class FindText {

    private String text;

    private boolean caseSensitive;

    private boolean wholeWord;

    private boolean regularExpression;

    public FindText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public FindText caseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        return this;
    }

    public boolean isWholeWord() {
        return wholeWord;
    }

    public FindText wholeWord(boolean wholeWord) {
        this.wholeWord = wholeWord;
        return this;
    }

    public boolean isRegularExpression() {
        return regularExpression;
    }

    public FindText regularExpression(boolean regularExpression) {
        this.regularExpression = regularExpression;
        return this;
    }

    public static FindText buildFindText(String text) {
        return new FindText(text);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FindText findText = (FindText) o;
        return caseSensitive == findText.caseSensitive &&
            wholeWord == findText.wholeWord &&
            regularExpression == findText.regularExpression &&
            Objects.equals(text, findText.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, caseSensitive, wholeWord, regularExpression);
    }
}
