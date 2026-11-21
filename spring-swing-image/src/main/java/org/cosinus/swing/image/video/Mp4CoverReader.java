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
import java.util.Optional;
import java.util.Set;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Reader of the cover image embedded in an MP4, M4V, MOV or 3GP video (ISO base media file format).
 * <p>
 * Two cover locations are supported:
 * <ul>
 *     <li>the iTunes cover art: moov/udta/meta/ilst/covr/data, also written by ffmpeg for an attached picture</li>
 *     <li>the QuickTime artwork: moov/meta/ilst entry of the "com.apple.quicktime.artwork" key</li>
 * </ul>
 */
public class Mp4CoverReader implements VideoCoverReader {

    private static final Set<String> FIRST_BOX_TYPES = Set.of("ftyp", "moov", "mdat", "wide", "free", "skip", "pnot");

    private static final int MOOV = fourCC("moov");
    private static final int UDTA = fourCC("udta");
    private static final int META = fourCC("meta");
    private static final int KEYS = fourCC("keys");
    private static final int ILST = fourCC("ilst");
    private static final int COVR = fourCC("covr");
    private static final int DATA = fourCC("data");

    private static final String QUICKTIME_ARTWORK_KEY = "com.apple.quicktime.artwork";

    private static final int BOX_HEADER_SIZE = 8;
    private static final int LARGE_BOX_HEADER_SIZE = 16;
    private static final int FULL_BOX_HEADER_SIZE = 4;

    // the data box content starts with the type indicator and the locale
    private static final int DATA_HEADER_SIZE = 8;

    @Override
    public boolean supports(final FileChannel channel) throws IOException {
        if (channel.size() < BOX_HEADER_SIZE) {
            return false;
        }
        ByteBuffer header = read(channel, 0, BOX_HEADER_SIZE);
        String type = new String(header.array(), 4, 4, ISO_8859_1);
        return FIRST_BOX_TYPES.contains(type);
    }

    @Override
    public Optional<byte[]> readCover(final FileChannel channel) throws IOException {
        Optional<Box> moov = findBox(channel, 0, channel.size(), MOOV);
        if (moov.isEmpty()) {
            return Optional.empty();
        }

        Optional<Box> iTunesCover = findBox(channel, moov.get(), UDTA)
            .flatMap(udta -> findMetaBox(channel, udta))
            .flatMap(meta -> findBox(channel, meta, ILST))
            .flatMap(ilst -> findBox(channel, ilst, COVR))
            .flatMap(covr -> findBox(channel, covr, DATA));
        if (iTunesCover.isPresent()) {
            return readData(channel, iTunesCover.get());
        }

        Optional<Box> quickTimeMeta = findMetaBox(channel, moov.get());
        if (quickTimeMeta.isPresent()) {
            Optional<Box> quickTimeCover = findArtworkKeyIndex(channel, quickTimeMeta.get())
                .flatMap(keyIndex -> findBox(channel, quickTimeMeta.get(), ILST)
                    .flatMap(ilst -> findBox(channel, ilst, keyIndex)))
                .or(() -> findBox(channel, quickTimeMeta.get(), ILST)
                    .flatMap(ilst -> findBox(channel, ilst, COVR)))
                .flatMap(item -> findBox(channel, item, DATA));
            if (quickTimeCover.isPresent()) {
                return readData(channel, quickTimeCover.get());
            }
        }
        return Optional.empty();
    }

    /**
     * Find the meta box in a parent box.
     * <p>
     * The meta box is a full box in MP4 (with version and flags before its children),
     * but a simple box in QuickTime.
     */
    private Optional<Box> findMetaBox(final FileChannel channel, final Box parent) {
        return findBox(channel, parent, META)
            .map(meta -> isFullBox(channel, meta) ?
                new Box(meta.contentStart() + FULL_BOX_HEADER_SIZE, meta.end()) :
                meta);
    }

    private boolean isFullBox(final FileChannel channel, final Box box) {
        try {
            // a full box starts with version 0 and no flags, a simple box starts with the size of its first child
            return box.end() - box.contentStart() >= FULL_BOX_HEADER_SIZE &&
                read(channel, box.contentStart(), FULL_BOX_HEADER_SIZE).getInt() == 0;
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * Find the 1-based index of the QuickTime artwork key, which is the type of its item in the ilst box.
     */
    private Optional<Integer> findArtworkKeyIndex(final FileChannel channel, final Box meta) {
        return findBox(channel, meta, KEYS)
            .flatMap(keys -> {
                try {
                    long position = keys.contentStart() + FULL_BOX_HEADER_SIZE;
                    int count = read(channel, position, 4).getInt();
                    position += 4;
                    for (int index = 1; index <= count && position + BOX_HEADER_SIZE <= keys.end(); index++) {
                        ByteBuffer keyHeader = read(channel, position, BOX_HEADER_SIZE);
                        int keySize = keyHeader.getInt();
                        if (keySize < BOX_HEADER_SIZE || position + keySize > keys.end()) {
                            break;
                        }
                        String key = new String(
                            read(channel, position + BOX_HEADER_SIZE, keySize - BOX_HEADER_SIZE).array(), UTF_8);
                        if (QUICKTIME_ARTWORK_KEY.equals(key)) {
                            return Optional.of(index);
                        }
                        position += keySize;
                    }
                } catch (IOException ex) {
                    return Optional.empty();
                }
                return Optional.empty();
            });
    }

    private Optional<byte[]> readData(final FileChannel channel, final Box data) throws IOException {
        long imageStart = data.contentStart() + DATA_HEADER_SIZE;
        return readCoverBytes(channel, imageStart, data.end() - imageStart);
    }

    private Optional<Box> findBox(final FileChannel channel, final Box parent, final int type) {
        return findBox(channel, parent.contentStart(), parent.end(), type);
    }

    /**
     * Find the first box of a type between two positions, skipping the other boxes by their size.
     *
     * @param channel the file channel
     * @param start   the start position
     * @param end     the end position
     * @param type    the box type
     * @return the found box, or empty if there is no such box or the boxes are corrupted
     */
    private Optional<Box> findBox(final FileChannel channel, final long start, final long end, final int type) {
        try {
            long position = start;
            while (position + BOX_HEADER_SIZE <= end) {
                ByteBuffer header = read(channel, position, BOX_HEADER_SIZE);
                long size = Integer.toUnsignedLong(header.getInt());
                int boxType = header.getInt();
                int headerSize = BOX_HEADER_SIZE;
                if (size == 1) {
                    if (position + LARGE_BOX_HEADER_SIZE > end) {
                        break;
                    }
                    size = read(channel, position + BOX_HEADER_SIZE, 8).getLong();
                    headerSize = LARGE_BOX_HEADER_SIZE;
                } else if (size == 0) {
                    // the box extends to the end of its parent
                    size = end - position;
                }
                if (size < headerSize || position + size > end) {
                    break;
                }
                if (boxType == type) {
                    return Optional.of(new Box(position + headerSize, position + size));
                }
                position += size;
            }
        } catch (IOException ex) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    private static int fourCC(final String type) {
        return ByteBuffer.wrap(type.getBytes(ISO_8859_1)).getInt();
    }

    /**
     * A box of the file.
     *
     * @param contentStart the position after the box header
     * @param end          the position after the box
     */
    private record Box(long contentStart, long end) {
    }
}
