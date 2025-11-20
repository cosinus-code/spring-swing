package org.cosinus.swing.file.mac;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.cosinus.swing.icon.IconSize.X32;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.error.ProcessExecutionException;
import org.cosinus.swing.exec.ProcessExecutor;
import org.cosinus.swing.file.Application;
import org.cosinus.swing.file.FileInfoProvider;
import org.cosinus.swing.translate.Translator;
import org.springframework.cache.annotation.Cacheable;

public class MacFileInfoProvider implements FileInfoProvider {

    private static final Logger LOG = LogManager.getLogger(MacFileInfoProvider.class);

    protected static final String NAME = "name";
    protected static final String PATH = "path";
    protected static final String EXECUTABLE = "executable";
    protected static final String ICONS = "icons";
    protected static final String LOCALIZED_NAMES = "localizednames";
    protected static final String LOCALIZED_DESCRIPTION = "localizeddescription";
    protected static final String BUNDLE_ID = "bundle id";
    protected static final String CLAIM_ID = "claim id";
    protected static final String CLAIMED_UTI = "claimed utis";
    protected static final String FLAGS = "flags";
    protected static final String BUNDLE = "bundle";
    protected static final String BINDINGS = "bindings";
    protected  static final String APPLE_DEFAULT_FLAG = "apple-default";

    public static final String FOLDER_MIME_TYPE = "public.folder";

    private final ProcessExecutor processExecutor;

    private final Translator translator;

    private final Map<String, Application> applicationsMap;

    private final Map<String, Set<Application>> compatibleApplicationsMap;

    private final Map<String, UniformType> uniformTypesMap;

    public MacFileInfoProvider(final ProcessExecutor processExecutor,
                               final Translator translator) {
        this.processExecutor = processExecutor;
        this.translator = translator;

        List<Map<String, String>> entries = buildRegisteredEntries();
        this.applicationsMap = buildApplicationsMap(entries);
        this.compatibleApplicationsMap = buildCompatibleApplicationsMap(entries);
        this.uniformTypesMap = buildUniformTypesMap(entries);
    }

    @Override
    @Cacheable("spring.swing.file.type.description")
    public Optional<String> getFileTypeDescription(final String uniformTypeIdentifier) {
        return ofNullable(uniformTypesMap.get(uniformTypeIdentifier))
            .flatMap(uniformType -> translator.getLocale()
                .flatMap(uniformType::getTranslatedName));
    }

    @Cacheable(value = "spring.swing.uniform.type.identifier",
        keyGenerator = "fileExtensionKeyGenerator")
    public Optional<String> findUniformTypeIdentifier(final File file) {
        try {
            return processExecutor.executeAndGetOutput(
                    "mdls", "-name", "kMDItemContentType", "-raw", file.getAbsolutePath())
                .map(uti -> uti.endsWith("%") ? uti.substring(0, uti.length() - 1) : uti);
        } catch (ProcessExecutionException executionException) {
            LOG.warn("Failed to detect the uniform type identifier of the file '%s'"
                .formatted(file.getAbsolutePath()));
            return Optional.empty();
        }
    }

    public Set<Application> getCompatibleApplications(String uniformTypeIdentifier) {
        return compatibleApplicationsMap.get(uniformTypeIdentifier);
    }

    public UniformType getUniformType(String uniformTypeIdentifier) {
        return uniformTypesMap.get(uniformTypeIdentifier);
    }

    private List<Map<String, String>> buildRegisteredEntries() {
        return processExecutor.executeAndGetOutput(
                "/System/Library/Frameworks/CoreServices.framework/Frameworks/" +
                    "LaunchServices.framework/Support/lsregister", "-dump")
            .map(output -> output.split("---+\\n"))
            .stream()
            .flatMap(Arrays::stream)
            .map(this::toKeyValuesMap)
            .toList();
    }

    private Map<String, Application> buildApplicationsMap(
        final List<Map<String, String>> entries) {

        return entries
            .stream()
            .filter(keyValueMap -> keyValueMap.containsKey(PATH))
            .filter(keyValueMap -> keyValueMap.containsKey(BUNDLE_ID))
            .collect(toMap(
                keyValuesMap -> keyValuesMap.get(BUNDLE_ID),
                this::builApplication,
                (u, v) -> u,
                LinkedHashMap::new));
    }

    private Map<String, Set<Application>> buildCompatibleApplicationsMap(
        final List<Map<String, String>> entries) {

        return entries
            .stream()
            .filter(keyValueMap -> keyValueMap.containsKey(BUNDLE_ID))
            .filter(keyValueMap -> keyValueMap.containsKey(CLAIMED_UTI))
            .flatMap(keyValueMap ->
                stream(keyValueMap.get(CLAIMED_UTI).split(",\\s*"))
                    .map(uri -> new ImmutablePair<>(
                        uri,
                        applicationsMap.get(keyValueMap.get(BUNDLE_ID)))))
            .filter(pair -> Objects.nonNull(pair.getValue()))
            .collect(groupingBy(
                Pair::getKey,
                mapping(Pair::getValue, toSet())));
    }

    private Map<String, UniformType> buildUniformTypesMap(
        final List<Map<String, String>> entries) {

        return entries
            .stream()
            .filter(keyValueMap -> keyValueMap.containsKey(BUNDLE))
            .filter(keyValueMap -> keyValueMap.containsKey(BINDINGS))
//            .filter(keyValueMap -> keyValueMap.containsKey(FLAGS))
//            .filter(keyValueMap ->
//                keyValueMap.get(FLAGS).contains(APPLE_DEFAULT_FLAG))
            .flatMap(keyValueMap -> stream(keyValueMap.get(BINDINGS).split(",\\s*"))
                .map(binding -> new ImmutablePair<>(
                    binding,
                    new UniformType(
                        keyValueMap.get(CLAIM_ID),
                        applicationsMap.get(keyValueMap.get(BUNDLE)),
                        keyValueMap.get(NAME),
                        translatedValues(keyValueMap.get(LOCALIZED_NAMES))))))
            .filter(pair -> Objects.nonNull(pair.getValue()))
            .collect(toMap(
                Pair::getKey,
                Pair::getValue,
                (u, v) -> v
            ));
    }

    private Map<String, String> toKeyValuesMap(String rawApplication) {
        return stream(rawApplication.split("\\n"))
            .filter(line -> !line.startsWith(" "))
            .map(line -> line.split(":\\s+"))
            .filter(linePieces -> linePieces.length > 1)
            .collect(toMap(
                linePieces -> linePieces[0].trim().toLowerCase(),
                linePieces -> linePieces[1].trim(),
                (u, v) -> u));
    }

    private Application builApplication(Map<String, String> keyValueMap) {
        String applicationPath = keyValueMap.get(PATH).substring(
            0, keyValueMap.get(PATH).lastIndexOf(" ("));

        Map<String, String> localizedNames =
            translatedValues(keyValueMap.get(LOCALIZED_NAMES));

        Map<String, String> localizedDescriptions =
            translatedValues(keyValueMap.get(LOCALIZED_DESCRIPTION));

        return new Application(
            keyValueMap.get(BUNDLE_ID),
            keyValueMap.get(NAME),
            //TODO: to move in Application all translated values,
            // not only the current language translation
            translator.getLocale()
                .map(Locale::toString)
                .map(localizedNames::get)
                .orElse(null),
            keyValueMap.get(NAME),
            translator.getLocale()
                .map(Locale::toString)
                .map(localizedDescriptions::get)
                .orElse(null),
            "\"" + applicationPath + "/" + keyValueMap.get(EXECUTABLE) + "\" %f",
            X32,
            applicationPath + "/" + keyValueMap.get(ICONS),
            false);
    }

    protected Map<String, String> translatedValues(String rawValues) {
        return ofNullable(rawValues)
            .map(raw -> raw.split(",\\s*"))
            .stream()
            .flatMap(Arrays::stream)
            .map(rawValue -> rawValue.split("\\s*=\\s*"))
            .filter(valuePair -> valuePair.length > 1)
            .map(valuePair -> new ImmutablePair<>(
                valuePair[0].replaceAll("^\"|\"$", ""),
                valuePair[1].replaceAll("^\"|\"$", "")
            ))
            .filter(pair -> !"?".equals(pair.getValue()))
            .collect(toMap(Pair::getKey, Pair::getValue, (u, v) -> v));
    }
}
