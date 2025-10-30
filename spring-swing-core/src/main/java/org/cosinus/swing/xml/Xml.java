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

package org.cosinus.swing.xml;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static javax.xml.xpath.XPathConstants.STRING;

public class Xml implements AutoCloseable {

    private static final String XPATH_FIND_MAP_VALUE_BY_NULL_KEY = "/%s[not(@%s)]/text()";

    private static final String XPATH_FIND_MAP_VALUE_BY_KEY = "/%s[@%s='%s']/text()";

    private final InputStream inputStream;

    private final XPath xPath;

    private final Document xml;

    public Xml(final File xmlFile) throws IOException {
        this(new FileInputStream(xmlFile));
    }

    public Xml(final InputStream inputStream) {
        this.inputStream = inputStream;

        try {
            this.xml = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(inputStream);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException("Failed to parse xml input stream", e);
        }

        xPath = XPathFactory.newInstance().newXPath();
    }

    public Optional<String> findMapValue(final String mapPath,
                                         final String keyName,
                                         final String keyValue) throws XPathExpressionException {

        String path = ofNullable(keyValue)
            .map(key -> XPATH_FIND_MAP_VALUE_BY_KEY.formatted(mapPath, keyName, key))
            .orElseGet(() -> XPATH_FIND_MAP_VALUE_BY_NULL_KEY.formatted(mapPath, keyName));

        return ofNullable(getTextByPath(path));
    }

    public String getTextByPath(final String path) {
        try {
            return (String) xPath.compile(path).evaluate(xml, STRING);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Failed to compile xml path: " + path, e);
        }
    }

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
    }
}
