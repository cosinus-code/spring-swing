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
package org.cosinus.swing.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cosinus.swing.convert.JsonFileConverter;
import org.cosinus.swing.error.JsonConvertException;
import org.cosinus.swing.error.SpringSwingException;
import org.cosinus.swing.resource.ResourceLocator;
import org.cosinus.swing.resource.ResourceResolver;

import java.util.Set;

import static org.cosinus.swing.resource.ResourceType.UI;

public class UIDescriptorProvider extends JsonFileConverter<UIDescriptor> {

    public UIDescriptorProvider(final ObjectMapper objectMapper,
                                final Set<ResourceResolver> resourceResolvers) {
        super(objectMapper, UIDescriptor.class, resourceResolvers);
    }

    public UIDescriptor getUIDescriptor(String name) {
        try {
            return convert(name)
                .orElseThrow(() -> new SpringSwingException("There is no UI descriptor for name: " + name));
        } catch (JsonConvertException ex) {
            throw new SpringSwingException("Failed to load UI descriptor.", ex);
        }
    }

    @Override
    protected ResourceLocator resourceLocator() {
        return UI;
    }
}
