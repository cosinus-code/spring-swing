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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.lang.Math.min;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.ROOT;
import static java.util.stream.IntStream.range;

/**
 * Reader of the cover image attached to an MKV or WebM video (Matroska container).
 * <p>
 * The cover is an image attachment, named "cover.jpg" or "cover.png" by the Matroska convention.
 * The attachments are found through the seek head, or by skipping the top level elements of the segment.
 */
public class MatroskaCoverReader implements VideoCoverReader {

    private static final long EBML_ID = 0x1A45DFA3L;
    private static final long SEGMENT_ID = 0x18538067L;
    private static final long SEEK_HEAD_ID = 0x114D9B74L;
    private static final long SEEK_ID = 0x4DBBL;
    private static final long SEEK_ELEMENT_ID = 0x53ABL;
    private static final long SEEK_POSITION_ID = 0x53ACL;
    private static final long ATTACHMENTS_ID = 0x1941A469L;
    private static final long ATTACHED_FILE_ID = 0x61A7L;
    private static final long FILE_NAME_ID = 0x466EL;
    private static final long FILE_MIME_TYPE_ID = 0x4660L;
    private static final long FILE_DATA_ID = 0x465CL;

    private static final String IMAGE_MIME_TYPE_PREFIX = "image/";

    // the attachment names of the covers, in the order of preference
    private static final List<String> COVER_NAMES = List.of("cover.", "cover_land.", "small_cover.", "small_cover_land.");

    // the maximum number of top level elements to skip while searching the attachments
    private static final int MAX_TOP_LEVEL_ELEMENTS = 100_000;

    // an element header has at most 4 bytes of id and 8 bytes of size
    private static final int MAX_ELEMENT_HEADER_SIZE = 12;

    private static final long UNKNOWN_SIZE = -1;

    @Override
    public boolean supports(final FileChannel channel) throws IOException {
        return channel.size() >= 4 && Integer.toUnsignedLong(read(channel, 0, 4).getInt()) == EBML_ID;
    }

    @Override
    public Optional<byte[]> readCover(final FileChannel channel) throws IOException {
        long fileSize = channel.size();
        Optional<Element> ebml = readElement(channel, 0, fileSize);
        if (ebml.isEmpty() || ebml.get().hasUnknownSize()) {
            return Optional.empty();
        }

        Optional<Element> segment = readElement(channel, ebml.get().end(), fileSize)
            .filter(element -> element.id() == SEGMENT_ID);
        if (segment.isEmpty()) {
            return Optional.empty();
        }
        long segmentStart = segment.get().dataStart();
        long segmentEnd = segment.get().hasUnknownSize() ? fileSize : segment.get().end();

        long position = segmentStart;
        for (int count = 0; count < MAX_TOP_LEVEL_ELEMENTS && position < segmentEnd; count++) {
            Optional<Element> element = readElement(channel, position, segmentEnd);
            if (element.isEmpty()) {
                break;
            }
            if (element.get().id() == ATTACHMENTS_ID) {
                return readCover(channel, element.get());
            }
            if (element.get().id() == SEEK_HEAD_ID) {
                Optional<Element> attachments = findAttachmentsPosition(channel, element.get())
                    .map(seekPosition -> segmentStart + seekPosition)
                    .flatMap(attachmentsPosition -> readElement(channel, attachmentsPosition, segmentEnd))
                    .filter(found -> found.id() == ATTACHMENTS_ID);
                if (attachments.isPresent()) {
                    return readCover(channel, attachments.get());
                }
            }
            if (element.get().hasUnknownSize()) {
                // an element with unknown size cannot be skipped, like the clusters of a live stream
                break;
            }
            position = element.get().end();
        }
        return Optional.empty();
    }

    /**
     * Find the position of the attachments relative to the segment data, in the seek head.
     */
    private Optional<Long> findAttachmentsPosition(final FileChannel channel, final Element seekHead)
        throws IOException {
        for (Element seek : children(channel, seekHead)) {
            if (seek.id() == SEEK_ID) {
                Long seekedId = null;
                Long seekPosition = null;
                for (Element child : children(channel, seek)) {
                    if (child.id() == SEEK_ELEMENT_ID) {
                        seekedId = readUnsigned(channel, child);
                    } else if (child.id() == SEEK_POSITION_ID) {
                        seekPosition = readUnsigned(channel, child);
                    }
                }
                if (seekedId != null && seekedId == ATTACHMENTS_ID && seekPosition != null) {
                    return Optional.of(seekPosition);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Read the preferred cover image among the image attachments.
     */
    private Optional<byte[]> readCover(final FileChannel channel, final Element attachments) throws IOException {
        List<Attachment> images = new ArrayList<>();
        for (Element attachedFile : children(channel, attachments)) {
            if (attachedFile.id() == ATTACHED_FILE_ID) {
                String name = "";
                String mimeType = "";
                Element data = null;
                for (Element child : children(channel, attachedFile)) {
                    if (child.id() == FILE_NAME_ID) {
                        name = readString(channel, child);
                    } else if (child.id() == FILE_MIME_TYPE_ID) {
                        mimeType = readString(channel, child);
                    } else if (child.id() == FILE_DATA_ID) {
                        data = child;
                    }
                }
                if (data != null && mimeType.toLowerCase(ROOT).startsWith(IMAGE_MIME_TYPE_PREFIX)) {
                    images.add(new Attachment(name.toLowerCase(ROOT), data));
                }
            }
        }

        Optional<Attachment> cover = images.stream().min(Comparator.comparingInt(Attachment::coverRank));
        if (cover.isEmpty()) {
            return Optional.empty();
        }
        Element data = cover.get().data();
        return readCoverBytes(channel, data.dataStart(), data.size());
    }

    /**
     * Get the children of an element with a known size.
     */
    private List<Element> children(final FileChannel channel, final Element parent) {
        List<Element> children = new ArrayList<>();
        if (parent.hasUnknownSize()) {
            return children;
        }
        long position = parent.dataStart();
        while (position < parent.end()) {
            Optional<Element> child = readElement(channel, position, parent.end());
            if (child.isEmpty() || child.get().hasUnknownSize()) {
                break;
            }
            children.add(child.get());
            position = child.get().end();
        }
        return children;
    }

    /**
     * Read the header of the element at a position.
     *
     * @param channel  the file channel
     * @param position the element position
     * @param limit    the position the element cannot exceed
     * @return the element, or empty if the element header is corrupted
     */
    private Optional<Element> readElement(final FileChannel channel, final long position, final long limit) {
        try {
            int available = (int) min(MAX_ELEMENT_HEADER_SIZE, limit - position);
            if (available < 2) {
                return Optional.empty();
            }
            ByteBuffer header = read(channel, position, available);

            int idLength = variableIntegerLength(header.get(0));
            if (idLength < 1 || idLength > 4 || idLength >= available) {
                return Optional.empty();
            }
            long id = 0;
            for (int index = 0; index < idLength; index++) {
                id = (id << 8) | (header.get(index) & 0xFF);
            }

            int sizeLength = variableIntegerLength(header.get(idLength));
            if (sizeLength < 1 || idLength + sizeLength > available) {
                return Optional.empty();
            }
            // the size value is the variable integer without its length marker bit
            long size = header.get(idLength) & (0xFF >> sizeLength);
            boolean allOnes = size == (0xFF >> sizeLength);
            for (int index = 1; index < sizeLength; index++) {
                int value = header.get(idLength + index) & 0xFF;
                allOnes &= value == 0xFF;
                size = (size << 8) | value;
            }

            long dataStart = position + idLength + sizeLength;
            if (allOnes) {
                return Optional.of(new Element(id, dataStart, UNKNOWN_SIZE));
            }
            if (size < 0 || dataStart + size > limit) {
                return Optional.empty();
            }
            return Optional.of(new Element(id, dataStart, size));
        } catch (IOException ex) {
            return Optional.empty();
        }
    }

    /**
     * Get the length of a variable integer from its first byte: the number of leading zero bits plus one.
     */
    private int variableIntegerLength(final byte firstByte) {
        int value = firstByte & 0xFF;
        return value == 0 ? 0 : Integer.numberOfLeadingZeros(value) - 23;
    }

    private long readUnsigned(final FileChannel channel, final Element element) throws IOException {
        if (element.size() < 1 || element.size() > 8) {
            return -1;
        }
        ByteBuffer buffer = read(channel, element.dataStart(), (int) element.size());
        long value = 0;
        while (buffer.hasRemaining()) {
            value = (value << 8) | (buffer.get() & 0xFF);
        }
        return value;
    }

    private String readString(final FileChannel channel, final Element element) throws IOException {
        if (element.size() > 1024) {
            return "";
        }
        // strings can be padded with zero bytes
        return new String(read(channel, element.dataStart(), (int) element.size()).array(), UTF_8)
            .replace("\0", "");
    }

    /**
     * An element of the file.
     *
     * @param id        the element id, including the length marker bits
     * @param dataStart the position of the element data
     * @param size      the size of the element data, or {@link #UNKNOWN_SIZE}
     */
    private record Element(long id, long dataStart, long size) {

        boolean hasUnknownSize() {
            return size == UNKNOWN_SIZE;
        }

        long end() {
            return dataStart + size;
        }
    }

    /**
     * An image attachment.
     *
     * @param name the lower case attachment name
     * @param data the attachment data element
     */
    private record Attachment(String name, Element data) {

        public int coverRank() {
            return range(0, COVER_NAMES.size())
                .filter(rank -> name.startsWith(COVER_NAMES.get(rank)))
                .findFirst()
                .orElse(COVER_NAMES.size());
        }
    }
}
