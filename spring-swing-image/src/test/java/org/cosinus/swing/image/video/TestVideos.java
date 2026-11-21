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

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Minimal MP4 and MKV video files for tests, with only the structure the cover readers need.
 */
public final class TestVideos {

    private static final int JPEG_DATA_TYPE = 13;

    private static final long EBML = 0x1A45DFA3L;
    private static final long DOC_TYPE = 0x4282L;
    private static final long SEGMENT = 0x18538067L;
    private static final long INFO = 0x1549A966L;
    private static final long CLUSTER = 0x1F43B675L;
    private static final long ATTACHMENTS = 0x1941A469L;
    private static final long ATTACHED_FILE = 0x61A7L;
    private static final long FILE_NAME = 0x466EL;
    private static final long FILE_MIME_TYPE = 0x4660L;
    private static final long FILE_DATA = 0x465CL;

    private TestVideos() {
    }

    /**
     * Create an MP4 video with an iTunes cover art.
     *
     * @param cover the encoded cover image
     * @return the video bytes
     */
    public static byte[] mp4WithCover(final byte[] cover) {
        return concat(
            mp4Ftyp(),
            mp4Box("mdat", new byte[1000]),
            mp4Box("moov",
                mp4Box("mvhd", new byte[100]),
                mp4Box("udta",
                    mp4Box("meta",
                        new byte[4],
                        mp4Box("hdlr", new byte[25]),
                        mp4Box("ilst",
                            mp4Box("covr",
                                mp4Box("data",
                                    ByteBuffer.allocate(8).putInt(JPEG_DATA_TYPE).putInt(0).array(),
                                    cover)))))));
    }

    /**
     * Create an MP4 video without cover.
     *
     * @return the video bytes
     */
    public static byte[] mp4WithoutCover() {
        return concat(
            mp4Ftyp(),
            mp4Box("mdat", new byte[1000]),
            mp4Box("moov", mp4Box("mvhd", new byte[100])));
    }

    /**
     * Create an MKV video with attachments.
     *
     * @param attachments the attachments, created with {@link #mkvAttachment(String, String, byte[])}
     * @return the video bytes
     */
    public static byte[] mkvWithAttachments(final byte[]... attachments) {
        return mkv(
            mkvElement(INFO, new byte[30]),
            mkvElement(ATTACHMENTS, attachments),
            mkvElement(CLUSTER, new byte[500]));
    }

    /**
     * Create an MKV video without attachments.
     *
     * @return the video bytes
     */
    public static byte[] mkvWithoutAttachments() {
        return mkv(mkvElement(INFO, new byte[30]), mkvElement(CLUSTER, new byte[500]));
    }

    /**
     * Create an MKV attachment.
     *
     * @param name     the attachment file name
     * @param mimeType the attachment mime type
     * @param data     the attachment data
     * @return the attachment element bytes
     */
    public static byte[] mkvAttachment(final String name, final String mimeType, final byte[] data) {
        return mkvElement(ATTACHED_FILE,
            mkvElement(FILE_NAME, name.getBytes(UTF_8)),
            mkvElement(FILE_MIME_TYPE, mimeType.getBytes(UTF_8)),
            mkvElement(FILE_DATA, data));
    }

    private static byte[] mkv(final byte[]... segmentChildren) {
        return concat(
            mkvElement(EBML, mkvElement(DOC_TYPE, "matroska".getBytes(UTF_8))),
            mkvElement(SEGMENT, segmentChildren));
    }

    private static byte[] mp4Ftyp() {
        return mp4Box("ftyp", "isom\0\0\2\0isommp41".getBytes(ISO_8859_1));
    }

    private static byte[] mp4Box(final String type, final byte[]... children) {
        byte[] content = concat(children);
        return ByteBuffer.allocate(8 + content.length)
            .putInt(8 + content.length)
            .put(type.getBytes(ISO_8859_1))
            .put(content)
            .array();
    }

    private static byte[] mkvElement(final long id, final byte[]... children) {
        byte[] data = concat(children);
        int idLength = id > 0xFFFFFF ? 4 : id > 0xFFFF ? 3 : id > 0xFF ? 2 : 1;
        ByteBuffer element = ByteBuffer.allocate(idLength + 4 + data.length);
        for (int index = idLength - 1; index >= 0; index--) {
            element.put((byte) (id >> (8 * index)));
        }
        // the size as a variable integer on 4 bytes
        return element
            .putInt(0x10000000 | data.length)
            .put(data)
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
