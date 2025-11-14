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

package org.cosinus.swing.file.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cosinus.swing.boot.condition.ConditionalOnLinux;
import org.cosinus.swing.boot.condition.ConditionalOnMac;
import org.cosinus.swing.boot.condition.ConditionalOnWindows;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.exec.ProcessExecutor;
import org.cosinus.swing.file.FileHandler;
import org.cosinus.swing.file.FileSystem;
import org.cosinus.swing.file.linux.LinuxFileSystem;
import org.cosinus.swing.file.mac.MacFileInfoProvider;
import org.cosinus.swing.file.mac.MacFileSystem;
import org.cosinus.swing.file.DefaultFileInfoProvider;
import org.cosinus.swing.file.linux.LinuxFileInfoProvider;
import org.cosinus.swing.file.windows.WindowsFileSystem;
import org.cosinus.swing.file.FileInfoProvider;
import org.cosinus.swing.file.mimetype.MimeTypeResolver;
import org.cosinus.swing.translate.Translator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class SpringSwingFileAutoConfiguration {

    @Bean
    public FileHandler fileHandler(final MimeTypeResolver mimeTypeResolver,
                                   final FileSystem fileSystem,
                                   final ProcessExecutor processExecutor) {
        return new FileHandler(mimeTypeResolver, fileSystem, processExecutor);
    }

    @Bean
    @ConditionalOnLinux
    public FileSystem linuxFileSystem(final ProcessExecutor processExecutor,
                                      final ObjectMapper objectMapper,
                                      final ErrorHandler errorHandler,
                                      final Translator translator,
                                      final MimeTypeResolver mimeTypeResolver,
                                      final FileInfoProvider fileInfoProvider) {
        return new LinuxFileSystem(processExecutor,
            objectMapper,
            errorHandler,
            translator,
            mimeTypeResolver,
            fileInfoProvider);
    }

    @Bean
    @ConditionalOnMac
    public FileSystem macFileSystem(final ProcessExecutor processExecutor,
                                    final FileInfoProvider fileTypeInfoProvider) {
        return new MacFileSystem(processExecutor, fileTypeInfoProvider);
    }

    @Bean
    @ConditionalOnWindows
    public FileSystem windowsFileSystem() {
        return new WindowsFileSystem();
    }
    @Bean
    @ConditionalOnLinux
    public FileInfoProvider linuxFileTypeInfoProvider(final Translator translator) {
        return new LinuxFileInfoProvider(translator);
    }

    @Bean
    @ConditionalOnMac
    public FileInfoProvider macFileTypeInfoProvider(final ProcessExecutor processExecutor,
                                                    final Translator translator) {
        return new MacFileInfoProvider(processExecutor, translator);
    }

    @Bean
    @ConditionalOnMissingBean
    public FileInfoProvider mimeTypeInfoProvider() {
        return new DefaultFileInfoProvider();
    }

    @Bean
    public MimeTypeResolver mimeTypeResolver() {
        return new MimeTypeResolver();
    }
}
