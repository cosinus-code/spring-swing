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
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.READ;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test the cover reading from MKV files built in memory,
 * with only the elements the reader needs and some elements to skip.
 */
public class MatroskaCoverReaderTest {

    private static final long EBML = 0x1A45DFA3L;
    private static final long DOC_TYPE = 0x4282L;
    private static final long SEGMENT = 0x18538067L;
    private static final long SEEK_HEAD = 0x114D9B74L;
    private static final long SEEK = 0x4DBBL;
    private static final long SEEK_ID = 0x53ABL;
    private static final long SEEK_POSITION = 0x53ACL;
    private static final long INFO = 0x1549A966L;
    private static final long TRACKS = 0x1654AE6BL;
    private static final long CLUSTER = 0x1F43B675L;
    private static final long ATTACHMENTS = 0x1941A469L;
    private static final long ATTACHED_FILE = 0x61A7L;
    private static final long FILE_NAME = 0x466EL;
    private static final long FILE_MIME_TYPE = 0x4660L;
    private static final long FILE_DATA = 0x465CL;
    private static final long FILE_UID = 0x46AEL;

    // the size value of 8 bytes with all value bits set, meaning unknown size
    private static final byte[] UNKNOWN_SIZE =
        {0x01, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

    private static final byte[] COVER = "cover jpeg bytes".getBytes(UTF_8);
    private static final byte[] SMALL_COVER = "small cover png bytes".getBytes(UTF_8);
    private static final byte[] FONT = "font bytes".getBytes(UTF_8);

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final MatroskaCoverReader reader = new MatroskaCoverReader();

    @Test
    public void testSupports() throws IOException {
        assertTrue(supports(ebmlHeader()));

        assertFalse(supports("\0\0\0\u0018ftypisom".getBytes(UTF_8)));
        assertFalse(supports(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0}));
        assertFalse(supports(new byte[]{0x1A, 0x45}));
    }

    @Test
    public void testReadCover() throws IOException {
        byte[] video = mkv(
            element(INFO, new byte[30]),
            element(TRACKS, new byte[50]),
            attachments(attachment("cover.jpg", "image/jpeg", COVER)),
            element(CLUSTER, new byte[500]));

        assertCover(COVER, video);
    }

    @Test
    public void testPreferCoverName() throws IOException {
        byte[] video = mkv(attachments(
            attachment("small_cover.png", "image/png", SMALL_COVER),
            attachment("Cover.JPG", "image/jpeg", COVER)));

        assertCover(COVER, video);
    }

    @Test
    public void testUseAnyImageWithoutCoverName() throws IOException {
        byte[] video = mkv(attachments(
            attachment("font.ttf", "application/x-truetype-font", FONT),
            attachment("poster.png", "image/png", SMALL_COVER)));

        assertCover(SMALL_COVER, video);
    }

    @Test
    public void testIgnoreNonImageAttachments() throws IOException {
        byte[] video = mkv(attachments(attachment("cover.ttf", "application/x-truetype-font", FONT)));

        assertNoCover(video);
    }

    @Test
    public void testReadCoverAfterClusters() throws IOException {
        // no seek head: the clusters with known size are skipped
        byte[] video = mkv(
            element(INFO, new byte[30]),
            element(CLUSTER, new byte[500]),
            element(CLUSTER, new byte[500]),
            attachments(attachment("cover.jpg", "image/jpeg", COVER)));

        assertCover(COVER, video);
    }

    @Test
    public void testReadCoverThroughSeekHead() throws IOException {
        byte[] info = element(INFO, new byte[30]);
        // the cluster with unknown size cannot be skipped, only the seek head leads to the attachments
        byte[] cluster = concat(id(CLUSTER), UNKNOWN_SIZE, new byte[200]);
        byte[] attachments = attachments(attachment("cover.jpg", "image/jpeg", COVER));

        // the seek head size doesn't depend on the position value, which is written on 4 bytes
        int seekHeadSize = seekHead(0).length;
        long attachmentsPosition = seekHeadSize + info.length + cluster.length;
        byte[] video = mkv(seekHead(attachmentsPosition), info, cluster, attachments);

        assertCover(COVER, video);
    }

    @Test
    public void testReadCoverFromSegmentWithUnknownSize() throws IOException {
        byte[] video = concat(
            ebmlHeader(),
            id(SEGMENT), UNKNOWN_SIZE,
            element(INFO, new byte[30]),
            attachments(attachment("cover.jpg", "image/jpeg", COVER)));

        assertCover(COVER, video);
    }

    @Test
    public void testNoAttachments() throws IOException {
        assertNoCover(mkv(element(INFO, new byte[30]), element(CLUSTER, new byte[500])));
    }

    @Test
    public void testStopAtClusterWithUnknownSize() throws IOException {
        byte[] video = mkv(
            concat(id(CLUSTER), UNKNOWN_SIZE, new byte[100]),
            attachments(attachment("cover.jpg", "image/jpeg", COVER)));

        assertNoCover(video);
    }

    @Test
    public void testTruncatedFile() throws IOException {
        byte[] video = mkv(attachments(attachment("cover.jpg", "image/jpeg", COVER)));

        assertNoCover(Arrays.copyOf(video, video.length - 5));
    }

    @Test
    public void testNoSegment() throws IOException {
        assertNoCover(concat(ebmlHeader(), element(INFO, new byte[30])));
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

    private static byte[] mkv(final byte[]... segmentChildren) {
        return concat(ebmlHeader(), element(SEGMENT, segmentChildren));
    }

    private static byte[] ebmlHeader() {
        return element(EBML, element(DOC_TYPE, "matroska".getBytes(UTF_8)));
    }

    private static byte[] seekHead(final long attachmentsPosition) {
        return element(SEEK_HEAD,
            element(SEEK,
                element(SEEK_ID, id(ATTACHMENTS)),
                element(SEEK_POSITION, new byte[]{
                    (byte) (attachmentsPosition >> 24),
                    (byte) (attachmentsPosition >> 16),
                    (byte) (attachmentsPosition >> 8),
                    (byte) attachmentsPosition})));
    }

    private static byte[] attachments(final byte[]... attachedFiles) {
        return element(ATTACHMENTS, attachedFiles);
    }

    private static byte[] attachment(final String name, final String mimeType, final byte[] data) {
        return element(ATTACHED_FILE,
            element(FILE_NAME, name.getBytes(UTF_8)),
            element(FILE_MIME_TYPE, mimeType.getBytes(UTF_8)),
            element(FILE_DATA, data),
            element(FILE_UID, new byte[]{1, 2, 3, 4}));
    }

    private static byte[] element(final long id, final byte[]... children) {
        byte[] data = concat(children);
        return concat(id(id), size(data.length), data);
    }

    /**
     * Write an element id: its bytes already contain the length marker.
     */
    private static byte[] id(final long id) {
        int length = id > 0xFFFFFF ? 4 : id > 0xFFFF ? 3 : id > 0xFF ? 2 : 1;
        byte[] bytes = new byte[length];
        for (int index = 0; index < length; index++) {
            bytes[index] = (byte) (id >> (8 * (length - 1 - index)));
        }
        return bytes;
    }

    /**
     * Write a size as a variable integer on 4 bytes, like some muxers do, not always the shortest form.
     */
    private static byte[] size(final int size) {
        return new byte[]{(byte) (0x10 | (size >> 24)), (byte) (size >> 16), (byte) (size >> 8), (byte) size};
    }

    private static byte[] concat(final byte[]... parts) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (byte[] part : parts) {
            output.writeBytes(part);
        }
        return output.toByteArray();
    }
}
