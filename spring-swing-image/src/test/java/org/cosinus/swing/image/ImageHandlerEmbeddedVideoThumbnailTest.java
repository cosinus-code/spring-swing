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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.cosinus.swing.image.video.TestVideos.mkvAttachment;
import static org.cosinus.swing.image.video.TestVideos.mkvWithAttachments;
import static org.cosinus.swing.image.video.TestVideos.mkvWithoutAttachments;
import static org.cosinus.swing.image.video.TestVideos.mp4WithCover;
import static org.cosinus.swing.image.video.TestVideos.mp4WithoutCover;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test the thumbnail created from the cover image embedded in a video file.
 */
public class ImageHandlerEmbeddedVideoThumbnailTest {

    private static final int SIZE = 100;

    // JPEG compression changes the colors a little
    private static final int COLOR_TOLERANCE = 16;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    // the embedded video thumbnail doesn't use the file handler
    private final ImageHandler imageHandler = new ImageHandler(null);

    @Test
    public void testMp4JpegCover() throws IOException {
        File video = file("movie.mp4", mp4WithCover(image("jpg", 600, 400, Color.RED)));

        BufferedImage thumbnail = videoThumbnail(video);

        assertEquals(SIZE, thumbnail.getWidth());
        assertEquals(SIZE * 400 / 600, thumbnail.getHeight(), 1);
        assertColor(Color.RED, thumbnail);
    }

    @Test
    public void testMp4PngCover() throws IOException {
        File video = file("movie.m4v", mp4WithCover(image("png", 300, 450, Color.BLUE)));

        BufferedImage thumbnail = videoThumbnail(video);

        assertEquals(SIZE * 300 / 450, thumbnail.getWidth(), 1);
        assertEquals(SIZE, thumbnail.getHeight());
        assertColor(Color.BLUE, thumbnail);
    }

    @Test
    public void testMkvCover() throws IOException {
        File video = file("movie.mkv", mkvWithAttachments(
            mkvAttachment("small_cover.png", "image/png", image("png", 120, 120, Color.BLUE)),
            mkvAttachment("cover.jpg", "image/jpeg", image("jpg", 600, 400, Color.RED))));

        assertColor(Color.RED, videoThumbnail(video));
    }

    @Test
    public void testDetectContainerByContent() throws IOException {
        // the reader is chosen by the file header, not by the extension
        File video = file("movie.video", mkvWithAttachments(
            mkvAttachment("cover.jpg", "image/jpeg", image("jpg", 600, 400, Color.GREEN))));

        assertColor(Color.GREEN, videoThumbnail(video));
    }

    @Test
    public void testMp4WithoutCover() throws IOException {
        assertNoThumbnail(file("movie.mp4", mp4WithoutCover()));
    }

    @Test
    public void testMkvWithoutAttachments() throws IOException {
        assertNoThumbnail(file("movie.mkv", mkvWithoutAttachments()));
    }

    @Test
    public void testCoverIsNotAnImage() throws IOException {
        assertNoThumbnail(file("movie.mp4", mp4WithCover("not an image".getBytes(UTF_8))));
    }

    @Test
    public void testTruncatedVideo() throws IOException {
        byte[] video = mp4WithCover(image("jpg", 600, 400, Color.RED));

        assertNoThumbnail(file("movie.mp4", Arrays.copyOf(video, video.length - 100)));
    }

    @Test
    public void testNotVideo() throws IOException {
        assertNoThumbnail(file("notes.txt", "some text in a file".getBytes(UTF_8)));
        assertNoThumbnail(file("photo.jpg", image("jpg", 600, 400, Color.RED)));
        assertNoThumbnail(file("empty.mp4", new byte[0]));
    }

    @Test
    public void testMissingFile() {
        assertNoThumbnail(new File(temporaryFolder.getRoot(), "missing.mp4"));
    }

    @Test
    public void testDirectory() throws IOException {
        assertNoThumbnail(temporaryFolder.newFolder("movie.mp4"));
    }

    private BufferedImage videoThumbnail(final File video) {
        Optional<Image> thumbnail = imageHandler.createEmbeddedVideoThumbnail(video, SIZE);
        assertTrue(thumbnail.isPresent());
        return (BufferedImage) thumbnail.get();
    }

    private void assertNoThumbnail(final File file) {
        assertFalse(imageHandler.createEmbeddedVideoThumbnail(file, SIZE).isPresent());
    }

    private void assertColor(final Color expected, final BufferedImage image) {
        Color actual = new Color(image.getRGB(image.getWidth() / 2, image.getHeight() / 2));
        assertTrue("expected " + expected + " but was " + actual,
            Math.abs(expected.getRed() - actual.getRed()) <= COLOR_TOLERANCE &&
                Math.abs(expected.getGreen() - actual.getGreen()) <= COLOR_TOLERANCE &&
                Math.abs(expected.getBlue() - actual.getBlue()) <= COLOR_TOLERANCE);
    }

    private File file(final String name, final byte[] content) throws IOException {
        File file = temporaryFolder.newFile(name);
        Files.write(file.toPath(), content);
        return file;
    }

    private static byte[] image(final String format, final int width, final int height, final Color color)
        throws IOException {
        BufferedImage image = new BufferedImage(width, height, TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(color);
            graphics.fillRect(0, 0, width, height);
        } finally {
            graphics.dispose();
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, format, output));
        return output.toByteArray();
    }
}
