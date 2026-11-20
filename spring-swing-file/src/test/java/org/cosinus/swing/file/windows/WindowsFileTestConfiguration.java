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
package org.cosinus.swing.file.windows;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * The Windows file system beans of the tests, built with fakes instead of the Windows native calls,
 * so they can run on any platform.
 * <p>
 * The beans are primary, to win over the real Windows beans when the tests run on Windows.
 */
@TestConfiguration
public class WindowsFileTestConfiguration {

    @Bean
    @Primary
    public RecordingProcessExecutor recordingProcessExecutor() {
        return new RecordingProcessExecutor();
    }

    @Bean
    @Primary
    public InMemoryFileAssociationQuery inMemoryFileAssociationQuery() {
        return new InMemoryFileAssociationQuery();
    }

    @Bean
    @Primary
    public InMemoryRegistry inMemoryRegistry(final InMemoryFileAssociationQuery fileAssociationQuery) {
        return new InMemoryRegistry(fileAssociationQuery);
    }

    @Bean
    public AssociationsChangedCounter associationsChangedCounter() {
        return new AssociationsChangedCounter();
    }

    @Bean
    @Primary
    public WindowsFileAssociations testWindowsFileAssociations(final InMemoryFileAssociationQuery fileAssociationQuery,
                                                               final InMemoryRegistry registry,
                                                               final AssociationsChangedCounter changedCounter) {
        return new WindowsFileAssociations(
            fileAssociationQuery,
            registry,
            command -> command.replace("%SystemRoot%", "C:\\Windows"),
            changedCounter);
    }

    @Bean
    @Primary
    public WindowsFileSystem testWindowsFileSystem(final RecordingProcessExecutor processExecutor,
                                                   final ObjectMapper objectMapper,
                                                   final WindowsFileAssociations testWindowsFileAssociations) {
        return new WindowsFileSystem(processExecutor, objectMapper, testWindowsFileAssociations);
    }
}
