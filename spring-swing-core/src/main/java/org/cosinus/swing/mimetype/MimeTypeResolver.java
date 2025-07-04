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

package org.cosinus.swing.mimetype;

import net.sf.jmimemagic.*;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.util.MimeType;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.Arrays.stream;
import static java.util.Locale.ENGLISH;
import static java.util.Optional.ofNullable;
import static net.sf.jmimemagic.Magic.getMagicMatch;
import static org.cosinus.swing.util.FileUtils.getExtension;
import static org.springframework.util.MimeTypeUtils.APPLICATION_OCTET_STREAM;
import static org.springframework.util.MimeTypeUtils.IMAGE_JPEG;
import static org.springframework.util.MimeTypeUtils.TEXT_PLAIN;
import static org.springframework.util.MimeTypeUtils.parseMimeType;
import static org.springframework.util.StringUtils.tokenizeToStringArray;

public class MimeTypeResolver {

    private static final Logger LOG = LogManager.getLogger(MimeTypeResolver.class);

    private static final String MIME_TYPES_FILE_NAME = "/mime.types";

    private static final String SHELL_SCRIPT = "x-shellscript";

    private static final MimeType IMAGE = new MimeType(IMAGE_JPEG.getType());
    private static final MimeType TEXT = new MimeType(TEXT_PLAIN.getType());
    private static final MimeType APPLICATION = new MimeType(APPLICATION_OCTET_STREAM.getType());
    private static final MimeType SHELL_SCRIPT_MIME_TYPE = new MimeType(APPLICATION.getType(), SHELL_SCRIPT);

    public static final String AR = "ar";
    public static final String ARJ = "arj";
    public static final String CPIO = "cpio";
    public static final String DUMP = "dump";
    public static final String JAR = "jar";
    public static final String RAR = "rar";
    public static final String TAR = "tar";
    public static final String ZIP = "zip";
    public static final String SEVEN_Z = "7z";
    public static final String GZ = "gz";
    public static final String GZIP = "gzip";
    public static final String BZ2 = "bz2";
    public static final String BZIP2 = "bzip2";

    public static final String SH = "sh";
    public static final String BAT = "bat";

    public static Set<String> ARCHIVE_TYPES =
        Set.of(AR, ARJ, CPIO, DUMP, JAR, RAR, TAR, ZIP, SEVEN_Z, GZ, GZIP, BZ2, BZIP2);

    public static Set<String> SHELL_SCRIPT_TYPES =
        Set.of(SH, BAT);

    private final ListValuedMap<String, MimeType> mimeTypesMap;

    public MimeTypeResolver() {
        mimeTypesMap = initMimeTypes();
    }

    public ListValuedMap<String, MimeType> initMimeTypes() {
        ListValuedMap<String, MimeType> mimeTypesMap = new ArrayListValuedHashMap<>();
        SHELL_SCRIPT_TYPES.forEach(type -> mimeTypesMap.put(type, SHELL_SCRIPT_MIME_TYPE));

        try (InputStream input = MimeTypeResolver.class.getResourceAsStream(MIME_TYPES_FILE_NAME)) {
            if (input != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, US_ASCII))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.isEmpty() || line.charAt(0) == '#') {
                            continue;
                        }
                        String[] tokens = tokenizeToStringArray(line, " \t\n\r\f");
                        if (tokens.length > 1) {
                            getMimeType(tokens[0])
                                .ifPresent(mimeType -> {
                                    stream(tokens, 1, tokens.length)
                                        .map(extension -> extension.toLowerCase(ENGLISH))
                                        .forEach(extension -> mimeTypesMap.put(extension, mimeType));
                                });
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOG.warn("Failed to parse mime types", e);
        }

        return mimeTypesMap;
    }

    private Optional<MimeType> getMimeType(String text) {
        try {
            return Optional.of(parseMimeType(text));
        } catch (InvalidMimeTypeException ex) {
            LOG.warn("Invalid mime type: {}", ex.getMessage());
        }
        return Optional.empty();
    }

    public List<MimeType> getMimeTypes(Path path) {
        return ofNullable(path)
            .map(Path::toString)
            .map(StringUtils::getFilenameExtension)
            .map(extension -> extension.toLowerCase(ENGLISH))
            .map(mimeTypesMap::get)
            .orElseGet(Collections::emptyList);
    }

    public boolean isTextCompatible(Path path) {
        return isCompatible(path, TEXT);
    }

    public boolean isImageCompatible(Path path) {
        return isCompatible(path, IMAGE);
    }

    public boolean isApplicationCompatible(Path path) {
        return isCompatible(path, APPLICATION);
    }

    public boolean isCompatible(Path path, MimeType mimeType) {
        return getMimeTypes(path)
            .stream()
            .anyMatch(mimeType::isCompatibleWith);
    }

    /**
     * Check if a file is archive.
     *
     * @param file the file to check
     * @return true if the file is a known archive
     */
    public boolean isArchive(File file) {
        return ARCHIVE_TYPES.contains(getExtension(file));
    }

    /**
     * Check if a file is shell script.
     *
     * @param file the file to check
     * @return true if the file is a known shell script
     */
    public boolean isShellScript(File file) {
        return SHELL_SCRIPT_TYPES.contains(getExtension(file));
    }

    public boolean hasUnknownMimeType(Path path) {
        return getMimeTypes(path).isEmpty();
    }

//    public Optional<String> mimeType(Path path) {
//        return ofNullable(path)
//            .map(Path::toFile)
//            .filter(not(File::isDirectory))
//            .map(file -> ofNullable(getFileContentType(file.toPath()))
//                .orElseGet(() -> getMagicMimeType(file)));
//    }
//
//    private String getFileContentType(Path path) {
//        try {
//            return Files.probeContentType(path);
//        } catch (IOException ex) {
//            LOG.error("Failed to probe the file content type for path: {}", path);
//            return null;
//        }
//    }

//        return processExecutor.executeAndGetOutput("file", "--mime-type", file.getAbsolutePath())
//            .map(output -> output.substring(output.lastIndexOf(" ") + 1))
//            .or(() -> processExecutor.executeAndGetOutput("xdg-mime", "query", "filetype", file.getAbsolutePath()))
//            .map(output -> output.replaceAll("\\n", ""));

    public Optional<MimeType> getMagicMimeType(File file) {
        try {
            return ofNullable(getMagicMatch(file, true))
                .map(MagicMatch::getMimeType)
                .flatMap(this::getMimeType);
        } catch (MagicMatchNotFoundException | MagicException | MagicParseException e) {
            LOG.error("Failed to match a mime type for path: {}", file);
            return Optional.empty();
        }
    }
}
