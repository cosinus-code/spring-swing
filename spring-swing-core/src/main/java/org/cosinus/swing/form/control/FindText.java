package org.cosinus.swing.form.control;

public class FindText {

    private final String text;

    private boolean caseSensitive;

    private boolean wordOnly;

    private boolean regularExpression;

    public FindText(String text) {
        this.text = text;
    };

    public String getText() {
        return text;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public FindText caseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        return this;
    }

    public boolean isWordOnly() {
        return wordOnly;
    }

    public FindText wordOnly(boolean wordOnly) {
        this.wordOnly = wordOnly;
        return this;
    }

    public boolean isRegularExpression() {
        return regularExpression;
    }

    public FindText regularExpression(boolean regularExpression) {
        this.regularExpression = regularExpression;
        return this;
    }
}
