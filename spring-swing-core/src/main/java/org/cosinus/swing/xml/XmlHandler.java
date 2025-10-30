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

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

public class XmlHandler {

    private final XmlMapper xmlMapper;

    public XmlHandler() {
        xmlMapper = new XmlMapper();
    }

    public <T> T readXmlFile(final File xmlFile, final Class<T> modelClass) {
        try {
            return xmlMapper.readValue(xmlFile, modelClass);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
