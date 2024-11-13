package org.cosinus.swing.image;

import static java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT;
import static java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY;
import static java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_DEFAULT;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_OFF;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
import static java.awt.RenderingHints.VALUE_RENDER_DEFAULT;
import static java.awt.RenderingHints.VALUE_RENDER_QUALITY;
import static java.awt.RenderingHints.VALUE_RENDER_SPEED;

/**
 * Encapsulation for settings on drawing images
 */
public class ImageSettings {

    public static final ImageSettings SPEED = new ImageSettings(
        VALUE_RENDER_SPEED,
        VALUE_INTERPOLATION_NEAREST_NEIGHBOR,
        VALUE_ALPHA_INTERPOLATION_SPEED,
        VALUE_ANTIALIAS_OFF,
        false);

    public static final ImageSettings QUALITY = new ImageSettings(
        VALUE_RENDER_QUALITY,
        VALUE_INTERPOLATION_BICUBIC,
        VALUE_ALPHA_INTERPOLATION_QUALITY,
        VALUE_ANTIALIAS_ON,
        true);

    public static final ImageSettings SPEED_QUALITY_BALANCE = new ImageSettings(
        VALUE_RENDER_DEFAULT,
        VALUE_INTERPOLATION_BILINEAR,
        VALUE_ALPHA_INTERPOLATION_DEFAULT,
        VALUE_ANTIALIAS_DEFAULT,
        false);

    private final Object renderingHint;

    private final Object interpolationHint;

    private final Object alphaInterpolationHint;

    private final Object antialiasingHint;

    private final boolean highQualityOnScaling;

    ImageSettings(final Object renderingHint,
                  final Object interpolationHint,
                  final Object alphaInterpolationHint,
                  final Object antialiasingHint,
                  boolean highQualityOnScaling) {
        this.renderingHint = renderingHint;
        this.interpolationHint = interpolationHint;
        this.alphaInterpolationHint = alphaInterpolationHint;
        this.antialiasingHint = antialiasingHint;
        this.highQualityOnScaling = highQualityOnScaling;
    }

    public Object getRenderingHint() {
        return renderingHint;
    }

    public Object getAlphaInterpolationHint() {
        return alphaInterpolationHint;
    }

    public Object getInterpolationHint() {
        return interpolationHint;
    }

    public Object getAntialiasingHint() {
        return antialiasingHint;
    }

    public boolean isHighQualityOnScaling() {
        return highQualityOnScaling;
    }
}
