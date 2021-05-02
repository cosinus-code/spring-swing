/*
 * Copyright 2020 Cosinus Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cosinus.swing.preference.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.cosinus.swing.preference.ObjectPreference;
import org.cosinus.swing.preference.Preference;
import org.cosinus.swing.preference.PreferenceType;

import java.awt.*;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.lang.String.join;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.preference.PreferenceType.FONT;
import static org.cosinus.swing.util.FontUtils.getFontStyle;
import static org.cosinus.swing.util.FontUtils.getFontStyleText;

/**
 * Implementation of {@link Preference} for font managed values.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_NULL)
public class FontPreference extends ObjectPreference<Font> {

    private static final String DELIMITER = ",";

    @Override
    @JsonIgnore
    public PreferenceType getType() {
        return FONT;
    }

    @Override
    public Font getRealValue() {
        return ofNullable(value)
            .map(descriptor -> descriptor.split(DELIMITER))
            .map(pieces -> new Font(pieces[0],
                                    getFontStyle(pieces[1]),
                                    Integer.parseInt(pieces[2])))
            .orElse(null);
    }

    @Override
    public void setRealValue(Font font) {
        super.setValue(join(DELIMITER,
                            font.getFamily(),
                            getFontStyleText(font),
                            Integer.toString(font.getSize())));
    }
}
