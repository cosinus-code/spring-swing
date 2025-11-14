package org.cosinus.swing.file.mac;

import static java.util.Optional.ofNullable;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.cosinus.swing.file.Application;

@Getter
@Setter
@AllArgsConstructor
public class UniformType {

    private static final String DEFAULT_LOCALIZED_KEY = "LSDefaultLocalizedValue";

    private String id;

    private Application defaultApplication;

    private String name;

    private Map<String, String> translatedNames;

    public Optional<String> getTranslatedName(Locale locale) {
        return ofNullable(translatedNames.get(locale.toString()))
            .or(() -> ofNullable(translatedNames.get(DEFAULT_LOCALIZED_KEY)));
    }
}
