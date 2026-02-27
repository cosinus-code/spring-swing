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

package org.cosinus.swing.image;

import lombok.Getter;

import java.awt.*;

import static java.awt.RenderingHints.*;

/**
 * Encapsulation for settings on drawing images
 */
@Getter
public class ImageSettings {

    public static final ImageSettings SPEED = new ImageSettings(
        VALUE_RENDER_SPEED,
        VALUE_INTERPOLATION_NEAREST_NEIGHBOR,
        VALUE_ALPHA_INTERPOLATION_SPEED,
        VALUE_ANTIALIAS_OFF,
        VALUE_STROKE_NORMALIZE,
        false);

    public static final ImageSettings QUALITY = new ImageSettings(
        VALUE_RENDER_QUALITY,
        VALUE_INTERPOLATION_BICUBIC,
        VALUE_ALPHA_INTERPOLATION_QUALITY,
        VALUE_ANTIALIAS_ON,
        VALUE_STROKE_PURE,
        true);

    public static final ImageSettings SPEED_QUALITY_BALANCE = new ImageSettings(
        VALUE_RENDER_DEFAULT,
        VALUE_INTERPOLATION_BILINEAR,
        VALUE_ALPHA_INTERPOLATION_DEFAULT,
        VALUE_ANTIALIAS_DEFAULT,
        VALUE_STROKE_DEFAULT,
        false);

    private final Object renderingHint;

    private final Object interpolationHint;

    private final Object alphaInterpolationHint;

    private final Object antialiasingHint;

    private final Object strokeControl;

    private final boolean highQualityOnScaling;

    ImageSettings(final Object renderingHint,
                  final Object interpolationHint,
                  final Object alphaInterpolationHint,
                  final Object antialiasingHint,
                  final Object strokeControl,
                  boolean highQualityOnScaling) {
        this.renderingHint = renderingHint;
        this.interpolationHint = interpolationHint;
        this.alphaInterpolationHint = alphaInterpolationHint;
        this.antialiasingHint = antialiasingHint;
        this.strokeControl = strokeControl;
        this.highQualityOnScaling = highQualityOnScaling;
    }

    public void apply(Graphics2D graphics) {
        graphics.setRenderingHint(KEY_RENDERING, renderingHint);
        graphics.setRenderingHint(KEY_ALPHA_INTERPOLATION, alphaInterpolationHint);
        graphics.setRenderingHint(KEY_INTERPOLATION, interpolationHint);
        graphics.setRenderingHint(KEY_ANTIALIASING, antialiasingHint);
        graphics.setRenderingHint(KEY_STROKE_CONTROL, strokeControl);
    }
}
