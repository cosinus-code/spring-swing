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

package org.cosinus.swing.image.video;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.READ;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test the cover reading from MP4 and QuickTime files built in memory,
 * with only the boxes the reader needs and some boxes to skip.
 */
public class Mp4CoverReaderTest {

    private static final byte[] COVER = "cover image bytes".getBytes(UTF_8);

    private static final byte[] OTHER = "other item bytes".getBytes(UTF_8);

    private static final int JPEG_DATA_TYPE = 13;

    private static final int UTF8_DATA_TYPE = 1;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final Mp4CoverReader reader = new Mp4CoverReader();

    @Test
    public void testSupports() throws IOException {
        assertTrue(supports(ftyp()));
        assertTrue(supports(box("moov", box("mvhd", new byte[100]))));
        assertTrue(supports(concat(box("wide", new byte[0]), box("mdat", new byte[10]))));

        assertFalse(supports(new byte[]{0x1A, 0x45, (byte) 0xDF, (byte) 0xA3, 0, 0, 0, 0}));
        assertFalse(supports(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0}));
        assertFalse(supports(new byte[]{0, 0, 0}));
    }

    @Test
    public void testReadITunesCover() throws IOException {
        byte[] video = concat(
            ftyp(),
            box("mdat", new byte[1000]),
            box("moov",
                box("mvhd", new byte[100]),
                box("trak", new byte[200]),
                box("udta",
                    box("free", new byte[20]),
                    fullBox("meta",
                        box("hdlr", new byte[25]),
                        box("ilst",
                            box("©too", data(UTF8_DATA_TYPE, OTHER)),
                            box("covr", data(JPEG_DATA_TYPE, COVER)))))));

        assertCover(COVER, video);
    }

    @Test
    public void testReadCoverAfterLargeBox() throws IOException {
        // mdat with a 64-bit size, before the moov box
        byte[] mdatContent = new byte[500];
        ByteBuffer largeMdat = ByteBuffer.allocate(16 + mdatContent.length)
            .putInt(1)
            .put("mdat".getBytes(ISO_8859_1))
            .putLong(16 + mdatContent.length)
            .put(mdatContent);
        byte[] video = concat(ftyp(), largeMdat.array(), moovWithITunesCover());

        assertCover(COVER, video);
    }

    @Test
    public void testReadQuickTimeArtwork() throws IOException {
        byte[] keys = fullBox("keys",
            ByteBuffer.allocate(4).putInt(2).array(),
            key("com.apple.quicktime.make"),
            key("com.apple.quicktime.artwork"));
        byte[] video = concat(
            ftyp(),
            box("moov",
                box("mvhd", new byte[100]),
                // QuickTime meta is a simple box, without version and flags
                box("meta",
                    box("hdlr", new byte[25]),
                    keys,
                    box("ilst",
                        indexBox(1, data(UTF8_DATA_TYPE, OTHER)),
                        indexBox(2, data(JPEG_DATA_TYPE, COVER))))));

        assertCover(COVER, video);
    }

    @Test
    public void testReadCoverFromMoovMeta() throws IOException {
        byte[] video = concat(
            ftyp(),
            box("moov",
                box("meta",
                    box("hdlr", new byte[25]),
                    box("ilst", box("covr", data(JPEG_DATA_TYPE, COVER))))));

        assertCover(COVER, video);
    }

    @Test
    public void testNoCover() throws IOException {
        byte[] video = concat(
            ftyp(),
            box("mdat", new byte[1000]),
            box("moov",
                box("mvhd", new byte[100]),
                box("udta", box("©xyz", new byte[20]))));

        assertNoCover(video);
    }

    @Test
    public void testNoMoov() throws IOException {
        assertNoCover(concat(ftyp(), box("mdat", new byte[1000])));
    }

    @Test
    public void testArtworkKeyWithoutItem() throws IOException {
        byte[] video = concat(
            ftyp(),
            box("moov",
                box("meta",
                    box("hdlr", new byte[25]),
                    fullBox("keys", ByteBuffer.allocate(4).putInt(1).array(), key("com.apple.quicktime.artwork")),
                    box("ilst"))));

        assertNoCover(video);
    }

    @Test
    public void testTruncatedFile() throws IOException {
        byte[] video = concat(ftyp(), moovWithITunesCover());

        assertNoCover(Arrays.copyOf(video, video.length - 5));
    }

    @Test
    public void testCorruptedBoxSize() throws IOException {
        byte[] video = concat(ftyp(), moovWithITunesCover());
        // the moov box declares a size smaller than its header
        ByteBuffer.wrap(video).putInt(ftyp().length, 3);

        assertNoCover(video);
    }

    private byte[] moovWithITunesCover() {
        return box("moov",
            box("udta",
                fullBox("meta",
                    box("hdlr", new byte[25]),
                    box("ilst", box("covr", data(JPEG_DATA_TYPE, COVER))))));
    }

    private void assertCover(final byte[] expectedCover, final byte[] video) throws IOException {
        Optional<byte[]> cover = readCover(video);
        assertTrue(cover.isPresent());
        assertArrayEquals(expectedCover, cover.get());
    }

    private void assertNoCover(final byte[] video) throws IOException {
        assertFalse(readCover(video).isPresent());
    }

    private boolean supports(final byte[] content) throws IOException {
        try (FileChannel channel = channel(content)) {
            return reader.supports(channel);
        }
    }

    private Optional<byte[]> readCover(final byte[] video) throws IOException {
        try (FileChannel channel = channel(video)) {
            assertTrue(reader.supports(channel));
            return reader.readCover(channel);
        }
    }

    private FileChannel channel(final byte[] content) throws IOException {
        File file = temporaryFolder.newFile();
        Files.write(file.toPath(), content);
        return FileChannel.open(file.toPath(), READ);
    }

    private static byte[] ftyp() {
        return box("ftyp", "isom\0\0\2\0isommp41".getBytes(ISO_8859_1));
    }

    private static byte[] box(final String type, final byte[]... children) {
        return box(type.getBytes(ISO_8859_1), concat(children));
    }

    private static byte[] indexBox(final int index, final byte[]... children) {
        return box(ByteBuffer.allocate(4).putInt(index).array(), concat(children));
    }

    private static byte[] box(final byte[] type, final byte[] content) {
        return ByteBuffer.allocate(8 + content.length)
            .putInt(8 + content.length)
            .put(type)
            .put(content)
            .array();
    }

    private static byte[] fullBox(final String type, final byte[]... children) {
        return box(type, concat(new byte[4], concat(children)));
    }

    private static byte[] data(final int dataType, final byte[] value) {
        return box("data", ByteBuffer.allocate(8 + value.length)
            .putInt(dataType)
            .putInt(0)
            .put(value)
            .array());
    }

    private static byte[] key(final String key) {
        byte[] value = key.getBytes(UTF_8);
        return ByteBuffer.allocate(8 + value.length)
            .putInt(8 + value.length)
            .put("mdta".getBytes(ISO_8859_1))
            .put(value)
            .array();
    }

    private static byte[] concat(final byte[]... parts) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (byte[] part : parts) {
            output.writeBytes(part);
        }
        return output.toByteArray();
    }
}
