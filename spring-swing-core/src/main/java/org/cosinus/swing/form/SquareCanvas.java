package org.cosinus.swing.form;

public class SquareCanvas implements Canvas {

    private final int size;

    public SquareCanvas(int size) {

        this.size = size;
    }

    @Override
    public int getWidth() {
        return size;
    }

    @Override
    public int getHeight() {
        return size;
    }
}
