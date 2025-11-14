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

package org.cosinus.swing.file.linux;

import org.cosinus.swing.file.FileInfoProvider;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.xml.Xml;
import org.springframework.cache.annotation.Cacheable;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

public class LinuxFileInfoProvider implements FileInfoProvider {

    private final Translator translator;

    public LinuxFileInfoProvider(final Translator translator) {
        this.translator = translator;
    }

    @Override
    @Cacheable("spring.swing.file.type.description")
    public Optional<String> getFileTypeDescription(final String mimeType) {
        return ofNullable(mimeType)
            .flatMap(this::getPathToMimeTypeXmlFile)
            .map(Path::toFile)
            .filter(File::exists)
            .flatMap(this::getMimeTypeDescription);

    }

    private Optional<String> getMimeTypeDescription(final File mimeTypeXmlFile) {
        try (Xml xml = new Xml(mimeTypeXmlFile)) {
            return getLocalizedDescription(xml)
                .or(() -> getDefaultDescription(xml));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Optional<String> getLocalizedDescription(final Xml xml) {
        return translator.getLocale()
            .map(Locale::toString)
            .flatMap(locale -> getMimeTypeDescription(xml, locale));
    }

    private Optional<String> getDefaultDescription(final Xml xml) {
        return getMimeTypeDescription(xml, null);
    }

    private Optional<String> getMimeTypeDescription(final Xml xml, String locale) {
        try {
            return xml.findMapValue("mime-type/comment", "lang", locale)
                .filter(not(String::isEmpty));
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    protected Optional<Path> getPathToMimeTypeXmlFile(final String mimeType) {
        return ofNullable(mimeType)
            .map(mime -> mime.split("/"))
            .filter(mime -> mime.length == 2)
            .map(mime -> Paths.get("/usr/share/mime/", mime[0], mime[1] + ".xml"));
    }
}
