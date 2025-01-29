package org.cosinus.swing.image;

import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static org.imgscalr.Scalr.Rotation.CW_180;
import static org.imgscalr.Scalr.Rotation.CW_270;
import static org.imgscalr.Scalr.Rotation.CW_90;
import static org.imgscalr.Scalr.Rotation.FLIP_HORZ;
import static org.imgscalr.Scalr.Rotation.FLIP_VERT;
import static org.imgscalr.Scalr.rotate;

public enum ImageRotation {

    NO_ROTATION(1, identity()),
    HORIZONTAL_FLIP(2, image -> rotate(image, FLIP_HORZ)),
    ROTATE_180(3, image -> rotate(image, CW_180)),
    VERTICAL_FLIP(4, image -> rotate(image, FLIP_VERT)),
    ROTATE_90_AND_HORIZONTAL_FLIP(5, image -> rotate(rotate(image, CW_90), FLIP_HORZ)),
    ROTATE_90(6, image -> rotate(image, CW_90)),
    ROTATE_90_AND_VERTICAL_FLIP(7, image -> rotate(rotate(image, CW_90), FLIP_VERT)),
    ROTATE_270(8, image -> rotate(image, CW_270));

    private final int code;

    private final Function<BufferedImage, BufferedImage> imageRotation;

    ImageRotation(int code, final Function<BufferedImage, BufferedImage> imageRotation) {
        this.code = code;
        this.imageRotation = imageRotation;
    }

    public int getOrientation() {
        return code;
    }

    public BufferedImage apply(BufferedImage image) {
        return imageRotation.apply(image);
    }

    public static Optional<ImageRotation> findImageRotation(int code) {
        return stream(values())
            .filter(orientation -> orientation.code == code)
            .findFirst();
    }
}
