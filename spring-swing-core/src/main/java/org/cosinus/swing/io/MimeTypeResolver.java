package org.cosinus.swing.io;

import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.util.MimeType;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.Arrays.stream;
import static java.util.Locale.ENGLISH;
import static java.util.Optional.ofNullable;
import static org.springframework.util.MimeTypeUtils.APPLICATION_OCTET_STREAM;
import static org.springframework.util.MimeTypeUtils.IMAGE_JPEG;
import static org.springframework.util.MimeTypeUtils.TEXT_PLAIN;
import static org.springframework.util.MimeTypeUtils.parseMimeType;
import static org.springframework.util.StringUtils.tokenizeToStringArray;

public class MimeTypeResolver {

    private static final Logger LOG = LogManager.getLogger(MimeTypeResolver.class);

    private static final String MIME_TYPES_FILE_NAME = "/mime.types";

    private static final MimeType IMAGE = new MimeType(IMAGE_JPEG.getType());
    private static final MimeType TEXT = new MimeType(TEXT_PLAIN.getType());
    private static final MimeType APPLICATION = new MimeType(APPLICATION_OCTET_STREAM.getType());

    private final SetValuedMap<String, MimeType> mimeTypesMap;

    public MimeTypeResolver() {
        mimeTypesMap = initMimeTypes();
    }

    public SetValuedMap<String, MimeType> initMimeTypes() {
        SetValuedMap<String, MimeType> mimeTypesMap = new HashSetValuedHashMap<>();
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

    public Set<MimeType> getMimeTypes(Path path) {
        return ofNullable(path)
            .map(Path::toString)
            .map(StringUtils::getFilenameExtension)
            .map(extension -> extension.toLowerCase(ENGLISH))
            .map(mimeTypesMap::get)
            .orElseGet(Collections::emptySet);
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

    public boolean hasUnknownMimeType(Path path) {
        return getMimeTypes(path).isEmpty();
    }
}
